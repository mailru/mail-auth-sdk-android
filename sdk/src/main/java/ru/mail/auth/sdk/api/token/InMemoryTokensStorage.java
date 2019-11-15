package ru.mail.auth.sdk.api.token;

import androidx.annotation.Nullable;
import ru.mail.auth.sdk.api.token.OAuthTokensStorage;

public class InMemoryTokensStorage implements OAuthTokensStorage {

    private String mAccessToken;
    private String mRefreshToken;

    public InMemoryTokensStorage(String accessToken, String refreshToken) {
        mAccessToken = accessToken;
        mRefreshToken = refreshToken;
    }

    @Nullable
    @Override
    public String getAccessToken() {
        return mAccessToken;
    }

    @Nullable
    @Override
    public String getRefreshToken() {
        return mRefreshToken;
    }

    @Override
    public void saveAccessToken(String token) {
        mAccessToken = token;
    }

    @Override
    public void saveRefreshToken(String token) {
        mRefreshToken = token;
    }
}
