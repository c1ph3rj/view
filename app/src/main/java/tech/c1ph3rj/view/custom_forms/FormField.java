package tech.c1ph3rj.view.custom_forms;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FormField {
    // To compare FormField objects by their order for sorting
    public static final Comparator<FormField> BY_ORDER = new Comparator<FormField>() {
        @Override
        public int compare(FormField o1, FormField o2) {
            return Integer.compare(o1.getOrder(), o2.getOrder());
        }
    };
    public boolean isRequired;
    public String regexPattern;
    public String validationMsg;
    private String key;
    private String label;
    private String type;
    private int order;
    private boolean isMultiple;
    private String placeholder;
    private String templateType;
    private boolean isSelectAllEnabled;
    private List<Options> options;
    private List<String> optionLabels;
    private String value;

    // Constructor
    public FormField(String key, String label, String type, String templateType, int order, boolean isMultiple, boolean isSelectAllEnabled, String placeholder, List<Options> options) {
        this.key = key;
        this.label = label;
        this.type = type;
        this.templateType = templateType;
        this.order = order;
        this.isMultiple = isMultiple;
        this.isSelectAllEnabled = isSelectAllEnabled;
        this.placeholder = placeholder;
        optionLabels = new ArrayList<>();
        try {
            if (!isMultiple) {
                optionLabels.add("select");
            }
            this.options = options;
            for (Options eachOption : options) {
                optionLabels.add(eachOption.label);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.value = "";
    }

    public List<String> getOptionLabels() {
        return optionLabels;
    }

    public void setOptionLabels(List<String> optionLabels) {
        this.optionLabels = optionLabels;
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

    public List<Options> getOptions() {
        return options;
    }

    public void setOptions(List<Options> options) {
        this.options = options;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public boolean isSelectAllEnabled() {
        return isSelectAllEnabled;
    }

    public void setSelectAllEnabled(boolean selectAllEnabled) {
        isSelectAllEnabled = selectAllEnabled;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static class Options {
        String label;
        String id;
        boolean isSelected;
    }
}

