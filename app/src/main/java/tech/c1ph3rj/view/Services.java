package tech.c1ph3rj.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.view.Window;
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
    public static String response = "{\n" +
            "  \"rcode\": 200,\n" +
            "  \"rObj\": {\n" +
            "    \"productIDs\": [\n" +
            "      327,\n" +
            "      123\n" +
            "    ],\n" +
            "    \"getAllAPIFormaly\": [\n" +
            "      {\n" +
            "        \"fieldGroup\": [\n" +
            "          {\n" +
            "            \"key\": \"VehicleTypeID\",\n" +
            "            \"type\": \"select\",\n" +
            "            \"templateOptions\": {\n" +
            "              \"label\": \"Vehicle Type\",\n" +
            "              \"rows\": 1,\n" +
            "              \"required\": true,\n" +
            "              \"disabled\": null,\n" +
            "              \"pattern\": null,\n" +
            "              \"options\": [],\n" +
            "              \"isOnloadAPICall\": false,\n" +
            "              \"placeholder\": null,\n" +
            "              \"valueProp\": \"masterDataID\",\n" +
            "              \"labelProp\": \"mdTitle\",\n" +
            "              \"type\": \"select\",\n" +
            "              \"multiple\": null,\n" +
            "              \"selectAllOption\": null,\n" +
            "              \"cascadingParentControl\": \"UsageTypeID\",\n" +
            "              \"apiUrl\": \"api/digital/core/MasterData/FetchMasterData\",\n" +
            "              \"rObjData\": \"rObj.fetchMasterData\",\n" +
            "              \"inputParameter\": \"parentMasterDataID\"\n" +
            "            },\n" +
            "            \"validation\": {\n" +
            "              \"messages\": {\n" +
            "                \"required\": \"Vehicle Type is required\",\n" +
            "                \"pattern\": null\n" +
            "              }\n" +
            "            },\n" +
            "            \"hideExpression\": null,\n" +
            "            \"orderData\": 1\n" +
            "          },\n" +
            "          {\n" +
            "            \"key\": \"UsageTypeID\",\n" +
            "            \"type\": \"select\",\n" +
            "            \"templateOptions\": {\n" +
            "              \"label\": \"Usage Type\",\n" +
            "              \"rows\": 1,\n" +
            "              \"required\": true,\n" +
            "              \"disabled\": null,\n" +
            "              \"pattern\": null,\n" +
            "              \"options\": [],\n" +
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
            "                \"required\": \"Usage Type is required\",\n" +
            "                \"pattern\": null\n" +
            "              }\n" +
            "            },\n" +
            "            \"hideExpression\": null,\n" +
            "            \"orderData\": 2\n" +
            "          },\n" +
            "          {\n" +
            "            \"key\": \"UsageSubTypeID\",\n" +
            "            \"type\": \"select\",\n" +
            "            \"templateOptions\": {\n" +
            "              \"label\": \"Usage Sub Type\",\n" +
            "              \"rows\": 1,\n" +
            "              \"required\": true,\n" +
            "              \"disabled\": null,\n" +
            "              \"pattern\": null,\n" +
            "              \"options\": [],\n" +
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
            "                \"required\": \"Usage Sub Type is required\",\n" +
            "                \"pattern\": null\n" +
            "              }\n" +
            "            },\n" +
            "            \"hideExpression\": null,\n" +
            "            \"orderData\": 3\n" +
            "          },\n" +
            "          {\n" +
            "            \"key\": \"makeID\",\n" +
            "            \"type\": \"select\",\n" +
            "            \"templateOptions\": {\n" +
            "              \"label\": \"Vehicle Make\",\n" +
            "              \"rows\": 1,\n" +
            "              \"required\": true,\n" +
            "              \"disabled\": null,\n" +
            "              \"pattern\": null,\n" +
            "              \"options\": [\n" +
            "                {\n" +
            "                  \"label\": \"Make\",\n" +
            "                  \"value\": \"a5d5e63f-bc03-4ab7-a7a5-78083e1f6b24\"\n" +
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
            "                \"required\": \"Vehicle Make is required\",\n" +
            "                \"pattern\": null\n" +
            "              }\n" +
            "            },\n" +
            "            \"hideExpression\": null,\n" +
            "            \"orderData\": 4\n" +
            "          },\n" +
            "          {\n" +
            "            \"key\": \"IDV\",\n" +
            "            \"type\": \"input\",\n" +
            "            \"templateOptions\": {\n" +
            "              \"label\": \"IDV\",\n" +
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
            "                \"required\": \"IDV is required\",\n" +
            "                \"pattern\": null\n" +
            "              }\n" +
            "            },\n" +
            "            \"hideExpression\": null,\n" +
            "            \"orderData\": 5\n" +
            "          },\n" +
            "          {\n" +
            "            \"key\": \"modelID\",\n" +
            "            \"type\": \"select\",\n" +
            "            \"templateOptions\": {\n" +
            "              \"label\": \"Vehicle Model\",\n" +
            "              \"rows\": 1,\n" +
            "              \"required\": true,\n" +
            "              \"disabled\": null,\n" +
            "              \"pattern\": null,\n" +
            "              \"options\": [\n" +
            "                {\n" +
            "                  \"label\": \"Make\",\n" +
            "                  \"value\": \"a5d5e63f-bc03-4ab7-a7a5-78083e1f6b24\"\n" +
            "                }\n" +
            "              ],\n" +
            "              \"isOnloadAPICall\": false,\n" +
            "              \"placeholder\": \"Select\",\n" +
            "              \"valueProp\": null,\n" +
            "              \"labelProp\": null,\n" +
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
            "                \"required\": \"Vehicle Model is required\",\n" +
            "                \"pattern\": null\n" +
            "              }\n" +
            "            },\n" +
            "            \"hideExpression\": null,\n" +
            "            \"orderData\": 6\n" +
            "          },\n" +
            "          {\n" +
            "            \"key\": \"cubicCapacity\",\n" +
            "            \"type\": \"input\",\n" +
            "            \"templateOptions\": {\n" +
            "              \"label\": \"Vehicle Cubic Capacity\",\n" +
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
            "                \"required\": \"Vehicle Cubic Capacity is required\",\n" +
            "                \"pattern\": null\n" +
            "              }\n" +
            "            },\n" +
            "            \"hideExpression\": null,\n" +
            "            \"orderData\": 7\n" +
            "          },\n" +
            "          {\n" +
            "            \"key\": \"YearOfManufacture\",\n" +
            "            \"type\": \"input\",\n" +
            "            \"templateOptions\": {\n" +
            "              \"label\": \"Year of Manufacture\",\n" +
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
            "                \"required\": \"Vehicle Age is required\",\n" +
            "                \"pattern\": null\n" +
            "              }\n" +
            "            },\n" +
            "            \"hideExpression\": null,\n" +
            "            \"orderData\": 8\n" +
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
            "            \"orderData\": 9\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  \"rmsg\": [\n" +
            "    {\n" +
            "      \"errorText\": \"Success\",\n" +
            "      \"errorCode\": \"Success\",\n" +
            "      \"fieldName\": null,\n" +
            "      \"fieldValue\": null\n" +
            "    }\n" +
            "  ],\n" +
            "  \"reqID\": \"4a038bbe-ab42-4fe4-aec2-4c7fd84d087d\",\n" +
            "  \"objectDBID\": null,\n" +
            "  \"transactionRef\": null,\n" +
            "  \"outcome\": true,\n" +
            "  \"outcomeMsgCode\": \"Success\",\n" +
            "  \"reDirectURL\": null\n" +
            "}";
    public static String token = "eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkEyNTZDQkMtSFM1MTIiLCJraWQiOiI4YmI1NDE4YWYzYjQ0YjdiOTI2OGM3YjRhYTg1NTYzMiIsInR5cCI6IkpXVCIsImN0eSI6IkpXVCJ9.HZiyeSpf2DOM8T8-i3UK1KuFqC-D9zIOdR1hgcm5V93zxGY8ZlZvhtUKTkGIGrHZ185_DW6WPOiZOVS9iaikp2I0-9rofJncHZAQz2kjzy4MCPaJOLHgHepxPpiql_Tohr75zizA0mxupiRYmQ2MC9lwtX3Qo1N9c-EuA_vFkSm7eVerD68RPDMe24_x8TXQsVeWjwV1z3yl4iRwFY4WOlti5Iist3XIB8D0A_gGJ9-WyvX-LnrQqum_569C3X2c8qkIDPAH15ZDLwWIjZL7ecrET99gaTqg-FLUYMHZXygkez2fIA1E23atsh_6zHm82fv7nWzmDH2ZcHoxDHZ6Qg.aXJ9Jlco0I674s5_HcvXbA.PRCSvyQB9onD_mHHpXCGMiAVjriUl5gcLASnOEBR0Jj_uJIaNAkDn7ISrMjwjvZYRRd8Vz2huKiJbXuHp-UeXw18HeMXSP5WaTier3clTnODgHeG-GdJ5Xg-a76d9m2rqUtYxRdmtaebHOgeU7MPQLnknHL2SNYrHfbb92fAfRg95TFYDmvV8Qpff5pmRVG3WyH-72wIXa6FviNoSoYiImofBy4U-7r98REye2suhYUkrM4k40dkCRVdc4UXm0Ydvt75l6hLIlhYdkebgViwuKQZhKR6auZMwlZb7ZfiY5-wwu__lDjk9MKfzcRcoWbSFVusQlO8w5mMKo-90gc8IGbpRl3uOWA6PC6Rg_fcJS5qGBTbM9i-STiQnSf5SDli6_scpN_kTCZE8-FYxtexXpgvSBMYxidYiB1UV9P1wm_uIDts2bpjoEzeEPes9HedjtV6v7w0IoYcMb9w_GGriuVbrcibPc-UllHlujB0ljmBVnhkofPURVubXn68-kSikZX1g1k7fpRuHp5ZdWqlr7OmzwuS-MBDRzFCUtfA4DHAdDM8ORvJmqsLgxwTYV0lIdthB8oC9aMSduIAvU0h8Xviy_YH_9pzSxSmmYHlm91m3Xn8n7_vXGv8N_dxT5Tb7cjgPPbGDyuNpCbxM5PDXVGJAv1eafjB4bxs74ziglxHGUQX89HnlLFww_he-9YJn1Sj9pOJWylCSPhCyuvMBACXsskwCMstzSTiBumouWvod48TR9dOKZnsapO4gvX2BLdzb4y1QUD7yW6P_EXaGa85zadwocZShe2Linf8zp4-9vQlGBCKGEyiHRND2B5dFhB8B-UUbQqGUKNC4UJVNl7Z3lp3w6HJefX0WBg6d3jE-9LlfpdBRdIp-ODyxtL_c0ZiD0zMtsUJtPrCDRWfYGofELa3sMMtBGqszKBky717FImqo9s3Uyp1LpgJcj8OfutLj8SbvBUNq2D6Tzdx2My21PIvRHS58ASXIxCYsM-50Rn_aWwRnorxQ8Z5CttvAsl41LUcridXRBG2xy8emS4t1F5riI1QA5VGy_i8a2VsufX1zNgNVxJQ-0Dp5HSrdCaH7l1QJT8dMM1PSQXrv0TUYN9hKiwhtog_Qr6TPgGtDuf1HAMdBXo2VRhXGFrVszVXgPE7LUmRQMhxa0xFhOoHdR4rfUcl9eSjzZtmYBGqZj42beerrFjrey8QLsJcmcY--mcp6fYlN-4folobmCPjeKPQ_LjvpIApHQ9-n1g47HbSDiJSBNU9LLoU6VENRm7xo7J4nr8CNJp7835QaUTDnnYKSF2Lpow279LXNhmdWvRzCEAzX4hAyvoWic2KlVM6kWuAbJ-udgKYtJGUc08ugtQw_E6Oje-QcX2xvcZtIEILt7wKlwcddu9_ZaFFl0xROCQBSkPwy9bqggx2rg.GJsoKV-tTAod-tiUsYzRT2PZszL7jxRrtJn3ZLnIx90";
    public String baseUrl;
    public Dialog loader;
    public static final MediaType JSON = MediaType.get("application/json");
    public static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build();

    public Services(Context context) {
        this.context = context;
        activity = (Activity)context;
        baseUrl = activity.getString(R.string.base_url);
        loader = initLoaderDialog();
    }

    public void showDialog() {
        activity.runOnUiThread(()->{
            if (loader != null && !loader.isShowing()) {
                loader.show();
            }
        });
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
