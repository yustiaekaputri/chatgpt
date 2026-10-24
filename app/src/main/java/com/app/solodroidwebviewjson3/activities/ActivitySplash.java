package com.app.solodroidwebviewjson3.activities;

import static com.solodroid.ads.sdk.util.Constant.ADMOB;
import static com.solodroid.ads.sdk.util.Constant.APPLOVIN;
import static com.solodroid.ads.sdk.util.Constant.APPLOVIN_MAX;
import static com.solodroid.ads.sdk.util.Constant.GOOGLE_AD_MANAGER;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.app.solodroidwebviewjson3.BuildConfig;
import com.app.solodroidwebviewjson3.R;
import com.app.solodroidwebviewjson3.callbacks.CallbackConfig;
import com.app.solodroidwebviewjson3.models.Apps;
import com.app.solodroidwebviewjson3.rests.RestAdapter;
import com.app.solodroidwebviewjson3.utils.AdsManager;
import com.app.solodroidwebviewjson3.utils.Constant;
import com.app.solodroidwebviewjson3.utils.SharedPref;
import com.app.solodroidwebviewjson3.utils.Tools;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivitySplash extends AppCompatActivity {

    public static final String TAG = "ActivitySplash";
    Call<CallbackConfig> callbackConfigCall = null;
    AdsManager adsManager;
    SharedPref sharedPref;
    ImageView imgSplash;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_splash);
        Tools.setNavigation(this);

        sharedPref = new SharedPref(this);
        adsManager = new AdsManager(this);
        adsManager.initializeAd();

        imgSplash = findViewById(R.id.imgSplash);
        if (sharedPref.getIsDarkTheme()) {
            imgSplash.setImageResource(R.drawable.bg_splash_dark);
        } else {
            imgSplash.setImageResource(R.drawable.bg_splash_default);
        }

        new Handler(Looper.getMainLooper()).postDelayed(this::requestConfig, Constant.DELAY_SPLASH);

    }

    private void requestConfig() {
        String data = Tools.decodeJson(getString(R.string.json_encode));
        String[] results = data.split("_applicationId_");
        String remoteUrl = results[0];
        String applicationId = results[1];
        if (applicationId.equals(BuildConfig.APPLICATION_ID)) {
            requestAPI(remoteUrl);
        } else {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Error")
                    .setMessage("Whoops! invalid key or applicationId, please check your configuration")
                    .setPositiveButton(getString(R.string.dialog_option_ok), (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
        }
    }

    private void requestAPI(@SuppressWarnings("SameParameterValue") String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            if (url.contains("https://drive.google.com")) {
                String driveUrl = url.replace("https://", "").replace("http://", "");
                List<String> data = Arrays.asList(driveUrl.split("/"));
                String googleDriveFileId = data.get(3);
                callbackConfigCall = RestAdapter.createApi().getDriveJsonFileId(googleDriveFileId);
            } else {
                callbackConfigCall = RestAdapter.createApi().getJsonUrl(url);
            }
        } else {
            callbackConfigCall = RestAdapter.createApi().getDriveJsonFileId(url);
        }
        callbackConfigCall.enqueue(new Callback<CallbackConfig>() {
            public void onResponse(@NonNull Call<CallbackConfig> call, @NonNull Response<CallbackConfig> response) {
                CallbackConfig resp = response.body();
                if (resp != null) {
                    if (resp.apps.get(0).enable_vpn_access) {
                        displayApiResults(resp);
                    } else {
                        if (Tools.isVpnConnectionAvailable()) {
                            Tools.showWarningDialog(ActivitySplash.this, getString(R.string.vpn_detected), getString(R.string.close_vpn));
                        } else {
                            displayApiResults(resp);
                        }
                    }
                }
                Log.d(TAG, "request config success");
            }

            public void onFailure(@NonNull Call<CallbackConfig> call, @NonNull Throwable th) {
                showOpenAdsIfAvailable(false);
                Log.d(TAG, "request config failed : " + th.getMessage());
            }
        });
    }

    private void displayApiResults(CallbackConfig resp) {

        if (resp != null) {
            sharedPref.saveAppsList(resp.apps);
            sharedPref.saveMenuList(resp.menus);
            sharedPref.saveAdList(resp.ads);
            sharedPref.saveMoreAppsList(resp.more_apps);

            Apps apps = resp.apps.get(0);
            if (apps.status) {
                showOpenAdsIfAvailable(true);
            } else {
                Intent intent = new Intent(getApplicationContext(), ActivityRedirect.class);
                intent.putExtra("redirect_url", apps.redirect_url);
                startActivity(intent);
                finish();
            }

            Constant.navigationDrawer = apps.navigation_drawer;
            Constant.navigationIconSolidColor = apps.navigation_icon_solid_color;
            Constant.privacyPolicyUrl = apps.privacy_policy_url;
            Constant.rtlMode = apps.enable_rtl_mode;
            Constant.defaultDarkTheme = apps.enable_dark_mode_as_default_theme;
            Constant.exitDialog = apps.enable_exit_dialog;
            Log.d(TAG, "initialize success");
        } else {
            Log.d(TAG, "initialize failed");
            showOpenAdsIfAvailable(false);
        }

    }

    private void showOpenAdsIfAvailable(boolean show) {
        if (show) {
            if (Constant.show_open_ad_on_app_start && Constant.ad_status) {
                Application application = getApplication();
                switch (Constant.main_ad) {
                    case ADMOB:
                        if (!Constant.admob_app_open_unit_id.equals("0")) {
                            ((MyApplication) application).showAdIfAvailable(ActivitySplash.this, this::startMainActivity);
                        } else {
                            startMainActivity();
                        }
                        break;
                    case GOOGLE_AD_MANAGER:
                        if (!Constant.ad_manager_app_open_unit_id.equals("0")) {
                            ((MyApplication) application).showAdIfAvailable(ActivitySplash.this, this::startMainActivity);
                        } else {
                            startMainActivity();
                        }
                        break;
                    case APPLOVIN:
                    case APPLOVIN_MAX:
                        if (!Constant.applovin_open_ad_unit_id.equals("0")) {
                            ((MyApplication) application).showAdIfAvailable(ActivitySplash.this, this::startMainActivity);
                        } else {
                            startMainActivity();
                        }
                        break;
                    default:
                        startMainActivity();
                        break;
                }
            } else {
                startMainActivity();
            }
        } else {
            startMainActivity();
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }

}
