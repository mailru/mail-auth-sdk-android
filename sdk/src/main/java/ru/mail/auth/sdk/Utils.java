package ru.mail.auth.sdk;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.os.Bundle;
import android.text.TextUtils;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;

public class Utils {
    private static final String MAIL_APP_PKG = "ru.mail.mailapp";
    private static final String MAIL_APP_SHA_FINGERPRINT = "FD6FC8C7D83723725A81C1AE81E9965E0E91A632855D708C774EDD8879DDE0B1";
    private static final String MAIL_APP_ACTION_LOGIN = "ru.mail.auth.sdk.OAUTH_LOGIN_V2";

    static boolean hasMailApp(Context c) {
        boolean isAppExists = isAppInstalled(c, MAIL_APP_PKG);
        boolean isIntentAvailable = isIntentAvailable(c, MAIL_APP_ACTION_LOGIN);
        boolean isValidFingerprint = isValidFingerprint(c);
        return isAppExists && isIntentAvailable && isValidFingerprint;
    }

    private static boolean isValidFingerprint(Context c) {
        boolean isValid = false;
        String[] certificateFingerprint = getCertificateFingerprint(c, MAIL_APP_PKG, DigestAlgorithm.SHA256);
        for (String s : certificateFingerprint) {
            isValid = TextUtils.equals(s, MAIL_APP_SHA_FINGERPRINT);
        }
        return isValid;
    }

    static Intent getMailAppLoginFlowIntent(String login) {
        Intent intent = new Intent(MAIL_APP_ACTION_LOGIN);
        Bundle bundle = new Bundle();
        MailRuAuthSdk.getInstance().getOAuthParams().writeToBundle(bundle);
        intent.putExtras(bundle);
        intent.putExtra(MailRuSdkServiceActivity.EXTRA_LOGIN, login);
        intent.setPackage(MAIL_APP_PKG);
        return intent;
    }

    private static boolean isAppInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        boolean appInstalled;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            appInstalled = true;
        } catch (Exception e) {
            appInstalled = false;
        }
        return appInstalled;
    }

    private static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        try {
            List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            return list.size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressLint("PackageManagerGetSignatures")
    public static String[] getCertificateFingerprint(Context ctx, String packageName, DigestAlgorithm algo) {
        try {
            if (ctx == null || ctx.getPackageManager() == null)
                return new String[0];
            PackageInfo info = ctx.getPackageManager().getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNATURES);
            String[] result = new String[info.signatures.length];
            int i = 0;
            for (Signature signature : info.signatures) {
                result[i++] = toHex(generateDigest(signature.toByteArray(), algo));
            }
            return result;
        } catch (Exception e) {
            return new String[0];
        }
    }

    public static byte[] generateDigest(byte[] data, DigestAlgorithm algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm.getName());
            md.update(data);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
        }
        return new byte[0];
    }

    public static byte[] generateRandomBytes(int count) {
        byte[] bytes = new byte[count];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }

    public static String toHex(byte[] bytes) {
        BigInteger bi = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bi);
    }

    public enum DigestAlgorithm {
        SHA1("SHA-1"),
        SHA256("SHA-256");

        private String mName;

        DigestAlgorithm(String name) {
            mName = name;
        }

        String getName() {
            return mName;
        }
    }
}
