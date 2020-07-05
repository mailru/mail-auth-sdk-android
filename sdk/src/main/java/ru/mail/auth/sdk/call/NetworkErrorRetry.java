package ru.mail.auth.sdk.call;

public class NetworkErrorRetry<R> extends BaseRetryMethodCall<R> {

    private static final int NETWORK_ERROR_RETRY = 3;

    public NetworkErrorRetry(MethodCall<R> decorated, int amount) {
        super(decorated, amount);
    }

    public NetworkErrorRetry(MethodCall<R> decorated) {
        super(decorated, NETWORK_ERROR_RETRY);
    }

    @Override
    protected boolean shouldDelayBeforeRetry() {
        return true;
    }

    @Override
    protected String getNameForLog() {
        return "NetworkRetry";
    }

    protected boolean handleException(CallException e) {
        return e.isNetworkError() || e.isInternalServerError();
    }
}
