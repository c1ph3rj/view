package tech.c1ph3rj.view.line_of_business;

import static tech.c1ph3rj.view.Services.checkNull;
import static tech.c1ph3rj.view.Services.token;
import static tech.c1ph3rj.view.Services.unAuthorize;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

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
import tech.c1ph3rj.view.products.ProductsScreen;

public class LineOfBusinessScreen extends AppCompatActivity {
    Services services;
    EditText searchView;
    ListView lineOfBusinessView;
    ShimmerFrameLayout loadingView;
    LineOfBusinessAdapter lineOfBusinessAdapter;
    List<LineOfBusinessModel> lineOfBusinessList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_of_business_screen);

        try {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("Line Of Business");
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

    void init() {
        try {
            lineOfBusinessView = findViewById(R.id.lineOfBusinessView);
            searchView = findViewById(R.id.searchView);
            loadingView = findViewById(R.id.loadingView);

            lineOfBusinessAdapter = new LineOfBusinessAdapter(this, new ArrayList<>());
            lineOfBusinessView.setAdapter(lineOfBusinessAdapter);

            lineOfBusinessView.setOnItemClickListener((adapterView, view, i, l) -> {
                LineOfBusinessModel selectedItem = lineOfBusinessAdapter.getItem(i);
                if (selectedItem != null) {
                    String lineOfBusinessId = selectedItem.masterDataID;
                    Intent intent = new Intent(this, ProductsScreen.class);
                    intent.putExtra("lineOfBusinessId", lineOfBusinessId);
                    startActivity(intent);
                }
            });
            lineOfBusinessView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);

            loadingView.setVisibility(View.GONE);
            startLoading();

            getAllLineOffBusinessAPI();

        } catch (Exception e) {
            services.handleException(e);
        }
    }

    void refresh() {
        runOnUiThread(() -> {
            try {
                loadingView.setVisibility(View.GONE);
                startLoading();
                getAllLineOffBusinessAPI();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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
                            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(120, TimeUnit.SECONDS).writeTimeout(120, TimeUnit.SECONDS).readTimeout(120, TimeUnit.SECONDS).build();
                            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                            JsonObject details = new JsonObject();
                            details.addProperty("functionalClassification", "26000");

                            String insertString = details.toString();


                            RequestBody body = RequestBody.create(insertString, JSON);
                            Request request = new Request.Builder().url(baseUrl).header("Authorization", "Bearer " + token).post(body).build();

                            Response staticResponse;

                            try {
                                staticResponse = client.newCall(request).execute();

                                if (staticResponse.body() != null) {
                                    if (staticResponse.code() == 401) {
                                        stopLoading();
                                        unAuthorize(this);
                                    } else {
                                        String responseString = staticResponse.body().string();
                                        JSONObject staticResObj = new JSONObject(responseString);
                                        String rCode = staticResObj.optString("rcode");
                                        if (checkNull(rCode)) {
                                            if (rCode.equals("401")) {
                                                unAuthorize(this);
                                            } else if (rCode.equals("200")) {
                                                // Parse the JSON array
                                                JSONArray jsonArray = staticResObj.getJSONObject("rObj").getJSONArray("fetchMasterData");

                                                // Create a list to store LineOfBusinessModel objects
                                                lineOfBusinessList = new ArrayList<>();

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

                                                    lineOfBusinessList.add(lineOfBusiness);
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
                                                    lineOfBusinessAdapter.addAll(lineOfBusinessList);
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
                services.checkNetworkVisibility();
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void filterData(String query) {
        // Create a new list to store the filtered data
        List<LineOfBusinessModel> filteredData = new ArrayList<>();

        // Iterate through the original data and add matching items to the filtered list
        for (LineOfBusinessModel model : lineOfBusinessList) {
            if (model != null) {
                String itemName = model.mdTitle;
                if (itemName.toLowerCase().contains(query.toLowerCase().trim())) {
                    filteredData.add(model);
                }
            }
        }
        lineOfBusinessAdapter.clear();
        lineOfBusinessAdapter.addAll(filteredData);
    }

    void handleException(Exception e) {
        stopLoading();
        services.somethingWentWrong();
        services.handleException(e);
    }
}