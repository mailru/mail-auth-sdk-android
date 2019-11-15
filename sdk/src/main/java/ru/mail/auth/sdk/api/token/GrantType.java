package ru.mail.auth.sdk.api.token;

enum GrantType {
    AUTH_CODE("authorization_code"),
    REFRESH_TOKEN("refresh_token");

    private String mValue;

    GrantType(String value) {
        mValue = value;
    }

    public String getValue() {
        return mValue;
    }
}
