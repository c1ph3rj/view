package tech.c1ph3rj.view.products;

import static tech.c1ph3rj.view.Services.checkNull;
import static tech.c1ph3rj.view.Services.token;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.button.MaterialButton;
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
import tech.c1ph3rj.view.user_information.UserInformationScreen;

public class ProductsScreen extends AppCompatActivity {
    Services services;
    EditText searchView;
    ImageView backBtn;
    ListView productsView;
    ShimmerFrameLayout loadingView;
    ProductsAdapter productsAdapter;
    List<ProductsModel> productsList;
    ArrayList<String> selectedProducts;
    LinearLayout nextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products_screen);

        try {
            ActionBar actionBar = getSupportActionBar();
            if(actionBar != null) {
                actionBar.setTitle("Products Screen");
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeButtonEnabled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        services = new Services(this);
        init();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        getOnBackPressedDispatcher().onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    void init() {
        try {
            String lineOfBusinessId = getIntent().getStringExtra("lineOfBusinessId");
            productsView = findViewById(R.id.productsView);
            searchView = findViewById(R.id.searchView);
            loadingView = findViewById(R.id.loadingView);
            nextBtn = findViewById(R.id.nextBtn);
            selectedProducts = new ArrayList<>();

            productsList = new ArrayList<>();

            nextBtn.setOnClickListener(onClickNext -> {
                Intent intent = new Intent(this, UserInformationScreen.class);
                String[] productIds = new String[selectedProducts.size()];
                for(int i = 0; i < selectedProducts.size(); i++) {
                    String eachProductId = selectedProducts.get(i);
                    productIds[i] = eachProductId;
                }
                intent.putExtra("productIds", productIds);
                startActivity(intent);
            });
            productsAdapter = new ProductsAdapter(this, new ArrayList<>(), new ProductsAdapter.onProductSelectionListener() {

                @Override
                public void itemChecked(ProductsModel model) {
//                    if(!selectedProducts.contains(model.productID)) {
                        selectedProducts.add(model.productID);
                        model.isChecked = true;
                        checkNextBtnVisibility();
//                    }
                }

                @Override
                public void itemRemoved(ProductsModel model) {
//                    if(selectedProducts.contains(model.productID)) {
                        selectedProducts.remove(model.productID);
                        model.isChecked = false;
                        checkNextBtnVisibility();
//                    }
                }

            });
            productsView.setAdapter(productsAdapter);

            productsView.setOnItemClickListener((adapterView, view, i, l) -> {

            });
            productsView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);

            loadingView.setVisibility(View.GONE);
            startLoading();

            getAllProductsAPI(lineOfBusinessId);

        } catch (Exception e) {
            services.handleException(e);
        }
    }

    private void checkNextBtnVisibility() {
        if(nextBtn != null) {
            nextBtn.setVisibility(selectedProducts.isEmpty() ? View.GONE : View.VISIBLE);
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

    private void getAllProductsAPI(String lineOfBusinessId) {
        try {
            if (services.isNetworkConnected()) {
                if (services.checkGpsStatus()) {
                    new Thread(() -> {
                        try {
                            String baseUrl = services.baseUrl + "api/digital/core/Product/GetAllProduct";
                            OkHttpClient client = new OkHttpClient.Builder()
                                    .connectTimeout(120, TimeUnit.SECONDS)
                                    .writeTimeout(120, TimeUnit.SECONDS)
                                    .readTimeout(120, TimeUnit.SECONDS)
                                    .build();
                            final MediaType JSON
                                    = MediaType.parse("application/json; charset=utf-8");
                            JsonObject details = new JsonObject();
                            details.addProperty("pageSize", "100");
                            details.addProperty("pageNumber", "1");
                            details.addProperty("lineOfBusinessID", lineOfBusinessId);

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
                                                JSONArray productsArray = staticResObj.getJSONObject("rObj").getJSONArray("getAllProduct");

                                                productsList.clear();
                                                for (int i = 0; i < productsArray.length(); i++) {
                                                    JSONObject productObject = productsArray.getJSONObject(i);

                                                    ProductsModel product = new ProductsModel();
                                                    product.productID = productObject.optString("productID", "");
                                                    product.productName = productObject.optString("productName", "");
                                                    product.productUniqueID = productObject.optString("productUniqueID", "");
                                                    product.productDescription = productObject.optString("productDescription", "");
                                                    product.lineOfBusinessID = productObject.optString("lineOfBusinessID", "");
                                                    product.lineOfBusinessText = productObject.optString("lineOfBusinessText", "");
                                                    product.categoryID = productObject.optString("categoryID", "");
                                                    product.categoryText = productObject.optString("categoryText", "");
                                                    product.currencyID = productObject.optString("currencyID", "");
                                                    product.currencyText = productObject.optString("currencyText", "");
                                                    product.enforcementStartDate = productObject.optString("enforcementStartDate", "");
                                                    product.enforcementEndDate = productObject.optString("enforcementEndDate", "");
                                                    product.defaultPolicyPeriodID = productObject.optString("defaultPolicyPeriodID", "");
                                                    product.defaultPolicyPeriodText = productObject.optString("defaultPolicyPeriodText", "");
                                                    product.usedTags = productObject.optString("usedTags", "");
                                                    product.addOnTags = productObject.optString("addOnTags", "");
                                                    product.productStatusID = productObject.optString("productStatusID", "");
                                                    product.productStatusText = productObject.optString("productStatusText", "");
                                                    product.isChecked = false;

                                                    productsList.add(product);
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
        List<ProductsModel> filteredData = new ArrayList<>();

        // Iterate through the original data and add matching items to the filtered list
        for (ProductsModel model : productsList) {
            if (model != null) {
                String itemName = model.productName;
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