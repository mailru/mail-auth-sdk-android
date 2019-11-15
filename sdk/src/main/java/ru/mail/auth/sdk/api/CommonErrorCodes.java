package ru.mail.auth.sdk.api;

public class CommonErrorCodes {
    public static final int NETWORK_ERROR = -1;
    public static final int SERVER_API_ERROR = -2;
    public static final int MALFORMED_URL_ERROR = -3;
    public static final int RETRY_LIMIT_EXCEEDED = -4;
    public static final int REQUEST_CANCELLED = -5;

    public static String toReadableString(int code) {
        switch (code) {
            case NETWORK_ERROR:
                return "Network error";
            case SERVER_API_ERROR:
                return "Server api error";
            case MALFORMED_URL_ERROR:
                return "Malformed url error";
            case RETRY_LIMIT_EXCEEDED:
                return "Network retry limit exceeded";
            case REQUEST_CANCELLED:
                return "Request cancelled";
            default:
                return "Unknown error";
        }
    }
}
