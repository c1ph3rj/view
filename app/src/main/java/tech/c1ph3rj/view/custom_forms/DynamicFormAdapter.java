package tech.c1ph3rj.view.custom_forms;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import tech.c1ph3rj.view.R;

public class DynamicFormAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<FormField> formFields;

    public DynamicFormAdapter(Context context, List<FormField> formFields) {
        this.context = context;
        this.formFields = formFields;
    }

    @Override
    public int getItemViewType(int position) {
        // Determine the type of view needed based on the field type
        FormField field = formFields.get(position);
        switch (field.getType()) {
            case "text":
                return R.layout.text_input_layout;
            case "select":
                return R.layout.spinner_input_layout;
            // Handle other cases...
        }
        return R.layout.text_input_layout;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the appropriate layout
        View view = LayoutInflater.from(context).inflate(viewType, parent, false);
        if (viewType == R.layout.text_input_layout) {
            return new TextViewHolder(view);
        } else if (viewType == R.layout.spinner_input_layout) {
            return new SpinnerViewHolder(view);
            // Handle other cases...
        }
        return new TextViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        // Bind the data to the holder
        FormField field = formFields.get(position);
        if (holder instanceof TextViewHolder) {
            ((TextViewHolder) holder).bind(field);
        } else if (holder instanceof SpinnerViewHolder) {
            ((SpinnerViewHolder) holder).bind(field);
        }
        // Handle other cases...
    }

    @Override
    public int getItemCount() {
        return formFields.size();
    }

    // ViewHolder for text input
    public static class TextViewHolder extends RecyclerView.ViewHolder {
        private TextView label;
        private EditText editText;

        public TextViewHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.textInputLabel);
            editText = itemView.findViewById(R.id.editTextInput);
        }

        public void bind(FormField field) {
            label.setText(field.getLabel());
            editText.setHint(field.getPlaceholder());
            // You can add more configuration here based on the field's properties
        }
    }

    // ViewHolder for spinner
    public static class SpinnerViewHolder extends RecyclerView.ViewHolder {
        private TextView label;
        private Spinner spinner;

        public SpinnerViewHolder(View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.spinnerInputLabel);
            spinner = itemView.findViewById(R.id.spinnerInput);
        }

        public void bind(FormField field) {
            label.setText(field.getLabel());
            ArrayAdapter<String> adapter = new ArrayAdapter<>(itemView.getContext(),
                    android.R.layout.simple_spinner_item, field.getOptions());
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
            // If the placeholder text is meant to be the first item in the spinner
            if (field.getPlaceholder() != null && !field.getPlaceholder().isEmpty()) {
                spinner.setPrompt(field.getPlaceholder());
            }
        }
    }
}
