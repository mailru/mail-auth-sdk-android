package ru.mail.auth.sdk;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AuthResult {
    @NonNull
    private final String mAuthCode;

    @Nullable
    private final String mCodeVerifier;

    public AuthResult(@NonNull String code, @Nullable String codeVerifier) {
        mAuthCode = code;
        mCodeVerifier = codeVerifier;
    }

    @NonNull
    public String getAuthCode() {
        return mAuthCode;
    }

    @Nullable
    public String getCodeVerifier() {
        return mCodeVerifier;
    }
}
