package tech.c1ph3rj.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Services {
    Context context;
    Activity activity;
    RefreshListener refreshListener;
    public static int base_version;
    public static String app_version;
    private DatabaseHelper mydb;
    private long mLastClickTime = 0;
    public static String response = "{\n" +
            "  \"rcode\": 200,\n" +
            "  \"rObj\": {\n" +
            "    \"productIDs\": [\n" +
            "      520\n" +
            "    ],\n" +
            "    \"getAllAPIFormaly\": [\n" +
            "      {\n" +
            "        \"fieldGroup\": [\n" +
            "          {\n" +
            "            \"key\": \"Gender\",\n" +
            "            \"type\": \"select\",\n" +
            "            \"templateOptions\": {\n" +
            "              \"label\": \"Gender\",\n" +
            "              \"rows\": 1,\n" +
            "              \"required\": true,\n" +
            "              \"disabled\": null,\n" +
            "              \"pattern\": null,\n" +
            "              \"options\": [\n" +
            "                {\n" +
            "                  \"label\": \"Female\",\n" +
            "                  \"value\": \"cf632196-f295-412e-80eb-9e94fd351443\"\n" +
            "                },\n" +
            "                {\n" +
            "                  \"label\": \"Male\",\n" +
            "                  \"value\": \"cf632196-f295-412e-80eb-9e94fd351442\"\n" +
            "                }\n" +
            "              ],\n" +
            "              \"isOnloadAPICall\": false,\n" +
            "              \"placeholder\": \"Select\",\n" +
            "              \"valueProp\": \"value\",\n" +
            "              \"labelProp\": \"label\",\n" +
            "              \"type\": \"radio\",\n" +
            "              \"multiple\": null,\n" +
            "              \"selectAllOption\": null,\n" +
            "              \"cascadingParentControl\": null,\n" +
            "              \"apiUrl\": null,\n" +
            "              \"rObjData\": null,\n" +
            "              \"inputParameter\": null\n" +
            "            },\n" +
            "            \"validation\": {\n" +
            "              \"messages\": {\n" +
            "                \"required\": \"Gender is required\",\n" +
            "                \"pattern\": null\n" +
            "              }\n" +
            "            },\n" +
            "            \"hideExpression\": null,\n" +
            "            \"orderData\": 1\n" +
            "          },\n" +
            "          {\n" +
            "            \"key\": \"IsSmoker\",\n" +
            "            \"type\": \"select\",\n" +
            "            \"templateOptions\": {\n" +
            "              \"label\": \"Smoker\",\n" +
            "              \"rows\": 1,\n" +
            "              \"required\": true,\n" +
            "              \"disabled\": null,\n" +
            "              \"pattern\": null,\n" +
            "              \"options\": [\n" +
            "                {\n" +
            "                  \"label\": \"Yes\",\n" +
            "                  \"value\": \"70c1d4e7-b21e-491e-bcf0-3c364ec16b4d\"\n" +
            "                },\n" +
            "                {\n" +
            "                  \"label\": \"No\",\n" +
            "                  \"value\": \"3a1233ac-1c5b-4b35-9ce6-202fdc9434eb\"\n" +
            "                }\n" +
            "              ],\n" +
            "              \"isOnloadAPICall\": false,\n" +
            "              \"placeholder\": \"Select\",\n" +
            "              \"valueProp\": \"value\",\n" +
            "              \"labelProp\": \"label\",\n" +
            "              \"type\": \"select\",\n" +
            "              \"multiple\": null,\n" +
            "              \"selectAllOption\": null,\n" +
            "              \"cascadingParentControl\": null,\n" +
            "              \"apiUrl\": null,\n" +
            "              \"rObjData\": null,\n" +
            "              \"inputParameter\": null\n" +
            "            },\n" +
            "            \"validation\": {\n" +
            "              \"messages\": {\n" +
            "                \"required\": \"Smoker is required\",\n" +
            "                \"pattern\": null\n" +
            "              }\n" +
            "            },\n" +
            "            \"hideExpression\": null,\n" +
            "            \"orderData\": 2\n" +
            "          },\n" +
            "          {\n" +
            "            \"key\": \"PaymentPeriodID\",\n" +
            "            \"type\": \"select\",\n" +
            "            \"templateOptions\": {\n" +
            "              \"label\": \"Payment Frequency\",\n" +
            "              \"rows\": 1,\n" +
            "              \"required\": true,\n" +
            "              \"disabled\": null,\n" +
            "              \"pattern\": null,\n" +
            "              \"options\": [\n" +
            "                {\n" +
            "                  \"label\": \"Monthly\",\n" +
            "                  \"value\": \"80984271-f1a0-4fee-ae3a-5ac72863c7d1\"\n" +
            "                },\n" +
            "                {\n" +
            "                  \"label\": \"Quarterly\",\n" +
            "                  \"value\": \"45d018d0-a40f-4dfc-a654-983575d1a5b0\"\n" +
            "                },\n" +
            "                {\n" +
            "                  \"label\": \"Semi-Annual\",\n" +
            "                  \"value\": \"2d42056b-04f7-45e5-aa0f-d3f430d27d10\"\n" +
            "                },\n" +
            "                {\n" +
            "                  \"label\": \"Annual\",\n" +
            "                  \"value\": \"88aed3b5-0ed6-4ce1-81f6-e50d208ac450\"\n" +
            "                }\n" +
            "              ],\n" +
            "              \"isOnloadAPICall\": false,\n" +
            "              \"placeholder\": \"Select\",\n" +
            "              \"valueProp\": \"value\",\n" +
            "              \"labelProp\": \"label\",\n" +
            "              \"type\": \"select\",\n" +
            "              \"multiple\": true,\n" +
            "              \"selectAllOption\": null,\n" +
            "              \"cascadingParentControl\": null,\n" +
            "              \"apiUrl\": null,\n" +
            "              \"rObjData\": null,\n" +
            "              \"inputParameter\": null\n" +
            "            },\n" +
            "            \"validation\": {\n" +
            "              \"messages\": {\n" +
            "                \"required\": \"Payment Frequency is required\",\n" +
            "                \"pattern\": null\n" +
            "              }\n" +
            "            },\n" +
            "            \"hideExpression\": null,\n" +
            "            \"orderData\": 3\n" +
            "          },\n" +
            "          {\n" +
            "            \"key\": \"Term\",\n" +
            "            \"type\": \"input\",\n" +
            "            \"templateOptions\": {\n" +
            "              \"label\": \"Term\",\n" +
            "              \"rows\": 1,\n" +
            "              \"required\": true,\n" +
            "              \"disabled\": null,\n" +
            "              \"pattern\": null,\n" +
            "              \"options\": [],\n" +
            "              \"isOnloadAPICall\": null,\n" +
            "              \"placeholder\": null,\n" +
            "              \"valueProp\": null,\n" +
            "              \"labelProp\": null,\n" +
            "              \"type\": \"text\",\n" +
            "              \"multiple\": null,\n" +
            "              \"selectAllOption\": null,\n" +
            "              \"cascadingParentControl\": null,\n" +
            "              \"apiUrl\": null,\n" +
            "              \"rObjData\": null,\n" +
            "              \"inputParameter\": null\n" +
            "            },\n" +
            "            \"validation\": {\n" +
            "              \"messages\": {\n" +
            "                \"required\": \"Term is required\",\n" +
            "                \"pattern\": null\n" +
            "              }\n" +
            "            },\n" +
            "            \"hideExpression\": null,\n" +
            "            \"orderData\": 4\n" +
            "          },\n" +
            "          {\n" +
            "            \"key\": \"DateOfBirth\",\n" +
            "            \"type\": \"input\",\n" +
            "            \"templateOptions\": {\n" +
            "              \"label\": \"Date Of Birth\",\n" +
            "              \"rows\": 1,\n" +
            "              \"required\": true,\n" +
            "              \"disabled\": null,\n" +
            "              \"pattern\": null,\n" +
            "              \"options\": [],\n" +
            "              \"isOnloadAPICall\": null,\n" +
            "              \"placeholder\": null,\n" +
            "              \"valueProp\": null,\n" +
            "              \"labelProp\": null,\n" +
            "              \"type\": \"date\",\n" +
            "              \"multiple\": null,\n" +
            "              \"selectAllOption\": null,\n" +
            "              \"cascadingParentControl\": null,\n" +
            "              \"apiUrl\": null,\n" +
            "              \"rObjData\": null,\n" +
            "              \"inputParameter\": null\n" +
            "            },\n" +
            "            \"validation\": {\n" +
            "              \"messages\": {\n" +
            "                \"required\": \"Date Of Birth is required\",\n" +
            "                \"pattern\": null\n" +
            "              }\n" +
            "            },\n" +
            "            \"hideExpression\": null,\n" +
            "            \"orderData\": 5\n" +
            "          },\n" +
            "          {\n" +
            "            \"key\": \"SumInsured\",\n" +
            "            \"type\": \"input\",\n" +
            "            \"templateOptions\": {\n" +
            "              \"label\": \"SumInsured\",\n" +
            "              \"rows\": 1,\n" +
            "              \"required\": true,\n" +
            "              \"disabled\": null,\n" +
            "              \"pattern\": null,\n" +
            "              \"options\": [],\n" +
            "              \"isOnloadAPICall\": null,\n" +
            "              \"placeholder\": null,\n" +
            "              \"valueProp\": null,\n" +
            "              \"labelProp\": null,\n" +
            "              \"type\": \"text\",\n" +
            "              \"multiple\": null,\n" +
            "              \"selectAllOption\": null,\n" +
            "              \"cascadingParentControl\": null,\n" +
            "              \"apiUrl\": null,\n" +
            "              \"rObjData\": null,\n" +
            "              \"inputParameter\": null\n" +
            "            },\n" +
            "            \"validation\": {\n" +
            "              \"messages\": {\n" +
            "                \"required\": \"SumInsured is required\",\n" +
            "                \"pattern\": null\n" +
            "              }\n" +
            "            },\n" +
            "            \"hideExpression\": null,\n" +
            "            \"orderData\": 6\n" +
            "          },\n" +
            "          {\n" +
            "            \"key\": \"AddOns\",\n" +
            "            \"type\": \"select\",\n" +
            "            \"templateOptions\": {\n" +
            "              \"label\": \"AddOn\",\n" +
            "              \"rows\": null,\n" +
            "              \"required\": false,\n" +
            "              \"disabled\": false,\n" +
            "              \"pattern\": null,\n" +
            "              \"options\": [],\n" +
            "              \"isOnloadAPICall\": null,\n" +
            "              \"placeholder\": \"Select\",\n" +
            "              \"valueProp\": \"value\",\n" +
            "              \"labelProp\": \"label\",\n" +
            "              \"type\": null,\n" +
            "              \"multiple\": true,\n" +
            "              \"selectAllOption\": \"Select All\",\n" +
            "              \"cascadingParentControl\": null,\n" +
            "              \"apiUrl\": null,\n" +
            "              \"rObjData\": null,\n" +
            "              \"inputParameter\": null\n" +
            "            },\n" +
            "            \"validation\": {\n" +
            "              \"messages\": null\n" +
            "            },\n" +
            "            \"hideExpression\": null,\n" +
            "            \"orderData\": 7\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    ],\n" +
            "    \"getQuotationSearch\": {\n" +
            "      \"quotationSearchID\": \"1b2962d4-6567-4490-bcb3-2762e56c7b07\",\n" +
            "      \"quotationID\": \"AA-AA10001\",\n" +
            "      \"paymentTypeID\": 0,\n" +
            "      \"ridersJson\": null,\n" +
            "      \"coverTypeJson\": null,\n" +
            "      \"orgGroupJson\": null,\n" +
            "      \"inputJson\": null,\n" +
            "      \"productJson\": \"[520]\",\n" +
            "      \"premiumCategoryJson\": null,\n" +
            "      \"requestTotalProduct\": 0,\n" +
            "      \"createdOn\": \"2023-12-22T05:28:45.803\",\n" +
            "      \"createdBy\": \"00000000-0000-0000-0000-000000000000\",\n" +
            "      \"modifyOn\": \"2023-12-22T05:28:45.803\",\n" +
            "      \"modifyBy\": \"00000000-0000-0000-0000-000000000000\",\n" +
            "      \"isActive\": true\n" +
            "    }\n" +
            "  },\n" +
            "  \"rmsg\": [\n" +
            "    {\n" +
            "      \"errorText\": \"Success\",\n" +
            "      \"errorCode\": \"Success\",\n" +
            "      \"fieldName\": null,\n" +
            "      \"fieldValue\": null\n" +
            "    }\n" +
            "  ],\n" +
            "  \"reqID\": \"bcfd7d13-b497-4d6f-8a69-e73620534e20\",\n" +
            "  \"objectDB  ID\": null,\n" +
            "  \"transactionRef\": null,\n" +
            "  \"outcome\": true,\n" +
            "  \"outcomeMsgCode\": \"Success\",\n" +
            "  \"reDirectURL\": null\n" +
            "}";
    public static String token = "eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkEyNTZDQkMtSFM1MTIiLCJraWQiOiI4YmI1NDE4YWYzYjQ0YjdiOTI2OGM3YjRhYTg1NTYzMiIsInR5cCI6IkpXVCIsImN0eSI6IkpXVCJ9.QIWpiJ5rdC5r7lakcFEDGjte-CgRdoSitc7kawO109YT5FMXBV7xWnm7UroTNbPcWmOQTkk_HV5cvyRExOBhKMp1fR9fJr7pt71XAptJuEW3YNPPGtXIsnC5qXAsgSPBNCMd9Xh2HoBUt1crx5a6IhWBT5M24r1WulY65dmQh8--1cCW2aPZYG-uQeelWEV799jhvOl3eyYMXrPC1w6qXiJpKOAE-el7nFf4ohDGAXOGeG49FPwgqjwzl5S2eh1QeFYkFp3sk8eFJ4FI3980629z2335TsKeQZvwfA6D6gxcBbR8V9hMSsLQgN7FqLehZdEpBWKghfJYSzQi65FPJA.D9zCaiDJng9xlye2BwhqzA.YC0lqYKVy9PQCY0_-c-7GTtg7wP1qttiYOtKD5Ni7TKvJEFrKpaQckm-fAaSmP9k2g2PnoZcGindL6tp6KWuVeCULQxX74o9_9jzLoWSdmmg74Pdq3KANogHNw-70FG-VXm_NTdpGpGLjB1UlsATSWyFfz_SxT1v8w09S3Em--StPVZLv6qBTnxYRuYSSwbLI_kVptm9D73_cL90p-osYI4ChVwFMpgXN04KJ1cfs2NZlYhJb_of1-hvW-KJpcwhGH4lus16rcT0pCSPl4__9lRL34-VdR2GN0K0p0wS7Drc8AK6hnrsC-Egvs_nsnMBbj3107B8NCD8Yzbw6LCPEsA6dXsb9iXp64XlhfSPApTg-s9kCxfmnPlG8ZXzuv6JJ-U7nI2t9bH1JMhvPx1zZxdZdkKsk7i4pLsT6dZGX2MJwgjVINX4k1BkQQGrRQcb27esZ6Gj62wq68LEqDVHWNLhMyN0qnN31e4ltXqRge5jeHu5JrIb_oybTU8OgZf_8ZgeDyI5xdZSI1zJW7XOUIPQWUPKFE9iHTHjPFwEUrD2piq1l7OQsAuhP9TfB6sIIoYNoFnw81DXWzJX7tJJuyFChV7uV5n8mlrFV8QcU9mBf6cJNy6VlHtac3KDte8k-1DiD1ItFxa8L3qRuKunuhyL_FKWB-XKn7b4WfUn0z4SIsQwXOzFEBvvCiIV9benrX3JjrKRKT3R9HG8OxaV89au6Qq8TWA_h0B2bvOqBljgDR700lo72rMhnSHJvMLpWG5qWXfVw-T9VXQW4L8Y8ZriTiSAGA56ngI8BTzzK1qnrPH41wFyhwj6mMqsIVakibkn9sgsY7PC_SOMB5QHq85JY43hG6vGZiWxzeznngqjwxARu4Ad3D0jJWKlkI4ms6Npan1gTSc3SFBsT8IVLo1Bet3Z1JtplXzFFtOeawAS6n3WjEMoqOuAkI_okjfAPs_s3QfpfSJuDWnefMyz7ciwULR8HlI9i9oercHZs2O3vtaCL0ECWwbTJMqgfuGc0xQ7nfw0qKbhitLqwpS961_uDFXFV5dt5JQZ5_EqFCGnRtvvZaQw7bdA-jyZl3OS6p2-CzkzZ1rdzkis9FmD5lgfvMz8nAYx1AO0j5zkQV9dODZ6iNiBV8Pe8hiRf9bkCacIq9aRet-gEBEbbhWya74PwANfI_KgCywXXRAoASnpbXp2tPvfdbVHuXhm2PTQs6ZPS7IKX1DGKO-T6Ff-FnyGhecH3Hx9ypbHfIway2e_j4NE7oXixbIHMdYtlvRsqPuuSBC4P8GB9pNBaCNrsSl4qSJcpk0x5D_-TWOh746VGTFIvh-wNk5yMw49Fu0KVxy493UUSEYeYyGftrBm8dVCBFBps9hoUNHYpOM_pAvKUu-__BlSWr8hQ-CrIKcEJEK2leHv-ooVKoNuCF2t5w.cNvYpCHJCncvaQCI4zvCjRXnlw0IIedlvk-EjM7W8bE";
    public String baseUrl;
    public Dialog loader;
    public static final MediaType JSON = MediaType.get("application/json");
    public static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build();

    public Services(Context context, RefreshListener refreshListener) {
        this.context = context;
        activity = (Activity)context;
        this.refreshListener = refreshListener;
        baseUrl = activity.getString(R.string.base_url);
        loader = initLoaderDialog();
        app_version = context.getString(R.string.app_version);
        String[] app_version_split = app_version.split("\\.");
        base_version = Integer.parseInt(app_version_split[0]);
    }

    public static void unAuthorize(Activity activity)
    {
        try {
            activity.runOnUiThread(() -> {
                AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
                dialog.setMessage(activity.getString(R.string.session_expired));
                dialog.setPositiveButton("Ok", (dialog1, which) -> {
                    DatabaseHelper mydb = new DatabaseHelper(activity);
                    mydb.deleteTokenData();
                    Intent login = new Intent(activity, MainActivity.class);
                    activity.startActivity(login);
                    mydb.close();
                });
                AlertDialog alert = dialog.create();
                alert.setCancelable(false);
                alert.show();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showDialog() {
        activity.runOnUiThread(()->{
            if (loader != null && !loader.isShowing()) {
                loader.show();
            }
        });
    }

    public void checkNetworkVisibility() {
        LinearLayout noInternetLayout = activity.findViewById(R.id.noInternetLayout);
        if (isNetworkConnected()) {
            if (noInternetLayout != null) {
                noInternetLayout.setVisibility(View.GONE);
            }
        } else {
            if (noInternetLayout != null) {
                noInternetLayout.setVisibility(View.VISIBLE);
                activity.findViewById(R.id.refreshButton).setOnClickListener(onClickRefresh -> {
                    if (preventDoubleClick()) {
                        noInternetLayout.setVisibility(View.GONE);
                        if(refreshListener != null) {
                            refreshListener.onCalledRefresh();
                        }
                    }
                });
            }
        }
    }


    // You can add more methods as needed, such as dismissing the dialog
    public void dismissDialog() {
        activity.runOnUiThread(()-> {
            if (loader != null && loader.isShowing()) {
                loader.dismiss();
            }
        });
    }

    public Dialog initLoaderDialog() {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.loading_view);
        dialog.setCancelable(false);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        return dialog;
    }

    public static String post(String url, String json) throws IOException {
        RequestBody requestBody = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + token)
                .post(requestBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return (response.body() != null) ? response.body().string() : "";
        }
    }

    public static String post(String url, String json, String orgAppId) throws IOException {
        RequestBody requestBody = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", token)
                .header("mobileParameters", "")
                .header("orgAppId", orgAppId)
                .post(requestBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return (response.body() != null) ? response.body().string() : "";
        }
    }

    public void handleException(Exception e) {
        e.printStackTrace();
    }

    public void somethingWentWrong(){
        activity.runOnUiThread(()-> Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show());
    }

    public boolean preventDoubleClick() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 2000){
            return false;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        return true;
    }

    public void showErrorMessageFromAPI(JSONArray rMsg) {
        activity.runOnUiThread(()->{
            try {
                JSONObject index = rMsg.getJSONObject(0);
                activity.runOnUiThread(() -> {
                    String errorText;
                    try {
                        errorText = index.getString("errorText");
                        AlertDialog.Builder alert = new AlertDialog.Builder(context);
                        alert.setCancelable(false);
                        alert.setMessage(errorText);
                        alert.setNegativeButton("Ok", (dialog, which) -> dialog.dismiss());
                        alert.show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    public interface RefreshListener {
        void onCalledRefresh();
    }

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public boolean checkGpsStatus() {
        try {
            LocationManager locationManager = null;
            // Boolean value to store the GPS state and Network state.
            boolean gps_enabled = false;
            boolean network_enabled = false;
            // Getting the location manager.
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            // Checking if the location and network is available or not and storing the boolean accordingly.
            try {
                gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return gps_enabled;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void redirectToGpsSettings() {
        activity.runOnUiThread(() -> {
            try {
                AlertDialog alertDialog = new AlertDialog.Builder(context)
                        .setTitle("Gps Required!")
                        .setMessage("Gps is not enabled, Please enable it to continue!")
                        .setPositiveButton("Ok", (dialogInterface, i) -> {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            context.startActivity(intent);
                        })
                        .show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static boolean checkNull(String value){
        return !(value == null || value.trim().isEmpty() || value.trim().toLowerCase(Locale.getDefault()).equals("null"));
    }
}
