package com.app.solodroidwebviewjson3.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.app.solodroidwebviewjson3.R;
import com.app.solodroidwebviewjson3.utils.Tools;
import com.google.android.material.snackbar.Snackbar;

public class ActivityWebView extends AppCompatActivity {

    String title;
    String url;
    WebView webView;
    FrameLayout customViewContainer;
    ProgressBar progressBar;
    private WebChromeClient webChromeClient;
    private WebChromeClient.CustomViewCallback customViewCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_webview);
        Tools.setNavigation(this);

        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        url = intent.getStringExtra("url");

        initView();
        loadData();
        setupToolbar();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (customViewCallback != null) {
                    webChromeClient.onHideCustomView();
                } else if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    finish();
                }
            }
        });

    }

    private void initView() {
        webView = findViewById(R.id.webView);
        customViewContainer = findViewById(R.id.customViewContainer);
        progressBar = findViewById(R.id.progressBar);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void loadData() {

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setSupportZoom(true);
        webView.setWebViewClient(new CustomWebViewClient());
        webView.loadUrl(url);

        customViewContainer = findViewById(R.id.customViewContainer);
        webChromeClient = new WebChromeClient() {
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customViewCallback != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                customViewCallback = callback;
                webView.setVisibility(View.INVISIBLE);
                customViewContainer.setVisibility(View.VISIBLE);
                customViewContainer.addView(view);
                Tools.fullScreenMode(ActivityWebView.this, true);
            }

            public void onProgressChanged(WebView view, int newProgress) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    progressBar.setProgress(newProgress, true);
                } else {
                    progressBar.setProgress(newProgress);
                }
                if (newProgress == 100) {
                    progressBar.setVisibility(View.INVISIBLE);
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }

            public void onHideCustomView() {
                customViewContainer.removeAllViews();
                WebChromeClient.CustomViewCallback callback = customViewCallback;
                customViewCallback = null;
                if (callback != null) {
                    callback.onCustomViewHidden();
                }
                webView.setVisibility(View.VISIBLE);
                customViewContainer.setVisibility(View.GONE);
                Tools.fullScreenMode(ActivityWebView.this, false);
            }
        };
        webView.setWebChromeClient(webChromeClient);

    }

    private class CustomWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
                if (url.contains("?target=external")) {
                    String newUrl = url.replace("?target=external", "");
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(newUrl));
                    startActivity(intent);
                } else if (url.contains("?package=")) {
                    Tools.startExternalApplication(ActivityWebView.this, url);
                } else {
                    view.loadUrl(url);
                }
            }

            actionHandler("mailto:", Intent.ACTION_SENDTO, url);
            actionHandler("sms:", Intent.ACTION_SENDTO, url);
            actionHandler("tel:", Intent.ACTION_DIAL, url);

            socialHandler(url, "intent://instagram", "com.instagram.android");
            socialHandler(url, "instagram://", "com.instagram.android");
            socialHandler(url, "twitter://", "com.twitter.android");
            socialHandler(url, "https://maps.google.com", "com.google.android.apps.maps");
            socialHandler(url, "https://api.whatsapp.com", "com.whatsapp");
            socialHandler(url, "https://play.google.com/store/apps/details?id=", null);

            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            progressBar.setVisibility(View.GONE);
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.failed_text), Snackbar.LENGTH_SHORT).show();
            view.loadUrl("about:blank");
        }
    }

    public void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        Tools.setupToolbar(this, toolbar, title, true);
    }

    public void actionHandler(String type, String action, String url) {
        if (url != null && url.startsWith(type)) {
            Intent intent = new Intent(action, Uri.parse(url));
            startActivity(intent);
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    public void socialHandler(String url, String socialUrl, String packageName) {
        PackageManager packageManager = getPackageManager();
        if (url != null && url.startsWith(socialUrl)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            try {
                intent.setPackage(packageName);
                intent.setData(Uri.parse(url));
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
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
        webView.destroy();
    }

}
