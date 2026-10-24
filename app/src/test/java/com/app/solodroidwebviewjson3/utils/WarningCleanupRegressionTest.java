package com.app.solodroidwebviewjson3.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.app.solodroidwebviewjson3.R;
import com.app.solodroidwebviewjson3.adapters.AdapterMenu;
import com.app.solodroidwebviewjson3.adapters.AdapterMoreApps;
import com.app.solodroidwebviewjson3.webview.AdvancedWebView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.util.Locale;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {28, 36}, application = Application.class)
public class WarningCleanupRegressionTest {
    @Test
    public void downloadableUrlsDoNotDependOnTheUsersLocale() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertTrue(Tools.isDownloadableFile("https://example.com/FILE.ZIP"));
            assertEquals("file.zip", Tools.getFileName("https://example.com/FILE.ZIP"));
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    public void flattenedRowsStillMatchTheirViewHoldersAndHaveRippleForegrounds() {
        Context context = RuntimeEnvironment.getApplication();
        context.setTheme(R.style.AppTheme);
        LayoutInflater inflater = LayoutInflater.from(context);
        View drawer = inflater.inflate(R.layout.item_drawer, null);
        AdapterMenu.OriginalViewHolder drawerHolder = new AdapterMenu.OriginalViewHolder(drawer);
        assertSame(drawer, drawerHolder.lytParent);
        assertNotNull(drawerHolder.lytItem);
        assertNotNull(drawerHolder.menuName);
        assertNotNull(((FrameLayout) drawer).getForeground());

        View moreApps = inflater.inflate(R.layout.item_more_apps, null);
        AdapterMoreApps.ViewHolder moreAppsHolder = new AdapterMoreApps.ViewHolder(moreApps);
        assertSame(moreApps, moreAppsHolder.lytParent);
        assertNotNull(moreAppsHolder.moreAppsImage);
        assertNotNull(moreAppsHolder.moreAppsTitle);
        assertNotNull(((FrameLayout) moreApps).getForeground());
    }

    @Test
    public void exitDialogRetainsTheAnimationPanelAndLabeledActions() {
        Context context = RuntimeEnvironment.getApplication();
        context.setTheme(R.style.AppTheme);
        View dialog = LayoutInflater.from(context).inflate(R.layout.dialog_exit, null);
        assertTrue(dialog instanceof LinearLayout);
        assertNotNull(dialog.findViewById(R.id.dialog_card_view));
        assertNotNull(dialog.findViewById(R.id.lyt_panel_dialog));
        assertEquals(context.getString(R.string.title_rate_us),
                dialog.findViewById(R.id.btn_rate).getContentDescription());
        assertEquals(context.getString(R.string.title_share),
                dialog.findViewById(R.id.btn_share).getContentDescription());
        // OneSignal resolves this drawable by name, so it must survive resource cleanup.
        int icon = context.getResources().getIdentifier(
                "ic_stat_onesignal_default", "drawable", context.getPackageName());
        assertTrue(icon != 0);
    }

    @Test
    public void webViewRejectsHttpSubresourcesOnHttpsPagesByDefault() {
        try (ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class)) {
            Activity activity = controller.setup().get();
            AdvancedWebView webView = new AdvancedWebView(activity);
            try {
                assertEquals(WebSettings.MIXED_CONTENT_NEVER_ALLOW,
                        webView.getSettings().getMixedContentMode());
            } finally {
                webView.destroy();
            }
        }
    }

    @Test
    public void browserDiscoveryOnlyChoosesASupportedWebHandler() {
        Context context = RuntimeEnvironment.getApplication();
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com"));
        browserIntent.addCategory(Intent.CATEGORY_BROWSABLE);
        Shadows.shadowOf(context.getPackageManager()).addResolveInfoForIntent(
                browserIntent, browserHandler("example.unsupported"));
        assertNull(AdvancedWebView.Browsers.getAlternative(context));
        Shadows.shadowOf(context.getPackageManager()).addResolveInfoForIntent(
                browserIntent, browserHandler("com.android.chrome"));
        assertEquals("com.android.chrome", AdvancedWebView.Browsers.getAlternative(context));
    }

    private static ResolveInfo browserHandler(String packageName) {
        ResolveInfo info = new ResolveInfo();
        info.activityInfo = new ActivityInfo();
        info.activityInfo.packageName = packageName;
        info.activityInfo.name = packageName + ".BrowserActivity";
        info.activityInfo.enabled = true;
        info.activityInfo.applicationInfo = new ApplicationInfo();
        info.activityInfo.applicationInfo.packageName = packageName;
        info.activityInfo.applicationInfo.enabled = true;
        return info;
    }
}
