package ru.mail.auth.sdk.api.token;

import ru.mail.auth.sdk.OAuthParams;
import ru.mail.auth.sdk.api.ApiManager;
import ru.mail.auth.sdk.call.CallException;
import ru.mail.auth.sdk.call.MethodCall;

public class ExpiredTokenUpdater implements TokenExpiredListener {

    private final OAuthTokensStorage mOAuthTokensStorage;
    private final OAuthParams mOAuthParams;

    public ExpiredTokenUpdater(OAuthTokensStorage oAuthTokensStorage, OAuthParams oAuthParams) {
        mOAuthTokensStorage = oAuthTokensStorage;
        mOAuthParams = oAuthParams;
    }

    @Override
    public void onTokenExpired() throws CallException {
        MethodCall<OAuthTokensResult> call = ApiManager.getRefreshAccessTokenCall(
                mOAuthParams.getClientId(),
                mOAuthTokensStorage.getRefreshToken()
        );
        OAuthTokensResult result = call.execute();
        mOAuthTokensStorage.saveAccessToken(result.getAccessToken());
    }
}
