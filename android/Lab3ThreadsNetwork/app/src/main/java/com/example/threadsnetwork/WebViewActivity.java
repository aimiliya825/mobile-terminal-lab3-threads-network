package com.example.threadsnetwork;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * 程序3：WebView 加载网页。
 *
 * 1) 初始化 WebView：调用 getSettings() 打开 JavaScript、DOM 存储等；
 * 2) 设置 WebViewClient：在 onPageStarted / onPageFinished / onReceivedError
 *    中处理页面加载过程；
 * 3) loadUrl() 加载 EditText 中输入的网址。
 */
public class WebViewActivity extends Activity {

    private EditText urlText;
    private Button goButton;
    private WebView webView;
    private TextView tvStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        urlText = (EditText) findViewById(R.id.url_field);
        goButton = (Button) findViewById(R.id.go_button);
        webView = (WebView) findViewById(R.id.web_view);
        tvStatus = (TextView) findViewById(R.id.tvStatus);

        // 初始化 WebView：允许执行 JavaScript、支持缩放和 DOM 存储
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        // 页面加载处理：开始、结束、出错时更新状态提示
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                tvStatus.setText("页面状态：正在加载 " + url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                tvStatus.setText("页面状态：加载完成 " + url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                tvStatus.setText("页面状态：加载失败（" + errorCode + "）" + description);
            }
        });

        goButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                openBrowser();
            }
        });

        urlText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    openBrowser();
                    return true;
                }
                return false;
            }
        });

        // 打开页面时先加载默认网址
        openBrowser();
    }

    /* 打开 EditText 中输入网址的网页 */
    private void openBrowser() {
        String url = urlText.getText().toString().trim();
        if (url.length() == 0) {
            url = "http://10.0.2.2:8080/index.html";
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        urlText.setText(url);
        // WebView 初始化：设置支持 JavaScript 脚本
        webView.getSettings().setJavaScriptEnabled(true);
        // 加载指定网址
        webView.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
