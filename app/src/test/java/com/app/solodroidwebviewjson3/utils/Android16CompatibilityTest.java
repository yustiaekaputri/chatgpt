package com.app.solodroidwebviewjson3.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.view.View;
import android.widget.FrameLayout;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.app.solodroidwebviewjson3.BuildConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.io.InputStream;
import java.io.OutputStream;

/** Regression checks for scoped uploads and the insets required by target SDK 36. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = {35, 36}, application = Application.class)
public class Android16CompatibilityTest {

    @Test
    @Config(sdk = {28, 35, 36})
    public void cameraChooserUsesWritableScopedContentUri() throws Exception {
        Context context = RuntimeEnvironment.getApplication();
        Tools tools = new Tools();
        Intent chooser = new Intent(Intent.ACTION_CHOOSER);
        tools.setupChooserIntent(context, chooser);

        Parcelable[] initialIntents = chooser.getParcelableArrayExtra(Intent.EXTRA_INITIAL_INTENTS);
        assertNotNull(initialIntents);
        Intent camera = (Intent) initialIntents[0];
        Uri uri = tools.getCapturedImageUri();
        assertNotNull(uri);
        assertEquals("content", uri.getScheme());
        assertEquals(BuildConfig.APPLICATION_ID + ".fileprovider", uri.getAuthority());
        assertEquals(uri, camera.getClipData().getItemAt(0).getUri());
        assertTrue((camera.getFlags() & Intent.FLAG_GRANT_WRITE_URI_PERMISSION) != 0);
        assertTrue((camera.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0);

        try (OutputStream stream = context.getContentResolver().openOutputStream(uri)) {
            assertNotNull(stream);
            stream.write(42);
        }
        try (InputStream stream = context.getContentResolver().openInputStream(uri)) {
            assertNotNull(stream);
            assertEquals(42, stream.read());
        }
        // The URI is delivered to the WebView only once.
        assertNull(tools.getCapturedImageUri());
    }

    @Test
    public void insetsProtectContentAndDoNotAccumulateOnRedispatch() {
        try (ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class)) {
            Activity activity = controller.setup().get();
            activity.setContentView(new FrameLayout(activity));
            Tools.setNavigation(activity);
            View content = activity.findViewById(android.R.id.content);
            WindowInsetsCompat insets = new WindowInsetsCompat.Builder()
                    .setInsets(WindowInsetsCompat.Type.systemBars(), Insets.of(1, 24, 2, 30))
                    .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, 200))
                    .build();

            WindowInsetsCompat result = ViewCompat.dispatchApplyWindowInsets(content, insets);
            assertTrue(result.isConsumed());
            assertEquals(1, content.getPaddingLeft());
            assertEquals(24, content.getPaddingTop());
            assertEquals(2, content.getPaddingRight());
            assertEquals(200, content.getPaddingBottom());
            ViewCompat.dispatchApplyWindowInsets(content, insets);
            assertEquals(24, content.getPaddingTop());
            assertEquals(200, content.getPaddingBottom());
        }
    }
}
