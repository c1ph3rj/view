package tech.c1ph3rj.view.custom_forms;

import static tech.c1ph3rj.view.Services.response;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import tech.c1ph3rj.view.R;
import tech.c1ph3rj.view.Services;

public class CustomForms extends AppCompatActivity {
    Services services;
    RecyclerView dynamicFormView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_forms);

        services = new Services(this);
        init();
    }

    void init(){
        try {
            dynamicFormView = findViewById(R.id.dynamicFormView);
            DynamicFormAdapter dynamicFormAdapter = new DynamicFormAdapter(this,extractFormFieldsFromResponse(response) );
            dynamicFormView.setLayoutManager(new LinearLayoutManager(this));
            dynamicFormView.setAdapter(dynamicFormAdapter);
        } catch (Exception e) {
            services.handleException(e);
        }
    }

    public static List<FormField> extractFormFieldsFromResponse(String jsonResponse) {
        List<FormField> formFields = new ArrayList<>();

        try {
            JSONObject response = new JSONObject(jsonResponse);
            JSONArray allApiFormally = response.getJSONObject("rObj").getJSONArray("getAllAPIFormaly");

            // Assuming we're only interested in the first element of the getAllAPIFormaly array
            JSONArray fieldGroups = allApiFormally.getJSONObject(0).getJSONArray("fieldGroup");

            for (int i = 0; i < fieldGroups.length(); i++) {
                JSONObject fieldGroup = fieldGroups.getJSONObject(i);
                JSONObject templateOptions = fieldGroup.getJSONObject("templateOptions");

                String key = fieldGroup.optString("key");
                String type = fieldGroup.optString("type");
                String label = templateOptions.optString("label");
                boolean required = templateOptions.optBoolean("required");
                String placeholder = templateOptions.optString("placeholder", ""); // Default to empty string if not provided
                boolean isMultiple = templateOptions.optBoolean("multiple");
                int orderData = fieldGroup.optInt("orderData");
                JSONArray optionsArray = templateOptions.optJSONArray("options");
                List<String> options = new ArrayList<>();

                if (optionsArray != null) {
                    for (int j = 0; j < optionsArray.length(); j++) {
                        JSONObject option = optionsArray.getJSONObject(j);
                        options.add(option.optString("label"));
                    }
                }

                FormField formField = new FormField(label, type, orderData, isMultiple, placeholder, options);
                formFields.add(formField);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Handle exception
        }

        return formFields;
    }

}