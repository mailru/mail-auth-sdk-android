package ru.mail.auth.sdk.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import androidx.annotation.Nullable;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewDatabase;
import android.widget.ProgressBar;

import ru.mail.auth.sdk.MailRuSdkServiceActivity;
import ru.mail.auth.sdk.OAuthRequest;
import ru.mail.auth.sdk.MailRuAuthSdk;
import ru.mail.auth.sdk.OAuthParams;
import ru.mail.auth.sdk.OAuthResponse;
import ru.mail.auth.sdk.pub.BuildConfig;
import ru.mail.auth.sdk.pub.R;


public class OAuthWebviewDialog {
    private Context mContext;
    private ProgressBar mProgressBar;
    private WebView mWebView;
    private Dialog mDialog;
    private OAuthParams mOAuthParams;

    private int mResult = MailRuAuthSdk.STATUS_CANCELLED;

    @Nullable
    private String mResultString;

    @Nullable
    private String mUserAgent;

    private OAuthRequest mOAuthRequest;

    public OAuthWebviewDialog(Context context) {
        this(context, MailRuAuthSdk.getInstance().getOAuthParams());
    }

    public OAuthWebviewDialog(Context context, OAuthParams params) {
        mContext = context;
        mOAuthParams = params;
        mOAuthRequest = OAuthRequest.from(mOAuthParams);
    }

    public OAuthParams getOAuthParams() {
        return mOAuthParams;
    }

    public void setPreferredLogin(String login) {
        mOAuthRequest.withLogin(login);
    }

    public void setUserAgent(String userAgent) {
        mUserAgent = userAgent;
    }

    public void show() {
        mDialog = new Dialog(mContext, R.style.OauthDialog);
        View inflate = View.inflate(mContext, R.layout.webview_dialog, null);
        mWebView = (WebView) inflate.findViewById(R.id.webview);
        mWebView.setWebViewClient(new OauthWebviewClient());
        mProgressBar = (ProgressBar) inflate.findViewById(R.id.progress);
        mDialog.setContentView(inflate);
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                sendResultBackToActivity();
            }
        });
        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                setResultAndState(MailRuAuthSdk.STATUS_CANCELLED, null);
            }
        });
        initWebView();
        mDialog.show();
    }

    private void initWebView() {
        WebViewDatabase.getInstance(mContext).clearUsernamePassword();
        WebViewDatabase.getInstance(mContext).clearHttpAuthUsernamePassword();
        WebViewDatabase.getInstance(mContext).clearFormData();

        if (!TextUtils.isEmpty(mUserAgent)) {
            mWebView.getSettings().setUserAgentString(mUserAgent);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(MailRuAuthSdk.getInstance().isDebugEnabled());
        }

        mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        String url = mOAuthRequest.toUri().toString();
        if (MailRuAuthSdk.getInstance().isDebugEnabled()) {
            Log.d(MailRuAuthSdk.AUTHSDK_TAG, "OAuth url: " + url);
        }
        mWebView.loadUrl(url);
    }

    private void sendResultBackToActivity() {
        if (mWebView.getContext() instanceof WebViewAuthFlowListener) {
            ((WebViewAuthFlowListener) mWebView.getContext()).onAuthResult(mResult,
                    MailRuSdkServiceActivity.packResult(mResultString, mOAuthRequest.getCodeVerifier()));
        }
    }

    private class OauthWebviewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mProgressBar.setVisibility(View.INVISIBLE);
            view.setVisibility(View.VISIBLE);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            setResultAndState(MailRuAuthSdk.STATUS_ERROR, description);
            safeDismiss();
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            if (MailRuAuthSdk.getInstance().isDebugEnabled()) {
                handler.proceed();
            } else {
                super.onReceivedSslError(view, handler, error);
            }
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            return super.shouldInterceptRequest(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Uri uri = Uri.parse(url);
            if (needHandleRedirectUrl(uri)) {
                if (MailRuAuthSdk.getInstance().isDebugEnabled()) {
                    Log.d(MailRuAuthSdk.AUTHSDK_TAG, "Handle redirect " + uri);
                }
                processUrl(uri);
                return true;
            }
            return false;
        }

        private void processUrl(Uri uri) {
            OAuthResponse response = OAuthResponse.from(mOAuthRequest, uri);
            setResultAndState(response.getResultCode(), response.getResult());
            safeDismiss();
        }

        private boolean needHandleRedirectUrl(Uri url) {
            Uri redirectURI = Uri.parse(mOAuthParams.getRedirectUrl());
            boolean schemeMatch = TextUtils.equals(redirectURI.getScheme(), url.getScheme());
            boolean authorityMatch = TextUtils.equals(redirectURI.getAuthority(), url.getAuthority());
            boolean pathMatch = redirectURI.getPathSegments().containsAll(url.getPathSegments());
            return schemeMatch && authorityMatch && pathMatch;
        }
    }

    private void safeDismiss() {
        if (mDialog != null) {
            try {
                mDialog.dismiss();
            } catch (Exception e) {
                /* safe dismiss */
            }
        }
    }

    private void setResultAndState(int resultCode, String result) {
        mResultString = result;
        mResult = resultCode;
    }

    public interface WebViewAuthFlowListener {
        void onAuthResult(int statusCode, @Nullable Intent data);
    }
}
