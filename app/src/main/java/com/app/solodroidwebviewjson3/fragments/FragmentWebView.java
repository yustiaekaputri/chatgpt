package com.app.solodroidwebviewjson3.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.solodroidwebviewjson3.R;
import com.app.solodroidwebviewjson3.activities.ActivityMoreOptions;
import com.app.solodroidwebviewjson3.activities.MainActivity;
import com.app.solodroidwebviewjson3.listener.LoadUrlListener;
import com.app.solodroidwebviewjson3.listener.WebViewOnTouchListener;
import com.app.solodroidwebviewjson3.models.Apps;
import com.app.solodroidwebviewjson3.utils.Constant;
import com.app.solodroidwebviewjson3.utils.MyFragment;
import com.app.solodroidwebviewjson3.utils.SharedPref;
import com.app.solodroidwebviewjson3.utils.Tools;
import com.app.solodroidwebviewjson3.webview.AdvancedWebView;
import com.app.solodroidwebviewjson3.webview.VideoEnabledWebChromeClient;
import com.app.solodroidwebviewjson3.webview.VideoEnabledWebView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.List;

@SuppressWarnings("deprecation")
public class FragmentWebView extends MyFragment implements AdvancedWebView.Listener {

    private MainActivity activity;
    private VideoEnabledWebChromeClient webChromeClient;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout lytProgress;
    private LinearProgressIndicator progressBar;
    private View lytNoNetwork;
    private View lytNoPage;
    View rootView, parentView;
    AdvancedWebView webView;
    String pageType;
    boolean showToolbar;
    String pageUrl;
    String pageTitle;
    Button btnRetry;
    private Toolbar toolbar;
    TextView toolbarTitle;
    ImageButton btnSettings;
    SharedPref sharedPref;
    List<Apps> appsList;
    Apps apps;


    public FragmentWebView() {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    /**
     * @noinspection deprecation
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        if (getArguments() != null) {
            pageTitle = getArguments().getString("name");
            pageType = getArguments().getString("type");
            if (pageType != null && pageType.equals("assets")) {
                pageUrl = "file:///android_asset/" + getArguments().getString("url");
            } else {
                pageUrl = getArguments().getString("url");
            }
            showToolbar = getArguments().getBoolean("toolbar");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_webview, container, false);
        parentView = activity.findViewById(R.id.main_non_video_layout);
        sharedPref = new SharedPref(activity);
        initView();
        setupToolbar();
        return rootView;
    }

    private void initView() {
        lytProgress = rootView.findViewById(R.id.lyt_progress);
        progressBar = rootView.findViewById(R.id.progressBar);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.color_swipe_refresh_layout);
        if (!Constant.ENABLE_SWIPE_REFRESH_LAYOUT) {
            swipeRefreshLayout.setEnabled(false);
        }
        webView = rootView.findViewById(R.id.webView);
        lytNoNetwork = rootView.findViewById(R.id.lyt_no_network);
        lytNoPage = rootView.findViewById(R.id.lyt_no_page);
        btnRetry = rootView.findViewById(R.id.btn_retry);
        toolbar = rootView.findViewById(R.id.toolbar);
        btnSettings = rootView.findViewById(R.id.btn_settings);
        toolbarTitle = rootView.findViewById(R.id.toolbar_title);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        appsList = sharedPref.getAppsList();
        if (appsList != null && appsList.size() > 0) {
            apps = appsList.get(0);

            if (apps.navigation_drawer) {
                activity.setupNavigationDrawer(toolbar);
            }

            if (apps.geolocation) {
                Tools.checkPermissionAccessLocation(this);
            }

            initComponents();

        }

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        }

        loadData();

        swipeRefreshLayout.setOnRefreshListener(this::refreshData);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        // The system file picker grants access to the returned content URI.
        // Forward cancellation too, so the WebView upload callback is released.
        webView.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        setUserVisibleHint(true);
        webView.saveState(outState);
    }

    @SuppressLint({"ClickableViewAccessibility", "SetJavaScriptEnabled"})
    private void initComponents() {
        webView.getSettings().setJavaScriptEnabled(true);

        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        //webView cache
        if (apps.webview_cache) {
            webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
            webView.getSettings().setDomStorageEnabled(true);
            webView.getSettings().setDatabaseEnabled(true);
        }

        if (!apps.user_agent.equals("")) {
            webView.getSettings().setUserAgentString(apps.user_agent);
        }

        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(false);

        //advanced webView settings
        webView.setListener(activity, FragmentWebView.this);

        if (apps.geolocation) {
            webView.getSettings().setGeolocationEnabled(true);
            webView.setGeolocationEnabled(true);
        }

        //webView style
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);

        //webView hardware acceleration
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        //webView chrome client
        View nonVideoLayout = activity.findViewById(R.id.main_non_video_layout);
        ViewGroup videoLayout = activity.findViewById(R.id.main_video_layout);
        @SuppressLint("InflateParams") View progressView = activity.getLayoutInflater().inflate(R.layout.lyt_progress, null);
        webChromeClient = new VideoEnabledWebChromeClient(nonVideoLayout, videoLayout, progressView, (VideoEnabledWebView) webView, progressBar);
        webChromeClient.setOnToggledFullscreen(new WebViewToggledFullscreenCallback());
        webView.setWebChromeClient(webChromeClient);

        //webView client
        webView.setWebViewClient(new MyWebViewClient());

        //webView touch listener
        webView.requestFocus(View.FOCUS_DOWN);
        webView.setOnTouchListener(new WebViewOnTouchListener());

    }

    public boolean handleBackPressed() {
        if (webChromeClient != null && webChromeClient.onBackPressed()) {
            return true;
        }
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            lytProgress.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    private void loadData() {
        if (activity != null) {
            if (Tools.isOnline(activity)) {
                webView.loadUrl(pageUrl);
            } else {
                showProgress(false);
                lytNoNetwork.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
                retryData();
            }
        }
    }

    public void refreshData() {
        if (activity != null) {
            if (Tools.isOnline(activity)) {
                showProgress(true);
                lytNoNetwork.setVisibility(View.GONE);
                lytNoPage.setVisibility(View.GONE);
                String pageUrl = webView.getUrl();
                if (pageUrl == null || pageUrl.equals("")) pageUrl = this.pageUrl;
                webView.loadUrl(pageUrl);
            } else {
                showProgress(false);
                lytNoNetwork.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
                retryData();
            }
        }
    }

    private void retryData() {
        btnRetry.setOnClickListener(v -> refreshData());
    }

    private class WebViewToggledFullscreenCallback implements VideoEnabledWebChromeClient.ToggledFullscreenCallback {
        @Override
        public void toggledFullscreen(boolean fullScreen) {
            if (fullScreen) {
                Tools.hideSystemUI(activity);
            } else {
                Tools.showSystemUI(activity);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private class MyWebViewClient extends WebViewClient {

        private boolean mSuccess = true;

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(final WebView view, final String url) {
            runTaskCallback(() -> {
                if (activity != null && mSuccess) {
                    swipeRefreshLayout.setRefreshing(false);
                    showProgress(false);
                    webView.setVisibility(View.VISIBLE);
                    CookieSyncManager.getInstance().sync();
                }
                mSuccess = true;
            });
        }

        @Override
        public void onReceivedError(final WebView view, final int errorCode, final String description, final String failingUrl) {
            runTaskCallback(() -> {
                if (activity != null) {
                    mSuccess = false;
                    lytNoPage.setVisibility(View.VISIBLE);
                    webView.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    showProgress(false);
                }
            });
        }

        @RequiresApi(Build.VERSION_CODES.M)
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            showProgress(false);
            onReceivedError(view, error.getErrorCode(), error.getDescription().toString(), request.getUrl().toString());
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            lytProgress.setVisibility(View.VISIBLE);
            if (Tools.isDownloadableFile(url)) {
                if (Build.VERSION.SDK_INT > 28) {
                    activity.showSnackBar(getString(R.string.msg_download));
                    Tools.downloadFile(activity, url, Tools.getFileName(url));
                } else if (Tools.checkPermissionWriteExternalStorage(FragmentWebView.this)) {
                    activity.showSnackBar(getString(R.string.msg_download));
                    Tools.downloadFile(activity, url, Tools.getFileName(url));
                    return true;
                }
                return true;
            } else if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
                if (url.contains("target=video") || url.contains("target=audio") || url.contains("target=image")) {
                    Tools.startIntentChooserActivity(activity, url);
                    return true;
                } else if (url.contains("target=url-checker")) {
                    activity.urlChecker();
                    return true;
                } else if (url.contains("package=")) {
                    Tools.startExternalApplication(activity, url);
                    return true;
                } else {
                    ((LoadUrlListener) activity).onLoadUrl(url);
                    boolean external = isLinkExternal(url);
                    boolean internal = isLinkInternal(url);
                    if (!external && !internal) {
                        external = apps.open_link_in_external_browser;
                    }
                    if (external) {
                        Tools.startWebActivity(activity, url);
                        return true;
                    } else {
                        showProgress(true);
                        return false;
                    }
                }
            } else if (url != null && url.startsWith("file://")) {
                ((LoadUrlListener) activity).onLoadUrl(url);
                return false;
            } else {
                return Tools.startIntentActivity(getContext(), url);
            }
        }
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
    }

    @Override
    public void onPageFinished(String url) {
    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {
    }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {
        if (Tools.checkPermissionWriteExternalStorage(FragmentWebView.this)) {
            activity.showSnackBar(getString(R.string.msg_download));
            Tools.downloadFile(activity, url, Tools.getFileName(url));
        }
    }

    @Override
    public void onExternalPageRequest(String url) {
    }

    private boolean isLinkExternal(String url) {
        for (String rule : Tools.LINKS_OPENED_IN_EXTERNAL_BROWSER) {
            if (url.contains(rule)) return true;
        }
        return false;
    }

    private boolean isLinkInternal(String url) {
        for (String rule : Tools.LINKS_OPENED_IN_INTERNAL_WEBVIEW) {
            if (url.contains(rule)) return true;
        }
        return false;
    }

    private void setupToolbar() {
        toolbar.setTitle("");
        toolbarTitle.setText(pageTitle);
        activity.setSupportActionBar(toolbar);

        if (showToolbar) {
            toolbar.setVisibility(View.VISIBLE);
        } else {
            toolbar.setVisibility(View.GONE);
        }

        if (sharedPref.getIsDarkTheme()) {
            toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.color_dark_toolbar));
            toolbar.getContext().setTheme(com.google.android.material.R.style.Base_ThemeOverlay_AppCompat_Dark_ActionBar);
            btnSettings.setColorFilter(ContextCompat.getColor(activity, R.color.color_dark_icon), PorterDuff.Mode.SRC_IN);
            toolbarTitle.setTextColor(ContextCompat.getColor(activity, R.color.color_dark_title_toolbar));
        } else {
            toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.color_white));
            toolbar.getContext().setTheme(com.google.android.material.R.style.ThemeOverlay_AppCompat_Light);
            btnSettings.setColorFilter(ContextCompat.getColor(activity, R.color.color_light_icon), PorterDuff.Mode.SRC_IN);
            toolbarTitle.setTextColor(ContextCompat.getColor(activity, R.color.color_light_title_toolbar));
        }

        btnSettings.setOnClickListener(view -> activity.startActivity(new Intent(activity, ActivityMoreOptions.class)));

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        webView.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        rootView = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void showProgress(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            new Handler().postDelayed(() -> {
                progressBar.setVisibility(View.INVISIBLE);
            }, 1500);
        }
    }

}