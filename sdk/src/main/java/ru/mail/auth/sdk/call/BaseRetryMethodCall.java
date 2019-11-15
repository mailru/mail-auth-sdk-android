package ru.mail.auth.sdk.call;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import ru.mail.auth.sdk.MailRuAuthSdk;

abstract class BaseRetryMethodCall<R> extends CallDecorator<R> {

    private int mTryCount = 0;
    private final int mMaxTryCnt;

    public BaseRetryMethodCall(MethodCall<R> decorated, int retryCnt) {
        super(decorated);
        mMaxTryCnt = retryCnt;
    }

    @Override
    public R execute() throws CallException {
        CallException latestException = null;
        while (canRetry()) {
            try {
                delayBeforeRetry();
                return super.execute();
            } catch (CallException e) {
                latestException = e;
                Log.d(MailRuAuthSdk.AUTHSDK_TAG, getNameForLog() +
                        " is trying to handle exception: " + e.toString());
                if (handleException(e)) {
                    Log.d(MailRuAuthSdk.AUTHSDK_TAG, getNameForLog() +
                            " handled exception: " + e.toString());
                    incrementTryCount();
                } else {
                    throw e;
                }
            }
        }
        throw CallException.retryLimitExceeded(latestException);
    }

    private void delayBeforeRetry() {
        if (shouldDelayBeforeRetry() && mTryCount > 0) {
            try {
                Thread.sleep(mTryCount * TimeUnit.SECONDS.toMillis(1));
            } catch (InterruptedException e) {
                /* ignore */
            }
        }
    }

    protected abstract boolean shouldDelayBeforeRetry();

    protected abstract String getNameForLog();

    protected abstract boolean handleException(CallException e) throws CallException;

    private void incrementTryCount() {
        mTryCount++;
    }

    private boolean canRetry() {
        return mTryCount <= mMaxTryCnt;
    }
}
