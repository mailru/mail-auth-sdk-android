package ru.mail.auth.sdk.call;

import androidx.annotation.NonNull;
import ru.mail.auth.sdk.api.token.TokenExpiredListener;

public class RefreshTokenRetry<R> extends BaseRetryMethodCall<R> {

    public static final int RETRY_CNT = 1;

    @NonNull
    private final TokenExpiredListener mRefreshTokenListener;

    public RefreshTokenRetry(MethodCall<R> decorated,
                             @NonNull TokenExpiredListener refreshTokenListener) {
        super(decorated, RETRY_CNT);
        mRefreshTokenListener = refreshTokenListener;
    }

    @Override
    protected String getNameForLog() {
        return "RefreshTokenRetry";
    }

    @Override
    protected boolean handleException(CallException e) throws CallException {
        if (e.isRecoverableAuthError()) {
            mRefreshTokenListener.onTokenExpired();
            return true;
        }
        return false;
    }

    @Override
    protected boolean shouldDelayBeforeRetry() {
        return false;
    }

}
