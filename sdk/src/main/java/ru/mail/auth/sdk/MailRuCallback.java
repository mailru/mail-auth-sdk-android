package ru.mail.auth.sdk;

import androidx.annotation.NonNull;

public interface MailRuCallback<Result, Error> {

    void onResult(@NonNull Result result);

    void onError(@NonNull Error error);
}
