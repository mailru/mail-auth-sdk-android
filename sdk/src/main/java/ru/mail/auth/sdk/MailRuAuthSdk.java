package ru.mail.auth.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;

import androidx.annotation.IntDef;
import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class MailRuAuthSdk {
    public static final int STATUS_OK = Activity.RESULT_OK;
    public static final int STATUS_ERROR = 1; // some network error
    public static final int STATUS_CANCELLED = Activity.RESULT_CANCELED; // user press go back
    public static final int STATUS_ACCESS_DENIED = 2; // user revoke access
    public static final String AUTHSDK_TAG = "MailRuAuthSDK";

    private static volatile MailRuAuthSdk sInstance;
    private OAuthParams mOAuthParams;
    private int mRequestCodeOffset = 4000;
    private final Context mContext;
    private volatile boolean mDebugEnabled;

    private MailRuAuthSdk(Context context) {
        mContext = context;
    }

    @UiThread
    public static void initialize(Context context) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalStateException("This method should be called from main thread");
        }
        if (sInstance == null) {
            sInstance = new MailRuAuthSdk(context.getApplicationContext());
        }
    }

    public void setRequestCodeOffest(int offset) {
        mRequestCodeOffset = offset;
    }

    int getRequestCodeOffset() {
        return mRequestCodeOffset;
    }

    public int getLoginRequestCode() {
        return RequestCodeOffset.LOGIN.toRequestCode();
    }

    public static MailRuAuthSdk getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("You must call initialize() first");
        }
        return sInstance;
    }

    public synchronized OAuthParams getOAuthParams() {
        if (mOAuthParams == null) {
            mOAuthParams = new OAuthParams(mContext);
        }
        return mOAuthParams;
    }

    public void setDebugEnabled(boolean enabled) {
        mDebugEnabled = enabled;
    }

    public boolean isDebugEnabled() {
        return mDebugEnabled;
    }

    public synchronized void setOAuthParams(OAuthParams params) {
        mOAuthParams = params;
    }

    Context getContext() {
        return mContext;
    }

    public void startLogin(Activity activity) {
        MailRuSdkServiceActivity.login(activity, RequestCodeOffset.LOGIN);
    }

    public void startLogin(Fragment fragment) {
        MailRuSdkServiceActivity.login(fragment, RequestCodeOffset.LOGIN);
    }

    public boolean handleActivityResult(int requestCode, int resultCode, Intent data, MailRuCallback<AuthResult, AuthError> callback) {
        if (requestCode == getLoginRequestCode()) {
            String code = data != null ? data.getStringExtra(MailRuSdkServiceActivity.AUTH_RESULT_EXTRA) : "";
            String codeVerifier = data != null ? data.getStringExtra(MailRuSdkServiceActivity.AUTH_RESULT_EXTRA_CODE_VERIFIER) : null;
            if (resultCode == MailRuAuthSdk.STATUS_OK) {
                callback.onResult(new AuthResult(code, codeVerifier));
            } else {
                callback.onError(AuthError.fromCode(resultCode));
            }
            return true;
        }
        return false;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STATUS_OK, STATUS_ERROR, STATUS_ACCESS_DENIED, STATUS_CANCELLED})
    public @interface Status {}
}
