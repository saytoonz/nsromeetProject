package com.nsromapa.nsromeet.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nsromapa.nsromeet.R;
import com.nsromapa.nsromeet.utils.Constants;

public class TermsAndPolicies extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_terms_and_policies);
        ImageView back = findViewById(R.id.toolbar_backIv);
        back.setOnClickListener(v -> finish());
        TextView toolbarText = findViewById(R.id.toolbar_text);
        toolbarText.setText(getString(R.string.t_and_c));


        WebView webView = findViewById(R.id.webView);
        ProgressBar progress_circular = findViewById(R.id.progress_circular);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progress_circular.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progress_circular.setVisibility(View.GONE);
            }
        });
        CookieManager manager = CookieManager.getInstance();
        manager.acceptCookie();
        webView.loadUrl(Constants.TERMS_AND_POLICIES);

    }
}
