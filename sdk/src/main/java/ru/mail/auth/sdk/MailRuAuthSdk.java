package ru.mail.auth.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.fragment.app.Fragment;
import ru.mail.auth.sdk.api.ApiManager;
import ru.mail.auth.sdk.api.CommonErrorCodes;
import ru.mail.auth.sdk.api.OAuthRequestErrorCodes;
import ru.mail.auth.sdk.api.token.InMemoryTokensStorage;
import ru.mail.auth.sdk.api.token.OAuthTokensResult;
import ru.mail.auth.sdk.api.user.UserInfoResult;

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

    public void setRequestCodeOffset(int offset) {
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

    public void requestOAuthTokens(AuthResult authResult,
                                   MailRuCallback<OAuthTokensResult, Integer> callback) {
        ApiManager.getAccessToken(authResult.getAuthCode(), authResult.getCodeVerifier(), callback);
    }

    public void requestUserInfo(OAuthTokensResult result,
                                MailRuCallback<UserInfoResult, Integer> callback) {
        ApiManager.getUserInfo(new InMemoryTokensStorage(result.getAccessToken(), result.getRefreshToken()), callback);
    }

    public boolean handleAuthResult(int requestCode,
                                    @Status int resultCode,
                                    Intent data,
                                    final MailRuCallback<OAuthTokensResult, Integer> callback) {
        return handleActivityResult(requestCode, resultCode, data, new MailRuCallback<AuthResult, AuthError>() {
            @Override
            public void onResult(@NonNull AuthResult authResult) {
                requestOAuthTokens(authResult, new MailRuCallback<OAuthTokensResult, Integer>() {
                    @Override
                    public void onResult(@NonNull OAuthTokensResult oAuthTokensResult) {
                        callback.onResult(oAuthTokensResult);
                    }

                    @Override
                    public void onError(@NonNull Integer integer) {
                        callback.onError(integer);
                    }
                });
            }

            @Override
            public void onError(@NonNull AuthError authError) {
                callback.onError(authError == AuthError.NETWORK_ERROR ?
                        CommonErrorCodes.NETWORK_ERROR : CommonErrorCodes.REQUEST_CANCELLED);
            }
        });
    }

    public boolean handleActivityResult(int requestCode,
                                        @Status int resultCode,
                                        Intent data,
                                        MailRuCallback<AuthResult, AuthError> callback) {
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
    public @interface Status {
    }
}
