package com.example.yingwang.flowerpot;

import com.avos.avoscloud.AVOSCloud;
import com.example.yingwang.flowerpot.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class OpenActivity extends MyBaseActivity {


    ImageView imageView;
    BluetoothDevice bluetoothDevice = null;
    BluetoothAdapter blueAdapter;
    final String targetDeviceName = "Beetle";

    String[] flowerNames = {"虹之玉", "十二卷", "蓝石莲", "其他"};
    boolean[] flowerBool = {true, false};
    Integer flowerID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_open);

        imageView = (ImageView) this.findViewById(R.id.open_view);
        AlphaAnimation anima = new AlphaAnimation(0.3f, 1.0f);
        anima.setDuration(3000);// 设置动画显示时间
        imageView.startAnimation(anima);
        anima.setAnimationListener(new AnimationImpl());
    }

    private class AnimationImpl implements Animation.AnimationListener {


        @Override
        public void onAnimationStart(Animation animation) {
            imageView.setBackgroundResource(R.drawable.open);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            skip(); // 动画结束后跳转到别的页面
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }

    private boolean initBluetooth() {
        blueAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!blueAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 3);

        }else {

            Set<BluetoothDevice> pairedDevices = blueAdapter.getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(targetDeviceName)) {
                    bluetoothDevice = device;
                    return true;
                }
            }
        }
        return false;

    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 3:{
                Set<BluetoothDevice> pairedDevices = blueAdapter.getBondedDevices();
                boolean isFind = false;
                for (BluetoothDevice device : pairedDevices) {
                    if (device.getName().equals(targetDeviceName)) {
                        bluetoothDevice = device;
                        isFind = true;
                        break;
                    }
                }
                if(!isFind){
                    blueAdapter.startDiscovery();
                }
                break;
            }
        }
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_PAIRED: {
                    Toast.makeText(getApplicationContext(), "配对成功", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent();
                    intent.putExtra("fid",flowerID);
                    startActivity(new Intent(OpenActivity.this, FullscreenActivity.class));
                    OpenActivity.this.finish();

                    break;
                }
                default:
                    break;
            }
        }
    };
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                Log.e("device", device.getAddress());
                if (device.getName() != null)
                    Log.e("device", device.getName());
                if (device.getName() != null && device.getName().equals(targetDeviceName)) {
                    Log.e("device", "find a device");

                    bluetoothDevice = device;
                    blueAdapter.cancelDiscovery();
                    try {
                        AlertDialog.Builder builder = new AlertDialog.Builder(OpenActivity.this);
                        builder.setTitle("选择您种植的品种");
                        builder.setItems(flowerNames, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                flowerID = which + 1;
                                new ConnectThread(bluetoothDevice, blueAdapter, handler, new InitHandler(flowerID)).start();
                            }
                        });
                        builder.show();
                    } catch (Exception e) {
                        Log.e("asd", "alert failed", e);
                    }
                }

            }
        }
    };


        private void skip() {
            initavos();
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
            if (initBluetooth()) {
                startActivity(new Intent(OpenActivity.this, FullscreenActivity.class));
            } else {
                Log.e("asd","startdis");
                blueAdapter.startDiscovery();
            }

        }
    private void initavos(){
        AVOSCloud.initialize(this, "0oifw26gzlq8i1hm3z8s15xyz11n0vf7nrxtlma6ksjnjy3c", "z3jhw51zpg2pv7wdfdpwp8osutflgfyoj0cg0zt830h4rohe");
    }

}


