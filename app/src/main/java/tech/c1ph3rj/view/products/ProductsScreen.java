package tech.c1ph3rj.view.products;

import static tech.c1ph3rj.view.Services.checkNull;
import static tech.c1ph3rj.view.Services.token;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tech.c1ph3rj.view.R;
import tech.c1ph3rj.view.Services;
import tech.c1ph3rj.view.line_of_business.LineOfBusinessAdapter;
import tech.c1ph3rj.view.line_of_business.LineOfBusinessModel;

public class ProductsScreen extends AppCompatActivity {
    Services services;
    EditText searchView;
    ImageView backBtn;
    ListView productsView;
    ShimmerFrameLayout loadingView;
    LineOfBusinessAdapter productsAdapter;
    List<LineOfBusinessModel> productsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products_screen);
        services = new Services(this);

        init();
    }

    void init() {
        try {
            backBtn = findViewById(R.id.backBtn);
            productsView = findViewById(R.id.lineOfBusinessView);
            searchView = findViewById(R.id.searchView);
            loadingView = findViewById(R.id.loadingView);

            productsAdapter = new LineOfBusinessAdapter(this, new ArrayList<>());
            productsView.setAdapter(productsAdapter);

            productsView.setOnItemClickListener((adapterView, view, i, l) -> {
                LineOfBusinessModel selectedItem = productsAdapter.getItem(i);
                if (selectedItem != null) {
                    String lineOfBusinessId = selectedItem.masterDataID;
                    Intent intent = new Intent(this, ProductsScreen.class);
                    intent.putExtra("lineOfBusinessId", lineOfBusinessId);
                    startActivity(intent);
                }
            });
            productsView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);

            loadingView.setVisibility(View.GONE);
            startLoading();

            backBtn.setOnClickListener(onClickBack -> getOnBackPressedDispatcher().onBackPressed());

            getAllLineOffBusinessAPI();

        } catch (Exception e) {
            services.handleException(e);
        }
    }

    void startLoading() {
        runOnUiThread(() -> {
            try {
                loadingView.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                services.handleException(e);
            }
        });
    }

    void stopLoading() {
        runOnUiThread(() -> {
            try {
                loadingView.setVisibility(View.GONE);
            } catch (Exception e) {
                services.handleException(e);
            }
        });
    }

    private void getAllLineOffBusinessAPI() {
        try {
            if (services.isNetworkConnected()) {
                if (services.checkGpsStatus()) {
                    new Thread(() -> {
                        try {
                            String baseUrl = services.baseUrl + "api/digital/core/MasterData/FetchMasterData";
                            OkHttpClient client = new OkHttpClient.Builder()
                                    .connectTimeout(120, TimeUnit.SECONDS)
                                    .writeTimeout(120, TimeUnit.SECONDS)
                                    .readTimeout(120, TimeUnit.SECONDS)
                                    .build();
                            final MediaType JSON
                                    = MediaType.parse("application/json; charset=utf-8");
                            JsonObject details = new JsonObject();
                            details.addProperty("functionalClassification", "26000");

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
                                                //TODO HANDLE UN AUTH
                                            } else if (rCode.equals("200")) {
                                                // Parse the JSON array
                                                JSONArray jsonArray = staticResObj.getJSONObject("rObj").getJSONArray("fetchMasterData");

                                                // Create a list to store LineOfBusinessModel objects
                                                productsList = new ArrayList<>();

                                                // Iterate through the JSON array and create LineOfBusinessModel objects
                                                for (int i = 0; i < jsonArray.length(); i++) {
                                                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                                                    LineOfBusinessModel lineOfBusiness = new LineOfBusinessModel();
                                                    lineOfBusiness.masterDataID = jsonObject.optString("masterDataID", "");
                                                    lineOfBusiness.parentMasterDataID = jsonObject.optString("parentMasterDataID", "");
                                                    lineOfBusiness.mdCategoryID = jsonObject.optString("mdCategoryID", "");
                                                    lineOfBusiness.mdTitle = jsonObject.optString("mdTitle", "");
                                                    lineOfBusiness.mdDesc = jsonObject.optString("mdDesc", "");
                                                    lineOfBusiness.mdValue = jsonObject.optString("mdValue", "");
                                                    lineOfBusiness.iconURL = jsonObject.optString("iconURL", "");
                                                    lineOfBusiness.regExValidation = jsonObject.optString("regExValidation", "");

                                                    productsList.add(lineOfBusiness);
                                                }

                                                runOnUiThread(() -> {
                                                    stopLoading();
                                                    searchView.addTextChangedListener(new TextWatcher() {
                                                        @Override
                                                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                                        }

                                                        @Override
                                                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                                        }

                                                        @Override
                                                        public void afterTextChanged(Editable editable) {
                                                            if (editable != null) {
                                                                filterData(editable.toString());
                                                            }
                                                        }
                                                    });
                                                    productsAdapter.addAll(productsList);
                                                });
                                            } else {
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
                                handleException(e);
                            }
                        } catch (Exception e) {
                            handleException(e);
                        }
                    }).start();
                } else {
                    services.redirectToGpsSettings();
                    stopLoading();
                }
            } else {
                //TODO HANDLE NETWORK
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void filterData(String query) {
        // Create a new list to store the filtered data
        List<LineOfBusinessModel> filteredData = new ArrayList<>();

        // Iterate through the original data and add matching items to the filtered list
        for (LineOfBusinessModel model : productsList) {
            if (model != null) {
                String itemName = model.mdTitle;
                if (itemName.toLowerCase().contains(query.toLowerCase().trim())) {
                    filteredData.add(model);
                }
            }
        }
        productsAdapter.clear();
        productsAdapter.addAll(filteredData);
    }

    void handleException(Exception e) {
        stopLoading();
        services.somethingWentWrong();
        services.handleException(e);
    }
}