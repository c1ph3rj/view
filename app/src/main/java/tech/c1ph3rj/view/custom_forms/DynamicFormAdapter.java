package tech.c1ph3rj.view.custom_forms;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

import tech.c1ph3rj.view.R;

public class DynamicFormAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    private final List<FormField> formFields;

    public DynamicFormAdapter(Context context, List<FormField> formFields) {
        this.context = context;
        this.formFields = formFields;
    }

    static void setRequiredLabel(TextView labelView, String label) {
        String labelText = label + " *";
        SpannableString spannableString = new SpannableString(labelText);
        spannableString.setSpan(new ForegroundColorSpan(Color.RED), labelText.length() - 1, labelText.length(), 0);
        labelView.setText(spannableString);
    }

    @Override
    public int getItemViewType(int position) {
        // Determine the type of view needed based on the field type
        FormField field = formFields.get(position);
        switch (field.getTemplateType()) {
            case "text":
                return R.layout.text_input_layout;
            case "select":
                return R.layout.spinner_input_layout;
            case "date":
                return R.layout.date_input_layout;
            case "radio":
                return R.layout.radio_button_layout;
        }
        return R.layout.empty_layout_view;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the appropriate layout
        View view = LayoutInflater.from(context).inflate(viewType, parent, false);
        if (viewType == R.layout.text_input_layout) {
            return new TextViewHolder(view);
        } else if (viewType == R.layout.spinner_input_layout) {
            return new SpinnerViewHolder(view);
        } else if (viewType == R.layout.date_input_layout) {
            return new DateViewHolder(view);
        } else if (viewType == R.layout.radio_button_layout) {
            return new RadioButtonViewHolder(view);
        }
        return new EmptyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // Bind the data to the holder
        FormField field = formFields.get(position);
        if (holder instanceof TextViewHolder) {
            ((TextViewHolder) holder).bind(field);
        } else if (holder instanceof SpinnerViewHolder) {
            ((SpinnerViewHolder) holder).bind(field);
        } else if (holder instanceof DateViewHolder) {
            ((DateViewHolder) holder).bind(field);
        } else if (holder instanceof RadioButtonViewHolder) {
            ((RadioButtonViewHolder) holder).bind(field);
        }
        // Handle other cases...
    }

    @Override
    public int getItemCount() {
        return formFields.size();
    }

    public static class TextViewHolder extends RecyclerView.ViewHolder {
        private final TextView label;
        private final EditText editText;

        public TextViewHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.textInputLabel);
            editText = itemView.findViewById(R.id.editTextInput);
        }

        public void bind(FormField field) {
            label.setText(field.getLabel());
            setRequiredLabel(label, field.getLabel());
            editText.setHint(field.getPlaceholder());
            editText.setText((!field.getValue().isEmpty()) ? field.getValue() : "");
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    field.setValue(editable.toString().trim());
                }
            });
        }
    }

    public static class SpinnerViewHolder extends RecyclerView.ViewHolder {
        private final TextView label;
        private final MultiSpinner multiSpinner;
        private final AppCompatSpinner spinner;

        public SpinnerViewHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.spinnerInputLabel);
            multiSpinner = itemView.findViewById(R.id.multiSelectSpinnerView);
            spinner = itemView.findViewById(R.id.spinnerView);
            spinner.setVisibility(View.GONE);
            multiSpinner.setVisibility(View.GONE);
        }

        public void bind(FormField field) {
            label.setText(field.getLabel());
            setRequiredLabel(label, field.getLabel());
            if (field.isMultiple()) {
                multiSpinner.setVisibility(View.VISIBLE);
                multiSpinner.setItems(field.getOptions(), new ArrayList<>(field.getOptionLabels()), "Select", selected -> {
                    JSONArray selectedJSONArray = new JSONArray();
                    for (FormField.Options eachOptions : selected) {
                        if (eachOptions.isSelected) {
                            selectedJSONArray.put(eachOptions.id);
                        }
                    }
                    field.setValue(selectedJSONArray.toString());
                });
                if (field.getPlaceholder() != null && !field.getPlaceholder().isEmpty()) {
                    multiSpinner.setPrompt(field.getPlaceholder());
                }


            } else {
                spinner.setVisibility(View.VISIBLE);
                spinner.setAdapter(new ArrayAdapter<>(itemView.getContext(), R.layout.simple_spinner_item, field.getOptionLabels()));
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        if (spinner.getSelectedItem() != null && !spinner.getSelectedItem().toString().equals("select")) {
                            field.setValue(field.getOptions().get(field.getOptionLabels().indexOf(spinner.getSelectedItem().toString()) - 1).id);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
                int pos = -1;
                for (FormField.Options eachOptions : field.getOptions()) {
                    if (eachOptions.isSelected) {
                        pos = field.getOptions().indexOf(eachOptions);
                    }
                }
                spinner.setSelection(pos);
            }
        }
    }

    public static class RadioButtonViewHolder extends RecyclerView.ViewHolder {
        private final TextView label;
        private final RadioGroup radioGroup;

        public RadioButtonViewHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.radioInputLabel);
            radioGroup = itemView.findViewById(R.id.radioGroup);
        }

        public void bind(FormField field) {
            setRequiredLabel(label, field.getLabel());
            radioGroup.removeAllViews();
            HashSet<String> selectedIds = new HashSet<>();

            // Prepopulate the set if there are already selected values
            try {
                JSONArray selectedValues = new JSONArray(field.getValue());
                for (int i = 0; i < selectedValues.length(); i++) {
                    selectedIds.add(selectedValues.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            ColorStateList colorStateList = new ColorStateList(
                    new int[][]
                            {
                                    new int[]{-android.R.attr.state_enabled}, // Disabled
                                    new int[]{android.R.attr.state_enabled}   // Enabled
                            },
                    new int[]
                            {
                                    itemView.getContext().getColor(R.color.themeColor), // disabled
                                    itemView.getContext().getColor(R.color.themeColor)   // enabled
                            }
            );

            for (FormField.Options option : field.getOptions()) {
                RadioButton radioButton = new RadioButton(itemView.getContext());
                radioButton.setText(option.label);
                radioButton.setId(View.generateViewId());

                radioButton.setButtonTintList(colorStateList);
                radioGroup.addView(radioButton);

                radioButton.setChecked(selectedIds.contains(option.id));

                radioButton.setOnCheckedChangeListener((compoundButton, b) -> {
                    if (radioButton.isChecked()) {
                        selectedIds.add(option.id);
                    } else {
                        selectedIds.remove(option.id);
                    }

                    // Convert the set of IDs to JSON array and set the value
                    JSONArray jsonArray = new JSONArray();
                    for (String id : selectedIds) {
                        jsonArray.put(id);
                    }
                    field.setValue(jsonArray.toString());
                });
            }
        }

    }

    public static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView label;
        EditText dateTextView;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateView);
            label = itemView.findViewById(R.id.dateViewLabel);
        }

        void bind(FormField field) {
            label.setText(field.getLabel());
            dateTextView.setHint(field.getPlaceholder());
            dateTextView.setText((!field.getValue().isEmpty()) ? field.getValue() : "");
            setDatePickerDialog(itemView.getContext(), dateTextView, field);
            setRequiredLabel(label, field.getLabel());
        }

        void setDatePickerDialog(final Context context, final EditText editText, FormField field) {
            final Calendar calendar = Calendar.getInstance();
            final int year = calendar.get(Calendar.YEAR);
            final int month = calendar.get(Calendar.MONTH);
            final int day = calendar.get(Calendar.DAY_OF_MONTH);

            editText.setOnClickListener(v -> {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        context,
                        (view, selectedYear, selectedMonth, selectedDay) -> {
                            // Set the selected date to the EditText
                            String selectedDate = selectedYear + "-" + (((selectedMonth + 1) <= 9) ? "0" + (selectedMonth + 1) : (selectedMonth + 1)) + "-" + (((selectedDay + 1) <= 9) ? "0" + (selectedDay + 1) : (selectedDay + 1));
                            editText.setText(selectedDate);
                            field.setValue(selectedDate);
                        },
                        year, month, day);

                // Optionally, set minimum and maximum date here
                // For example, to set a minimum date of one year ago:
                // Calendar minDate = Calendar.getInstance();
                // minDate.add(Calendar.YEAR, -1);
                // datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());

                // Show the DatePicker dialog
                datePickerDialog.show();
            });
        }
    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {

        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
