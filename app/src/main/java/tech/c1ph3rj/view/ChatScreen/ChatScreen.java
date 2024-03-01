package tech.c1ph3rj.view.ChatScreen;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import tech.c1ph3rj.view.ChatScreen.adapter.ChatAdapter;
import tech.c1ph3rj.view.ChatScreen.model.ChatMessages;
import tech.c1ph3rj.view.PermissionManager;
import tech.c1ph3rj.view.R;
import tech.c1ph3rj.view.Timer;


public class ChatScreen extends AppCompatActivity {


    ArrayList<ChatMessages> messagesArrayList;
    ChatAdapter chatAdapter;
    ImageView sendButton, micBtn, backButton;
    EditText inputQuestionView;
    RecyclerView chatView;
    Context context;
    Activity activity;
    LinearLayout bottomLayout;
    String time;
    JSONObject exceptionJson;
    PermissionManager permissionManager;
    boolean isTyping = false;
    boolean isRecording = false;
    boolean isResponseStopped = false;
    String selectedLanguage = "english";

    SpeechRecognizer speechRecognizer;
    Dialog speechRecognizerDialog;
    TextView outputView;
    ImageView recorderIc;
    ChatMessages lastChatResponse;
    TextView messageView;
    TextView senderView;
    LinearLayout greetingsView;
    Call currentResponseCall;
    ChipGroup suggestionView;

    private String QUERY_URL = "https://rag-llm.azurewebsites.net/mongo_atlas_memory_dynamic_streamed_ask";

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

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);
        context = this;
        activity = this;
        exceptionJson = getExceptionJson();
        try {
            permissionManager = new PermissionManager(this);
            permissionManager.setPermissionResultListener(
                    new PermissionManager.PermissionResultListener() {
                        @Override
                        public void onPermissionGranted() {
                            init();
                        }

                        @Override
                        public void onPermissionDenied() {
                            permissionManager.showPermissionExplanationDialog();
                        }
                    }
            );
        } catch (Exception e) {
            showExceptionMsg(R.string.ERR100);
            e.printStackTrace();
        }
        try {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }
            init();
        } catch (Exception e) {
            showExceptionMsg(R.string.ERR100);
            e.printStackTrace();
        }
    }


    public boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
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

    private Intent getSpeechRecognizerIntent(String selectedLanguage) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, (selectedLanguage.equals("hindi")) ? "hi-IN" : (selectedLanguage.equals("tamil") ? "ta-IN" : (selectedLanguage.equals("english") ? "en" : /*(selectedLanguage.equals("chichewa"))? "ny":*/ (selectedLanguage.equals("swahili")) ? "sw" : (selectedLanguage.equals("arabic")) ? "ar" : "fr")));
        return intent;
    }

    private void initSpeechRecognizerDialog() {
        speechRecognizerDialog = new Dialog(context);
        speechRecognizerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // Remove the title bar
        speechRecognizerDialog.setContentView(R.layout.recorder_view);// Set the custom layout
        Window window = speechRecognizerDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(context.getColor(android.R.color.transparent)));
// Get the LayoutParams of the dialog window
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
// Set the width and height to WRAP_CONTENT
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
// Set the updated LayoutParams to the dialog window
            window.setAttributes(layoutParams);
        }
        speechRecognizerDialog.setCancelable(true);
        outputView = speechRecognizerDialog.findViewById(R.id.outputView);
        recorderIc = speechRecognizerDialog.findViewById(R.id.recorderIc);
        ImageView closeBtn = speechRecognizerDialog.findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(onClickCLose -> {
            speechRecognizer.cancel();
            speechRecognizerDialog.dismiss();
        });

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                outputView.setText(R.string.speak);
            }

            @Override
            public void onBeginningOfSpeech() {
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
                speechRecognizerDialog.dismiss();
            }

            @Override
            public void onError(int i) {
                recorderIc.setImageTintList(ColorStateList.valueOf(getColor(R.color.black)));
                outputView.setText("");
                speechRecognizerDialog.dismiss();
            }

            @Override
            public void onResults(Bundle result) {
                ArrayList<String> matches = result.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null) {
                    String outputText = matches.get(0);
                    if (outputText.isEmpty()) {
                        outputView.setText("");
                    } else {
                        try {
                            isRecording = false;
                            outputView.setText("");
                            speechRecognizerDialog.dismiss();
                            String previouslyTypedText = inputQuestionView.getText().toString();
                            outputText = previouslyTypedText + outputText;
                            inputQuestionView.setText(outputText);
                            sendButton.performClick();
                        } catch (Exception e) {
                            showExceptionMsg(R.string.ERR100);
                            e.printStackTrace();
                        }
                    }
                } else {
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
    }

    void checkGreetingsVisibility() {
        try {
            greetingsView.setVisibility((messagesArrayList.isEmpty()) ? View.VISIBLE : View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void init() {
        try {
            sendButton = findViewById(R.id.sendMessage);
            micBtn = findViewById(R.id.micBtn);
            backButton = findViewById(R.id.back);
            inputQuestionView = findViewById(R.id.inputMessage);
            chatView = findViewById(R.id.rv_messages);
            bottomLayout = findViewById(R.id.bottomLayout);
            greetingsView = findViewById(R.id.greetingsView);
            suggestionView = findViewById(R.id.suggestionView);

            messagesArrayList = new ArrayList<>();
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setStackFromEnd(true);
            chatView.setLayoutManager(layoutManager);
            chatAdapter = new ChatAdapter(messagesArrayList, (message, messageView, senderView) -> {
                lastChatResponse = message;
                this.messageView = messageView;
                this.senderView = senderView;

            });
            chatView.setAdapter(chatAdapter);

            initSpeechRecognizerDialog();

            micBtn.setOnClickListener(onClickMic -> {
                if (permissionManager.hasPermissions(permissionManager.recordAudioPermissionArray())) {
                    if (isNetworkConnected()) {
                        if (isTyping) {
                            showExceptionMsg(R.string.ERR002);
                        } else {
                            if (isRecording) {
                                speechRecognizer.stopListening();
                            } else {
                                try {
                                    speechRecognizerDialog.show();
                                    recorderIc.setImageTintList(ColorStateList.valueOf(getColor(R.color.red)));
                                    final Intent speechRecognizerIntent = getSpeechRecognizerIntent(selectedLanguage);
                                    speechRecognizer.startListening(speechRecognizerIntent);
                                } catch (Exception e) {
                                    showExceptionMsg(R.string.ERR100);
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else {
                        showExceptionMsg(R.string.ERR001);
                    }
                } else {
                    permissionManager.requestPermissions(permissionManager.recordAudioPermissionArray());
                }
            });

            sendButton.setOnClickListener(l -> {
                try {
                    if (isTyping) {
                        if (currentResponseCall != null && !isResponseStopped) {
                            currentResponseCall.cancel();
                            isResponseStopped = true;
                            lastChatResponse.date = getTime();
                            sendButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.chat_send_inline));
                            return;
                        }
                        showExceptionMsg(R.string.ERR002);
                        return;
                    }

                    if (inputQuestionView.length() != 0 && !inputQuestionView.getText().toString().trim().equals("")) {
                        if (isNetworkConnected()) {
                            time = getTime();
                            ChatMessages element = new ChatMessages(inputQuestionView.getText().toString().trim(), "", true, "You", time);
                            messagesArrayList.add(element);
                            element = new ChatMessages("", "", false, "Digital Assistant", "");
                            element.isMsgLoading = true;
                            messagesArrayList.add(element);
                            chatAdapter.notifyDataSetChanged();
                            chatView.smoothScrollToPosition(Objects.requireNonNull(chatView.getAdapter()).getItemCount() - 1);
                            isTyping = true;
                            translateAndAskLlm(inputQuestionView.getText().toString().trim());
                            inputQuestionView.setText("");
                            hideKeyboard(inputQuestionView);
                            checkGreetingsVisibility();
                        } else {
                            showExceptionMsg(R.string.ERR001);
                        }
                    } else {
                        showExceptionMsg(R.string.VAL001);
                    }
                } catch (Exception e) {
                    isTyping = false;
                    showExceptionMsg(R.string.ERR100);
                    e.printStackTrace();
                }
            });
            backButton.setOnClickListener(l -> {
                try {
                    getOnBackPressedDispatcher().onBackPressed();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            chatView.addOnLayoutChangeListener((view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                if (bottom < oldBottom) {
                    chatView.scrollBy(0, oldBottom - bottom);
                }
            });

            getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    try {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(ChatScreen.this);
                        dialog.setMessage("Are you sure you want to exit the app?");
                        dialog.setPositiveButton("Yes", (dialog1, which) -> finishAffinity());
                        dialog.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
                        AlertDialog alert = dialog.create();
                        alert.show();
                    } catch (Exception e) {
                        showExceptionMsg(R.string.ERR100);
                        e.printStackTrace();
                    }
                }
            });


            initializeSuggestions();

            checkGreetingsVisibility();
        } catch (Exception e) {
            showExceptionMsg(R.string.ERR100);
            e.printStackTrace();
        }
    }

    private void initializeSuggestions() {
        try {
            ArrayList<String> suggestions = new ArrayList<>();
            suggestions.add("What is Insurance?");
            suggestions.add("What is Life Insurance?");
            suggestions.add("What is Comprehensive?");
            suggestions.add("What is Third Party?");
            suggestions.add("What is Third Party Fire and Theft?");
            for (String eachSuggestion : suggestions) {
                Chip suggestionChip = new Chip(this);
                suggestionChip.setText(eachSuggestion);
                suggestionChip.setChipBackgroundColorResource(R.color.greyLight); // Set chip background color
                suggestionChip.setTextColor(Color.BLACK);
                suggestionChip.setOnClickListener(onClickSuggestion -> {
                    inputQuestionView.setText(suggestionChip.getText().toString());
                    sendButton.performClick();
                });
                suggestionView.addView(suggestionChip);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void updateAdapterResponse() {
        try {
            runOnUiThread(() -> chatAdapter.notifyItemChanged(messagesArrayList.size() - 1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void translateAndAskLlm(String outputText) {
        try {
            QUERY_URL = String.format(QUERY_URL);
            HttpUrl queryUrl = HttpUrl.parse(QUERY_URL);
            if (queryUrl != null) {
                HttpUrl.Builder urlBuilder = queryUrl.newBuilder();
                final MediaType JSON = MediaType.get("application/json");
                JsonObject responseBodyObj = new JsonObject();
                responseBodyObj.addProperty("index", "client_toggle_index");
                responseBodyObj.addProperty("collection_name", "client_toggle");
                responseBodyObj.addProperty("question", outputText);
                RequestBody requestBody = RequestBody.create(responseBodyObj.toString(), JSON);
                askLlmAPI(requestBody, urlBuilder.build().url().toString());
            }
        } catch (Exception e) {
            updateErrorToChat(R.string.ERR100, false);
            e.printStackTrace();
        }
    }

    private void askLlmAPI(RequestBody requestBody, String url) {
        Timer timer = new Timer();
        timer.startTimer();
        try {
            if (isNetworkConnected()) {
                Thread askLlmThread = new Thread(() -> {
                    try {
                        OkHttpClient client = new OkHttpClient.Builder()
                                .connectTimeout(60, TimeUnit.SECONDS)
                                .writeTimeout(60, TimeUnit.SECONDS)
                                .readTimeout(60, TimeUnit.SECONDS)
                                .build();

                        System.out.println(url);
                        Request request = new Request.Builder().url(url).post(requestBody).build();
                        isResponseStopped = false;
                        try {
                            client.newCall(request).enqueue(new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                    updateErrorToChat(R.string.API001, true);
                                    e.printStackTrace();
                                }

                                /** @noinspection BusyWait*/
                                @Override
                                public void onResponse(@NonNull Call call, @NonNull Response response) {
                                    try {
                                        if (!response.isSuccessful())
                                            throw new IOException("Unexpected code " + response);
                                        try (ResponseBody responseBody = response.body()) {
                                            if (responseBody != null) {
                                                BufferedReader reader = new BufferedReader(new InputStreamReader(responseBody.byteStream()));
                                                String line;
                                                AtomicBoolean isStoppedLoading = new AtomicBoolean(false);
                                                StringBuilder responseString = new StringBuilder();
                                                currentResponseCall = call;
                                                runOnUiThread(() -> sendButton.setImageDrawable(AppCompatResources.getDrawable(ChatScreen.this, R.drawable.stop_recording_ic)));
                                                while (!call.isCanceled() && !isResponseStopped && ((line = reader.readLine()) != null)) {
                                                    if (responseString.length() > 1 && !isStoppedLoading.get()) {
                                                        runOnUiThread(() -> {
                                                            lastChatResponse.isMsgLoading = false;
                                                            updateAdapterResponse();
                                                            isStoppedLoading.set(true);
                                                        });
                                                    }
                                                    // Each line is a complete JSON object
                                                    JSONObject jsonObject = new JSONObject(line);
                                                    try {
                                                        responseString.append(jsonObject.optString("response"));
                                                        lastChatResponse.message = responseString.toString();
//                                                        updateAdapterResponse();
                                                        updateTextView();
                                                        Thread.sleep(50);
                                                    } catch (Exception e) {
                                                        updateErrorToChat(R.string.API001, true);
                                                        e.printStackTrace();

                                                    }
                                                }
                                                currentResponseCall = null;
                                                runOnUiThread(() -> {
                                                    sendButton.setImageDrawable(AppCompatResources.getDrawable(ChatScreen.this, R.drawable.chat_send_inline));
                                                    lastChatResponse.date = getTime();
                                                    updateAdapterResponse();
                                                    isTyping = false;
                                                });
                                            }
                                        }
                                    } catch (Exception e) {
                                        runOnUiThread(() -> sendButton.setImageDrawable(AppCompatResources.getDrawable(ChatScreen.this, R.drawable.chat_send_inline)));
                                        updateErrorToChat(R.string.API001, false);
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (Exception e) {
                            updateErrorToChat(R.string.API001, false);
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        updateErrorToChat(R.string.ERR100, true);
                        e.printStackTrace();
                    }
                });
                askLlmThread.start();
            } else {
                updateErrorToChat(R.string.ERR001, true);
            }
        } catch (Exception e) {
            updateErrorToChat(R.string.ERR001, true);
            e.printStackTrace();
        }
    }

    private void updateTextView() {
        try {
            runOnUiThread(() -> messageView.setText(lastChatResponse.message));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateErrorToChat(int errorCode, boolean showAlert) {
        runOnUiThread(() -> {
            try {
                isTyping = false;
                lastChatResponse.isMsgLoading = false;
                lastChatResponse.date = time;
                lastChatResponse.message = "(" + getString(errorCode) + ") " + getException(errorCode);
                if (showAlert) {
                    showExceptionMsg(errorCode);
                }
                updateAdapterResponse();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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

    String getException(int errorCode) {
        String errorCodeString = getString(errorCode);
        return exceptionJson.optString(errorCodeString, exceptionJson.optString(getString(R.string.standard_exception)));
    }

    private String getTime() {
        Date d = new Date();
        String dateFormat = "hh:mm a";
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sf = new SimpleDateFormat(dateFormat);
        return sf.format(d);
    }
}