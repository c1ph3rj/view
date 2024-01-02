package tech.c1ph3rj.view.audio_translate;

import static tech.c1ph3rj.view.Services.checkNull;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tech.c1ph3rj.view.PermissionManager;
import tech.c1ph3rj.view.R;
import tech.c1ph3rj.view.Services;
import tech.c1ph3rj.view.speech_recognition.SpeechRecognition;

public class AudioRecorder extends AppCompatActivity {
    PermissionManager permissionManager;
    CardView recordBtn;
    ImageView recorderIc;
    Spinner languageSpinner;
    TextView outputView;
    Services services;
    SpeechRecognizer speechRecognizer;
    boolean isRecording;
    boolean isTamilModelAvailable = false;
    boolean isFrenchModelAvailable = false;
    String question;
    Translator languageTranslator = null;
    TextView answerView;
    ShimmerFrameLayout loadingView;
    TextView userQuestionView;
    CardView questionAndAnswerLayout;
    Translator outputTranslator = null;
    boolean isTyping;
    int index = 0;
    private final String QUERY_URL = "https://rag-llm.azurewebsites.net/ask_v2";

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

    private void checkLanguageModelsAvailability() {
        try {
            RemoteModelManager modelManager = RemoteModelManager.getInstance();
            TranslateRemoteModel tamilModel =
                    new TranslateRemoteModel.Builder(TranslateLanguage.TAMIL).build();
            TranslateRemoteModel frenchModel =
                    new TranslateRemoteModel.Builder(TranslateLanguage.FRENCH).build();
            DownloadConditions conditions = new DownloadConditions.Builder()
                    .build();
            modelManager.getDownloadedModels(TranslateRemoteModel.class)
                    .addOnCompleteListener(task -> {
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

            answerView.setVisibility(View.GONE);
            questionAndAnswerLayout.setVisibility(View.GONE);

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recordBtn.setOnClickListener(onClickRecordBtn -> {
                if (isFrenchModelAvailable && isTamilModelAvailable) {
                    if (isTyping) {
                        Toast.makeText(this, "Please wait while the previous response is processing...", Toast.LENGTH_SHORT).show();
                    } else {
                        if (isRecording) {
                            speechRecognizer.stopListening();
                        } else {
                            String selectedLanguage = languageSpinner.getSelectedItem().toString();
                            final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_RESULTS, RecognizerIntent.EXTRA_PARTIAL_RESULTS);
                            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                                @Override
                                public void onReadyForSpeech(Bundle bundle) {
                                    outputView.setText("Speak...");
                                }

                                @Override
                                public void onBeginningOfSpeech() {
                                    isTyping = true;
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
                                    isTyping = false;
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
                                                    languageTranslator.translate(outputText).addOnCompleteListener(task -> {
                                                        if (task.isSuccessful()) {
                                                            String translatedText = task.getResult();
                                                            question = outputText;
                                                            HttpUrl queryUrl = HttpUrl.parse(QUERY_URL);
                                                            if (queryUrl != null) {
                                                                HttpUrl.Builder urlBuilder = queryUrl.newBuilder();
                                                                urlBuilder.addQueryParameter("input", translatedText);
                                                                answerView.setText("");
                                                                answerView.setVisibility(View.GONE);
                                                                startLoading();
                                                                userQuestionView.setText("");
                                                                userQuestionView.setText(question);
                                                                questionAndAnswerLayout.setVisibility(View.VISIBLE);
                                                                askLlmAPI(urlBuilder.build().toString());
                                                            }
                                                        } else {
                                                            outputView.setText(outputText);
                                                            if (task.getException() != null) {
                                                                task.getException().printStackTrace();
                                                            }
                                                        }
                                                    });
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
                            TranslatorOptions languageTranslatorOption =
                                    new TranslatorOptions.Builder()
                                            .setSourceLanguage((selectedLanguage.equals("French")) ? TranslateLanguage.FRENCH : TranslateLanguage.TAMIL)
                                            .setTargetLanguage(TranslateLanguage.ENGLISH)
                                            .build();
                            TranslatorOptions responseTranslatorOption =
                                    new TranslatorOptions.Builder()
                                            .setSourceLanguage(TranslateLanguage.ENGLISH)
                                            .setTargetLanguage((selectedLanguage.equals("French")) ? TranslateLanguage.FRENCH : TranslateLanguage.TAMIL)
                                            .build();
                            languageTranslator =
                                    Translation.getClient(languageTranslatorOption);
                            outputTranslator = Translation.getClient(responseTranslatorOption);

                            getLifecycle().addObserver(languageTranslator);

                            DownloadConditions conditions = new DownloadConditions.Builder()
                                    .build();
                            languageTranslator.downloadModelIfNeeded(conditions)
                                    .addOnSuccessListener(unused -> {
                                        outputView.setText("");
                                        recorderIc.setImageTintList(ColorStateList.valueOf(getColor(R.color.red)));
                                        speechRecognizer.startListening(speechRecognizerIntent);
                                    })
                                    .addOnFailureListener(e -> {
                                        outputView.setText("");
                                        e.printStackTrace();
                                    });
                            outputTranslator.downloadModelIfNeeded(conditions)
                                    .addOnSuccessListener(unused -> {
                                        System.out.println("Downloaded Required Models");
                                    })
                                    .addOnFailureListener(Throwable::printStackTrace);
                        }
                    }
                } else {
                    Toast.makeText(this, "Please wait while the required languages models are downloading...", Toast.LENGTH_SHORT).show();
                    Toast.makeText(this, "Please wait while the required languages models are downloading...", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            services.handleException(e);
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

    private void askLlmAPI(String queryUrl) {
        try {
            if (services.isNetworkConnected()) {
                if (services.checkGpsStatus()) {
                    new Thread(() -> {
                        try {
                            OkHttpClient client = new OkHttpClient.Builder()
                                    .connectTimeout(120, TimeUnit.SECONDS)
                                    .writeTimeout(120, TimeUnit.SECONDS)
                                    .readTimeout(120, TimeUnit.SECONDS)
                                    .build();

                            Request request = new Request.Builder()
                                    .url(queryUrl)
                                    .get()
                                    .build();

                            Response staticResponse;

                            try {
                                staticResponse = client.newCall(request).execute();

                                if (staticResponse.body() != null) {
                                    String responseString = staticResponse.body().string();
                                    JSONObject staticResObj = new JSONObject(responseString);
                                    String answer = staticResObj.optString("answer");
                                    if (checkNull(answer)) {
                                        if (outputTranslator != null) {
                                            outputTranslator.translate(answer)
                                                    .addOnCompleteListener(task -> {
                                                        stopLoading();
                                                        if (task.isSuccessful()) {
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
                                    services.somethingWentWrong();
                                }


                            } catch (Exception e) {
                                stopLoading();
                                isTyping = false;
                                services.somethingWentWrong();
                                services.handleException(e);
                            }
                        } catch (Exception e) {
                            stopLoading();
                            isTyping = false;
                            services.somethingWentWrong();
                            services.handleException(e);
                        }
                    }).start();
                } else {
                    stopLoading();
                    isTyping = false;
                    services.redirectToGpsSettings();
                }
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
                index = 0;
                final Handler handler = new Handler(getMainLooper());
                answerView.setVisibility(View.VISIBLE);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (index <= fullText.length()) {
                            String partialText = fullText.substring(0, index);
                            answerView.setText(partialText);
                            index++;
                            handler.postDelayed(this, 35);
                        } else {
                            isTyping = false;
                        }
                    }
                }, 35);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}