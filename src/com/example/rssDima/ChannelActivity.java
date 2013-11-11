package com.example.rssDima;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;


public class ChannelActivity extends Activity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel);

        TextView textViewTitle = (TextView) findViewById(R.id.textViewTitle);
        TextView textViewLink = (TextView) findViewById(R.id.textViewLink);
        textViewTitle.setText(getIntent().getStringExtra("title"));
        textViewLink.setText(getIntent().getStringExtra("link"));
        WebView webView = (WebView) findViewById(R.id.webView);
        webView.loadData(getIntent().getStringExtra("summaries"), "text/html; charset=UTF-8", null);
    }
}