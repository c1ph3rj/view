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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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
    public static final MediaType JSON = MediaType.get("application/json");
    public static int base_version;
    public static String app_version;
    public static String token = "eyJhbGciOiJSU0EtT0FFUCIsImVuYyI6IkEyNTZDQkMtSFM1MTIiLCJraWQiOiI4YmI1NDE4YWYzYjQ0YjdiOTI2OGM3YjRhYTg1NTYzMiIsInR5cCI6IkpXVCIsImN0eSI6IkpXVCJ9.awyeGMCSdtj95BvM1BXOwDubagstFPpFMo2T-SYLWAh69UfcHbMmMeI1DBzUuiM7gwqCreL2yyaK0C2R7qLfW-ZtrUx-T4keOAPwzEHws_7COJKBmYwraeNmocaqT-6ti06bGwCiUxT-okuKWNXfeRkk15Piwzj7x-0kSHDT0WpvIGShqNd-JjtwpQFV3XNV6ux430ds1O9S1CVFavS9INVeGtVoHWd-lDmuF4npfFxhoKY-JKRPlhgUwMgRLUoAnJymI3scKPGOHZsvCylW35LJ6Ge2h4dskwaSSxOzZzXz8Z9MXYhYwvEHXDfHzT3pzjHRr6q-8FJxYw9EdAkr7Q.5zG-gC5iiMIWSEQNyhvkbg.IsKzCfSIDaNyWn2dnjXVkGCSDoWRKzb0JpIHNE-k7SzPaaMcH0zn6xsPu4obVAsy9uDCaXQ8-_pJukWM9vJzNpqR__Euhw1ITe2EzDjF8CXVVJL63CMTk0zc76X63Fb8J_pgeaHHm7M6IcM5HKOah-IoIGyQsebU_tlHT8cMRd4uQUVpN5-W6VBDjIGWfn6YPfdTkznXi_gMWIGiayE3Hgs8JqEYc17WqteQAT3AniDdrDVFXcEFF92K0dURDcI7h3CU0Ycp2xaKl6bhd9gR5C3e9or2OReQvMwyigNVpPdxm2rqzgj-727hnJ31KE-fiQbp0hpMMMWwQ0PyTE9-KKx4LUGe0mZy5RKg85oBmuRfzPKQGeiiI-NPezsuVlcXdDucQ6qqQXYDq7FOATQpHr2MaB3xn-nL4hDPImZAT9K-VCLMd0l6YB5BcB2Y7XTAwNLyGsY5CuIaZYHeu92cyXnjsw1kC6kPs_NRKUWkHBgsdXNHTZ0Kahmvokf6hDUCEueVowkA-fgfWh_JEJAlyyUsim7Z38CquIirJWbTh8CmSks_AwdVwN0emGg5An08MH50uq1hRuSp33PLVE_ya-ymJdbMUpiZKZyDo0mLW847Y6jc6hck-h9fNYR3K-T0aPdVU9aB6ufou9XR5XeTf56c82p5JkIerWFAtYlwsmm-4R5dM2ajmhbe0AR8bwBD-ZPGa_REi_AyyS4nAYw_5UzOFKLtnoPJa1MprTP72jw9BzwMHlssucbMxidrxAyXQmdF1bQREX02cTSKYeqBYs0BSit7B709P1-EP_dZYuLcJHL9QEL_-W3BOSj7Zv5IpGCDzKw8er7glIhjMt5az-obOx8ifnZbngxYRsmJjEIvpXlle2YpjhJBsN0JC3kIm9U5juffMGqgIgCaABDDnrg5gm4IsYhXoEKQmyndMfeU4HHWXi1RwGMMCnnIVr4n68MNBKD5oEEH8WB2UUdlO2Q5wAA3PJWvQaEogCI0YHY-modK7vFp3QZFp4Dj56UzI5WBX3WIRiKIcWfTd1WPE6YJ7DUqd38nGBJdFloMbofURfV8AQKCLL1meboqj1S7gEdGuP_BwX3vvT7nSMGT1y712IbxVVIw2qos1BqU_7Fxyx0DDUv7M608BK9rGN6is0MS2rzCawmsDn0xlKaXUyOVFMPB7GerD32a_Y7cC11j50ks3cTwvyyVM5y7rk91MIbYwCUxU27t-up0Ebg3_-VnpLa-tk5_xnlnAVzLp7dK2K_ZB6te9RKvxFJRcnz5Je2qCOu68z2o-yCtoAncV6FjaBtCGP4tiI7eOfuOJc_HSUEINUXhwrrnRZuReYCgjyD_xYBaFp01P4op-uvJfyCgsTqUVF_3mHsKtBHE7ofiA1bmL8E2-BGGXRKna6qY1ePRuemZHmKJTQWAAQjyRg.Ll5olXatauZgRvCoYMrGg984I8PorGnorDCeUkgGapw";
    public static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build();
    public String baseUrl;
    public Dialog loader;
    Context context;
    Activity activity;
    RefreshListener refreshListener;
    private DatabaseHelper mydb;
    private long mLastClickTime = 0;

    public Services(Context context, RefreshListener refreshListener) {
        this.context = context;
        activity = (Activity) context;
        this.refreshListener = refreshListener;
        baseUrl = activity.getString(R.string.base_url);
        loader = initLoaderDialog();
        app_version = context.getString(R.string.app_version);
        String[] app_version_split = app_version.split("\\.");
        base_version = Integer.parseInt(app_version_split[0]);
    }

    public static void unAuthorize(Activity activity) {
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

    public static boolean checkNull(String value) {
        return !(value == null || value.trim().isEmpty() || value.trim().toLowerCase(Locale.getDefault()).equals("null"));
    }

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void showKeyboard(View view) {
        if (view instanceof EditText) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    public void showDialog() {
        activity.runOnUiThread(() -> {
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
                        if (refreshListener != null) {
                            refreshListener.onCalledRefresh();
                        }
                    }
                });
            }
        }
    }

    // You can add more methods as needed, such as dismissing the dialog
    public void dismissDialog() {
        activity.runOnUiThread(() -> {
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

    public void handleException(Exception e) {
        e.printStackTrace();
    }

    public void somethingWentWrong() {
        activity.runOnUiThread(() -> Toast.makeText(context, "Something went wrong!", Toast.LENGTH_SHORT).show());
    }

    public boolean preventDoubleClick() {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 2000) {
            return false;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        return true;
    }

    public void showErrorMessageFromAPI(JSONArray rMsg) {
        activity.runOnUiThread(() -> {
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

    public interface RefreshListener {
        void onCalledRefresh();
    }
}
