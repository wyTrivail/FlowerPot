package com.example.yingwang.flowerpot;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by yingwang on 15/1/24.
 */
public class MyBaseActivity extends Activity {
    private BroadcastReceiver mFinishReceiver = new BroadcastReceiver() {

        @Override

        public void onReceive(Context context, Intent intent) {

            if("finish".equals(intent.getAction())) {

                Log.e("#########", "I am " + getLocalClassName()

                        + ",now finishing myself...");

                finish();

            }

        }

    };

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter();

        filter.addAction("finish");

        registerReceiver(mFinishReceiver, filter);
    }
}
