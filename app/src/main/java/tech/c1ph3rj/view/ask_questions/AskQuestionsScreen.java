package tech.c1ph3rj.view.ask_questions;

import static tech.c1ph3rj.view.Services.checkNull;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tech.c1ph3rj.view.R;
import tech.c1ph3rj.view.Services;

public class AskQuestionsScreen extends AppCompatActivity {
    Services services;
    TextView answerView;
    ShimmerFrameLayout loadingView;
    TextView userQuestionView;
    LinearLayout questionAndAnswerLayout;
    EditText questionView;
    LinearLayout sendBtn;
    LinearLayout askQuestionLayout;
    boolean isTyping;
    int index = 0;
    private final String QUERY_URL = "https://rag-llm.azurewebsites.net/ask_v2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ask_questions_screen);

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

        services = new Services(this, this::refresh);
        init();
    }

    void refresh() {
        try {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void init() {
        try {
            askQuestionLayout = findViewById(R.id.askQuestionsLayout);
            questionView = findViewById(R.id.questionView);
            answerView = findViewById(R.id.answerView);
            loadingView = findViewById(R.id.loadingView);
            userQuestionView = findViewById(R.id.userQuestionView);
            questionAndAnswerLayout = findViewById(R.id.questionAndAnswerView);
            sendBtn = findViewById(R.id.sendBtn);

            loadingView.setVisibility(View.GONE);
            answerView.setVisibility(View.GONE);
            questionAndAnswerLayout.setVisibility(View.GONE);

            sendBtn.setOnClickListener(onClickSend -> {
                String question = questionView.getText().toString().trim();
                if (question.isEmpty()) {
                    Toast.makeText(this, "Please type your question to continue!", Toast.LENGTH_SHORT).show();
                    return;
                } else if (isTyping) {
                    Toast.makeText(this, "Wait while the response is loading!", Toast.LENGTH_SHORT).show();
                    return;
                }

                HttpUrl queryUrl = HttpUrl.parse(QUERY_URL);
                if (queryUrl != null) {
                    HttpUrl.Builder urlBuilder = queryUrl.newBuilder();
                    urlBuilder.addQueryParameter("input", question);
                    answerView.setText("");
                    answerView.setVisibility(View.GONE);
                    startLoading();
                    userQuestionView.setText("");
                    userQuestionView.setText(question);
                    questionAndAnswerLayout.setVisibility(View.VISIBLE);
                    isTyping = true;
                    askLlmAPI(urlBuilder.build().toString());
                }
                questionView.setText("");
                hideKeyboard(questionView);
                questionView.clearFocus();


            });
        } catch (Exception e) {
            services.handleException(e);
        }
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        getOnBackPressedDispatcher().onBackPressed();
        return super.onOptionsItemSelected(item);
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
                                    stopLoading();
                                    if (checkNull(answer)) {
                                        startTypingAnimation(answer);
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