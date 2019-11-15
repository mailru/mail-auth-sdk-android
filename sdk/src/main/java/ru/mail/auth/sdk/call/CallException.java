package ru.mail.auth.sdk.call;

import ru.mail.auth.sdk.api.CommonErrorCodes;
import ru.mail.auth.sdk.api.OAuthRequestErrorCodes;

public class CallException extends Exception {

    private final int mErrorCode;
    private final String mErrorMsg;
    private CallException mOriginalException;

    public CallException(int errorCode) {
        this(errorCode, OAuthRequestErrorCodes.toReadableString(errorCode));
    }

    public CallException(int errorCode, String errorMsg) {
        super(errorMsg);
        mErrorCode = errorCode;
        mErrorMsg = errorMsg;
    }

    public int getErrorCode() {
        return mErrorCode;
    }

    public String getErrorMsg() {
        return mErrorMsg;
    }

    public static CallException retryLimitExceeded(CallException original) {
        CallException exception = new CallException(CommonErrorCodes.RETRY_LIMIT_EXCEEDED, "retry limit exceeded");
        exception.setOriginalException(original);
        return exception;
    }

    public boolean isInvalidCredentialsError() {
        return mErrorCode == OAuthRequestErrorCodes.INVALID_CREDENTIALS;
    }

    public boolean isRecoverableAuthError() {
        return mErrorCode == OAuthRequestErrorCodes.INVALID_TOKEN;
    }

    public boolean isNetworkError() {
        return mErrorCode == CommonErrorCodes.NETWORK_ERROR;
    }

    public boolean isInternalServerError() {
        return mErrorCode == CommonErrorCodes.SERVER_API_ERROR;
    }

    @Override
    public String toString() {
        return String.format("Code: %s, Msg: %s", mErrorCode, mErrorMsg);
    }

    public CallException getOriginalException() {
        return mOriginalException;
    }

    public void setOriginalException(CallException originalException) {
        mOriginalException = originalException;
    }
}
