package com.bag2bag.st.dto;

public class EnhanceRequest {
    private String text;
    private String fieldType;

    public EnhanceRequest() {}

    public EnhanceRequest(String text, String fieldType) {
        this.text = text;
        this.fieldType = fieldType;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }
}
