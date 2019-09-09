package ru.mail.auth.sdk.browser;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

import ru.mail.auth.sdk.OAuthRequest;
import ru.mail.auth.sdk.pub.R;


public class BrowserRequestInitiator {

    // HACK: Using a StringBuilder prevents Jetifier from tempering with our constants.
    @SuppressWarnings("StringBufferReplaceableByString")
    private static final String ACTION_CUSTOM_TABS_TITLE_VISIBILITY = new StringBuilder("android")
            .append(".support.customtabs.extra.TITLE_VISIBILITY").toString();


    public static final List<VersionedBrowserMatcher> WHITE_LIST = Arrays.asList(
            VersionedBrowserMatcher.CHROME_BROWSER,
            VersionedBrowserMatcher.CHROME_CUSTOM_TAB,
            VersionedBrowserMatcher.FIREFOX_CUSTOM_TAB,
            VersionedBrowserMatcher.FIREFOX_BROWSER,
            VersionedBrowserMatcher.SAMSUNG_BROWSER,
            VersionedBrowserMatcher.SAMSUNG_CUSTOM_TAB);

    public void startOAuthFlow(OAuthRequest request, Context context) throws ActivityNotFoundException {
        BrowserDescriptor browserDescriptor = selectBestBrowser(context);
        if (browserDescriptor == null) {
            throw new ActivityNotFoundException("no suitable browser found");
        }
        Intent intent;
        if (browserDescriptor.useCustomTab) {
            CustomTabsIntent.Builder tabBuilder = new CustomTabsIntent.Builder();
            tabBuilder.setToolbarColor(context.getResources().getColor(R.color.mailru_webview_progress_color));
            intent = tabBuilder.build().intent;
            intent.putExtra(ACTION_CUSTOM_TABS_TITLE_VISIBILITY, 0);
        } else {
            intent = new Intent(Intent.ACTION_VIEW);
        }
        intent.setPackage(browserDescriptor.packageName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(request.toUri());
        context.startActivity(intent);
    }

    @Nullable
    private BrowserDescriptor selectBestBrowser(Context context) {
        try {
            List<BrowserDescriptor> allBrowsers = BrowserSelector.getAllBrowsers(context);
            for (BrowserDescriptor descriptor : allBrowsers) {
                for (VersionedBrowserMatcher versionedBrowserMatcher : WHITE_LIST) {
                    if (versionedBrowserMatcher.matches(descriptor)) {
                        return descriptor;
                    }
                }
            }
            return allBrowsers.isEmpty() ? null : allBrowsers.get(0);
        } catch (Exception e) {
            Log.e("BrowserSelector", "Exception in select browser", e);
            return null;
        }
    }
}
