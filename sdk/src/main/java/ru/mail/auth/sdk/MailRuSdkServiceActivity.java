package ru.mail.auth.sdk;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import androidx.fragment.app.Fragment;
import ru.mail.auth.sdk.browser.BrowserRequestInitiator;
import ru.mail.auth.sdk.ui.OAuthWebviewDialog;

/**
 * Middleman UI-less activity to handle interactions and set results
 * between SDK clients and OAuth flow providers (webview, mail app)
 * <p>
 */

public class MailRuSdkServiceActivity extends Activity implements OAuthWebviewDialog.WebViewAuthFlowListener {
    static final String ACTION_LOGIN = "ru.mail.auth.sdk.login";
    public static final String AUTH_RESULT_EXTRA = "ru.mail.auth.sdk.EXTRA_RESULT";
    public static final String AUTH_RESULT_EXTRA_CODE_VERIFIER = "ru.mail.auth.sdk.EXTRA_RESULT_CODE_VERIFIER";
    public static final String EXTRA_LOGIN = "ru.mail.auth.sdk.EXTRA_LOGIN";
    public static final String AUTH_STARTED = "auth_started";
    public static final String EXTRA_AUTH_TYPE = "ru.mail.auth.sdk.EXTRA_AUTH_TYPE";

    private BrowserRequestInitiator mBrowserRequestInitiator = new BrowserRequestInitiator();
    private OAuthRequest mBrowserOAuthRequest;
    private boolean mBrowserAuthStarted;
    private Analytics.Type mAuthType = Analytics.Type.WEB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mBrowserAuthStarted = savedInstanceState.getBoolean(AUTH_STARTED);
            mAuthType = (Analytics.Type) savedInstanceState.getSerializable(EXTRA_AUTH_TYPE);
        } else {
            if (TextUtils.equals(getIntent().getAction(), ACTION_LOGIN)) {
                if (Utils.hasMailApp(getApplicationContext())) {
                    MailRuAuthSdk.getInstance().getAnalytics().onLoginStarted(mAuthType = Analytics.Type.APP);
                    startActivityForResult(Utils.getMailAppLoginFlowIntent(getIntent().getStringExtra(EXTRA_LOGIN)),
                            RequestCodeOffset.LOGIN.toRequestCode());
                } else {
                    MailRuAuthSdk.getInstance().getAnalytics().onLoginStarted(mAuthType = Analytics.Type.WEB);
                    if (shouldUseExternalBrowser()) {
                        createBrowserRequest();
                    } else {
                        showOAuthDialog();
                    }
                }
            }
        }
    }

    private boolean shouldUseExternalBrowser() {
        MailRuAuthSdk sdk = MailRuAuthSdk.getInstance();
        String redirectUrl = sdk.getOAuthParams().getRedirectUrl();
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT // only from KitKat browser flow is stable.
                && !redirectUrl.startsWith("http"); // only custom scheme redirects i.e. "ru.mail://redirect" are supported.
    }

    private void showOAuthDialog() {
        new OAuthWebviewDialog(this).show();
    }

    private void createBrowserRequest() {
        mBrowserOAuthRequest = OAuthRequest.from(MailRuAuthSdk.getInstance().getOAuthParams());
    }

    static void login(Activity activity, RequestCodeOffset code, String login) {
        activity.startActivityForResult(getLoginIntent(activity, login), code.toRequestCode());
    }

    static void login(Activity activity, RequestCodeOffset code) {
        login(activity, code, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBrowserOAuthRequest != null) {
            if (!mBrowserAuthStarted) {
                mBrowserAuthStarted = true;
                try {
                    mBrowserRequestInitiator.startOAuthFlow(mBrowserOAuthRequest, this);
                } catch (ActivityNotFoundException e) {
                    /* no suitable browser found... fallback to Webview */
                    mBrowserAuthStarted = false;
                    mBrowserOAuthRequest = null;
                    showOAuthDialog();
                }
            } else {
                Intent intent = getIntent();
                if (intent != null) {
                    Uri uri = intent.getParcelableExtra(RedirectReceiverActivity.EXTRA_URI);
                    OAuthResponse response = OAuthResponse.from(mBrowserOAuthRequest, uri);
                    onAuthResult(response.getResultCode(), packResult(response.getResult(), mBrowserOAuthRequest.getCodeVerifier()));
                }
            }
        }
    }


    static void login(Fragment fragment, RequestCodeOffset code) {
        login(fragment, code, null);
    }

    static void login(Fragment fragment, RequestCodeOffset code, String email) {
        fragment.startActivityForResult(getLoginIntent(fragment.getContext(), email), code.toRequestCode());
    }

    @NonNull
    private static Intent getLoginIntent(Context context, @Nullable String email) {
        Intent intent = new Intent(context, MailRuSdkServiceActivity.class);
        intent.putExtra(EXTRA_LOGIN, email);
        intent.setAction(ACTION_LOGIN);
        return intent;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Nullable
    public static Intent packResult(@Nullable String code, @Nullable String codeVerifier) {
        Intent intent = null;
        if (code != null) {
            intent = new Intent();
            intent.putExtra(AUTH_RESULT_EXTRA, code);
            intent.putExtra(AUTH_RESULT_EXTRA_CODE_VERIFIER, codeVerifier);
        }
        return intent;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(AUTH_STARTED, mBrowserAuthStarted);
        outState.putSerializable(EXTRA_AUTH_TYPE, mAuthType);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            data = new Intent();
        }
        data.putExtra(EXTRA_AUTH_TYPE, mAuthType);
        setResult(resultCode, data);
        finish();
    }

    @Override
    public void onAuthResult(@MailRuAuthSdk.Status int statusCode, @Nullable Intent result) {
        onActivityResult(RequestCodeOffset.LOGIN.toRequestCode(), statusCode, result);
    }
}
