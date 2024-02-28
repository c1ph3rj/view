package tech.c1ph3rj.view.custom_forms;

import static tech.c1ph3rj.view.Services.checkNull;
import static tech.c1ph3rj.view.Services.token;
import static tech.c1ph3rj.view.Services.unAuthorize;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tech.c1ph3rj.view.GetPremium.GetPremium;
import tech.c1ph3rj.view.R;
import tech.c1ph3rj.view.Services;


public class CustomForms extends AppCompatActivity {
    Services services;
    RecyclerView dynamicFormView;
    LinearLayout nextBtn;
    List<FormField> formFields;

    String[] selectedProductIds;

    String premiumCategoryIds;
    String quotationSearchId, quotationRefId;

    public static List<FormField> extractFormFieldsFromResponse(String jsonResponse) {
        List<FormField> formFields = new ArrayList<>();

        try {
            JSONObject response = new JSONObject(jsonResponse);
            JSONArray allApiFormally = response.getJSONObject("rObj").getJSONArray("getAllAPIFormaly");

            // Assuming we're only interested in the first element of the getAllAPIFormally array
            JSONArray fieldGroups = allApiFormally.getJSONObject(0).getJSONArray("fieldGroup");

            for (int i = 0; i < fieldGroups.length(); i++) {
                JSONObject fieldGroup = fieldGroups.getJSONObject(i);
                JSONObject templateOptions = fieldGroup.getJSONObject("templateOptions");

                String key = fieldGroup.optString("key", "");
                String type = fieldGroup.optString("type", "");
                String label = templateOptions.optString("label", "");
                boolean isRequired = templateOptions.optBoolean("required", false);
                String placeholder = templateOptions.optString("placeholder", "");
                String templateType = templateOptions.optString("type", "");
                boolean isSelectAllEnabled = templateOptions.optBoolean("selectAllOption", false);
                boolean isMultiple = templateOptions.optBoolean("multiple", false);
                String regexPattern = templateOptions.optString("pattern", "");
                String validationMsg = "";
                try {
                    validationMsg = templateOptions.getJSONObject("validation").getJSONObject("messages").optString("required", "");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                int orderData = fieldGroup.optInt("orderData");
                JSONArray optionsArray = templateOptions.optJSONArray("options");
                List<FormField.Options> options = new ArrayList<>();

                if (optionsArray != null) {
                    for (int j = 0; j < optionsArray.length(); j++) {
                        JSONObject eachOptionObj = optionsArray.getJSONObject(j);
                        FormField.Options eachOption = new FormField.Options();
                        eachOption.label = eachOptionObj.optString("label", "");
                        eachOption.id = eachOptionObj.optString("value", "");
                        eachOption.isSelected = false;
                        options.add(eachOption);
                    }
                }

                if (!checkNull(placeholder)) {
                    placeholder = "";
                }


                if (isRequired && ((type.toLowerCase(Locale.ROOT).equals("select") && !options.isEmpty()) || type.toLowerCase(Locale.ROOT).equals("input"))) {
                    FormField formField = new FormField(key, label, type, templateType, orderData, isMultiple, isSelectAllEnabled, placeholder, options);
                    formField.isRequired = true;
                    formField.regexPattern = regexPattern;
                    formField.validationMsg = validationMsg;
                    formFields.add(formField);
                }
            }
            formFields.sort(Comparator.comparingInt(FormField::getOrder));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return formFields;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_forms);

        try {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeAsUpIndicator(R.drawable.back_ic);
                getSupportActionBar().setDisplayShowTitleEnabled(true);
                getSupportActionBar().setTitle("Custom Forms");
            }
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }

        services = new Services(this, null);
        init();
    }

    void init() {
        try {
            selectedProductIds = getIntent().getStringArrayExtra("productIDs");
            premiumCategoryIds = getIntent().getStringExtra("premiumCategoryIDs");
            quotationSearchId = getIntent().getStringExtra("quotationSearchID");
            quotationRefId = getIntent().getStringExtra("quotationRefID");
            dynamicFormView = findViewById(R.id.dynamicFormView);
            nextBtn = findViewById(R.id.nextBtn);
            services.showDialog();
            getProducts();
            nextBtn.setOnClickListener(onClickNext -> {
                for (FormField eachFormField : formFields) {
                    if (eachFormField.isRequired) {
                        String value = eachFormField.getValue();
                        System.out.println(eachFormField.getLabel());
                        System.out.println(value + "\n\n\n\n");
                        if (value.isEmpty()) {
                            Toast.makeText(this, "Please " + (eachFormField.getType().equals("select") ? "select " : "enter ") + eachFormField.getLabel() + " to continue!", Toast.LENGTH_SHORT).show();
                            return;
                        } else if (eachFormField.regexPattern != null && !eachFormField.regexPattern.isEmpty() && !eachFormField.validationMsg.isEmpty()) {
                            Toast.makeText(this, eachFormField.validationMsg, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }

                //TODO Redirect to the next api.
                Intent intent = new Intent(this, GetPremium.class);
                intent.putExtra("getPremiumBody", getAnswerValues().toString());
                startActivity(intent);
            });
        } catch (Exception e) {
            services.handleException(e);
        }
    }

    private JsonObject getAnswerValues() {
        try {
            JsonObject answerValues = new JsonObject();
            for (FormField eachFormField : formFields) {
                answerValues.addProperty(eachFormField.getKey(), eachFormField.getValue());
            }

            answerValues.add("productIDs", new Gson().toJsonTree((convertStringArrayToInt(selectedProductIds))));
            answerValues.addProperty("quotationSearchID", quotationSearchId);
            answerValues.addProperty("quotationRefID", quotationRefId);
            return answerValues;
        } catch (Exception e) {
            e.printStackTrace();
            services.handleException(e);
        }
        return new JsonObject();
    }

    private int[] convertStringArrayToInt(String[] stringArray) {
        int length = stringArray.length;
        int[] intArray = new int[length];

        for (int i = 0; i < length; i++) {
            // Use Integer.parseInt to convert each element of stringArray to an int
            intArray[i] = Integer.parseInt(stringArray[i]);
        }

        return intArray;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        getOnBackPressedDispatcher().onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    private void getProducts() {
        try {
            if (services.isNetworkConnected()) {
                new Thread(() -> {
                    try {
                        String baseUrl = services.baseUrl + "api/digital/core/PremiumLogic/GetProducts";
                        OkHttpClient client = new OkHttpClient.Builder()
                                .connectTimeout(120, TimeUnit.SECONDS)
                                .writeTimeout(120, TimeUnit.SECONDS)
                                .readTimeout(120, TimeUnit.SECONDS)
                                .build();
                        final MediaType JSON
                                = MediaType.parse("application/json; charset=utf-8");
                        JsonObject details = new JsonObject();
                        details.add("productIDs", null);
                        details.addProperty("quotationSearchID", quotationSearchId);
                        details.add("premiumCategoryIDs", new Gson().toJsonTree(Collections.emptyList()));

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
                                    unAuthorize(this);
                                    stopLoading();
                                } else {
                                    String responseString = staticResponse.body().string();
                                    JSONObject staticResObj = new JSONObject(responseString);
                                    String rCode = staticResObj.optString("rcode");
                                    if (checkNull(rCode)) {
                                        if (rCode.equals("401")) {
                                            services.dismissDialog();
                                            //TODO HANDLE UN AUTH
                                        } else if (rCode.equals("200")) {
                                            formFields = extractFormFieldsFromResponse(responseString);
                                            runOnUiThread(() -> {
                                                DynamicFormAdapter dynamicFormAdapter = new DynamicFormAdapter(CustomForms.this, formFields);
                                                dynamicFormView.setLayoutManager(new LinearLayoutManager(CustomForms.this));
                                                dynamicFormView.setAdapter(dynamicFormAdapter);
                                            });
                                            services.dismissDialog();
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
                services.dismissDialog();
                services.checkNetworkVisibility();
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