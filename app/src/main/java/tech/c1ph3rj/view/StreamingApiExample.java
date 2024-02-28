package tech.c1ph3rj.view;

import androidx.annotation.NonNull;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Call;
import okhttp3.Callback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.json.JSONObject;

public class StreamingApiExample {

    private void streamApi() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://rag-llm.azurewebsites.net/streamapi_test")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                try {
                    try (ResponseBody responseBody = response.body()) {
                        if (responseBody != null) {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(responseBody.byteStream()));

                            String line;
                            while ((line = reader.readLine()) != null) {
                                // Each line is a complete JSON object
                                JSONObject jsonObject = new JSONObject(line);
                                // Process the JSON object as needed
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
