package org.helpapaw.helpapaw.privacypolicy;

import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import org.helpapaw.helpapaw.R;

public class PrivacyPolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        WebView webView = findViewById(R.id.pp_web_view);
        webView.setWebChromeClient(new WebChromeClient() {
            private ProgressDialog mProgress;

            @Override
            public void onProgressChanged(WebView wv, int progress) {
                if (PrivacyPolicyActivity.this.isFinishing()) {
                    wv.stopLoading();
                    return;
                }

                if (mProgress == null) {
                    mProgress = new ProgressDialog(PrivacyPolicyActivity.this);
                    mProgress.show();
                }
                mProgress.setMessage("Loading " + progress + "%");
                if (progress == 100) {
                    mProgress.dismiss();
                    mProgress = null;
                }
            }
        });
        webView.loadUrl(getString(R.string.url_privacy_policy));
    }

}
