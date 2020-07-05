package ru.mail.auth.sdk;

import androidx.annotation.Nullable;

public interface Analytics {
    void onLoginStarted(@Nullable Type type);
    void onLoginSuccess(Type type);
    void onLoginFailed(Type type, String error);

    enum Type {
        WEB,
        APP
    }
}
