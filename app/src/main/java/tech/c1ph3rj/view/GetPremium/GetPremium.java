package tech.c1ph3rj.view.GetPremium;

import static tech.c1ph3rj.view.Services.token;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
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
import tech.c1ph3rj.view.GetPremium.adapter.GetPremiumAdapter;
import tech.c1ph3rj.view.GetPremium.model.GetPremiumModel;
import tech.c1ph3rj.view.R;
import tech.c1ph3rj.view.Services;

public class GetPremium extends AppCompatActivity {
    RecyclerView getPremiumRV;
    GetPremiumAdapter adapter;
    List<GetPremiumModel> premiumList;
    Context context;
    Activity activity;
    Services services;
    LinearLayout nextButton;
    String getPremiumBody;
    GetPremiumModel selectedPremium;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_premium);
        context = this;
        activity = this;
        try {
            services = new Services(this, null);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeAsUpIndicator(R.drawable.back_ic);
                getSupportActionBar().setDisplayShowTitleEnabled(true);
                getSupportActionBar().setTitle("Quotation");
            }
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void init() {
        try {
            getPremiumBody = getIntent().getStringExtra("getPremiumBody");
            getPremiumRV = findViewById(R.id.getPremiumRV);
            nextButton = findViewById(R.id.nextButton);

            nextButton.setOnClickListener(onClickNext -> {
                if(selectedPremium == null) {
                    Toast.makeText(context, "Please select a Quotation to continue!", Toast.LENGTH_SHORT).show();
                    return;
                }

                //TODO Handle redirect to the next page.
            });
            checkNextButtonVisibility();
            basicFunctions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void checkNextButtonVisibility() {
        if(selectedPremium == null) {
            nextButton.setVisibility(View.GONE);
        } else {
            nextButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        getOnBackPressedDispatcher().onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    void basicFunctions() {
        try {
            getPremiumAPI();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void getPremiumAPI() {
        try {
            if (services.isNetworkConnected()) {
                if (services.checkGpsStatus()) {

                    //creating the thread to run operations in background thread.
                    Thread t = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        services.showDialog();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            String postUrl = getString(R.string.base_url) + "api/digital/core/PremiumLogic/GetPremium";
                            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//                            JsonObject details = new JsonObject();
//                            try {
//                                details.addProperty("quotationSearchID", "28537ee6-42bb-434f-8728-dea2c637b20d");
//                                details.addProperty("CertificateTypeID", "3168360f-81a7-4385-99b8-068d611c27a5");
//                                details.addProperty("IDV", "750000");
//                                details.addProperty("PaymentPeriodID", "88aed3b5-0ed6-4ce1-81f6-e50d208ac454");
//                                details.addProperty("TypeOfVehicle", "3168360f-81a7-4385-99b8-068d611c27c4");
//                                details.addProperty("YearOfManufacture", "2022");
//                                JsonArray addOnsArray = new JsonArray();
//                                addOnsArray.add("c32280e0-bdcd-4b60-9fc6-c08a46d23931");
//                                details.add("AddOns", addOnsArray);
//                                JsonArray productIDsArray = new JsonArray();
//                                productIDsArray.add(527);
//                                productIDsArray.add(521);
//                                details.add("productIDs", productIDsArray);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
                            RequestBody body = RequestBody.create(getPremiumBody, JSON);
                            Request request = new Request.Builder()
                                    .url(postUrl)
                                    .header("Authorization", "Bearer " + token)
                                    .header("orgAppID", "6130")
                                    .post(body)
                                    .build();
                            OkHttpClient client = generateOkHttpClient(context).build();
                            Response staticResponse = null;
                            try {
                                staticResponse = client.newCall(request).execute();
                                String staticRes = staticResponse.body().string();

                                if (staticResponse.code() == 200) {
                                    final JSONObject staticJsonObj = new JSONObject(staticRes);

                                    if (staticJsonObj.getInt("rcode") == 200) {
                                        runOnUiThread(() -> {
                                            try {
                                                services.dismissDialog();
                                                premiumList = new ArrayList<>();
                                                if (staticJsonObj.has("rObj")) {
                                                    JSONObject rObj = staticJsonObj.getJSONObject("rObj");

                                                    if (rObj.has("getAllQuotationSearchProduct")) {
                                                        JSONArray productsArray = rObj.getJSONArray("getAllQuotationSearchProduct");

                                                        // Iterate through each product
                                                        for (int i = 0; i < productsArray.length(); i++) {
                                                            JSONObject product = productsArray.getJSONObject(i);

                                                            // Extracting required keys
                                                            String productName = product.getString("productName");
                                                            String sBasicPremium = product.getString("sBasicPremium");
                                                            String sAddOnsPremium = product.getString("sAddOnsPremium");
                                                            String sDiscountPremium = product.getString("sDiscountPremium");
                                                            String sBeforeTax = product.getString("sBeforeTax");
                                                            String sTaxPremium = product.getString("sTaxPremium");
                                                            String sTotalPremium = product.getString("sTotalPremium");

                                                            premiumList.add(new GetPremiumModel(productName, sBasicPremium, sAddOnsPremium, sDiscountPremium, sBeforeTax, sTaxPremium, sTotalPremium));
                                                            // Use the extracted values as needed
                                                            System.out.println("Product Name: " + productName);
                                                            System.out.println("Basic Premium: " + sBasicPremium);
                                                            System.out.println("Add-Ons Premium: " + sAddOnsPremium);
                                                            System.out.println("Discount Premium: " + sDiscountPremium);
                                                            System.out.println("Before Tax: " + sBeforeTax);
                                                            System.out.println("Tax Premium: " + sTaxPremium);
                                                            System.out.println("Total Premium: " + sTotalPremium);
                                                            System.out.println("---------------------------------------------");
                                                        }
                                                        adapter = new GetPremiumAdapter(premiumList, context, new GetPremiumAdapter.onItemSelectListener() {
                                                            @Override
                                                            public void itemSelected(GetPremiumModel premiumModel, int pos) {
                                                                if(selectedPremium != null) {
                                                                    int previousPos = premiumList.indexOf(selectedPremium);
                                                                    premiumList.get(previousPos).isSelected = false;
                                                                    adapter.notifyItemChanged(previousPos);
                                                                }
                                                                selectedPremium = premiumModel;
                                                                adapter.notifyItemChanged(pos);
                                                                checkNextButtonVisibility();
                                                            }

                                                            @Override
                                                            public void itemUnSelected(int pos) {
                                                                if(selectedPremium != null) {
                                                                    if(premiumList.get(pos).equals(selectedPremium)) {
                                                                        selectedPremium = null;
                                                                    }
                                                                }
                                                                adapter.notifyItemChanged(pos);
                                                                checkNextButtonVisibility();
                                                            }
                                                        });

                                                        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
                                                        getPremiumRV.setLayoutManager(layoutManager);
                                                        getPremiumRV.setAdapter(adapter);

                                                    }
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        });
                                    } else if (staticJsonObj.getInt("rcode") == 502) {
                                        runOnUiThread(() -> {
                                            try {
                                                services.dismissDialog();
                                                JSONObject errorMessage = staticJsonObj.getJSONArray("rmsg").getJSONObject(0);
                                                String errorText;
                                                errorText = getString(R.string.cmn_something_wrong_msg);
                                                alertTheUser("", errorText)
                                                        .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss()).setCancelable(false)
                                                        .show();
                                            } catch (Exception e) {
                                                e.printStackTrace();


                                            }
                                        });
                                    } else {
                                        runOnUiThread(new Runnable() {
                                            public void run() {
                                                try {
                                                    services.dismissDialog();
                                                    alertTheUser("", getString(R.string.cmn_something_wrong_msg))
                                                            .setPositiveButton("Ok", (dialogInterface, i) -> dialogInterface.dismiss()).show();

                                                } catch (Exception e) {
                                                    e.printStackTrace();


                                                }
                                            }
                                        });

                                    }
                                } else {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            try {
                                                services.dismissDialog();
                                                alertTheUser("", getString(R.string.cmn_something_wrong_msg))
                                                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                dialogInterface.dismiss();
                                                            }
                                                        }).show();
                                            } catch (Exception e) {
                                                e.printStackTrace();


                                            }
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();


                            }
                        }
                    });
                    t.start();


                } else {
                    android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(context);
                    dialog.setMessage(getString(R.string.gps_not_enabled));
                    dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //this will navigate user to the device location settings screen
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    });
                    android.app.AlertDialog alert = dialog.create();
                    alert.show();
                }
            } else {
                Toast.makeText(this, getString(R.string.nonetwork), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();


        }

    }

    AlertDialog.Builder alertTheUser(String title, String message) {
        // Returning the alert dialog.
        return new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message);
    }// End of alertTheUser().


    public static OkHttpClient.Builder generateOkHttpClient(Context context) {
        try {
            // Create a simple builder for our http client, this is only for example purposes
            OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
            httpClientBuilder.readTimeout(180, TimeUnit.SECONDS);
            httpClientBuilder.writeTimeout(180, TimeUnit.SECONDS);
            httpClientBuilder.connectTimeout(180, TimeUnit.SECONDS);
            //Finally set the sslSocketFactory to our builder and build it
            return httpClientBuilder;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}