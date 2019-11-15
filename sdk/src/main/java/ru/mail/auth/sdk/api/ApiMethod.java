package ru.mail.auth.sdk.api;

public enum ApiMethod {
    USERINFO("userinfo"),
    TOKEN("token");

    private String mValue;

    ApiMethod(String value) {
        mValue = value;
    }

    public String getValue() {
        return mValue;
    }
}
