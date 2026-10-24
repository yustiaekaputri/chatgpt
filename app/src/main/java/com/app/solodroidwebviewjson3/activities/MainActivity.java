package com.app.solodroidwebviewjson3.activities;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.solodroidwebviewjson3.BuildConfig;
import com.app.solodroidwebviewjson3.R;
import com.app.solodroidwebviewjson3.adapters.AdapterMenu;
import com.app.solodroidwebviewjson3.fragments.FragmentWebView;
import com.app.solodroidwebviewjson3.listener.DrawerStateListener;
import com.app.solodroidwebviewjson3.listener.LoadUrlListener;
import com.app.solodroidwebviewjson3.models.Menu;
import com.app.solodroidwebviewjson3.utils.AdsManager;
import com.app.solodroidwebviewjson3.utils.Constant;
import com.app.solodroidwebviewjson3.utils.SharedPref;
import com.app.solodroidwebviewjson3.utils.Tools;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.solodroid.push.sdk.provider.OneSignalPush;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements LoadUrlListener, DrawerStateListener {

    private long exitTime = 0;
    private final static String COLLAPSING_TOOLBAR_FRAGMENT_TAG = "collapsing_toolbar";
    private final static String SELECTED_TAG = "selected_index";
    private static int selectedIndex;
    private final static int COLLAPSING_TOOLBAR = 0;
    private DrawerLayout drawerLayout;
    private FragmentManager fragmentManager;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;
    RecyclerView recyclerView;
    AdsManager adsManager;
    SharedPref sharedPref;
    Menu menu;
    List<Menu> menuList;
    CoordinatorLayout parentView;
    public static final int IMMEDIATE_APP_UPDATE_REQ_CODE = 124;
    private AppUpdateManager appUpdateManager;
    View lytDialogExit;
    LinearLayout lytPanelView;
    LinearLayout lytPanelDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_main);
        Tools.setNavigation(this);
        sharedPref = new SharedPref(this);
        menuList = sharedPref.getMenuList();
        if (menuList != null && menuList.size() > 0) {
            menu = menuList.get(0);
        }

        fragmentManager = getSupportFragmentManager();

        adsManager = new AdsManager(this);
        adsManager.initializeAd();
        adsManager.updateConsentStatus();
        adsManager.loadBannerAd(Constant.show_banner_ad);
        adsManager.loadInterstitialAd(Constant.show_interstitial_ad, Constant.interstitial_ad_interval);

        parentView = findViewById(R.id.parent_view);
        navigationView = findViewById(R.id.navigationView);

        if (sharedPref.getIsDarkTheme()) {
            navigationView.setBackgroundColor(getResources().getColor(R.color.color_dark_background_nav_drawer));
        } else {
            navigationView.setBackgroundColor(getResources().getColor(R.color.color_light_background_nav_drawer));
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        if (!Constant.navigationDrawer) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

        selectedIndex = COLLAPSING_TOOLBAR;
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new FragmentWebView(), COLLAPSING_TOOLBAR_FRAGMENT_TAG).commit();

        appUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
        if (!BuildConfig.DEBUG) {
            inAppUpdate();
            inAppReview();
        }

        loadWebPage();
        new Handler(Looper.getMainLooper()).postDelayed(this::notificationOpenHandler, 500);

        new OneSignalPush.Builder(this).requestNotificationPermission();

        initExitDialog();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackButtonPressed();
            }
        });

    }

    private void loadWebPage() {
        loadWebPage(menu.name, menu.type, menu.url, menu.url_dark, menu.toolbar);
        loadNavigationMenu(true);
        sharedPref.setLastItemPosition(1);
    }

    private void notificationOpenHandler() {
        if (getIntent().hasExtra("id")) {
            String title = getIntent().getStringExtra(OneSignalPush.EXTRA_TITLE);
            String url = getIntent().getStringExtra(OneSignalPush.EXTRA_LAUNCH_URL);
            if (url != null && !url.equals("")) {
                if (url.contains("play.google.com") || url.contains("?target=external")) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } else {
                    Intent intent = new Intent(getApplicationContext(), ActivityWebView.class);
                    intent.putExtra("title", title);
                    intent.putExtra("url", url);
                    startActivity(intent);
                }
            }
        }
    }

    /** @noinspection SameParameterValue*/
    private void loadNavigationMenu(boolean selectFirstItem) {

//        LinearLayout nativeAdViewDrawer = findViewById(R.id.native_ad_view_drawer);
//        Tools.setNativeAdStyle(this, nativeAdViewDrawer);
//        adsManager.loadNativeAd(Constant.show_native_ad_on_drawer_menu, Constant.native_ad_style);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        AdapterMenu adapterMenu = new AdapterMenu(this, new ArrayList<>());
        adapterMenu.setListData(menuList);

        recyclerView.setAdapter(adapterMenu);

        if (selectFirstItem) {
            recyclerView.postDelayed(() -> Objects.requireNonNull(recyclerView.findViewHolderForAdapterPosition(1)).itemView.performClick(), 10);
        }

        adapterMenu.setOnItemClickListener((v, obj, position) -> drawerLayout.closeDrawer(GravityCompat.START));

    }

    public void loadWebPage(String name, String type, String url, String urlDark, boolean toolbar) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            FragmentWebView argumentFragment = new FragmentWebView();
            Bundle data = new Bundle();
            data.putString("name", name);
            data.putString("type", type);
            if (sharedPref.getIsDarkTheme()) {
                if (urlDark != null && !urlDark.equals("")) {
                    data.putString("url", urlDark);
                } else {
                    data.putString("url", url);
                }
            } else {
                data.putString("url", url);
            }
            data.putBoolean("toolbar", toolbar);
            argumentFragment.setArguments(data);
            fragmentManager.beginTransaction().replace(R.id.fragment_container, argumentFragment).commit();
        }, 250);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_TAG, selectedIndex);
    }

    @Override
    public void onLoadUrl(String url) {
        adsManager.showInterstitialAd();
    }

    public void setupNavigationDrawer(Toolbar toolbar) {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, 0, 0) {
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                showBannerAd(!(slideOffset > 0));
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    @Override
    public boolean isDrawerOpen() {
        return drawerLayout.isDrawerOpen(GravityCompat.START);
    }

    @Override
    public void onBackButtonPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
            if (fragment instanceof FragmentWebView
                    && ((FragmentWebView) fragment).handleBackPressed()) {
                return;
            }
            exitApp();
        }
    }

    public void exitApp() {
        if (Constant.exitDialog) {
            if (lytDialogExit.getVisibility() != View.VISIBLE) {
                showDialog(true);
            }
        } else {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                showSnackBar(getString(R.string.exit_msg));
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                Constant.isAppOpen = false;
                adsManager.destroyBannerAd(Constant.show_banner_ad);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Constant.isAppOpen = false;
        adsManager.destroyBannerAd(Constant.show_banner_ad);
    }

    @Override
    public void onResume() {
        super.onResume();
        adsManager.resumeBannerAd(Constant.show_banner_ad);
    }

    @Override
    public AssetManager getAssets() {
        return getResources().getAssets();
    }

    private void inAppReview() {
        if (sharedPref.getInAppReviewToken() <= 3) {
            sharedPref.updateInAppReviewToken(sharedPref.getInAppReviewToken() + 1);
        } else {
            ReviewManager manager = ReviewManagerFactory.create(this);
            Task<ReviewInfo> request = manager.requestReviewFlow();
            request.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    ReviewInfo reviewInfo = task.getResult();
                    manager.launchReviewFlow(MainActivity.this, reviewInfo).addOnFailureListener(e -> {
                    }).addOnCompleteListener(complete -> {
                            }
                    ).addOnFailureListener(failure -> {
                    });
                }
            }).addOnFailureListener(failure -> Log.d("In-App Review", "In-App Request Failed " + failure));
        }
    }

    private void inAppUpdate() {
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                startUpdateFlow(appUpdateInfo);
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                startUpdateFlow(appUpdateInfo);
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void startUpdateFlow(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(appUpdateInfo, AppUpdateType.IMMEDIATE, this, IMMEDIATE_APP_UPDATE_REQ_CODE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, intent);
        }
        if (requestCode == IMMEDIATE_APP_UPDATE_REQ_CODE) {
            if (resultCode == RESULT_CANCELED) {
                showSnackBar(getString(R.string.msg_update_canceled));
            } else if (resultCode == RESULT_OK) {
                showSnackBar(getString(R.string.msg_update_success));
            } else {
                showSnackBar(getString(R.string.msg_update_failed));
                inAppUpdate();
            }
        }
    }

    public void showSnackBar(String message) {
        Snackbar.make(parentView, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    public void urlChecker() {
        Tools.urlChecker(this, fragmentManager);
    }

    public void initExitDialog() {

        lytDialogExit = findViewById(R.id.lyt_dialog_exit);
        lytPanelView = (LinearLayout) lytDialogExit;
        lytPanelDialog = findViewById(R.id.lyt_panel_dialog);

        if (sharedPref.getIsDarkTheme()) {
            lytPanelView.setBackgroundColor(getResources().getColor(R.color.color_dialog_background_dark_overlay));
            lytPanelDialog.setBackgroundResource(R.drawable.bg_rounded_dark);
        } else {
            lytPanelView.setBackgroundColor(getResources().getColor(R.color.color_dialog_background_light));
            lytPanelDialog.setBackgroundResource(R.drawable.bg_rounded_light);
        }

        lytPanelView.setOnClickListener(view -> {
            //empty state
        });

        LinearLayout nativeAdView = findViewById(R.id.native_ad_view);
        Tools.setNativeAdStyle(this, nativeAdView);
        adsManager.loadNativeAd(Constant.show_native_ad_on_exit_dialog, "medium");

        Button btnCancel = findViewById(R.id.btn_cancel);
        Button btnExit = findViewById(R.id.btn_exit);

        FloatingActionButton btnRate = findViewById(R.id.btn_rate);
        FloatingActionButton btnShare = findViewById(R.id.btn_share);

        btnCancel.setOnClickListener(view -> showDialog(false));

        btnExit.setOnClickListener(view -> {
            showDialog(false);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                finish();
                adsManager.destroyBannerAd(Constant.show_banner_ad);
            }, 300);
        });

        btnRate.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)));
            showDialog(false);
        });

        btnShare.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + "\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
            intent.setType("text/plain");
            startActivity(intent);
            showDialog(false);
        });
    }

    private void showDialog(boolean show) {
        if (show) {
            lytDialogExit.setVisibility(View.VISIBLE);
            slideUp(findViewById(R.id.dialog_card_view));
            ObjectAnimator.ofFloat(lytDialogExit, View.ALPHA, 0.1f, 1.0f).setDuration(300).start();
            Tools.fullScreenDialog(this, true);
            showBannerAd(false);
        } else {
            slideDown(findViewById(R.id.dialog_card_view));
            ObjectAnimator.ofFloat(lytDialogExit, View.ALPHA, 1.0f, 0.1f).setDuration(300).start();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                lytDialogExit.setVisibility(View.GONE);
                Tools.fullScreenDialog(this, false);
                Tools.setNavigation(this);
                showBannerAd(true);
            }, 300);
        }
    }

    public void slideUp(View view) {
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(0, 0, findViewById(R.id.main_content).getHeight(), 0);
        animate.setDuration(300);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    public void slideDown(View view) {
        TranslateAnimation animate = new TranslateAnimation(0, 0, 0, findViewById(R.id.main_content).getHeight());
        animate.setDuration(300);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    private void showBannerAd(boolean show) {
        if (show) {
            findViewById(R.id.adView).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.adView).setVisibility(View.GONE);
        }
    }

}
