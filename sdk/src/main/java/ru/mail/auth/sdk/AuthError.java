package ru.mail.auth.sdk;

import androidx.annotation.StringRes;

import ru.mail.auth.sdk.pub.R;

public enum AuthError {
    NETWORK_ERROR(MailRuAuthSdk.STATUS_ERROR, R.string.mailru_oauth_network_error),
    USER_CANCELLED(MailRuAuthSdk.STATUS_CANCELLED, R.string.mailru_oauth_user_cancelled),
    ACCESS_DENIED(MailRuAuthSdk.STATUS_ACCESS_DENIED, R.string.mailru_oauth_user_denied);

    private int mStatusCode;
    private @StringRes int mRepresentation;

    AuthError(int statusCode, int representation) {
        mStatusCode = statusCode;
        mRepresentation = representation;
    }

    public String getErrorReason() {
        return MailRuAuthSdk.getInstance().getContext().getString(mRepresentation);
    }

    static AuthError fromCode(int statusCode) {
        for (AuthError authError : values()) {
            if (authError.mStatusCode == statusCode) {
                return authError;
            }
        }
        throw new IllegalStateException("Unknown error code " + statusCode);
    }
}
