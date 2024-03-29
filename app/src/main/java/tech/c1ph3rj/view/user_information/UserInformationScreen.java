package tech.c1ph3rj.view.user_information;

import static tech.c1ph3rj.view.Services.checkNull;
import static tech.c1ph3rj.view.Services.token;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tech.c1ph3rj.view.R;
import tech.c1ph3rj.view.Services;
import tech.c1ph3rj.view.custom_forms.CustomForms;

public class UserInformationScreen extends AppCompatActivity {
    Services services;
    EditText nameView, phoneNoView, emailView;
    LinearLayout nextBtn;
    String name, email, phoneNo;
    String[] selectedProductIds;


    String premiumCategoryIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_information_screen);
        try {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("User Information");
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        getOnBackPressedDispatcher().onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    void refresh() {
        try {
            services.showDialog();
            insertQuotationAPI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void init() {
        try {
            selectedProductIds = getIntent().getStringArrayExtra("productIds");
            premiumCategoryIds = getIntent().getStringExtra("lineOfBusinessId");
            nameView = findViewById(R.id.nameView);
            emailView = findViewById(R.id.emailView);
            phoneNoView = findViewById(R.id.phoneNoView);
            nextBtn = findViewById(R.id.nextBtn);

            nextBtn.setOnClickListener(onClickNext -> {
                name = nameView.getText().toString().trim();
                email = emailView.getText().toString().trim().toLowerCase(Locale.getDefault());
                phoneNo = phoneNoView.getText().toString().trim();
                String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

                if (!checkNull(name)) {
                    Toast.makeText(this, "Please enter your full name to continue!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!checkNull(phoneNo)) {
                    Toast.makeText(this, "Please enter your phone number to continue!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (phoneNo.length() < 9) {
                    Toast.makeText(this, "Please enter your valid phone number to continue!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!checkNull(email)) {
                    Toast.makeText(this, "Please enter your email address to continue!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!email.matches(emailRegex)) {
                    Toast.makeText(this, "Please enter your valid email address to continue!", Toast.LENGTH_SHORT).show();
                    return;
                }

                refresh();
            });

        } catch (Exception e) {
            services.handleException(e);
        }
    }

    private void insertQuotationAPI() {
        try {
            if (services.isNetworkConnected()) {
                if (services.checkGpsStatus()) {
                    new Thread(() -> {
                        try {
                            String baseUrl = services.baseUrl + "api/digital/core/Quotation/InsertQuotation";
                            OkHttpClient client = new OkHttpClient.Builder()
                                    .connectTimeout(120, TimeUnit.SECONDS)
                                    .writeTimeout(120, TimeUnit.SECONDS)
                                    .readTimeout(120, TimeUnit.SECONDS)
                                    .build();
                            final MediaType JSON
                                    = MediaType.parse("application/json; charset=utf-8");
                            JsonObject details = new JsonObject();
                            details.add("productIDs", new Gson().toJsonTree(Arrays.asList(selectedProductIds)));
                            details.addProperty("email", email);
                            details.addProperty("name", name);
                            details.addProperty("phoneNumber", phoneNo);

                            String insertString = details.toString();


                            RequestBody body = RequestBody.create(insertString, JSON);
                            Request request = new Request.Builder()
                                    .url(baseUrl)
                                    .header("Authorization", "Bearer " + token)
                                    .post(body)
                                    .build();

                            Response staticResponse;

                            try {
                                staticResponse = client.newCall(request).execute();

                                if (staticResponse.body() != null) {
                                    if (staticResponse.code() == 401) {
                                        //TODO HANDLE UN AUTH
                                    } else {
                                        String responseString = staticResponse.body().string();
                                        JSONObject staticResObj = new JSONObject(responseString);
                                        String rCode = staticResObj.optString("rcode");
                                        if (checkNull(rCode)) {
                                            if (rCode.equals("401")) {
                                                services.dismissDialog();
                                                //TODO HANDLE UN AUTH
                                            } else if (rCode.equals("200")) {
                                                //TODO HANDLE SUCCESS
                                                JSONObject rObj = staticResObj.getJSONObject("rObj");
                                                String quotationSearchID = rObj.optString("quotationSearchID");
                                                String quotationRefId = rObj.optString("quotationRefID");
                                                Intent intent = new Intent(this, CustomForms.class);
                                                intent.putExtra("productIDs", selectedProductIds);
                                                intent.putExtra("quotationSearchID", quotationSearchID);
                                                intent.putExtra("quotationRefID", quotationRefId);
                                                intent.putExtra("premiumCategoryIDs", premiumCategoryIds);
                                                services.dismissDialog();
                                                startActivity(intent);

                                            } else {
                                                services.dismissDialog();
                                                JSONArray rMsg = staticResObj.getJSONArray("rmsg");
                                                services.showErrorMessageFromAPI(rMsg);
                                            }
                                        } else {
                                            stopLoading();
                                            services.somethingWentWrong();
                                        }
                                    }
                                } else {
                                    stopLoading();
                                    services.somethingWentWrong();
                                }


                            } catch (Exception e) {
                                services.dismissDialog();
                                services.handleException(e);
                            }
                        } catch (Exception e) {
                            services.dismissDialog();
                            services.handleException(e);
                        }
                    }).start();
                } else {
                    services.redirectToGpsSettings();
                    stopLoading();
                }
            } else {
                services.dismissDialog();
                //TODO HANDLE NETWORK
            }
        } catch (Exception e) {
            services.dismissDialog();
            services.handleException(e);
        }
    }

    private void stopLoading() {
        services.dismissDialog();
    }
}