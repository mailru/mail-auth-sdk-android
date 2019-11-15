package ru.mail.auth.sdk.call;

public interface MethodCall<R> {
    R execute() throws CallException;
}
