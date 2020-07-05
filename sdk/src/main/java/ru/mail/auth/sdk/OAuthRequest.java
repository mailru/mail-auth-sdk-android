package ru.mail.auth.sdk;

import android.net.Uri;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;

import java.nio.charset.Charset;

public class OAuthRequest {
    private static final int CODE_VERIFIER_LEN = 32;
    private static final int STATE_LEN = 32;

    private OAuthParams mOAuthParams;

    private String mState;

    @Nullable
    private String mPreferredLogin;

    private String mCodeVerifier;

    private OAuthRequest(OAuthParams params, String state, String codeVerifier) {
        mOAuthParams = params;
        mState = state;
        mCodeVerifier = codeVerifier;
    }

    public static OAuthRequest from(OAuthParams authParams) {
        return new OAuthRequest(authParams, generateState(), generateCodeVerifier());
    }

    public OAuthRequest withLogin(@Nullable String login) {
        mPreferredLogin = login;
        return this;
    }

    public Uri toUri() {
        Uri parse = Uri.parse(mOAuthParams.getAuthUrl());
        final Uri.Builder builder = parse.buildUpon();
        builder.appendQueryParameter("client_id", mOAuthParams.getClientId());
        builder.appendQueryParameter("scope", mOAuthParams.getScope());
        builder.appendQueryParameter("redirect_uri", mOAuthParams.getRedirectUrl());
        builder.appendQueryParameter("response_type", "code");
        builder.appendQueryParameter("client", "mobile.app");

        if (!TextUtils.isEmpty(mPreferredLogin)) {
            builder.appendQueryParameter("login", mPreferredLogin);
        }

        if (mOAuthParams.isUseCodeChallenge()) {
            builder.appendQueryParameter("code_challenge_method", "S256");
            builder.appendQueryParameter("code_challenge", calculateCodeChallenge(mCodeVerifier));
        }

        builder.appendQueryParameter("state", mState);
        return builder.build();
    }

    private static String generateState() {
        return Utils.toHex(Utils.generateRandomBytes(STATE_LEN));
    }

    public static String generateCodeVerifier() {
        return encodeToSafeUrl(Utils.generateRandomBytes(CODE_VERIFIER_LEN));
    }

    private static String encodeToSafeUrl(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
    }

    public static String calculateCodeChallenge(String codeVerifier) {
        return encodeToSafeUrl(Utils.generateDigest(
                codeVerifier.getBytes(Charset.defaultCharset()),
                Utils.DigestAlgorithm.SHA256));
    }

    public String getState() {
        return mState;
    }

    @Nullable
    public String getCodeVerifier() {
        return mOAuthParams.isUseCodeChallenge() ? mCodeVerifier : null;
    }
}
