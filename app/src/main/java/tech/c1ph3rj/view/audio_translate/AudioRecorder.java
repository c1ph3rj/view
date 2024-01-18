package tech.c1ph3rj.view.audio_translate;

import static tech.c1ph3rj.view.Services.checkNull;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.JsonObject;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tech.c1ph3rj.view.PermissionManager;
import tech.c1ph3rj.view.R;
import tech.c1ph3rj.view.Services;
import tech.c1ph3rj.view.Timer;

public class AudioRecorder extends AppCompatActivity {
    private final String QUERY_URL = "https://rag-llm.azurewebsites.net/dynamic_ask_v4";
    PermissionManager permissionManager;
    CardView recordBtn;
    ImageView recorderIc;
    Spinner languageSpinner;
    TextView outputView;
    Services services;
    SpeechRecognizer speechRecognizer;
    TextView timerText;
    boolean isRecording;
    LinearLayout editBtn;
    ImageView sendBtn;
    boolean isPositiveFeedback;
    boolean isNegativeFeedback;
    ImageView positiveFeedback;
    ImageView negativeFeedback;
    LinearLayout feedbackLayout;
    boolean isTamilModelAvailable = false;
    boolean isEnglishModelAvailable = false;
    boolean isFrenchModelAvailable = false;
    boolean isHindiModelAvailable = false;
    boolean isSwahiliModelAvailable = false;
    boolean isArabicModelAvailable = false;
//    boolean isChichewaLanguageAvailable = false;
    LinearLayout stopResponse;
    String question;
    Translator languageTranslator = null;
    TextView answerView;
    ShimmerFrameLayout loadingView;
    EditText userQuestionView;
    CardView questionAndAnswerLayout;
    Translator outputTranslator = null;
    boolean isTyping;
    int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_recorder);


        checkLanguageModelsAvailability();

        try {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("Ask More Questions");
                actionBar.setHomeAsUpIndicator(R.drawable.back_ic);
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeButtonEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        services = new Services(this, null);
        permissionManager = new PermissionManager(this);
        permissionManager.setPermissionResultListener(new PermissionManager.PermissionResultListener() {
            @Override
            public void onPermissionGranted() {
                init();
            }

            @Override
            public void onPermissionDenied() {
                permissionManager.showPermissionExplanationDialog();
            }
        });

        if (permissionManager.hasPermissions(permissionManager.recordAudioPermissionArray())) {
            init();
        } else {
            permissionManager.requestPermissions(permissionManager.recordAudioPermissionArray());
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home: {
                android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(AudioRecorder.this);
                dialog.setMessage("Are you sure you want to exit the app?");
                dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity();
                    }
                });
                dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                android.app.AlertDialog alert = dialog.create();
                alert.show();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void checkLanguageModelsAvailability() {
        try {
            RemoteModelManager modelManager = RemoteModelManager.getInstance();
            TranslateRemoteModel englishModel = new TranslateRemoteModel.Builder(TranslateLanguage.ENGLISH).build();
            TranslateRemoteModel tamilModel = new TranslateRemoteModel.Builder(TranslateLanguage.TAMIL).build();
            TranslateRemoteModel frenchModel = new TranslateRemoteModel.Builder(TranslateLanguage.FRENCH).build();
            TranslateRemoteModel hindiModel = new TranslateRemoteModel.Builder(TranslateLanguage.HINDI).build();
            TranslateRemoteModel swahiliModel = new TranslateRemoteModel.Builder(TranslateLanguage.SWAHILI).build();
            TranslateRemoteModel arabicModel = new TranslateRemoteModel.Builder(TranslateLanguage.ARABIC).build();
//            String chichewaLanguage = TranslateLanguage.fromLanguageTag("ny");
//            TranslateRemoteModel chichewaModel;
//            if (chichewaLanguage != null) {
//                chichewaModel = new TranslateRemoteModel.Builder(chichewaLanguage).build();
//            } else {
//                chichewaModel = null;
//            }
            DownloadConditions conditions = new DownloadConditions.Builder().build();
            modelManager.getDownloadedModels(TranslateRemoteModel.class).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Set<TranslateRemoteModel> models = task.getResult();
                    if (!models.contains(tamilModel)) {
                        modelManager.download(tamilModel, conditions).addOnCompleteListener(tamilModelDownloadResult -> {
                            if (tamilModelDownloadResult.isSuccessful()) {
                                isTamilModelAvailable = true;
                            } else {
                                if (tamilModelDownloadResult.getException() != null) {
                                    tamilModelDownloadResult.getException().printStackTrace();
                                }
                            }
                        });
                    } else {
                        isTamilModelAvailable = true;
                    }
                    if (!models.contains(frenchModel)) {
                        modelManager.download(frenchModel, conditions).addOnCompleteListener(frenchModelDownloadResult -> {
                            if (frenchModelDownloadResult.isSuccessful()) {
                                isFrenchModelAvailable = true;
                            } else {
                                if (frenchModelDownloadResult.getException() != null) {
                                    frenchModelDownloadResult.getException().printStackTrace();
                                }
                            }
                        });
                    } else {
                        isFrenchModelAvailable = true;
                    }
                    if (!models.contains(hindiModel)) {
                        modelManager.download(hindiModel, conditions).addOnCompleteListener(hindiModelDownloadResult -> {
                            if (hindiModelDownloadResult.isSuccessful()) {
                                isHindiModelAvailable = true;
                            } else {
                                if (hindiModelDownloadResult.getException() != null) {
                                    hindiModelDownloadResult.getException().printStackTrace();
                                }
                            }
                        });
                    } else {
                        isHindiModelAvailable = true;
                    }
                    if (!models.contains(englishModel)) {
                        modelManager.download(englishModel, conditions).addOnCompleteListener(englishModelDownloadResult -> {
                            if (englishModelDownloadResult.isSuccessful()) {
                                isEnglishModelAvailable = true;
                            } else {
                                if (englishModelDownloadResult.getException() != null) {
                                    englishModelDownloadResult.getException().printStackTrace();
                                }
                            }
                        });
                    } else {
                        isEnglishModelAvailable = true;
                    }

                    if (!models.contains(swahiliModel)) {
                        modelManager.download(swahiliModel, conditions).addOnCompleteListener(swahiliModelDownloadResult -> {
                            if (swahiliModelDownloadResult.isSuccessful()) {
                                isSwahiliModelAvailable = true;
                            } else {
                                if (swahiliModelDownloadResult.getException() != null) {
                                    swahiliModelDownloadResult.getException().printStackTrace();
                                }
                            }
                        });
                    } else {
                        isSwahiliModelAvailable = true;
                    }

                    if (!models.contains(arabicModel)) {
                        modelManager.download(arabicModel, conditions).addOnCompleteListener(arabicModelDownloadResult -> {
                            if (arabicModelDownloadResult.isSuccessful()) {
                                isArabicModelAvailable = true;
                            } else {
                                if (arabicModelDownloadResult.getException() != null) {
                                    arabicModelDownloadResult.getException().printStackTrace();
                                }
                            }
                        });
                    } else {
                        isArabicModelAvailable = true;
                    }
//                    if (chichewaModel != null) {
//                        isChichewaLanguageAvailable = true;
//                        if (!models.contains(chichewaModel)) {
//                            modelManager.download(chichewaModel, conditions).addOnCompleteListener(chichewaModelDownloadResult -> {
//                                if (chichewaModelDownloadResult.isSuccessful()) {
//                                    isChichewaModelAvailable = true;
//                                } else {
//                                    if (chichewaModelDownloadResult.getException() != null) {
//                                        chichewaModelDownloadResult.getException().printStackTrace();
//                                    }
//                                }
//                            });
//                        } else {
//                            isChichewaModelAvailable = true;
//                        }
//                    } else {
//                        String[] originalArray = getResources().getStringArray(R.array.languageList);
//                        String valueToRemove = "Chichewa";
//                        List<String> modifiedArray = new ArrayList<>(Arrays.asList(originalArray));
//                        modifiedArray.remove(valueToRemove);
//                        String[] finalArray = modifiedArray.toArray(new String[0]);
//                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, finalArray);
//                        adapter.setDropDownViewResource(R.layout.simple_spinner_item);
//                        languageSpinner.setAdapter(adapter);
//                        isChichewaLanguageAvailable = false;
//                    }
                } else {
                    if (task.getException() != null) {
                        task.getException().printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionManager.onRequestPermissionsResult(permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        permissionManager.handleSettingsActivityResult(permissionManager.recordAudioPermissionArray(), requestCode, resultCode);
    }

    void init() {
        try {
            recorderIc = findViewById(R.id.recorderIc);
            outputView = findViewById(R.id.outputView);
            recordBtn = findViewById(R.id.recordBtn);
            languageSpinner = findViewById(R.id.languageSpinner);
            answerView = findViewById(R.id.answerView);
            loadingView = findViewById(R.id.loadingView);
            userQuestionView = findViewById(R.id.userQuestionView);
            questionAndAnswerLayout = findViewById(R.id.questionAndAnswerView);
            editBtn = findViewById(R.id.editBtn);
            sendBtn = findViewById(R.id.sendBtn);
            timerText = findViewById(R.id.timerText);
            positiveFeedback = findViewById(R.id.positiveBtn);
            negativeFeedback = findViewById(R.id.negativeBtn);
            feedbackLayout = findViewById(R.id.feedBackLayout);
            stopResponse = findViewById(R.id.stopResponse);


            positiveFeedback.setOnClickListener(onclickPositive -> {
                isPositiveFeedback = !isPositiveFeedback;
                if (isNegativeFeedback) {
                    negativeFeedback.setImageDrawable(AppCompatResources.getDrawable(AudioRecorder.this, R.drawable.thumbs_down_ic));
                    isNegativeFeedback = !isNegativeFeedback;
                }
                positiveFeedback.setImageDrawable(AppCompatResources.getDrawable(AudioRecorder.this, (isPositiveFeedback) ? R.drawable.thumbs_up_fill_ic : R.drawable.thumbs_up_ic));
            });

            negativeFeedback.setOnClickListener(onClickNegative -> {
                isNegativeFeedback = !isNegativeFeedback;
                if (isPositiveFeedback) {
                    positiveFeedback.setImageDrawable(AppCompatResources.getDrawable(AudioRecorder.this, R.drawable.thumbs_up_ic));
                    isPositiveFeedback = !isPositiveFeedback;
                }
                negativeFeedback.setImageDrawable(AppCompatResources.getDrawable(AudioRecorder.this, (isNegativeFeedback) ? R.drawable.thumbs_down_fill_ic : R.drawable.thumbs_down_ic));
            });


            stopResponse.setVisibility(View.GONE);
            feedbackLayout.setVisibility(View.GONE);
            sendBtn.setVisibility(View.GONE);
            answerView.setVisibility(View.GONE);
            questionAndAnswerLayout.setVisibility(View.GONE);
            userQuestionView.setEnabled(false);

            editBtn.setOnClickListener(onClickEdit -> {
                if (isTyping) {
                    Toast.makeText(this, "Please wait while the previous response is processing...", Toast.LENGTH_SHORT).show();
                    return;
                }
                editBtn.setVisibility(View.GONE);
                userQuestionView.setEnabled(true);
                languageSpinner.setVisibility(View.GONE);
                userQuestionView.setSelection(userQuestionView.getText().length());
                userQuestionView.setFocusable(true);
                userQuestionView.requestFocus();
                services.showKeyboard(userQuestionView);
                sendBtn.setVisibility(View.VISIBLE);
            });

            sendBtn.setOnClickListener(onClickSend -> {
                String questionText = userQuestionView.getText().toString().trim();
                if (questionText.isEmpty()) {
                    Toast.makeText(this, "Please type/ask something to continue!", Toast.LENGTH_SHORT).show();
                    return;
                }
                isTyping = true;
                translateAndAskLlm(questionText);
                editBtn.setVisibility(View.GONE);
                userQuestionView.setEnabled(false);
                languageSpinner.setVisibility(View.VISIBLE);
                services.hideKeyboard(userQuestionView);
                sendBtn.setVisibility(View.GONE);
            });

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recordBtn.setOnClickListener(onClickRecordBtn -> {
                try {
                    userQuestionView.setEnabled(false);
                    editBtn.setVisibility(View.GONE);
                    services.hideKeyboard(userQuestionView);
                    languageSpinner.setVisibility(View.VISIBLE);
                    sendBtn.setVisibility(View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (isFrenchModelAvailable && isTamilModelAvailable && isHindiModelAvailable && isEnglishModelAvailable && /*(!isChichewaLanguageAvailable || isChichewaModelAvailable)*/ isSwahiliModelAvailable && isArabicModelAvailable) {
                    if (isTyping) {
                        Toast.makeText(this, "Please wait while the previous response is processing...", Toast.LENGTH_SHORT).show();
                    } else {
                        if (isRecording) {
                            speechRecognizer.stopListening();
                        } else {
                            String selectedLanguage = languageSpinner.getSelectedItem().toString().toLowerCase(Locale.ROOT);
                            final Intent speechRecognizerIntent = getSpeechRecognizerIntent(selectedLanguage);
                            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                                @Override
                                public void onReadyForSpeech(Bundle bundle) {
                                    outputView.setText("Speak...");
                                }

                                @Override
                                public void onBeginningOfSpeech() {
                                    isTyping = true;
                                    editBtn.setVisibility(View.GONE);
                                    userQuestionView.setEnabled(false);
                                    outputView.setText("Listening...");
                                }

                                @Override
                                public void onRmsChanged(float v) {

                                }

                                @Override
                                public void onBufferReceived(byte[] bytes) {

                                }

                                @Override
                                public void onEndOfSpeech() {
                                    recorderIc.setImageTintList(ColorStateList.valueOf(getColor(R.color.black)));
                                    outputView.setText("Processing...");
                                }

                                @Override
                                public void onError(int i) {
                                    recorderIc.setImageTintList(ColorStateList.valueOf(getColor(R.color.black)));
                                    isTyping = false;
                                    editBtn.setVisibility(View.VISIBLE);
                                    outputView.setText("");
                                }

                                @Override
                                public void onResults(Bundle result) {
                                    ArrayList<String> matches = result.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                                    if (matches != null) {
                                        String outputText = matches.get(0);
                                        if (outputText.isEmpty()) {
                                            isTyping = false;
                                            outputView.setText("");
                                        } else {
                                            try {
                                                if (languageTranslator != null) {
                                                    userQuestionView.setText("");
                                                    userQuestionView.setText(outputText);
                                                    questionAndAnswerLayout.setVisibility(View.VISIBLE);
                                                    editBtn.setVisibility(View.GONE);
//                                                    sendBtn.setVisibility(View.VISIBLE);
//                                                    userQuestionView.setEnabled(true);
//                                                    isTyping = false;
                                                    isRecording = false;
//                                                    editBtn.performClick();
                                                    outputView.setText("");
                                                    translateAndAskLlm(outputText);
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    } else {
                                        isTyping = false;
                                        outputView.setText("");
                                    }
                                }

                                @Override
                                public void onPartialResults(Bundle bundle) {

                                }

                                @Override
                                public void onEvent(int i, Bundle bundle) {

                                }
                            });
                            TranslatorOptions languageTranslatorOption = getTranslationOptionSource(selectedLanguage, false);
                            TranslatorOptions responseTranslatorOption = getTranslationOptionSource(selectedLanguage, true);
                            languageTranslator = Translation.getClient(languageTranslatorOption);
                            outputTranslator = Translation.getClient(responseTranslatorOption);

                            getLifecycle().addObserver(languageTranslator);
                            DownloadConditions conditions = new DownloadConditions.Builder().build();
                            languageTranslator.downloadModelIfNeeded(conditions).addOnSuccessListener(unused -> {
                                outputView.setText("");
                                recorderIc.setImageTintList(ColorStateList.valueOf(getColor(R.color.red)));
                                speechRecognizer.startListening(speechRecognizerIntent);
                            }).addOnFailureListener(e -> {
                                outputView.setText("");
                                e.printStackTrace();
                            });
                            outputTranslator.downloadModelIfNeeded(conditions).addOnSuccessListener(unused -> {
                                System.out.println("Downloaded Required Models");
                            }).addOnFailureListener(Throwable::printStackTrace);
                        }
                    }
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(this)
                            .setMessage("Please wait, the ML model is downloading. It'll just take a few seconds to complete. Thanks for your patience!")
                            .setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss()).create();
                    alertDialog.show();
                }
            });

            getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(AudioRecorder.this);
                    dialog.setMessage("Are you sure you want to exit the app?");
                    dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finishAffinity();
                        }
                    });
                    dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    android.app.AlertDialog alert = dialog.create();
                    alert.show();
                }
            });
        } catch (Exception e) {
            services.handleException(e);
        }
    }

    private TranslatorOptions getTranslationOptionSource(String selectedLanguage, boolean isReversed) {
        String sourceLanguage = (selectedLanguage.equals("french")) ? TranslateLanguage.FRENCH : (selectedLanguage.equals("tamil")) ? TranslateLanguage.TAMIL : (selectedLanguage.equals("english")) ? TranslateLanguage.ENGLISH : /*(isChichewaLanguageAvailable && selectedLanguage.equals("chichewa"))? TranslateLanguage.fromLanguageTag("ny") :*/ (selectedLanguage.equals("swahili")) ? TranslateLanguage.SWAHILI : (selectedLanguage.equals("arabic")) ? TranslateLanguage.ARABIC : TranslateLanguage.HINDI;
        if (isReversed) {
            return new TranslatorOptions.Builder().setTargetLanguage(sourceLanguage).setSourceLanguage(TranslateLanguage.ENGLISH).build();
        } else {
            return new TranslatorOptions.Builder().setSourceLanguage(sourceLanguage).setTargetLanguage(TranslateLanguage.ENGLISH).build();
        }
    }

    private void translateAndAskLlm(String outputText) {
        try {
            languageTranslator.translate(outputText).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String translatedText = task.getResult();
                    question = outputText;
                    HttpUrl queryUrl = HttpUrl.parse(QUERY_URL);
                    if (queryUrl != null) {
//                        HttpUrl.Builder urlBuilder = queryUrl.newBuilder();
//                        urlBuilder.addQueryParameter("input", translatedText);
                        answerView.setText("");
                        answerView.setVisibility(View.GONE);
                        feedbackLayout.setVisibility(View.GONE);
                        timerText.setVisibility(View.GONE);
                        editBtn.setVisibility(View.GONE);
                        startLoading();
                        final MediaType JSON = MediaType.get("application/json");
                        JsonObject responseBodyObj = new JsonObject();
                        responseBodyObj.addProperty("index", "jcg_index");
                        responseBodyObj.addProperty("question", translatedText);
                        RequestBody requestBody = RequestBody.create(responseBodyObj.toString(), JSON);
                        askLlmAPI(requestBody);
                    }
                } else {
                    outputView.setText(outputText);
                    if (task.getException() != null) {
                        task.getException().printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            isTyping = false;
            editBtn.setVisibility(View.VISIBLE);
            services.somethingWentWrong();
            e.printStackTrace();
        }
    }

    private Intent getSpeechRecognizerIntent(String selectedLanguage) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, (selectedLanguage.equals("hindi")) ? "hi-IN" : (selectedLanguage.equals("tamil") ? "ta-IN" : (selectedLanguage.equals("english") ? "en" : /*(selectedLanguage.equals("chichewa"))? "ny":*/ (selectedLanguage.equals("swahili")) ? "sw" : (selectedLanguage.equals("arabic")) ? "ar" : "fr")));
        return intent;
    }


    private void startLoading() {
        runOnUiThread(() -> {
            try {
                loadingView.setVisibility(View.VISIBLE);
                loadingView.startShimmer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void askLlmAPI(RequestBody requestBody) {
        try {
            Timer timer = new Timer();
            timer.startTimer();
            if (services.isNetworkConnected()) {
                new Thread(() -> {
                    try {
                        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(120, TimeUnit.SECONDS).writeTimeout(120, TimeUnit.SECONDS).readTimeout(120, TimeUnit.SECONDS).build();

                        Request request = new Request.Builder().url(QUERY_URL).post(requestBody).build();

                        Response staticResponse;

                        try {
                            staticResponse = client.newCall(request).execute();

                            if (staticResponse.body() != null) {
                                String responseString = staticResponse.body().string();
                                JSONObject staticResObj = new JSONObject(responseString);
                                String answer = staticResObj.optString("answer");
                                if (checkNull(answer)) {
                                    if (outputTranslator != null) {
                                        outputTranslator.translate(answer).addOnCompleteListener(task -> {
                                            stopLoading();
                                            if (task.isSuccessful()) {
                                                runOnUiThread(() -> {
                                                    timerText.setText(timer.stopTimer());
                                                    timerText.setVisibility(View.VISIBLE);
                                                });
                                                String translatedOutputText = task.getResult();
                                                if (checkNull(translatedOutputText)) {
                                                    outputView.setText("");
                                                    startTypingAnimation(translatedOutputText);
                                                }
                                            } else {
                                                if (task.getException() != null) {
                                                    task.getException().printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                }
                            } else {
                                stopLoading();
                                isTyping = false;
                                runOnUiThread(() -> editBtn.setVisibility(View.VISIBLE));
                                services.somethingWentWrong();
                            }


                        } catch (Exception e) {
                            stopLoading();
                            isTyping = false;
                            runOnUiThread(() -> editBtn.setVisibility(View.VISIBLE));
                            services.somethingWentWrong();
                            services.handleException(e);
                        }
                    } catch (Exception e) {
                        stopLoading();
                        isTyping = false;
                        runOnUiThread(() -> editBtn.setVisibility(View.VISIBLE));
                        services.somethingWentWrong();
                        services.handleException(e);
                    }
                }).start();
            } else {
                stopLoading();
                isTyping = false;
                services.checkNetworkVisibility();
            }
        } catch (Exception e) {
            stopLoading();
            isTyping = false;
            services.handleException(e);
        }
    }

    private void stopLoading() {
        runOnUiThread(() -> {
            try {
                loadingView.stopShimmer();
                loadingView.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void startTypingAnimation(String fullText) {
        runOnUiThread(() -> {
            try {
                stopResponse.setVisibility(View.VISIBLE);
                index = 0;
                final Handler handler = new Handler(getMainLooper());
                answerView.setVisibility(View.VISIBLE);
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (index <= fullText.length()) {
                            String partialText = fullText.substring(0, index);
                            answerView.setText(partialText);
                            index++;
                            handler.postDelayed(this, 25);
                        } else {
                            isTyping = false;
                            feedbackLayout.setVisibility(View.VISIBLE);
                            positiveFeedback.setImageDrawable(AppCompatResources.getDrawable(AudioRecorder.this, R.drawable.thumbs_up_ic));
                            negativeFeedback.setImageDrawable(AppCompatResources.getDrawable(AudioRecorder.this, R.drawable.thumbs_down_ic));
                            editBtn.setVisibility(View.VISIBLE);
                            stopResponse.setVisibility(View.GONE);
                        }
                    }
                };
                handler.postDelayed(runnable, 35);

                stopResponse.setOnClickListener(onClickStop -> {
                    handler.removeCallbacks(runnable);
                    isTyping = false;
                    feedbackLayout.setVisibility(View.VISIBLE);
                    positiveFeedback.setImageDrawable(AppCompatResources.getDrawable(AudioRecorder.this, R.drawable.thumbs_up_ic));
                    negativeFeedback.setImageDrawable(AppCompatResources.getDrawable(AudioRecorder.this, R.drawable.thumbs_down_ic));
                    editBtn.setVisibility(View.VISIBLE);
                    stopResponse.setVisibility(View.GONE);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


}