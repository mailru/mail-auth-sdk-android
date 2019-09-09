package ru.mail.auth.sdk;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;

public class RedirectReceiverActivity extends Activity{

    public static final String EXTRA_URI = "extra_uri";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(createResponseHandlingIntent());
        finish();
    }

    private Intent createResponseHandlingIntent() {
        Uri uri = getIntent().getData();
        Intent intent = new Intent(this, MailRuSdkServiceActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(EXTRA_URI, uri);
        return intent;
    }
}
