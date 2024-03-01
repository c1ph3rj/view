package tech.c1ph3rj.view.audio_translate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.gson.JsonObject;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import tech.c1ph3rj.view.PermissionManager;
import tech.c1ph3rj.view.R;
import tech.c1ph3rj.view.Timer;

public class LLMAssistant extends AppCompatActivity {
    private static final AtomicLong counter = new AtomicLong(0);
    PermissionManager permissionManager;
    CardView recordBtn;
    ImageView recorderIc;
    Spinner languageSpinner;
    TextView outputView;
    SpeechRecognizer speechRecognizer;
    TextView timerText;
    LinearLayout editBtn;
    ImageView sendBtn;
    boolean isPositiveFeedback;
    boolean isNegativeFeedback;
    ImageView positiveFeedback;
    ImageView negativeFeedback;
    LinearLayout feedbackLayout;
    boolean isEnglishModelAvailable = false;
    LinearLayout stopResponse;
    String question;
    Translator languageTranslator = null;
    TextView answerView;
    boolean isRecording;
    boolean isResponseStopped= false;
    JSONObject exceptionJson;
    ShimmerFrameLayout loadingView;
    EditText userQuestionView;
    CardView questionAndAnswerLayout;
    boolean isTyping;
    Translator outputTranslator = null;
    private String QUERY_URL = "https://rag-llm.azurewebsites.net/mongo_atlas_memory_dynamic_streamed_ask";

    public static String generateUniqueName() {
        // Get the current timestamp in nanoseconds
        long timestamp = Instant.now().toEpochMilli() * 1_000_000; // Convert milliseconds to nanoseconds

        // Get the current value of the counter
        long count = counter.getAndIncrement();

        // Combine timestamp and count to create a unique name
        return "A" + timestamp + "_" + count + "@randomGenerated.com";
    }

    public void showAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> {
                    // Positive button click handler (OK button)
                    dialog.dismiss(); // Dismiss the dialog
                })
                .show();
    }

    private TranslatorOptions getTranslationOptionSource(String selectedLanguage, boolean isReversed) {
        String sourceLanguage = (selectedLanguage.equals("french")) ? TranslateLanguage.FRENCH : (selectedLanguage.equals("tamil")) ? TranslateLanguage.TAMIL : (selectedLanguage.equals("english")) ? TranslateLanguage.ENGLISH : /*(isChichewaLanguageAvailable && selectedLanguage.equals("chichewa"))? TranslateLanguage.fromLanguageTag("ny") :*/ (selectedLanguage.equals("swahili")) ? TranslateLanguage.SWAHILI : (selectedLanguage.equals("arabic")) ? TranslateLanguage.ARABIC : TranslateLanguage.HINDI;
        if (isReversed) {
            return new TranslatorOptions.Builder().setTargetLanguage(sourceLanguage).setSourceLanguage(TranslateLanguage.ENGLISH).build();
        } else {
            return new TranslatorOptions.Builder().setSourceLanguage(sourceLanguage).setTargetLanguage(TranslateLanguage.ENGLISH).build();
        }
    }

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
            showExceptionMsg(R.string.ERR100);
        }


        try {
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
        } catch (Exception e) {
            showExceptionMsg(R.string.ERR100);
            e.printStackTrace();
        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Handle item selection
        if (item.getItemId() == android.R.id.home) {
            try {
                android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(LLMAssistant.this);
                dialog.setMessage("Are you sure you want to exit the app?");
                dialog.setPositiveButton("Yes", (dialog1, which) -> finishAffinity());
                dialog.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
                android.app.AlertDialog alert = dialog.create();
                alert.show();
            } catch (Exception e) {
                showExceptionMsg(R.string.ERR100);
                e.printStackTrace();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void downloadLanguageModel(TranslateRemoteModel modelToDownload, OnCompleteListener<Void> onCompleteListener) {
        RemoteModelManager modelManager = RemoteModelManager.getInstance();
        DownloadConditions conditions = new DownloadConditions.Builder().build();
        Handler mHandler = new Handler(Looper.getMainLooper());
        Runnable runnable  = new Runnable() {
            @Override
            public void run() {
                if (LLMAssistant.this.isNetworkConnected()) {
                    modelManager.download(modelToDownload, conditions).addOnCompleteListener(onCompleteListener);
                } else {
                    mHandler.postDelayed(this, 500);
                }
            }
        };
        mHandler.post(runnable);
    }


    private void checkLanguageModelsAvailability() {
        try {
            RemoteModelManager modelManager = RemoteModelManager.getInstance();

            TranslateRemoteModel englishModel = new TranslateRemoteModel.Builder(TranslateLanguage.ENGLISH).build();
//            TranslateRemoteModel tamilModel = new TranslateRemoteModel.Builder(TranslateLanguage.TAMIL).build();
            modelManager.getDownloadedModels(TranslateRemoteModel.class)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Set<TranslateRemoteModel> models = task.getResult();
                            if (!models.contains(englishModel)) {
                                downloadLanguageModel(englishModel,
                                        englishModelDownloadResult -> {
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
//                            if(!models.contains(tamilModel)) {
//                                downloadLanguageModel(tamilModel,
//                                        tamilModelDownloadResult -> {
//                                            if (tamilModelDownloadResult.isSuccessful()) {
//                                                isTamilModelAvailable = true;
//                                            } else {
//                                                if (tamilModelDownloadResult.getException() != null) {
//                                                    tamilModelDownloadResult.getException().printStackTrace();
//                                                }
//                                            }
//                                        });
//                            } else {
//                                isTamilModelAvailable = true;
//                            }
                        } else {
                            if (task.getException() != null) {
                                showExceptionMsg(R.string.ERR100);
                                task.getException().printStackTrace();
                            }
                        }
                    });
        } catch (Exception e) {
            showExceptionMsg(R.string.ERR100);
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


    public boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

    private Intent getSpeechRecognizerIntent(String selectedLanguage) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, (selectedLanguage.equals("hindi")) ? "hi-IN" : (selectedLanguage.equals("tamil") ? "ta-IN" : (selectedLanguage.equals("english") ? "en" : /*(selectedLanguage.equals("chichewa"))? "ny":*/ (selectedLanguage.equals("swahili")) ? "sw" : (selectedLanguage.equals("arabic")) ? "ar" : "fr")));
        return intent;
    }

    public JSONObject getExceptionJson() {
        String fileName = "ExceptionJson.json";
        JSONObject jsonObject = null;
        try {
            // Get the AssetManager
            InputStream inputStream = getAssets().open(fileName);

            // Read the JSON file into a string
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            System.out.println(inputStream.read(buffer));
            inputStream.close();
            String jsonString = new String(buffer, StandardCharsets.UTF_8);

            // Parse the JSON string into a JSONObject
            jsonObject = new JSONObject(jsonString);
        } catch (IOException | JSONException e) {

            e.printStackTrace();
        }
        return jsonObject;
    }

    String getException(int errorCode) {
        String errorCodeString = getString(errorCode);
        return exceptionJson.optString(errorCodeString, exceptionJson.optString(getString(R.string.standard_exception)));
    }

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void showKeyboard(View view) {
        if (view instanceof EditText) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    void init() {
        try {
            exceptionJson = getExceptionJson();
            recorderIc = findViewById(R.id.recorderIc);
            outputView = findViewById(R.id.outputView);
            recordBtn = findViewById(R.id.recordBtn);
            languageSpinner = findViewById(R.id.languageSpinner);
            languageSpinner.setEnabled(false);
            languageSpinner.setVisibility(View.VISIBLE);
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
                try {
                    isPositiveFeedback = !isPositiveFeedback;
                    if (isNegativeFeedback) {
                        negativeFeedback.setImageDrawable(AppCompatResources.getDrawable(LLMAssistant.this, R.drawable.thumbs_down_ic));
                        isNegativeFeedback = !isNegativeFeedback;
                    }
                    positiveFeedback.setImageDrawable(AppCompatResources.getDrawable(LLMAssistant.this, (isPositiveFeedback) ? R.drawable.thumbs_up_fill_ic : R.drawable.thumbs_up_ic));
                } catch (Exception e) {
                    showExceptionMsg(R.string.ERR100);
                    e.printStackTrace();
                }
            });

            negativeFeedback.setOnClickListener(onClickNegative -> {
                try {
                    isNegativeFeedback = !isNegativeFeedback;
                    if (isPositiveFeedback) {
                        positiveFeedback.setImageDrawable(AppCompatResources.getDrawable(LLMAssistant.this, R.drawable.thumbs_up_ic));
                        isPositiveFeedback = !isPositiveFeedback;
                    }
                    negativeFeedback.setImageDrawable(AppCompatResources.getDrawable(LLMAssistant.this, (isNegativeFeedback) ? R.drawable.thumbs_down_fill_ic : R.drawable.thumbs_down_ic));
                } catch (Exception e) {
                    showExceptionMsg(R.string.ERR100);
                    e.printStackTrace();
                }
            });


            resetViewVisibility();

            editBtn.setOnClickListener(onClickEdit -> {
                try {
                    if (isNetworkConnected()) {
                        if (isTyping) {
                            showExceptionMsg(R.string.ERR002);
                            return;
                        }
                        editBtn.setVisibility(View.GONE);
                        userQuestionView.setEnabled(true);
                        languageSpinner.setVisibility(View.GONE);
                        userQuestionView.setSelection(userQuestionView.getText().length());
                        userQuestionView.setFocusable(true);
                        userQuestionView.requestFocus();
                        showKeyboard(userQuestionView);
                        sendBtn.setVisibility(View.VISIBLE);
                    } else {
                        showExceptionMsg(R.string.ERR001);
                    }
                } catch (Exception e) {
                    showExceptionMsg(R.string.ERR100);
                    e.printStackTrace();
                }
            });

            sendBtn.setOnClickListener(onClickSend -> {
                try {
                    if (isNetworkConnected()) {
                        String questionText = userQuestionView.getText().toString().trim();
                        if (questionText.isEmpty()) {
                            showExceptionMsg(R.string.VAL001);
                            return;
                        }
                        userQuestionView.setEnabled(false);
                        languageSpinner.setVisibility(View.VISIBLE);
                        hideKeyboard(userQuestionView);
                        sendBtn.setVisibility(View.GONE);
                        if(!questionText.equals(question.trim()) || isResponseStopped) {
                            isTyping = true;
                            translateAndAskLlm(questionText);
                            editBtn.setVisibility(View.GONE);
                        } else {
                            editBtn.setVisibility(View.VISIBLE);
                        }
                    } else {
                        showExceptionMsg(R.string.ERR001);
                    }
                } catch (Exception e) {
                    showExceptionMsg(R.string.ERR100);
                    e.printStackTrace();
                }
            });

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recordBtn.setOnClickListener(onClickRecordBtn -> {
                try {
                    if (isNetworkConnected()) {
                        try {
                            userQuestionView.setEnabled(false);
                            editBtn.setVisibility(View.GONE);
                            hideKeyboard(userQuestionView);
                            languageSpinner.setVisibility(View.VISIBLE);
                            sendBtn.setVisibility(View.GONE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (isEnglishModelAvailable) {
                            if (isTyping) {
                                showExceptionMsg(R.string.ERR002);
                            }
                            else {
                                if (isRecording) {
                                    speechRecognizer.stopListening();
                                }
                                else {
                                    String selectedLanguage = languageSpinner.getSelectedItem().toString().toLowerCase(Locale.ROOT);
                                    final Intent speechRecognizerIntent = getSpeechRecognizerIntent(selectedLanguage);
                                    speechRecognizer.setRecognitionListener(new RecognitionListener() {
                                        @Override
                                        public void onReadyForSpeech(Bundle bundle) {
                                            outputView.setText(R.string.speak);
                                        }

                                        @Override
                                        public void onBeginningOfSpeech() {
                                            isTyping = true;
                                            editBtn.setVisibility(View.GONE);
                                            userQuestionView.setEnabled(false);
                                            outputView.setText(R.string.listening);
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
                                            outputView.setText(R.string.processing);
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
                                                            isRecording = false;
                                                            outputView.setText("");
                                                            translateAndAskLlm(outputText);
                                                        }
                                                    } catch (Exception e) {
                                                        showExceptionMsg(R.string.ERR100);
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
                                    .setMessage(getException(R.string.ERR003))
                                    .setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss()).create();
                            alertDialog.show();
                        }
                    } else {
                        showExceptionMsg(R.string.ERR001);
                    }
                } catch (Exception e) {
                    showExceptionMsg(R.string.ERR100);
                    e.printStackTrace();
                }
            });

            getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(LLMAssistant.this);
                    dialog.setMessage(R.string.exit_confirmation);
                    dialog.setPositiveButton(R.string.yes, (dialog1, which) -> finishAffinity());
                    dialog.setNegativeButton(R.string.no, (dialogInterface, i) -> dialogInterface.dismiss());
                    android.app.AlertDialog alert = dialog.create();
                    alert.show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetViewVisibility() {
        try {
            stopResponse.setVisibility(View.GONE);
            feedbackLayout.setVisibility(View.GONE);
            sendBtn.setVisibility(View.GONE);
            answerView.setVisibility(View.GONE);
            questionAndAnswerLayout.setVisibility(View.GONE);
            userQuestionView.setEnabled(false);
        } catch (Exception e) {
            showExceptionMsg(R.string.ERR100);
            e.printStackTrace();
        }
    }

    private void showExceptionMsg(int exceptionCode) {
        try {
            String exceptionCodeString = getString(exceptionCode);
            String exceptionMessage = getException(exceptionCode);
            showAlert(exceptionCodeString, exceptionMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void translateAndAskLlm(String outputText) {
        try {
            languageTranslator.translate(outputText).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String translatedText = task.getResult();
                    question = outputText;
                    SharedPreferences sharedPreferences = getSharedPreferences("UserDetails", MODE_PRIVATE);
                    String uniqueUserId = sharedPreferences.getString("userId", "");
                    if (uniqueUserId.isEmpty()) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        uniqueUserId = generateUniqueName();
                        editor.putString("userId", uniqueUserId);
                        editor.apply();
                    }
                    QUERY_URL = String.format(QUERY_URL, uniqueUserId);
                    HttpUrl queryUrl = HttpUrl.parse(QUERY_URL);
                    if (queryUrl != null) {
                        HttpUrl.Builder urlBuilder = queryUrl.newBuilder();
                        answerView.setText("");
                        answerView.setVisibility(View.GONE);
                        feedbackLayout.setVisibility(View.GONE);
                        timerText.setVisibility(View.GONE);
                        editBtn.setVisibility(View.GONE);
                        startLoading();
                        final MediaType JSON = MediaType.get("application/json");
                        JsonObject responseBodyObj = new JsonObject();
                        responseBodyObj.addProperty("index", "aki_insurance_index");
                        responseBodyObj.addProperty("collection_name", "aki_insurance");
                        responseBodyObj.addProperty("question", translatedText);
                        RequestBody requestBody = RequestBody.create(responseBodyObj.toString(), JSON);
                        askLlmAPI(requestBody, urlBuilder.build().url().toString());
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
            showExceptionMsg(R.string.ERR100);
            e.printStackTrace();
        }
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

    private void askLlmAPI(RequestBody requestBody, String url) {
        Timer timer = new Timer();
        timer.startTimer();
        try {
            if (isNetworkConnected()) {
                Thread askLlmThread = new Thread(() -> {
                    try {
                        OkHttpClient client = new OkHttpClient.Builder()
                                .connectTimeout(120, TimeUnit.SECONDS)
                                .writeTimeout(120, TimeUnit.SECONDS)
                                .readTimeout(120, TimeUnit.SECONDS)
                                .build();

                        System.out.println(url);
                        Request request = new Request.Builder().url(url).post(requestBody).build();
                        isResponseStopped = false;
                        try {
                            client.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    String errorMsg = "("+ getString(R.string.API001) + ") " + getException(R.string.API001);
                                    answerView.setText(errorMsg);
                                    e.printStackTrace();
                                }

                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                    try {
                                        if (!response.isSuccessful())
                                            throw new IOException("Unexpected code " + response);
                                        try (ResponseBody responseBody = response.body()) {
                                            if (responseBody != null) {
                                                BufferedReader reader = new BufferedReader(new InputStreamReader(responseBody.byteStream()));
                                                String line;
                                                System.out.println("Here START");
                                                AtomicBoolean isStoppedLoading = new AtomicBoolean(false);
                                                StringBuilder responseString = new StringBuilder();
                                                while (!call.isCanceled() && !isResponseStopped && ((line = reader.readLine()) != null)) {
                                                    if (responseString.length() > 1 && !isStoppedLoading.get()) {
                                                        runOnUiThread(() -> {
                                                            stopLoading();
                                                            answerView.setText("");
                                                            stopResponse.setVisibility(View.VISIBLE);
                                                            answerView.setVisibility(View.VISIBLE);
                                                            stopResponse.setOnClickListener(onClickStop -> {
                                                                isResponseStopped = true;
                                                                handleCompleteResponse();
                                                                if (!call.isCanceled()) {
                                                                    call.cancel();
                                                                }
                                                            });
                                                            isStoppedLoading.set(true);
                                                        });
                                                    }
                                                    // Each line is a complete JSON object
                                                    JSONObject jsonObject = new JSONObject(line);
                                                    try {
                                                        System.out.println(line);
                                                        responseString.append(jsonObject.optString("response"));
                                                        String finalResponseString = responseString.toString();
                                                        runOnUiThread(() -> answerView.setText(finalResponseString));
                                                        Thread.sleep(50);
                                                    } catch (Exception e) {
                                                        showExceptionMsg(R.string.API001);
                                                        e.printStackTrace();
                                                        handleCompleteResponse();
                                                    }
                                                    // Process the JSON object as needed
                                                }
                                                runOnUiThread(() -> {
                                                    handleCompleteResponse();
                                                    timerText.setText(timer.stopTimer());
                                                    timerText.setVisibility(View.VISIBLE);
                                                });

                                                System.out.println("Here END");
                                            }
                                        }
                                    } catch (Exception e) {
                                        runOnUiThread(() -> {
                                            stopLoading();
                                            answerView.setText("");
                                            stopResponse.setVisibility(View.VISIBLE);
                                            answerView.setVisibility(View.VISIBLE);
                                            handleCompleteResponse();
                                            String errorMsg = "("+ getString(R.string.API001) + ") " + getException(R.string.API001);
                                            answerView.setText(errorMsg);
                                        });
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (Exception e) {
                            stopLoading();
                            isTyping = false;
                            runOnUiThread(() -> editBtn.setVisibility(View.VISIBLE));
                            String errorMsg = "("+ getString(R.string.API001) + ") " + getException(R.string.API001);
                            answerView.setText(errorMsg);
                            handleCompleteResponse();
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        stopLoading();
                        isTyping = false;
                        runOnUiThread(() -> editBtn.setVisibility(View.VISIBLE));
                        showExceptionMsg(R.string.ERR100);
                        handleCompleteResponse();
                        e.printStackTrace();
                    }
                });
                askLlmThread.start();
            } else {
                stopLoading();
                isTyping = false;
                handleCompleteResponse();
                showExceptionMsg(R.string.ERR001);
            }
        } catch (Exception e) {
            stopLoading();
            isTyping = false;
            handleCompleteResponse();
            showExceptionMsg(R.string.ERR001);
            e.printStackTrace();
        }
    }

    private void handleCompleteResponse() {
        runOnUiThread(() -> {
            isTyping = false;
            feedbackLayout.setVisibility(View.VISIBLE);
            positiveFeedback.setImageDrawable(AppCompatResources.getDrawable(LLMAssistant.this, R.drawable.thumbs_up_ic));
            negativeFeedback.setImageDrawable(AppCompatResources.getDrawable(LLMAssistant.this, R.drawable.thumbs_down_ic));
            editBtn.setVisibility(View.VISIBLE);
            stopResponse.setVisibility(View.GONE);
        });
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
}