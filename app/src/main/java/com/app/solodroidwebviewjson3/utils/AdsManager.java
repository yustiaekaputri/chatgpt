package com.app.solodroidwebviewjson3.utils;

import static com.solodroid.ads.sdk.util.Constant.IRONSOURCE;

import android.app.Activity;

import com.app.solodroidwebviewjson3.BuildConfig;
import com.app.solodroidwebviewjson3.R;
import com.app.solodroidwebviewjson3.models.Ads;
import com.solodroid.ads.sdk.format.AdNetwork;
import com.solodroid.ads.sdk.format.BannerAd;
import com.solodroid.ads.sdk.format.InterstitialAd;
import com.solodroid.ads.sdk.format.NativeAd;
import com.solodroid.ads.sdk.format.NativeAdView;
import com.solodroid.ads.sdk.gdpr.GDPR;

import java.util.List;

public class AdsManager {

    Activity activity;
    AdNetwork.Initialize adNetwork;
    BannerAd.Builder bannerAd;
    InterstitialAd.Builder interstitialAd;
    NativeAd.Builder nativeAd;
    NativeAdView.Builder nativeAdView;
    SharedPref sharedPref;
    List<Ads> adsList;
    Ads ads;
    GDPR gdpr;

    public AdsManager(Activity activity) {
        this.activity = activity;
        this.sharedPref = new SharedPref(activity);
        this.gdpr = new GDPR(activity);
        adNetwork = new AdNetwork.Initialize(activity);
        bannerAd = new BannerAd.Builder(activity);
        interstitialAd = new InterstitialAd.Builder(activity);
        nativeAd = new NativeAd.Builder(activity);
        nativeAdView = new NativeAdView.Builder(activity);
        initAds();
    }

    public void initializeAd() {
        if (Constant.ad_status) {
            adNetwork.setAdStatus("1")
                    .setAdNetwork(Constant.main_ad)
                    .setBackupAdNetwork(Constant.backup_ad)
                    .setStartappAppId(Constant.startapp_app_id)
                    .setUnityGameId(Constant.unity_game_id)
                    .setIronSourceAppKey(Constant.ironsource_app_key)
                    .setDebug(BuildConfig.DEBUG)
                    .build();
        }
    }

    public void loadBannerAd(boolean placementStatus) {
        if (Constant.ad_status) {
            if (placementStatus) {
                bannerAd.setAdStatus("1")
                        .setAdNetwork(Constant.main_ad)
                        .setBackupAdNetwork(Constant.backup_ad)
                        .setAdMobBannerId(Constant.admob_banner_unit_id)
                        .setGoogleAdManagerBannerId(Constant.ad_manager_banner_unit_id)
                        .setFanBannerId(Constant.fan_banner_unit_id)
                        .setUnityBannerId(Constant.unity_banner_placement_id)
                        .setAppLovinBannerId(Constant.applovin_banner_unit_id)
                        .setAppLovinBannerZoneId(Constant.applovin_banner_zone_id)
                        .setIronSourceBannerId(Constant.ironsource_banner_id)
                        .setAlienAdsBannerId(Constant.alien_ads_banner_unit_id)
                        .setDarkTheme(sharedPref.getIsDarkTheme())
                        .setPlacementStatus(1)
                        .setLegacyGDPR(false)
                        .build();
            }
        }
    }

    public void loadInterstitialAd(boolean placementStatus, int interval) {
        if (Constant.ad_status) {
            if (placementStatus) {
                interstitialAd.setAdStatus("1")
                        .setAdNetwork(Constant.main_ad)
                        .setBackupAdNetwork(Constant.backup_ad)
                        .setAdMobInterstitialId(Constant.admob_interstitial_unit_id)
                        .setGoogleAdManagerInterstitialId(Constant.ad_manager_interstitial_unit_id)
                        .setFanInterstitialId(Constant.fan_interstitial_unit_id)
                        .setUnityInterstitialId(Constant.unity_interstitial_placement_id)
                        .setAppLovinInterstitialId(Constant.applovin_interstitial_unit_id)
                        .setAppLovinInterstitialZoneId(Constant.applovin_interstitial_zone_id)
                        .setIronSourceInterstitialId(Constant.ironsource_interstitial_id)
                        .setAlienAdsInterstitialId(Constant.alien_ads_interstitial_unit_id)
                        .setInterval(interval)
                        .setPlacementStatus(1)
                        .setLegacyGDPR(false)
                        .build();
            }
        }
    }

    public void loadNativeAd(boolean placementStatus, String style) {
        if (Constant.ad_status) {
            if (placementStatus) {
                nativeAd.setAdStatus("1")
                        .setAdNetwork(Constant.main_ad)
                        .setBackupAdNetwork(Constant.backup_ad)
                        .setAdMobNativeId(Constant.admob_native_unit_id)
                        .setAdManagerNativeId(Constant.ad_manager_native_unit_id)
                        .setFanNativeId(Constant.fan_native_unit_id)
                        .setAppLovinNativeId(Constant.applovin_native_manual_unit_id)
                        .setAppLovinDiscoveryMrecZoneId(Constant.applovin_banner_zone_id)
                        .setAlienAdsNativeId(Constant.alien_ads_native_unit_id)
                        .setPlacementStatus(1)
                        .setDarkTheme(sharedPref.getIsDarkTheme())
                        .setLegacyGDPR(false)
                        .setNativeAdStyle(style)
                        .setNativeAdBackgroundColor(R.color.color_light_native_ad_background, R.color.color_dark_native_ad_background)
                        .build();
            }
        }
    }

    private void initAds() {
        adsList = sharedPref.getAdList();
        if (adsList != null && adsList.size() > 0) {
            ads = adsList.get(0);
            onlineJson(ads);
        }
    }

    private void onlineJson(Ads ads) {
        Constant.ad_status = ads.ad_status;
        Constant.main_ad = ads.main_ad;
        Constant.backup_ad = ads.backup_ad;
        Constant.admob_banner_unit_id = ads.admob_banner_unit_id;
        Constant.admob_interstitial_unit_id = ads.admob_interstitial_unit_id;
        Constant.admob_native_unit_id = ads.admob_native_unit_id;
        Constant.admob_app_open_unit_id = ads.admob_app_open_unit_id;
        Constant.ad_manager_banner_unit_id = ads.ad_manager_banner_unit_id;
        Constant.ad_manager_interstitial_unit_id = ads.ad_manager_interstitial_unit_id;
        Constant.ad_manager_native_unit_id = ads.ad_manager_native_unit_id;
        Constant.ad_manager_app_open_unit_id = ads.ad_manager_app_open_unit_id;
        Constant.alien_ads_banner_unit_id = ads.alien_ads_banner_unit_id;
        Constant.alien_ads_interstitial_unit_id = ads.alien_ads_interstitial_unit_id;
        Constant.alien_ads_native_unit_id = ads.alien_ads_native_unit_id;
        Constant.fan_banner_unit_id = ads.fan_banner_unit_id;
        Constant.fan_interstitial_unit_id = ads.fan_interstitial_unit_id;
        Constant.fan_native_unit_id = ads.fan_native_unit_id;
        Constant.startapp_app_id = ads.startapp_app_id;
        Constant.unity_game_id = ads.unity_game_id;
        Constant.unity_banner_placement_id = ads.unity_banner_placement_id;
        Constant.unity_interstitial_placement_id = ads.unity_interstitial_placement_id;
        Constant.applovin_banner_unit_id = ads.applovin_banner_unit_id;
        Constant.applovin_interstitial_unit_id = ads.applovin_interstitial_unit_id;
        Constant.applovin_native_manual_unit_id = ads.applovin_native_manual_unit_id;
        Constant.applovin_open_ad_unit_id = ads.applovin_open_ad_unit_id;
        Constant.applovin_banner_zone_id = ads.applovin_banner_zone_id;
        Constant.applovin_interstitial_zone_id = ads.applovin_interstitial_zone_id;
        Constant.ironsource_app_key = ads.ironsource_app_key;
        Constant.ironsource_banner_id = ads.ironsource_banner_id;
        Constant.ironsource_interstitial_id = ads.ironsource_interstitial_id;
        Constant.interstitial_ad_interval = ads.interstitial_ad_interval;
        Constant.native_ad_style = ads.native_ad_style;
        Constant.show_banner_ad = ads.show_banner_ad;
        Constant.show_interstitial_ad = ads.show_interstitial_ad;
        Constant.show_native_ad_on_drawer_menu = ads.show_native_ad_on_drawer_menu;
        Constant.show_native_ad_on_exit_dialog = ads.show_native_ad_on_exit_dialog;
        Constant.show_open_ad_on_app_start = ads.show_open_ad_on_app_start;
        Constant.show_open_ad_on_app_resume = ads.show_open_ad_on_app_resume;
    }

    public void showInterstitialAd() {
        interstitialAd.show();
    }

    public void destroyBannerAd(boolean placementStatus) {
        if (placementStatus) {
            bannerAd.destroyAndDetachBanner();
        }
    }

    public void resumeBannerAd(boolean placementStatus) {
        if (Constant.ad_status && !Constant.ironsource_banner_id.equals("0")) {
            if (Constant.main_ad.equals(IRONSOURCE) || Constant.backup_ad.equals(IRONSOURCE)) {
                loadBannerAd(placementStatus);
            }
        }
    }

    public void updateConsentStatus() {
        gdpr.updateGDPRConsentStatus(Constant.main_ad, false, false);
    }

}
