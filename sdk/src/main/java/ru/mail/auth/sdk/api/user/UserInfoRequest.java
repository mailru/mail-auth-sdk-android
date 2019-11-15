package ru.mail.auth.sdk.api.user;

import org.json.JSONException;
import org.json.JSONObject;

import ru.mail.auth.sdk.api.ApiCommand;
import ru.mail.auth.sdk.api.ApiMethod;
import ru.mail.auth.sdk.api.ApiQuery;
import ru.mail.auth.sdk.api.BaseAuthResponseProcessor;
import ru.mail.auth.sdk.api.token.OAuthTokensStorage;
import ru.mail.auth.sdk.api.ResponseProcessor;

public class UserInfoRequest extends ApiCommand<UserInfoResult> {
    private OAuthTokensStorage mProvider;

    public UserInfoRequest(OAuthTokensStorage provider) {
        mProvider = provider;
    }

    @Override
    protected ApiQuery getQuery() {
        return new ApiQuery.Builder()
                .withHost(ApiCommand.O2_API_HOST)
                .withMethodName(ApiMethod.USERINFO.getValue())
                .withPostParam("access_token", mProvider.getAccessToken())
                .build();
    }

    @Override
    protected ResponseProcessor<UserInfoResult> getResponseProcessor() {
        return new Processor();
    }

    private static class Processor extends BaseAuthResponseProcessor<UserInfoResult> {

        @Override
        protected UserInfoResult processOkResult(JSONObject jsonObject) throws JSONException {
            String email = jsonObject.optString("email");
            String name = jsonObject.optString("name");
            String image = jsonObject.optString("image");
            String id = jsonObject.getString("id");
            return new UserInfoResult(name, image, email, id);
        }
    }
}
