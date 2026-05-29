package de.baumann.browser;

import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ImageView;
import android.view.inputmethod.InputMethodManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import de.baumann.browser.DubliWebViewClient;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.webkit.WebResourceRequest;
import android.webkit.WebViewClient;
import android.view.animation.DecelerateInterpolator;
import android.view.ViewStub;

public class MainActivity extends androidx.activity.ComponentActivity {

    private WebView webView;
    private EditText urlInput;
    private ImageButton btnRefresh;
    private ImageButton btnMenu;
    private View customSwitch;
    private TextView siteTitle; 

    private void toggleCapsuleState(boolean isMinimized) {
        View mainCapsule = findViewById(R.id.main_capsule_container);
        View miniCapsule = findViewById(R.id.mini_capsule_container);

        if (isMinimized) {
            mainCapsule.animate().alpha(0f).translationY(100).setDuration(300)
                .withEndAction(() -> mainCapsule.setVisibility(View.GONE));

            miniCapsule.setAlpha(0f);
            miniCapsule.setVisibility(View.VISIBLE);
            miniCapsule.animate().alpha(1f).translationY(0).setDuration(300).start();
        } else {
            mainCapsule.setVisibility(View.VISIBLE);
            mainCapsule.animate().alpha(1f).translationY(0).setDuration(300).start();

            miniCapsule.animate().alpha(0f).translationY(100).setDuration(300)
                .withEndAction(() -> miniCapsule.setVisibility(View.GONE));
        }
    }

    private boolean isCapsuleLocked = true;

    private void toggleCapsule() {
        isCapsuleLocked = !isCapsuleLocked;

        urlInput.setEnabled(!isCapsuleLocked);
        urlInput.setFocusableInTouchMode(!isCapsuleLocked);
        urlInput.setAlpha(isCapsuleLocked ? 0.5f : 1.0f);
        
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        
        if (!isCapsuleLocked) {
            urlInput.requestFocus();
            urlInput.setSelection(urlInput.getText().length());
            if (imm != null) {
                imm.showSoftInput(urlInput, InputMethodManager.SHOW_IMPLICIT);
            }
        } else {
            urlInput.clearFocus();
            if (imm != null) {
                imm.hideSoftInputFromWindow(urlInput.getWindowToken(), 0);
            }
        }
    }

    private boolean isMinimized = false;
    private int lastScrollY = 0;
    private final int SCROLL_THRESHOLD = 20;

    private BrowserViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme); 
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        siteTitle = findViewById(R.id.siteTitle);

        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(BrowserViewModel.class);

        viewModel.getCurrentUrl().observe(this, url -> {
            if (!urlInput.getText().toString().equals(url)) {
                urlInput.setText(url);
            }   
        });

        webView = findViewById(R.id.webView);
        urlInput = findViewById(R.id.urlInput);
        btnRefresh = findViewById(R.id.btn_refresh);
        btnMenu = findViewById(R.id.btn_menu);

        urlInput.setEnabled(false);
        urlInput.setFocusableInTouchMode(false);
        urlInput.setAlpha(0.5f);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(true);

        webView.setOnLongClickListener(v -> {
            WebView.HitTestResult result = webView.getHitTestResult();
            if (result.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE || 
                result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                showCustomContextMenu(result.getExtra());
                return true;
            }
            return false;
        });

        webView.setWebViewClient(new DubliWebViewClient(new DubliWebViewClient.WebViewClientCallback() {
            @Override
            public void onPageStarted(String url) {
                siteTitle.setText("Loading...");
            }

            @Override
            public void onPageFinished(String url) {
                String title = webView.getTitle();
                siteTitle.setText((title != null && !title.isEmpty()) ? title : url);
            }
        }));

        webView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY > oldScrollY + 20 && !isMinimized) {
                isMinimized = true;
                toggleCapsuleState(true);
            } else if (scrollY < oldScrollY - 20 && isMinimized) {
                isMinimized = false;
                toggleCapsuleState(false);
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                String title = view.getTitle();
                TextView siteTitle = findViewById(R.id.siteTitle);
        
                if (title != null && !title.isEmpty()) {
                    siteTitle.setText(title);
                } else {
                    siteTitle.setText(url);
                }
            }
        });

        webView.loadUrl("https://www.google.com");

        urlInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE 
                    || actionId == android.view.inputmethod.EditorInfo.IME_ACTION_GO
                    || (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER)) {
        
                String url = urlInput.getText().toString().trim();
        
                if (!url.isEmpty()) {
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        url = "https://" + url;
                    }
                    webView.loadUrl(url);
                    toggleCapsule(); 
                    v.clearFocus();
                }
                return true;
            }
            return false;
        });

        final android.view.GestureDetector gestureDetector = new android.view.GestureDetector(this, new android.view.GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(android.view.MotionEvent e1, android.view.MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
        
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        if (webView.canGoBack()) webView.goBack();
                    } else {
                        if (webView.canGoForward()) webView.goForward();
                    }
                    return true;
                }
                return false;
            }
        });

        urlInput.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        ImageView btnUnlock = findViewById(R.id.btn_unlock);
        if (btnUnlock != null) {
            btnUnlock.setOnClickListener(v -> toggleCapsule());
        }

        btnMenu.setOnClickListener(v -> {
            final android.app.Dialog dialog = new android.app.Dialog(MainActivity.this, android.R.style.Theme_Material_NoActionBar_Fullscreen);
            dialog.setContentView(R.layout.activity_settings);

            View dialogRoot = dialog.findViewById(R.id.appBarLayout).getRootView();
            View appBarLayout = dialog.findViewById(R.id.appBarLayout);
            View contentFrame = dialog.findViewById(R.id.content_frame);
            androidx.appcompat.widget.Toolbar settingsToolbar = dialog.findViewById(R.id.toolbar);
            View themeSwitch = dialog.findViewById(R.id.theme_switch);
            View thumb = dialog.findViewById(R.id.thumb);

            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(dialogRoot, (view, windowInsets) -> {
                androidx.core.graphics.Insets insets = windowInsets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
                if (appBarLayout != null) appBarLayout.setPadding(0, insets.top, 0, 0);
                if (contentFrame != null) contentFrame.setPadding(contentFrame.getPaddingLeft(), contentFrame.getPaddingTop(), contentFrame.getPaddingRight(), insets.bottom);
                return windowInsets;
            });

            if (themeSwitch != null && thumb != null) {
                float density1 = getResources().getDisplayMetrics().density;
                int thumbMove = (int) (18 * density1);

                themeSwitch.setActivated(webView.getSettings().getJavaScriptEnabled());
                thumb.setTranslationX(themeSwitch.isActivated() ? thumbMove : 0);

                themeSwitch.setOnClickListener(view -> {
                    boolean newState = !themeSwitch.isActivated();
                    themeSwitch.setActivated(newState);
                    thumb.animate().translationX(newState ? thumbMove : 0).setDuration(250).start();
                    webView.getSettings().setJavaScriptEnabled(newState);
                });
            }

            if (settingsToolbar != null) {
                settingsToolbar.setNavigationOnClickListener(backView -> dialog.dismiss());
            }

            dialog.show();
        });

        btnRefresh.setOnClickListener(v -> webView.reload());

        View bottomBarContainer = findViewById(R.id.main_capsule_container);

        if (bottomBarContainer != null) {
            ViewCompat.setOnApplyWindowInsetsListener(bottomBarContainer, (v, windowInsets) -> {
                Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
        
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

                params.bottomMargin = systemBars.bottom + 16; 

                params.leftMargin = 16;
                params.rightMargin = 16;
        
                v.setLayoutParams(params);

                if (webView != null) {
                    webView.setPadding(0, 0, 0, v.getHeight() + params.bottomMargin);
                }
        
                return windowInsets;
            });
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.clearHistory();
            webView.clearCache(true);
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }

    private void showCustomContextMenu(String url) {
        final android.app.Dialog dialog = new android.app.Dialog(MainActivity.this);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
        dialog.setContentView(R.layout.dialog_context_menu);

        TextView tvUrl = dialog.findViewById(R.id.tv_url);
        TextView btnCopy = dialog.findViewById(R.id.btn_copy);
        TextView btnOpen = dialog.findViewById(R.id.btn_open);

        if (tvUrl != null) tvUrl.setText(url);

        if (btnCopy != null) {
            btnCopy.setOnClickListener(v -> {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("URL", url));
                }
                dialog.dismiss();
            });
        }

        if (btnOpen != null) {
            btnOpen.setOnClickListener(v -> {
                webView.loadUrl(url);
                dialog.dismiss();
            });
        }

        dialog.show();
    }
}