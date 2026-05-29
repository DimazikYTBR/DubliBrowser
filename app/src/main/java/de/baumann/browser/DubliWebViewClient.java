package de.baumann.browser;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.graphics.Bitmap;
import android.webkit.WebResourceRequest;

public class DubliWebViewClient extends WebViewClient {

    private final WebViewClientCallback callback;

    public interface WebViewClientCallback {
        void onPageStarted(String url);
        void onPageFinished(String url);
    }

    public DubliWebViewClient(WebViewClientCallback callback) {
        this.callback = callback;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        view.loadUrl(request.getUrl().toString());
        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        if (callback != null) callback.onPageStarted(url);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (callback != null) callback.onPageFinished(url);
    }
}