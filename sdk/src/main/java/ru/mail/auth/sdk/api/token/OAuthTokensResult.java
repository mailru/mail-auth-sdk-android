package ru.mail.auth.sdk.api.token;

public class OAuthTokensResult {
    private final String mAccessToken;
    private final String mRefreshToken;

    public OAuthTokensResult(String accessToken, String refreshToken) {
        mAccessToken = accessToken;
        mRefreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return mRefreshToken;
    }

    public String getAccessToken() {
        return mAccessToken;
    }
}
