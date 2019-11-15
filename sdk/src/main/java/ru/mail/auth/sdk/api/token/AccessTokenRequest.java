package ru.mail.auth.sdk.api.token;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.Nullable;
import ru.mail.auth.sdk.OAuthParams;
import ru.mail.auth.sdk.api.ApiCommand;
import ru.mail.auth.sdk.api.ApiQuery;
import ru.mail.auth.sdk.api.BaseAuthResponseProcessor;
import ru.mail.auth.sdk.api.ResponseProcessor;

/**
 *  Performs OAuth 2.0 tokens exchange using authorization code flow.
 *  See https://tools.ietf.org/html/rfc6749 for more details.
 */

public class AccessTokenRequest extends ApiCommand<OAuthTokensResult> {

    private final ApiQuery mApiQuery;

    public AccessTokenRequest(OAuthParams oAuthParams,
                              String authCode,
                              @Nullable String codeVerifier) {
        mApiQuery = new ApiQuery.Builder()
                .withHost(ApiCommand.O2_API_HOST)
                .withMethodName("token")
                .withPostParam("grant_type", GrantType.AUTH_CODE.getValue())
                .withPostParam("client_id", oAuthParams.getClientId())
                .withPostParam("redirect_uri", oAuthParams.getRedirectUrl())
                .withPostParam("code", authCode)
                .withPostParam("code_verifier", codeVerifier)
                .build();
    }

    @Override
    protected ApiQuery getQuery() {
        return mApiQuery;
    }

    @Override
    protected ResponseProcessor<OAuthTokensResult> getResponseProcessor() {
        return new Processor();
    }

    private class Processor extends BaseAuthResponseProcessor<OAuthTokensResult> {
        @Override
        protected OAuthTokensResult processOkResult(JSONObject jsonObject) throws JSONException {
            String accessToken = jsonObject.getString("access_token");
            String refreshToken = jsonObject.getString("refresh_token");
            return new OAuthTokensResult(accessToken, refreshToken);
        }
    }
}
