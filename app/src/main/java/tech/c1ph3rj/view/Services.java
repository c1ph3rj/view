package tech.c1ph3rj.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Services {
    Context context;
    Activity activity;
    public String baseUrl;
    public static String token = "eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkEyNTZDQkMtSFM1MTIiLCJraWQiOiI4YmI1NDE4YWYzYjQ0YjdiOTI2OGM3YjRhYTg1NTYzMiIsInR5cCI6IkpXVCIsImN0eSI6IkpXVCJ9.O5pbBbpq4KTq-TwC2u3Rxkf3f7KnH46lZ4lbOsObNL459yxVcJw4Qc0vyehQwwsXSOlYlKr8SHVHbay9xRhl_D2j-dfKNicv0NuUGWoEF02iqg2u1EQW2cwxZm7WWku5mPLvyNz9Nj1poA7a-aOpNt-nDEGYsh_HlgODEW05-N6CPpVTYct0ax_XnNQgoxRg5YBKQDHg1qbelw-oGwxqTtfKnIFjaHndXu1fTehgMSAtLLojm5WZCmIUszQoxva_xgJexmPNb65NT9iZjaGn5vqInEKD-c-hZvPXmJ17H_M5SCHCry9pBlpl6EXRqnS8XrBjHmEy9pYyjdzevI4c9A.N-oprwC-vK02zRUjBmasyg.sgY7aQnW259b9xSeDNyMr1VRrfiHtQL8JNbMqzV78vu6uXF27Mgw5MGqR_WAXlYbBvJaIKZcYDD1J-1chXcf5RCikMowfR7Tn1GkOxkxqB7TsOdhdTp9dz4J6zmOCvkjbMhLgm7f-Ftx7TCIHRcV2EaenlBMZyY4VCtveqwz5CNavJDGE5aM_c6DN4XaQ17CDH18jUeXBZ7GNbySoYDZtN-0dnmrpHKqRtl2Ryx-3VxfQcjYCPQA1qEneW1VZekBUEXZyFD8gc2Yu59SFiRDhl0aJTECsrV_0ibCe3XhjTwY79PPThHN4g9O6Fa9LkaUjaBkGDRuH3m2W2EEDZfBWV-hsy09h2PHlsTlRnA3sZ_E2L-d1ugVotBtEFWOu6HHL3DReeniRkEdeMER1OktlzueckkkoN7qnGZ-7tBP6IoIaAU2BzaMDa7KqyUG1D5jJtU3Z6kN3RNALOC_CWxAxjqtyXG9W8u42IJqhrWv3tGIvrFZJHG2f1UFr-DApKmZk20Sj6DhwGcilI77SJ_1fgoHDRYStTo-IdjBGfOakaiwDZgRtq7EIJa8GH3SpyoXPaeBlfzLshynIvHb-fEvH2CMQo1iD2Cl7NHpt-BS77xCS8vPzTRnhfYn7M2x77oA37-ihQqcv_fcOkK0xvLYLyAZ350exmSTxaeaPOQRI_EGSbn4tg4zh0U0D5PlZdJu9TE_dntgqd4clgkllo1qocycvWcnhoTfpTu8pEmGxQw3R_5_kHI3F-NU3jdxHpA9H-yq9ph8P7lDjwf6-9X7B23DEYXJsRWGmTiPXtENB7BBdLJDX230KmXjkqSOWQrl1e4xUKk4qnUcUtDYZZxQjFQoi8TTDSOlXki9hnC366Lw1q8C_tpiQuWgKr8T60XQcC57ad8U-l-auRTFvrj1JV1RLJv94H9Jp7txy2oRH7l1yvu2W_mUsG1_JE58xDegMwICOY3uW-aXYiLwjwwiGsDDtMMwJdPG5MAgJM92Jym4HbZ-Bi6aPBCvixU3pC-MYlaUOM98SNfGjV46KLlotVK1muxa8nGSWjATFKD1x77zbD0e1pyMGtjndg3Ly_lzNtQHJD0uxo-Aj194Qb60UxPN2c97OZDqIuM1xVo3iNJvDEE7NjGHcwU2WzN-TvxYoCoWl9u8lYE_XIKekr-y69yhbj-9sSZVKnQn20FFy4tC36dQ5npj18jf_gbR5RUNL92mLfyXVvgO4HadlW8HLXHnwHBFv9RiQR_7sclj79VlGTzBRg9q0BZqnM7eiLcF-tdB49RJ0Jm2PwqmUSnDIkJSuhYvnKjyLay4FwJd_hjj8IookNaogobc5j6YdwriX0TebTjUa-20UPsDvFS_DxCisOx5R8cP8DV9hRSdFovi-nsI4RzTFpMousyUD4s91xJCcXe6mibLOL507yzYww.hZ9waX0zaiHkyVjGHTSnmAAwnkPGFmxyyUwG_JkrdZk";
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
