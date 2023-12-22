package tech.c1ph3rj.view.custom_forms;

import static tech.c1ph3rj.view.Services.checkNull;
import static tech.c1ph3rj.view.Services.response;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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

    void init() {
        try {
            dynamicFormView = findViewById(R.id.dynamicFormView);
            DynamicFormAdapter dynamicFormAdapter = new DynamicFormAdapter(this, extractFormFieldsFromResponse(response));
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

            // Assuming we're only interested in the first element of the getAllAPIFormally array
            JSONArray fieldGroups = allApiFormally.getJSONObject(0).getJSONArray("fieldGroup");

            for (int i = 0; i < fieldGroups.length(); i++) {
                JSONObject fieldGroup = fieldGroups.getJSONObject(i);
                JSONObject templateOptions = fieldGroup.getJSONObject("templateOptions");

                String key = fieldGroup.optString("key", "");
                String type = fieldGroup.optString("type", "");
                String label = templateOptions.optString("label", "");
                boolean isRequired = templateOptions.optBoolean("required", false);
                String placeholder = templateOptions.optString("placeholder", "");
                String templateType = templateOptions.optString("type", "");
                boolean isSelectAllEnabled = templateOptions.optBoolean("selectAllOption", false);
                boolean isMultiple = templateOptions.optBoolean("multiple", false);
                int orderData = fieldGroup.optInt("orderData");
                JSONArray optionsArray = templateOptions.optJSONArray("options");
                List<FormField.Options> options = new ArrayList<>();

                if (optionsArray != null) {
                    for (int j = 0; j < optionsArray.length(); j++) {
                        JSONObject eachOptionObj = optionsArray.getJSONObject(j);
                        FormField.Options eachOption = new FormField.Options();
                        eachOption.label = eachOptionObj.optString("label", "");
                        eachOption.id = eachOptionObj.optString("value", "");
                        eachOption.isSelected = false;
                        options.add(eachOption);
                    }
                }

                if (!checkNull(placeholder)) {
                    placeholder = "";
                }


                if (isRequired && ((type.toLowerCase(Locale.ROOT).equals("select") && !options.isEmpty()) || type.toLowerCase(Locale.ROOT).equals("input"))) {
                    FormField formField = new FormField(key, label, type, templateType,  orderData, isMultiple, isSelectAllEnabled,  placeholder, options);
                    formFields.add(formField);
                }
            }
            formFields.sort(Comparator.comparingInt(FormField::getOrder));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return formFields;
    }

}