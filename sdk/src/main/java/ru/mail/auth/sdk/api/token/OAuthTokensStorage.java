package ru.mail.auth.sdk.api.token;

import androidx.annotation.Nullable;

public interface OAuthTokensStorage {
    @Nullable
    String getAccessToken();

    @Nullable
    String getRefreshToken();

    void saveAccessToken(String token);

    void saveRefreshToken(String token);
}
