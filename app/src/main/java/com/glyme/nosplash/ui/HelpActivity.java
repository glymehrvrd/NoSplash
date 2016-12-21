package com.glyme.nosplash.ui;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import com.glyme.nosplash.R;

/**
 * Created by Glyme on 2016/11/28.
 */
public class HelpActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setLogo(R.mipmap.ic_launcher);
        getActionBar().setDisplayUseLogoEnabled(true);
        setContentView(R.layout.activity_help);

        WebView webview = (WebView) findViewById(R.id.webViewHelp);
        webview.setVerticalScrollBarEnabled(false);
        webview.setHorizontalScrollBarEnabled(false);
        webview.loadUrl("file:///android_asset/help.html");
    }
}
