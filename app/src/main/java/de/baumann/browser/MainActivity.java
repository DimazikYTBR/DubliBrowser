package de.baumann.browser;

import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private EditText urlInput;
    private ImageButton btnBack;
    private ImageButton btnForward;
    private ImageButton btnRefresh;
    private ImageButton btnMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        urlInput = findViewById(R.id.urlInput);
        btnBack = findViewById(R.id.btnBack);
        btnForward = findViewById(R.id.btnForward);
        btnRefresh = findViewById(R.id.btn_refresh);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        webView.loadUrl("https://google.com");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                urlInput.setText(url);

                if (view.canGoForward()) {
                    btnForward.setVisibility(View.VISIBLE);
                } else {
                    btnForward.setVisibility(View.GONE);
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoBack()) {
                    webView.goBack();
                }
            }
        });

        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webView.canGoForward()) {
                    webView.goForward();
                }
            }
        });

        ImageButton btnMenu = findViewById(R.id.btn_menu);

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final android.app.Dialog dialog = new android.app.Dialog(MainActivity.this, android.R.style.Theme_Material_NoActionBar_Fullscreen);

                dialog.setContentView(R.layout.activity_settings);

                androidx.appcompat.widget.Toolbar settingsToolbar = dialog.findViewById(R.id.toolbar);
                if (settingsToolbar != null) {
                    settingsToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                }

                dialog.show();
            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload();
            }
        });

        float density = getResources().getDisplayMetrics().density;
        int paddingInPx = (int) (80 * density);
        webView.setPadding(
            webView.getPaddingLeft(),
            webView.getPaddingTop(),
            webView.getPaddingRight(),
            paddingInPx
        );
    }
}