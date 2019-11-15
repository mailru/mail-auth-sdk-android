package ru.mail.auth.sdk.api.user;

public class UserInfoResult {
    private String mName;
    private String mAvatarUrl;
    private String mEmail;
    private String mMailID;

    public UserInfoResult(String name, String avatarUrl, String email, String mailID) {
        mName = name;
        mAvatarUrl = avatarUrl;
        mEmail = email;
        mMailID = mailID;
    }

    public String getName() {
        return mName;
    }

    public String getAvatarUrl() {
        return mAvatarUrl;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getMailID() {
        return mMailID;
    }
}
