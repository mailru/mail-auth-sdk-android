package ru.mail.auth.sdk;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Keep;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import ru.mail.auth.sdk.pub.R;


public class OAuthParams implements Serializable {
    private static final long serialVersionUID = -2733518595880403901L;
    private static final String MAIL_APP_BUNDLE_OAUTH_EXTRA = "ru.mail.auth.sdk.extra_params";
    private final String mAuthUrl;
    private final String mRedirectUrl;
    private final String mScope;
    private final String mClientId;
    private final Map<String, String> additionalParams = new HashMap<>();
    private final boolean mIsUseCodeChallenge;

    OAuthParams(Context context) {
        this(context.getString(R.string.mailru_oauth_url),
                context.getString(R.string.mailru_oauth_redirect_url),
                context.getString(R.string.mailru_oauth_scope),
                context.getString(R.string.mailru_oauth_client_id),
                context.getResources().getBoolean(R.bool.mailru_code_challenge_enabled));
    }

    private OAuthParams(String authUrl,
                        String redirectUrl,
                        String scope,
                        String clientId,
                        boolean isUseCodeChallenge) {
        mRedirectUrl = redirectUrl;
        mScope = scope;
        mClientId = clientId;
        mAuthUrl = authUrl;
        mIsUseCodeChallenge = isUseCodeChallenge;
    }

    public static OAuthParams newInstance(String authUrl,
                                          String redirectUrl,
                                          String scope,
                                          String clientId,
                                          boolean isUseCodeChallenge) {
        return new OAuthParams(authUrl, redirectUrl, scope, clientId, isUseCodeChallenge);
    }

    public void setAdditionalParams(Map<String, String> additionalParams) {
        this.additionalParams.clear();
        this.additionalParams.putAll(additionalParams);
    }

    @Nullable
    public static OAuthParams getFromBundle(@Nullable Bundle bundle) {
        if (bundle != null) {
            Serializable serializable = bundle.getSerializable(MAIL_APP_BUNDLE_OAUTH_EXTRA);
            if (serializable instanceof OAuthParams) {
                return (OAuthParams) serializable;
            }
        }
        return null;
    }

    void writeToBundle(Bundle dst) {
        dst.putSerializable(MAIL_APP_BUNDLE_OAUTH_EXTRA, this);
    }

    public String getAuthUrl() {
        return mAuthUrl;
    }

    public String getScope() {
        return mScope;
    }

    public String getClientId() {
        return mClientId;
    }

    public String getRedirectUrl() {
        return mRedirectUrl;
    }

    public boolean isUseCodeChallenge() {
        return mIsUseCodeChallenge;
    }

    public Map<String, String> getAdditionalParams() {
        return new HashMap<>(additionalParams);
    }
}
