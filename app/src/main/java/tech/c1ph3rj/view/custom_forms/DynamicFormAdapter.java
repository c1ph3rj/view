package tech.c1ph3rj.view.custom_forms;

import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import tech.c1ph3rj.view.R;

public class DynamicFormAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    private final List<FormField> formFields;

    public DynamicFormAdapter(Context context, List<FormField> formFields) {
        this.context = context;
        this.formFields = formFields;
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
        }
        // Handle other cases...
    }

    @Override
    public int getItemCount() {
        return formFields.size();
    }

    // ViewHolder for text input
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
            editText.setHint((!field.getValue().isEmpty()) ? field.getValue() : field.getPlaceholder());
        }
    }

    // ViewHolder for spinner
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
            if (field.isMultiple()) {
                multiSpinner.setVisibility(View.VISIBLE);
                multiSpinner.setItems(field.getOptions(), new ArrayList<>(field.getOptionLabels()), "Select", selected -> {

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
                        if(spinner.getSelectedItem() != null) {
                            field.setValue(spinner.getSelectedItem().toString());
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
                int pos = -1;
                for(FormField.Options eachOptions : field.getOptions()) {
                    if(eachOptions.isSelected) {
                        pos = field.getOptions().indexOf(eachOptions);
                    }
                }
                spinner.setSelection(pos);
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
            dateTextView.setText((!field.getValue().isEmpty()) ? field.getValue() : field.getPlaceholder());
                setDatePickerDialog(itemView.getContext(), dateTextView, field);

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
                            String selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
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
