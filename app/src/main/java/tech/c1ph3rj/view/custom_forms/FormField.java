package tech.c1ph3rj.view.custom_forms;

import java.util.Comparator;
import java.util.List;

public class FormField {
    private String label;
    private String type;
    private int order;
    private boolean isMultiple;
    private String placeholder;
    private List<String> options; // Assuming options for select-type fields

    // Constructor
    public FormField(String label, String type, int order, boolean isMultiple, String placeholder, List<String> options) {
        this.label = label;
        this.type = type;
        this.order = order;
        this.isMultiple = isMultiple;
        this.placeholder = placeholder;
        this.options = options;
    }

    // Getters and Setters
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public boolean isMultiple() {
        return isMultiple;
    }

    public void setMultiple(boolean multiple) {
        isMultiple = multiple;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    // To compare FormField objects by their order for sorting
    public static final Comparator<FormField> BY_ORDER = new Comparator<FormField>() {
        @Override
        public int compare(FormField o1, FormField o2) {
            return Integer.compare(o1.getOrder(), o2.getOrder());
        }
    };
}

