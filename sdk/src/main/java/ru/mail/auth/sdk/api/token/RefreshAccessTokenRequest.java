package ru.mail.auth.sdk.api.token;

import org.json.JSONException;
import org.json.JSONObject;

import ru.mail.auth.sdk.api.ApiCommand;
import ru.mail.auth.sdk.api.ApiMethod;
import ru.mail.auth.sdk.api.ApiQuery;
import ru.mail.auth.sdk.api.BaseAuthResponseProcessor;
import ru.mail.auth.sdk.api.ResponseProcessor;

/**
 * Performs OAuth 2.0 Access token refresh request.
 */

public class RefreshAccessTokenRequest extends ApiCommand<OAuthTokensResult> {
    private String mClientId;
    private OAuthTokensStorage mTokensStorage;

    public RefreshAccessTokenRequest(String clientId, OAuthTokensStorage tokensStorage) {
        mClientId = clientId;
        mTokensStorage = tokensStorage;
    }

    @Override
    protected ApiQuery getQuery() {
        return new ApiQuery.Builder()
                .withHost(ApiCommand.O2_API_HOST)
                .withMethodName(ApiMethod.TOKEN.getValue())
                .withPostParam("grant_type", GrantType.REFRESH_TOKEN.getValue())
                .withPostParam("client_id", mClientId)
                .withPostParam("refresh_token", mTokensStorage.getRefreshToken())
                .build();
    }

    @Override
    protected ResponseProcessor<OAuthTokensResult> getResponseProcessor() {
        return new Processor();
    }

    private class Processor extends BaseAuthResponseProcessor<OAuthTokensResult> {
        @Override
        protected OAuthTokensResult processOkResult(JSONObject jsonObject) throws JSONException {
            String accessToken = jsonObject.getString("access_token");
            return new OAuthTokensResult(accessToken, mTokensStorage.getRefreshToken());
        }
    }
}
