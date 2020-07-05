package ru.mail.auth.sdk.api;

import org.json.JSONException;
import org.json.JSONObject;

import ru.mail.auth.sdk.call.CallException;

public abstract class BaseAuthResponseProcessor<T> implements ResponseProcessor<T> {
    @Override
    public final T process(int code, String response) throws CallException {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.has("error")) {
                int errorCode = jsonObject.getInt("error_code");
                throw new CallException(errorCode, jsonObject.optString("error_description"));
            } else {
                return processOkResult(jsonObject);
            }
        } catch (JSONException e) {
            throw new CallException(CommonErrorCodes.SERVER_API_ERROR, e.getMessage());
        }
    }

    protected abstract T processOkResult(JSONObject jsonObject) throws JSONException;
}
