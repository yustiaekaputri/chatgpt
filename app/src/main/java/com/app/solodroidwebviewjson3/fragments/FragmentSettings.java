package com.app.solodroidwebviewjson3.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.app.solodroidwebviewjson3.BuildConfig;
import com.app.solodroidwebviewjson3.R;
import com.app.solodroidwebviewjson3.activities.ActivityWebView;
import com.app.solodroidwebviewjson3.activities.MainActivity;
import com.app.solodroidwebviewjson3.models.Apps;
import com.app.solodroidwebviewjson3.utils.Constant;
import com.app.solodroidwebviewjson3.utils.SharedPref;
import com.app.solodroidwebviewjson3.utils.Tools;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

public class FragmentSettings extends Fragment {

    private static final String TAG = "Settings";
    MaterialSwitch switchTheme;
    RelativeLayout btnSwitchTheme;
    View rootView;
    Activity activity;
    TextView txt_cache_size;
    SharedPref sharedPref;
    List<Apps> appsList;
    Apps apps;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        sharedPref = new SharedPref(activity);
        appsList = sharedPref.getAppsList();
        if (appsList != null && appsList.size() > 0) {
            apps = appsList.get(0);
            initView();
        }
        return rootView;
    }

    private void initView() {
        switchTheme = rootView.findViewById(R.id.switch_theme);
        switchTheme.setChecked(sharedPref.getIsDarkTheme());
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPref.setIsDarkTheme(isChecked);
            Intent intent = new Intent(activity, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        btnSwitchTheme = rootView.findViewById(R.id.btn_switch_theme);
        btnSwitchTheme.setOnClickListener(v -> {
            if (switchTheme.isChecked()) {
                sharedPref.setIsDarkTheme(false);
                switchTheme.setChecked(false);
            } else {
                sharedPref.setIsDarkTheme(true);
                switchTheme.setChecked(true);
            }
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(activity, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }, 200);
        });

        rootView.findViewById(R.id.btn_notification).setOnClickListener(v -> {
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, BuildConfig.APPLICATION_ID);
            } else {
                intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                intent.putExtra("app_package", BuildConfig.APPLICATION_ID);
                intent.putExtra("app_uid", activity.getApplicationInfo().uid);
            }
            startActivity(intent);
        });

        txt_cache_size = rootView.findViewById(R.id.txt_cache_size);
        initializeCache();

        rootView.findViewById(R.id.btn_clear_cache).setOnClickListener(v -> clearCache());

        rootView.findViewById(R.id.btn_privacy_policy).setOnClickListener(v -> {
            Intent intent = new Intent(activity, ActivityWebView.class);
            intent.putExtra("title", getString(R.string.menu_privacy));
            intent.putExtra("url", Constant.privacyPolicyUrl);
            startActivity(intent);
        });

        rootView.findViewById(R.id.btn_share).setOnClickListener(v -> Tools.shareApp(activity, getString(R.string.share_text)));

        rootView.findViewById(R.id.btn_rate).setOnClickListener(v -> Tools.rateApp(activity));

        rootView.findViewById(R.id.btn_about).setOnClickListener(v -> Tools.showAboutDialog(activity));

    }

    private void clearCache() {
        FileUtils.deleteQuietly(activity.getCacheDir());
        FileUtils.deleteQuietly(activity.getExternalCacheDir());
        txt_cache_size.setText(getString(R.string.cache_space_format, readableFileSize(0)));
        Snackbar.make(activity.findViewById(android.R.id.content), getString(R.string.msg_cache_cleared), Snackbar.LENGTH_SHORT).show();
    }

    private void initializeCache() {
        txt_cache_size.setText(getString(R.string.cache_space_format, readableFileSize(getDirSize(activity.getCacheDir()) + getDirSize(activity.getExternalCacheDir()))));
    }

    public long getDirSize(File dir) {
        long size = 0;
        for (File file : dir.listFiles()) {
            if (file != null && file.isDirectory()) {
                size += getDirSize(file);
            } else if (file != null && file.isFile()) {
                size += file.length();
            }
        }
        return size;
    }

    public static String readableFileSize(long size) {
        if (size <= 0) {
            return "0 Bytes";
        }
        String[] units = new String[]{"Bytes", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10((double) size) / Math.log10(1024.0d));
        StringBuilder stringBuilder = new StringBuilder();
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.#");
        double d = (double) size;
        double pow = Math.pow(1024.0d, (double) digitGroups);
        Double.isNaN(d);
        stringBuilder.append(decimalFormat.format(d / pow));
        stringBuilder.append(" ");
        stringBuilder.append(units[digitGroups]);
        return stringBuilder.toString();
    }

}
