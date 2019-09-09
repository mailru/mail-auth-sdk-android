package ru.mail.auth.sdk;

public interface MailRuCallback<Result, Error> {

    void onResult(Result result);

    void onError(Error error);
}
