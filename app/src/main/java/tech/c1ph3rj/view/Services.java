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
    public static String token = "eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkEyNTZDQkMtSFM1MTIiLCJraWQiOiI4YmI1NDE4YWYzYjQ0YjdiOTI2OGM3YjRhYTg1NTYzMiIsInR5cCI6IkpXVCIsImN0eSI6IkpXVCJ9.CwcE30-vfw8CYWlmu2IKw2szvOA6C2kObzUweC-1LIOg15LT7UrdSppD8jMbY5UGuk_zaPF-8AV14pwIPrssyTZ8nlk_xZ1_lXArh8hD_S2ihxP09r-wQK3xnUv_p3Rpnuhm8tYzRiMZ_Bo2_UivI9OucqSyoHMit-CNogvD69-l-eBGU5YJLqCZzr1k7HSW9r9oy0jU36bmLlSjLBFw9qnP3y1p7HoLu2vg6O3SlMi2j6eMRmoVdEEzd_DzfISFPZwigHYA14Xr7YDp-zbOuXJadLWoTqjHD3hWu-5mtV_5ZKI-7EPnkp0Arr5CQ75nSDTyo1RpSRK355cWW0wUcA.tZIhiJXA6jeaEZ0y8-YvyQ.v7HOLSgv-dv31au2QnHMAal2jlvduzj0jnIefvjo9qK5-yj4WCP69sV-kVkuV4IcdyzL66wRbffDRF8RT4LOdl2j12mKeCXfJOdZHp9mymXXCtkybpJf_-krZM1gIc87WFDwtJMzJqETh279z1nNUlBvhK8s4WuMKwyXWXFgIdxWq3ud3e8HBiqzYWAc4fzu4PfVCD2FN_dYBxaUDc4biTKrIzL1mA6kLczH9GHl1bzk1GCrscyLPpJuc34JwuDwUVi7DkAghLHPEpFUIVfWbD2NS6gk4x-4UvortJIxOtKLrzynuoadPpS-rJ_cmRg4WYNy6uydRb0CjdcWpmygTOql99nJjdQDJlLQj9BKdTzbVbWFBb44JGS1MWpnrNfqDHvtBnDu2q3NkCo_LuDWcbeWZDgF_2qRmuXu2IyojpDrmseevuJ4CFqBbp14fDKPxzwNzkZ2lLLZrGt_f77wQYgsGGBpd-7SvmBWRimc72VAr1BoltAzDUETMNTbnShy_b9n6Tzs33aKG-X4y4CBeJdWXVjgOActtqugLj-szARvEVbVaEU6cXnjBhqJYo-yqV9QeOY2u53w-cHzu5iNktSSdYxcon-4Q8eoU1lu40FXy7ByKFps7-5fPlYtxYzoLWeeNSU9_SQ_LKCpxPI81YOPk9gH9Dk1pLgAKpC58FFYfUqSwZuKwNnuKuAdbH9KCDNEcE3a5RqSFYCMWT1AqoKig9YiTSNNGjv54InQFrpdaOpJxbfXeNfx2zjSfvGZzTE8yDxxcq24qpM4gq19QQ0z3z2jJdyi9LTUv1SYyKH_A4w6umDRLlEZ4lDGDbcA5yOSNvSLRkQENa_zsl-Oo59qtm1XZPqtgYxFpF_y4PXEdZJNHLHNUd2BVxrX3dP3FQwzwupLks7LuukEfusGjPEDLOx_AcUe8rOcsC9rl8oSdXOifLSwSbU1LSs1RhyiysMpQGK1nx_mgSycXxJ40Ve7kE6p9yy4PV2hBeEDjbAxR_W3qsmBOshCBxA8IofzHsCZqGiRXSK3iL0XYqzxN8KTuDHtroYGZbGfKdiXSwlc7gQ_E08OThrx7FPQT_q75S_fK_I2gBi8nIlonKeOyEsoLxnGdUnkmXBT6eNz9OHRb1pwIfv5NAjKl9o4Tea8dwZ6pDnBwIdk6BFOOASp9156uLKzG1lyMGQwOXViB6zLW2iLCoMrpQZUo_bp_Xk-YNmd9764YsSgdrUO9BkJGVZ9szwt5ULbE6H7LRznT4gezz2npyI9KMq_SWSYlhFnWVme7OcFp-yO7v1m3TM3mdfRSU4G6nkDcb1I7n_vVaAePpMs9KqgqJ6iPy2v2u8aUdPFIiYSZ2KBwYPMt6cdB8PqrYleiePJ0Kod232tHEBvmCg-ESrZUWKybbVDlk7DUCCBHc-4a6JO8tiVBbB9jA.Zek0DfGrEZkP-eva2P0Wk8nJMK7c-h2GhTn2HEizdG8";
    private long mLastClickTime = 0;
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
