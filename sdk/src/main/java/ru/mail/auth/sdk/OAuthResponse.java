package ru.mail.auth.sdk;

import android.net.Uri;
import androidx.annotation.Nullable;
import android.text.TextUtils;

public class OAuthResponse {
    private int mStatus;
    private String mResult;

    private OAuthResponse(int code, String result) {
        mStatus = code;
        mResult = result;
    }

    public static OAuthResponse from(OAuthRequest request, @Nullable Uri redirectResponse) {
        String queryParam;
        if (redirectResponse == null) {
            return new OAuthResponse(MailRuAuthSdk.STATUS_CANCELLED, null);
        } else if ((queryParam = redirectResponse.getQueryParameter("code")) != null
                && TextUtils.equals(request.getState(), redirectResponse.getQueryParameter("state"))) {
            return new OAuthResponse(MailRuAuthSdk.STATUS_OK, queryParam);
        } else if ((queryParam = redirectResponse.getQueryParameter("error")) != null) {
            if (TextUtils.equals(queryParam, "access_denied")) {
                return new OAuthResponse(MailRuAuthSdk.STATUS_ACCESS_DENIED, null);
            } else {
                return new OAuthResponse(MailRuAuthSdk.STATUS_ERROR, queryParam);
            }
        } else {
            return new OAuthResponse(MailRuAuthSdk.STATUS_ERROR, null);
        }
    }

    public int getResultCode() {
        return mStatus;
    }

    public String getResult() {
        return mResult;
    }
}
