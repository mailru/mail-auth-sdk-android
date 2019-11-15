package ru.mail.auth.sdk.api;

public class OAuthRequestErrorCodes extends CommonErrorCodes {
    public static final int INVALID_CLIENT_ID = 1;
    public static final int INVALID_REQUEST = 2;
    public static final int INVALID_CREDENTIALS = 3;
    public static final int INVALID_TOKEN = 6;

    public static String toReadableString(int code) {
        switch (code) {
            case INVALID_CLIENT_ID:
                return "Invalid client id";
            case INVALID_REQUEST:
                return "Invalid request";
            case INVALID_TOKEN:
                return "Invalid credentials";
            case INVALID_CREDENTIALS:
                return "Invalid token";
            default:
                return CommonErrorCodes.toReadableString(code);
        }
    }
}
