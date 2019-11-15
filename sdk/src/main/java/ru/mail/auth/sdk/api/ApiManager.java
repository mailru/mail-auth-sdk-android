package ru.mail.auth.sdk.api;

import ru.mail.auth.sdk.MRExecutors;
import ru.mail.auth.sdk.MailRuAuthSdk;
import ru.mail.auth.sdk.MailRuCallback;
import ru.mail.auth.sdk.OAuthParams;
import ru.mail.auth.sdk.api.token.AccessTokenRequest;
import ru.mail.auth.sdk.api.token.ExpiredTokenUpdater;
import ru.mail.auth.sdk.api.token.InMemoryTokensStorage;
import ru.mail.auth.sdk.api.token.OAuthTokensResult;
import ru.mail.auth.sdk.api.token.OAuthTokensStorage;
import ru.mail.auth.sdk.api.token.RefreshAccessTokenRequest;
import ru.mail.auth.sdk.api.user.UserInfoRequest;
import ru.mail.auth.sdk.api.user.UserInfoResult;
import ru.mail.auth.sdk.call.CallBuilder;
import ru.mail.auth.sdk.call.CallException;
import ru.mail.auth.sdk.call.MethodCall;

public class ApiManager {

    public static void getAccessToken(OAuthParams oAuthParams, String authCode, String codeVerifier,
                                      final MailRuCallback<OAuthTokensResult, Integer> callback) {
        AccessTokenRequest command = new AccessTokenRequest(oAuthParams, authCode, codeVerifier);
        MethodCall<OAuthTokensResult> call = CallBuilder.from(command)
                .withNetworkRetry()
                .build();
        executeCall(call, callback);
    }

    public static void getAccessToken(String authCode, String codeVerifier,
                                      final MailRuCallback<OAuthTokensResult, Integer> callback) {
        OAuthParams authParams = MailRuAuthSdk.getInstance().getOAuthParams();
        getAccessToken(authParams, authCode, codeVerifier, callback);
    }

    public static void getUserInfo(OAuthTokensStorage tokensStorage,
                                   final MailRuCallback<UserInfoResult, Integer> callback) {
        getUserInfo(MailRuAuthSdk.getInstance().getOAuthParams(),
                tokensStorage, callback);
    }

    public static void getUserInfo(OAuthParams oAuthParams,
                                   OAuthTokensStorage tokensStorage,
                                   final MailRuCallback<UserInfoResult, Integer> callback) {
        UserInfoRequest request = new UserInfoRequest(tokensStorage);
        MethodCall<UserInfoResult> call = CallBuilder.from(request)
                .withNetworkRetry()
                .withAccessTokenRefresh(new ExpiredTokenUpdater(tokensStorage, oAuthParams))
                .build();
        executeCall(call, callback);
    }

    public static MethodCall<OAuthTokensResult> getRefreshAccessTokenCall(String clientId,
                                                                          String refreshToken) {
        InMemoryTokensStorage storage = new InMemoryTokensStorage("", refreshToken);
        RefreshAccessTokenRequest request = new RefreshAccessTokenRequest(clientId, storage);
        return CallBuilder.from(request)
                .withNetworkRetry()
                .build();
    }

    private static <R> void executeCall(final MethodCall<R> call,
                                        final MailRuCallback<R, Integer> callback) {
        MRExecutors.networkIO().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final R response = call.execute();
                    MRExecutors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            callback.onResult(response);
                        }
                    });
                } catch (final CallException e) {
                    MRExecutors.mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(e.getErrorCode());
                        }
                    });
                }
            }
        });
    }
}
