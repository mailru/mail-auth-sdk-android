package ru.mail.auth.sdk;

enum RequestCodeOffset {
    LOGIN(1);

    private final int mCode;

    RequestCodeOffset(int code) {
        mCode = code;
    }

    int toRequestCode() {
        return MailRuAuthSdk.getInstance().getRequestCodeOffset() + mCode;
    }
}
