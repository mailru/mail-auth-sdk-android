package ru.mail.auth.sdk;

import androidx.annotation.Nullable;

public class StubAnalytics implements Analytics {
    @Override
    public void onLoginStarted(@Nullable Type type) {
        /* no op */
    }

    @Override
    public void onLoginSuccess(Type type) {
        /* no op */
    }

    @Override
    public void onLoginFailed(Type type, String error) {
        /* no op */
    }
}
