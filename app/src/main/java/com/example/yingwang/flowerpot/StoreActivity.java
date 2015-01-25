package com.example.yingwang.flowerpot;

import com.example.yingwang.flowerpot.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class StoreActivity extends Activity {



    WebView myWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_store);


            String url = getIntent().getStringExtra("url");
            myWebView = (WebView) findViewById(R.id.webView);
            myWebView.setWebViewClient(new WebViewClient() {

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    // TODO Auto-generated method stub
                    view.loadUrl(url);
                    return true;
                }
            });
            myWebView.loadUrl(url);
        }catch (Exception ex){
            Log.e("ads","error",ex);
        }


    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Log.e("asd",myWebView.getUrl());
            if(myWebView.canGoBack()) {
                myWebView.goBack();
                return true;
            }else{
                Intent intent = new Intent();

        /* 指定intent要启动的类 */
                intent.setClass(StoreActivity.this, FullscreenActivity.class);
        /* 启动一个新的Activity */
                StoreActivity.this.startActivity(intent);
        /* 关闭当前的Activity */
                StoreActivity.this.finish();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


}
