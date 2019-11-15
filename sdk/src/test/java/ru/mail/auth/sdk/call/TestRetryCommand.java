package ru.mail.auth.sdk.call;

import org.junit.Assert;
import org.junit.Test;

import ru.mail.auth.sdk.api.CommonErrorCodes;
import ru.mail.auth.sdk.api.token.InMemoryTokensStorage;
import ru.mail.auth.sdk.api.OAuthRequestErrorCodes;
import ru.mail.auth.sdk.api.token.OAuthTokensStorage;
import ru.mail.auth.sdk.api.token.TokenExpiredListener;

import static org.junit.Assert.assertEquals;

public class TestRetryCommand {
    private static final String TEST_OK_RESULT = "test";
    private static final String TEST_VALID_TOKEN = "valid_token";

    @Test
    public void runCommandOk() throws CallException {
        MethodCall<String> call = CallBuilder.from(new TestCall(0))
                .build();
        assertEquals(TEST_OK_RESULT, call.execute());
    }

    @Test
    public void rerunFailedCommandTestOk() throws CallException {
        MethodCall<String> call = CallBuilder.from(new TestCall(2))
                .withNetworkRetry(2)
                .build();
        assertEquals(TEST_OK_RESULT, call.execute());
    }

    @Test
    public void rerunFailedCommandNetworkError() {
        try {
            CallBuilder.from(new TestCall(2))
                    .withNetworkRetry(1)
                    .build()
                    .execute();
        } catch (CallException e) {
            Assert.assertEquals("Wrong code", CommonErrorCodes.RETRY_LIMIT_EXCEEDED, e.getErrorCode());
            assertEquals("Wrong code", CommonErrorCodes.NETWORK_ERROR, e.getOriginalException().getErrorCode());
        }
    }

    @Test
    public void rerunCommandWithAuth() throws CallException {
        InMemoryTokensStorage tokensStorage = new InMemoryTokensStorage("123", "123");
        String result = CallBuilder.from(new TestAuthCall(2, tokensStorage))
                .withNetworkRetry(1)
                .withAccessTokenRefresh(new RefreshTokenListener(tokensStorage, TEST_VALID_TOKEN))
                .build()
                .execute();
        assertEquals(TEST_OK_RESULT, result);
    }

    @Test
    public void rerunCommandWithAuthFailed() {
        InMemoryTokensStorage tokensStorage = new InMemoryTokensStorage("321", "123");
        try {
            CallBuilder.from(new TestAuthCall(1, tokensStorage))
                    .withNetworkRetry(2)
                    .withAccessTokenRefresh(new RefreshTokenListener(tokensStorage, "bad"))
                    .build()
                    .execute();
        } catch (CallException e) {
            Assert.assertEquals("wrong message code", OAuthRequestErrorCodes.RETRY_LIMIT_EXCEEDED, e.getErrorCode());
            assertEquals("wrong message code", OAuthRequestErrorCodes.INVALID_TOKEN, e.getOriginalException().getErrorCode());
        }
    }

    private static class RefreshTokenListener implements TokenExpiredListener {

        private InMemoryTokensStorage mTokensStorage;
        private String mNewToken;

        public RefreshTokenListener(InMemoryTokensStorage tokensStorage, String newToken) {
            mTokensStorage = tokensStorage;
            mNewToken = newToken;
        }

        @Override
        public void onTokenExpired() {
            mTokensStorage.saveAccessToken(mNewToken);
        }
    }

    private static class TestAuthCall extends TestCall {
        private final OAuthTokensStorage mTokensStorage;

        private TestAuthCall(int succeedFromTry, OAuthTokensStorage tokensStorage) {
            super(succeedFromTry);
            mTokensStorage = tokensStorage;
        }

        @Override
        public String execute() throws CallException {
            if (!mTokensStorage.getAccessToken().equals(TEST_VALID_TOKEN)) {
                throw new CallException(OAuthRequestErrorCodes.INVALID_TOKEN);
            }
            return TEST_OK_RESULT;
        }
    }

    private static class TestCall implements MethodCall<String> {
        private int mNumberOfRetries;
        private int mCurrentExecCount = 0;

        private TestCall(int succeedFromTry) {
            mNumberOfRetries = succeedFromTry;
        }

        @Override
        public String execute() throws CallException {
            if (mCurrentExecCount++ < mNumberOfRetries) {
                throw new CallException(CommonErrorCodes.NETWORK_ERROR);
            }
            return TEST_OK_RESULT;
        }
    }
}
