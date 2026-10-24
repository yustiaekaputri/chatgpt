package com.app.solodroidwebviewjson3.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.MailTo;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.app.solodroidwebviewjson3.BuildConfig;
import com.app.solodroidwebviewjson3.R;
import com.app.solodroidwebviewjson3.fragments.FragmentWebView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** @noinspection deprecation*/
public class Tools {

    private static final String TAG = "utils";
    private Uri mCapturedImageUri;
    private static final int LOCATION_SETTINGS_PROMPT_DURATION = 10000;
    public static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 102;
    public static final int REQUEST_PERMISSION_ACCESS_LOCATION = 103;
    public static final String[] DOWNLOAD_FILE_TYPES = {".*zip$", ".*rar$", ".*pdf$", ".*doc$", ".*xls$", ".*mp3$", ".*wma$", ".*ogg$", ".*m4a$", ".*wav$", ".*avi$", ".*mov$", ".*mp4$", ".*mpg$", ".*3gp$", ".*drive.google.com.*download.*", ".*dropbox.com/s/.*"};
    public static final String[] LINKS_OPENED_IN_EXTERNAL_BROWSER = {"target=blank", "target=external", "play.google.com/store", "youtube.com/watch", "facebook.com/sharer", "twitter.com/share", "plus.google.com/share"};
    public static final String[] LINKS_OPENED_IN_INTERNAL_WEBVIEW = {"target=webview", "target=internal"};

    public Tools() {

    }

    public static void getTheme(Context context) {
        SharedPref sharedPref = new SharedPref(context);
        if (sharedPref.getIsDarkTheme()) {
            context.setTheme(R.style.AppDarkTheme);
        } else {
            context.setTheme(R.style.AppTheme);
        }
    }

    public static void setupToolbar(AppCompatActivity activity, Toolbar toolbar, String title, boolean backButton) {
        activity.setSupportActionBar(toolbar);
        final ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            if (backButton) {
                activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                activity.getSupportActionBar().setHomeButtonEnabled(true);
            }
            activity.getSupportActionBar().setTitle(title);
        }
        SharedPref sharedPref = new SharedPref(activity);
        if (sharedPref.getIsDarkTheme()) {
            toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.color_dark_toolbar));
            toolbar.getContext().setTheme(com.google.android.material.R.style.Base_ThemeOverlay_AppCompat_Dark_ActionBar);
        } else {
            toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.color_white));
            toolbar.getContext().setTheme(com.google.android.material.R.style.ThemeOverlay_AppCompat_Light);
        }
    }

    public static void setNavigation(Activity activity) {
        SharedPref sharedPref = new SharedPref(activity);
        if (sharedPref.getIsDarkTheme()) {
            Tools.darkNavigation(activity);
        } else {
            Tools.lightNavigation(activity);
        }
        setLayoutDirection(activity);
        applySystemBarInsets(activity);
    }

    private static void applySystemBarInsets(Activity activity) {
        if (Build.VERSION.SDK_INT < 35) {
            return;
        }
        // Android 16 no longer permits opting out of edge-to-edge for target 36.
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
        View content = activity.findViewById(android.R.id.content);
        final int left = content.getPaddingLeft();
        final int top = content.getPaddingTop();
        final int right = content.getPaddingRight();
        final int bottom = content.getPaddingBottom();
        SharedPref sharedPref = new SharedPref(activity);
        content.setBackgroundColor(ContextCompat.getColor(activity, sharedPref.getIsDarkTheme()
                ? R.color.color_dark_toolbar : R.color.color_white));
        ViewCompat.setOnApplyWindowInsetsListener(content, (view, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.ime());
            view.setPadding(left + insets.left, top + insets.top,
                    right + insets.right, bottom + insets.bottom);
            // Consume here to avoid applying legacy fitsSystemWindows padding twice.
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(content);
    }

    private static void setSystemBarsVisible(Activity activity, boolean visible) {
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(
                activity.getWindow(), activity.getWindow().getDecorView());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        if (visible) {
            controller.show(WindowInsetsCompat.Type.systemBars());
            boolean lightTheme = !new SharedPref(activity).getIsDarkTheme();
            controller.setAppearanceLightStatusBars(lightTheme);
            controller.setAppearanceLightNavigationBars(lightTheme);
        } else {
            controller.hide(WindowInsetsCompat.Type.systemBars());
        }
        ViewCompat.requestApplyInsets(activity.findViewById(android.R.id.content));
    }

    public static void setLayoutDirection(Activity activity) {
        if (Constant.rtlMode) {
            activity.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }

    public static void darkNavigation(Activity activity) {
        if (Build.VERSION.SDK_INT >= 35) {
            WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(
                    activity.getWindow(), activity.getWindow().getDecorView());
            boolean lightTheme = !new SharedPref(activity).getIsDarkTheme();
            controller.setAppearanceLightStatusBars(lightTheme);
            controller.setAppearanceLightNavigationBars(lightTheme);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.getWindow().setNavigationBarColor(ContextCompat.getColor(activity, R.color.color_dark_status_bar));
            activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.color_dark_toolbar));
            activity.getWindow().getDecorView().setSystemUiVisibility(0);
        }
    }

    public static void lightNavigation(Activity activity) {
        if (Build.VERSION.SDK_INT >= 35) {
            WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(
                    activity.getWindow(), activity.getWindow().getDecorView());
            boolean lightTheme = !new SharedPref(activity).getIsDarkTheme();
            controller.setAppearanceLightStatusBars(lightTheme);
            controller.setAppearanceLightNavigationBars(lightTheme);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.getWindow().setNavigationBarColor(ContextCompat.getColor(activity, R.color.color_white));
        }
    }

    public static boolean isDownloadableFile(String url) {
        url = url.toLowerCase(Locale.ROOT);
        for (String type : DOWNLOAD_FILE_TYPES) {
            if (url.matches(type)) return true;
        }
        return false;
    }

    public static String getFileName(String url) {
        int idx = url.indexOf("?");
        if (idx > -1) {
            url = url.substring(0, idx);
        }
        url = url.toLowerCase(Locale.ROOT);
        idx = url.lastIndexOf("/");
        if (idx > -1) {
            return url.substring(idx + 1);
        } else {
            return Long.toString(System.currentTimeMillis());
        }
    }

    public static void downloadFile(@NonNull Context context, @NonNull String url, @NonNull String fileName) {
        try {
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri downloadUri = Uri.parse(url);
            DownloadManager.Request request = new DownloadManager.Request(downloadUri);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle(fileName)
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                    .allowScanningByMediaScanner();
            downloadManager.enqueue(request);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public void setupChooserIntent(Context context, Intent chooserIntent) {
        try {
            Intent captureIntent = createImageCaptureIntent(context);
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{captureIntent});
        } catch (IOException e) {
            // The system file picker can still be used if a camera file cannot be created.
            Log.w(TAG, "Unable to create camera upload file", e);
        }
    }

    public Uri getCapturedImageUri() {
        Uri uri = null;
        if (mCapturedImageUri != null) {
            uri = mCapturedImageUri;
            mCapturedImageUri = null;
        }
        return uri;
    }

    public Uri[] getCapturedImageUris() {
        Uri[] uris = null;
        if (mCapturedImageUri != null) {
            uris = new Uri[]{mCapturedImageUri};
            mCapturedImageUri = null;
        }
        return uris;
    }

    private Intent createImageCaptureIntent(Context context) throws IOException {
        File cameraDir = new File(context.getCacheDir(), "camera");
        if (!cameraDir.exists() && !cameraDir.mkdirs()) {
            throw new IOException("Unable to create camera cache directory");
        }
        File image = File.createTempFile("webview_", ".jpg", cameraDir);
        mCapturedImageUri = FileProvider.getUriForFile(context,
                BuildConfig.APPLICATION_ID + ".fileprovider", image);
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageUri);
        captureIntent.setClipData(ClipData.newRawUri("Camera upload", mCapturedImageUri));
        captureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        return captureIntent;
    }

    public static boolean startIntentActivity(Context context, String url) {
        if (url != null && url.startsWith("mailto:")) {
            MailTo mailTo = MailTo.parse(url);
            Tools.startEmailActivity(context, mailTo.getTo(), mailTo.getSubject(), mailTo.getBody());
            return true;
        } else if (url != null && url.startsWith("tel:")) {
            Tools.startCallActivity(context, url);
            return true;
        } else if (url != null && url.startsWith("sms:")) {
            Tools.startSmsActivity(context, url);
            return true;
        } else if (url != null && url.startsWith("geo:")) {
            Tools.startMapSearchActivity(context, url);
            return true;
        } else if (url != null && url.startsWith("fb://")) {
            Tools.startWebActivity(context, url);
            return true;
        } else if (url != null && url.startsWith("twitter://")) {
            Tools.startWebActivity(context, url);
            return true;
        } else if (url != null && url.startsWith("whatsapp://")) {
            Tools.startWebActivity(context, url);
            return true;
        } else {
            return false;
        }
    }

    public static void startWebActivity(Context context, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    public static void startEmailActivity(Context context, String email, String subject, String text) {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("mailto:");
            builder.append(email);
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(builder.toString()));
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, text);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void startCallActivity(Context context, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void startSmsActivity(Context context, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void startMapSearchActivity(Context context, String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void startIntentChooserActivity(Context context, String url) {
        try {
            String[] results = url.split("target=");
            String type = results[1];
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.setDataAndType(Uri.parse(url), type + "/*");
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.complete_action)));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void startExternalApplication(Context context, String url) {
        try {
            String[] results = url.split("package=");
            String packageName = results[1];
            boolean isAppInstalled = appInstalledOrNot(context, packageName);
            if (isAppInstalled) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.setPackage(packageName);
                intent.setData(Uri.parse(url));
                context.startActivity(intent);
                Log.i(TAG, "Application is already installed.");
            } else {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
                Log.i(TAG, "Application is not currently installed.");
            }
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "error : " + e.getMessage());
            Toast.makeText(context, context.getString(R.string.msg_error_handler), Toast.LENGTH_SHORT).show();
        }
    }

    private static boolean appInstalledOrNot(Context context, String uri) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "NameNotFoundException");
        }
        return false;
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode;
        try {
            locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return locationMode != Settings.Secure.LOCATION_MODE_OFF;
    }

    @SuppressLint("WrongConstant")
    public static void showLocationSettingsPrompt(final View view) {
        Snackbar.make(view, "Device location is disabled", LOCATION_SETTINGS_PROMPT_DURATION)
                .setAction("Settings", v -> {
                    final Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    view.getContext().startActivity(intent);
                })
                .show();
    }

    public static boolean isOnline(@NonNull Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected());
    }

    public static int getType(@NonNull Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            return networkInfo.getType();
        } else {
            return -1;
        }
    }

    public static String getTypeName(@NonNull Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null) {
            return networkInfo.getTypeName();
        } else {
            return null;
        }
    }

    public static String decodeJson(String data) {
        return com.solodroid.ads.sdk.util.Tools.jsonDecode(data);
    }

    public static boolean isVpnConnectionAvailable() {
        String iface = "";
        try {
            for (NetworkInterface networkInst : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (networkInst.isUp())
                    iface = networkInst.getName();
                if (iface.contains("tun") || iface.contains("ppp") || iface.contains("pptp")) {
                    return true;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void showWarningDialog(Activity activity, String title, String message) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(activity.getString(R.string.dialog_option_ok), (dialog, which) -> activity.finish())
                .setCancelable(false)
                .show();
    }

    public static boolean checkPermissionWriteExternalStorage(final Fragment fragment) {
        // DownloadManager can write to public Downloads without storage permission on API 29+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true;
        }
        return checkPermissions(fragment, R.string.permission_write_external_storage);
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean checkPermissionAccessLocation(final Fragment fragment) {
        return checkPermissions(fragment, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                new int[]{R.string.permission_access_location, R.string.permission_access_location},
                REQUEST_PERMISSION_ACCESS_LOCATION);
    }

    private static boolean checkPermissions(final Fragment fragment, final int explanation) {
        final int result = ContextCompat.checkSelfPermission(fragment.requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result != PackageManager.PERMISSION_GRANTED) {
            if (fragment.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Snackbar.make(fragment.requireView(), explanation, Snackbar.LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok, v -> fragment.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                Tools.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE)).show();
            } else {
                fragment.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, Tools.REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
            }
        }
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressWarnings("ToArrayCallWithZeroLengthArrayArgument")
    private static boolean checkPermissions(final Fragment fragment, final String[] permissions, final int[] explanations, final int requestCode) {
        final int[] results = new int[permissions.length];
        for (int i = 0; i < permissions.length; i++) {
            results[i] = ContextCompat.checkSelfPermission(fragment.requireActivity(), permissions[i]);
        }
        final List<String> deniedPermissions = new ArrayList<>();
        for (int i = 0; i < results.length; i++) {
            if (results[i] != PackageManager.PERMISSION_GRANTED) {
                deniedPermissions.add(permissions[i]);
            }
        }
        if (!deniedPermissions.isEmpty()) {
            final String[] params = deniedPermissions.toArray(new String[deniedPermissions.size()]);
            boolean isShown = false;
            for (int i = 0; i < permissions.length; i++) {
                if (fragment.shouldShowRequestPermissionRationale(permissions[i])) {
                    Snackbar.make(fragment.requireView(), explanations[i], Snackbar.LENGTH_INDEFINITE)
                            .setAction(android.R.string.ok, v -> fragment.requestPermissions(params, requestCode)).show();
                    isShown = true;
                    break;
                }
            }
            if (!isShown) {
                fragment.requestPermissions(params, requestCode);
            }
        }
        return deniedPermissions.isEmpty();
    }

    public static void fullScreenMode(AppCompatActivity activity, boolean show) {
        setSystemBarsVisible(activity, !show);
        if (activity.getSupportActionBar() != null) {
            if (show) {
                activity.getSupportActionBar().hide();
            } else {
                activity.getSupportActionBar().show();
            }
        }
    }

    public static void shareApp(Activity activity, String title) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, title + "\n\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
        sendIntent.setType("text/plain");
        activity.startActivity(sendIntent);
    }

    public static void shareContent(Activity activity, String title, String message) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, title + "\n\n" + message + "\n\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
        sendIntent.setType("text/plain");
        activity.startActivity(sendIntent);
    }

    public static void rateApp(Activity activity) {
        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)));
    }

    public static void moreApps(Activity activity, String moreAppsUrl) {
        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(moreAppsUrl)));
    }

    public static void showAboutDialog(Activity activity) {
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View view = layoutInflater.inflate(R.layout.dialog_about, null);
        TextView txtAppVersion = view.findViewById(R.id.txt_app_version);
        txtAppVersion.setText(activity.getString(R.string.about_version_format, BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME));
        final MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(activity);
        alert.setView(view);
        alert.setPositiveButton(R.string.dialog_option_ok, (dialog, which) -> dialog.dismiss());
        alert.show();
    }

    public static void setNativeAdStyle(Activity activity, LinearLayout nativeAdView) {
        switch (Constant.native_ad_style) {
            case "small":
            case "radio":
                nativeAdView.addView(View.inflate(activity, com.solodroid.ads.sdk.R.layout.view_native_ad_radio, null));
                break;
            case "medium":
            case "news":
                nativeAdView.addView(View.inflate(activity, com.solodroid.ads.sdk.R.layout.view_native_ad_news, null));
                break;
            default:
                nativeAdView.addView(View.inflate(activity, com.solodroid.ads.sdk.R.layout.view_native_ad_medium, null));
                break;
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public static void hideSystemUI(Activity activity) {
        setSystemBarsVisible(activity, false);
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public static void showSystemUI(Activity activity) {
        setSystemBarsVisible(activity, true);
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    public static String baseUrl = "https";
    public static void urlChecker(Activity activity, FragmentManager fragmentManager) {
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View view = layoutInflater.inflate(R.layout.dialog_launch, null);

        EditText edtUrl = view.findViewById(R.id.edt_url);

        RadioGroup radioGroup = view.findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            baseUrl = checkedId == R.id.radio_button_https ? "https" : "http";
        });

        final MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(activity);
        alert.setView(view);
        alert.setCancelable(false);
        alert.setPositiveButton("GO", (dialog, which) -> {
            if (edtUrl.getText().toString().equals("")) {
                Toast.makeText(activity, "Url cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                new Handler().postDelayed(() -> {
                    String url = baseUrl + "://" + edtUrl.getText().toString();
                    FragmentWebView argumentFragment = new FragmentWebView();
                    Bundle data = new Bundle();
                    data.putString("name", "Test My Url");
                    data.putString("type", "url");
                    data.putString("url", url);
                    data.putBoolean("toolbar", true);
                    argumentFragment.setArguments(data);
                    fragmentManager.beginTransaction().replace(R.id.fragment_container, argumentFragment).commit();
                }, 250);
            }
        });
        alert.setNegativeButton("Cancel", null);
        alert.show();
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public static void fullScreenDialog(AppCompatActivity activity, boolean show) {
        SharedPref sharedPref = new SharedPref(activity);
        if (show) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
            setWindowFlag(activity, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, false);
            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
            activity.getWindow().setNavigationBarColor(Color.TRANSPARENT);
        } else {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            if (!sharedPref.getIsDarkTheme()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                }
            }
        }
    }

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

}
