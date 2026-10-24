package com.app.solodroidwebviewjson3.activities;

import static com.solodroid.ads.sdk.util.Constant.ADMOB;
import static com.solodroid.ads.sdk.util.Constant.APPLOVIN;
import static com.solodroid.ads.sdk.util.Constant.APPLOVIN_MAX;
import static com.solodroid.ads.sdk.util.Constant.GOOGLE_AD_MANAGER;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;
import androidx.multidex.MultiDex;

import com.app.solodroidwebviewjson3.R;
import com.app.solodroidwebviewjson3.utils.Constant;
import com.app.solodroidwebviewjson3.utils.SharedPref;
import com.solodroid.ads.sdk.format.AppOpenAdAppLovin;
import com.solodroid.ads.sdk.format.AppOpenAdManager;
import com.solodroid.ads.sdk.format.AppOpenAdMob;
import com.solodroid.ads.sdk.util.OnShowAdCompleteListener;
import com.solodroid.push.sdk.provider.OneSignalPush;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";
    private AppOpenAdMob appOpenAdMob;
    private AppOpenAdManager appOpenAdManager;
    private AppOpenAdAppLovin appOpenAdAppLovin;
    SharedPref sharedPref;
    String notificationId;
    String notificationTitle = "";
    String notificationMessage = "";
    String notificationBigImage = "";
    String notificationUrl = "";
    Activity currentActivity;

    @Override
    public void onCreate() {
        super.onCreate();
        this.registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
        sharedPref = new SharedPref(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(lifecycleObserver);
        appOpenAdMob = new AppOpenAdMob();
        appOpenAdManager = new AppOpenAdManager();
        appOpenAdAppLovin = new AppOpenAdAppLovin();
        initNotification();
    }

    public void initNotification() {
        new OneSignalPush.Builder(this)
                .setOneSignalAppId(getResources().getString(R.string.onesignal_app_id))
                .build(() -> {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(OneSignalPush.EXTRA_ID, OneSignalPush.Data.id);
                    intent.putExtra(OneSignalPush.EXTRA_TITLE, OneSignalPush.Data.title);
                    intent.putExtra(OneSignalPush.EXTRA_MESSAGE, OneSignalPush.Data.message);
                    intent.putExtra(OneSignalPush.EXTRA_IMAGE, OneSignalPush.Data.bigImage);
                    intent.putExtra(OneSignalPush.EXTRA_LAUNCH_URL, OneSignalPush.Data.launchUrl);
                    startActivity(intent);
                });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    LifecycleObserver lifecycleObserver = new DefaultLifecycleObserver() {
        @Override
        public void onStart(@NonNull LifecycleOwner owner) {
            if (Constant.isAppOpen) {
                if (Constant.show_open_ad_on_app_resume) {
                    if (Constant.ad_status) {
                        switch (Constant.main_ad) {
                            case ADMOB:
                                if (!Constant.admob_app_open_unit_id.equals("0")) {
                                    if (!currentActivity.getIntent().hasExtra("unique_id")) {
                                        appOpenAdMob.showAdIfAvailable(currentActivity, Constant.admob_app_open_unit_id);
                                    }
                                }
                                break;
                            case GOOGLE_AD_MANAGER:
                                if (!Constant.ad_manager_app_open_unit_id.equals("0")) {
                                    if (!currentActivity.getIntent().hasExtra("unique_id")) {
                                        appOpenAdManager.showAdIfAvailable(currentActivity, Constant.ad_manager_app_open_unit_id);
                                    }
                                }
                                break;
                            case APPLOVIN:
                            case APPLOVIN_MAX:
                                if (!Constant.applovin_open_ad_unit_id.equals("0")) {
                                    if (!currentActivity.getIntent().hasExtra("unique_id")) {
                                        appOpenAdAppLovin.showAdIfAvailable(currentActivity, Constant.applovin_open_ad_unit_id);
                                    }
                                }
                                break;
                        }
                    }
                }
            }
            DefaultLifecycleObserver.super.onStart(owner);
        }

    };

    ActivityLifecycleCallbacks activityLifecycleCallbacks = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
            if (Constant.show_open_ad_on_app_start) {
                if (Constant.ad_status) {
                    switch (Constant.main_ad) {
                        case ADMOB:
                            if (!Constant.admob_app_open_unit_id.equals("0")) {
                                if (!appOpenAdMob.isShowingAd) {
                                    currentActivity = activity;
                                }
                            }
                            break;
                        case GOOGLE_AD_MANAGER:
                            if (!Constant.ad_manager_app_open_unit_id.equals("0")) {
                                if (!appOpenAdManager.isShowingAd) {
                                    currentActivity = activity;
                                }
                            }
                            break;
                        case APPLOVIN:
                        case APPLOVIN_MAX:
                            if (!Constant.applovin_open_ad_unit_id.equals("0")) {
                                if (!appOpenAdAppLovin.isShowingAd) {
                                    currentActivity = activity;
                                }
                            }
                            break;
                    }
                }
            }
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
        }
    };

    public void showAdIfAvailable(@NonNull Activity activity, @NonNull OnShowAdCompleteListener onShowAdCompleteListener) {
        if (Constant.show_open_ad_on_app_start) {
            if (Constant.ad_status) {
                switch (Constant.main_ad) {
                    case ADMOB:
                        if (!Constant.admob_app_open_unit_id.equals("0")) {
                            appOpenAdMob.showAdIfAvailable(activity, Constant.admob_app_open_unit_id, onShowAdCompleteListener);
                            Constant.isAppOpen = true;
                        }
                        break;
                    case GOOGLE_AD_MANAGER:
                        if (!Constant.ad_manager_app_open_unit_id.equals("0")) {
                            appOpenAdManager.showAdIfAvailable(activity, Constant.ad_manager_app_open_unit_id, onShowAdCompleteListener);
                            Constant.isAppOpen = true;
                        }
                        break;
                    case APPLOVIN:
                    case APPLOVIN_MAX:
                        if (!Constant.applovin_open_ad_unit_id.equals("0")) {
                            appOpenAdAppLovin.showAdIfAvailable(activity, Constant.applovin_open_ad_unit_id, onShowAdCompleteListener);
                            Constant.isAppOpen = true;
                        }
                        break;
                }
            }
        }
    }

}