package ru.mail.auth.sdk.api;

import ru.mail.auth.sdk.call.CallException;

public interface ResponseProcessor<Result> {
    Result process(int code, String response) throws CallException;
}
