package ru.mail.auth.sdk.call;

import ru.mail.auth.sdk.api.token.TokenExpiredListener;

public class CallBuilder<R> {
    MethodCall<R> mCall;

    private CallBuilder(MethodCall<R> call) {
        mCall = call;
    }

    public static <R> CallBuilder<R> from(MethodCall<R> call) {
        return new CallBuilder<>(call);
    }

    public CallBuilder<R> withNetworkRetry() {
        mCall = new NetworkErrorRetry<>(mCall);
        return this;
    }

    public CallBuilder<R> withNetworkRetry(int retryAmount) {
        mCall = new NetworkErrorRetry<>(mCall, retryAmount);
        return this;
    }

    public CallBuilder<R> withAccessTokenRefresh(TokenExpiredListener listener) {
        mCall = new RefreshTokenRetry<>(mCall, listener);
        return this;
    }

    public MethodCall<R> build() {
        return mCall;
    }
}
