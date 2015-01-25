package com.example.yingwang.flowerpot;


import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
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
import android.content.ReceiverCallNotAllowedException;
import android.media.AudioManager;
import android.media.Image;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;



/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity extends MyBaseActivity {

    int music;
    SoundPool sp;
    ListView listView;
    ImageView faceImageView;
    ArrayAdapter<String> arrayAdapter;
    SwipeRefreshLayout swipeRefreshLayout;

    BluetoothAdapter blueAdapter;
    BluetoothDevice bluetoothDevice = null;
    final String targetDeviceName = "Beetle";
    final String dataFileName = "data";
    FileOutputStream fos;
    ConnectThread connectThread;
    String[] flowerNames = {"虹之玉", "十二卷", "蓝石莲", "其他"};
    boolean[] flowerBool = {true,false};
    Integer flowerID = null;
    private boolean initBluetooth(){
        blueAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!blueAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
        }

        Set<BluetoothDevice> pairedDevices = blueAdapter.getBondedDevices();
        for(BluetoothDevice device: pairedDevices){
            if(device.getName().equals(targetDeviceName)){
                bluetoothDevice = device;
                return true;
            }
        }

        return false;
    }
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                Log.e("device", device.getAddress());
                if(device.getName() != null)
                    Log.e("device", device.getName());
                if(device.getName() != null && device.getName().equals(targetDeviceName)){
                    Log.e("device", "find a device");

                    bluetoothDevice = device;
                    blueAdapter.cancelDiscovery();
                    try {
                        AlertDialog.Builder builder = new AlertDialog.Builder(FullscreenActivity.this);
                        builder.setTitle("选择您种植的品种");
                        builder.setItems(flowerNames,new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                flowerID= which+1;
                                new ConnectThread(bluetoothDevice, blueAdapter, handler, new InitHandler(flowerID)).start();
                            }
                        });
                        builder.show();
                    }catch (Exception e){
                        Log.e("asd","alert failed",e);
                    }
                }

            }
        }
    };
    private Handler handler = new Handler(){
        public void handleMessage (Message msg) {
            switch (msg.what){
                case Constants.MESSAGE_READ:
                {
                    ArrayList<String> lines = (ArrayList<String>)msg.obj;
                    try {
                        for(String line: lines) {
                            arrayAdapter.insert(line, 0);
                        }
                    }catch (Exception ex){
                        Log.e("fos", "write data failed", ex);
                    }
                    swipeRefreshLayout.setRefreshing(false);
                    arrayAdapter.notifyDataSetChanged();


                    break;

                }
                case Constants.MESSAGE_CONNECT_FAILED:
                {
                    Toast.makeText(getApplicationContext(),"连接失败,靠近花盆一点哟",Toast.LENGTH_LONG).show();
                    swipeRefreshLayout.setRefreshing(false);
                    break;
                }
                case Constants.MESSAGE_PAIRED:
                {
                    Toast.makeText(getApplicationContext(), "配对成功,下拉可获取数据", Toast.LENGTH_LONG).show();
                    break;
                }
                case Constants.MESSAGE_PIC_CHANGE:
                {
                    Integer status = (Integer)msg.obj;
                    switch (status){
                        case Checker.STATUS_WARTER:{
                            faceImageView.setImageResource(R.drawable.weak);
                            AlertDialog.Builder builder = new AlertDialog.Builder(FullscreenActivity.this);
                            builder.setTitle("自动调节");
                            builder.setMessage("您的植物状态需要浇水，是否进行自动调节");
                            builder.setPositiveButton("是",new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new ConnectThread(bluetoothDevice,blueAdapter,handler,new WaterHandler()).start();
                                    Toast.makeText(getApplicationContext(),"成功发出命令",Toast.LENGTH_LONG).show();
                                }
                            });
                            builder.setNegativeButton("否",new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            builder.show();
                            break;
                        }
                        case Checker.STATUS_DIE:{
                            faceImageView.setImageResource(R.drawable.dead);
                            AlertDialog.Builder builder = new AlertDialog.Builder(FullscreenActivity.this);
                            builder.setTitle("自动调节");
                            builder.setMessage("您的植物状态很糟糕，是否进行自动调节");
                            builder.setPositiveButton("是",new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new ConnectThread(bluetoothDevice,blueAdapter,handler,new WaterHandler()).start();
                                    Toast.makeText(getApplicationContext(),"成功发出命令",Toast.LENGTH_LONG).show();
                                }
                            });
                            builder.setNegativeButton("否",new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            builder.show();
                            break;
                        }
                        case Checker.STATUS_HAPPY:{
                            faceImageView.setImageResource(R.drawable.healthy);
                            Toast.makeText(getApplicationContext(),"植物状态很好，请继续保持哟",Toast.LENGTH_LONG).show();
                            break;
                        }
                        case Checker.STATUS_SOSO:{
                            faceImageView.setImageResource(R.drawable.weak);
                            AlertDialog.Builder builder = new AlertDialog.Builder(FullscreenActivity.this);
                            builder.setTitle("自动调节");
                            builder.setMessage("您的植物状态不太好，是否进行自动调节");
                            builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new ConnectThread(bluetoothDevice, blueAdapter, handler, new WaterHandler()).start();
                                    Toast.makeText(getApplicationContext(), "成功发出命令", Toast.LENGTH_LONG).show();
                                }
                            });
                            builder.setNegativeButton("否",new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            builder.show();
                            break;
                        }
                        default:
                            break;
                    }
                }
                default:
                    break;
            }
        }
    };

    private void initData() throws IOException {
        arrayAdapter = new ArrayAdapter<String>(this,R.layout.list_item);
        /*arrayAdapter.add(ConnectedHandler.generateLine("08:12","30","30",1));
        arrayAdapter.add(ConnectedHandler.generateLine("08:12","30","30",1));*/
        AVQuery<AVObject> query = new AVQuery<AVObject>("FlowerRecord");
        query.whereEqualTo("uid", Constants.UID);
        query.orderByAscending("iid");
        try {
            query.findInBackground(new FindCallback<AVObject>() {
                public void done(List<AVObject> avObjects, AVException e) {
                    if (e == null) {
                        for(AVObject avObject: avObjects){
                            String temp = String.valueOf(avObject.getInt("temp"));
                            String water = String.valueOf(avObject.getInt("water"));
                            Integer isDry = avObject.getInt("is_dry");
                            String time = avObject.getString("time");
                            Integer flowerID = avObject.getInt("fid");
                            FullscreenActivity.this.flowerID = flowerID;
                            String line = ConnectedHandler.generateLine(time,water,temp,isDry);
                            arrayAdapter.insert(line,0);

                        }
                        Log.e("成功", "查询到" + avObjects.size() + " 条符合条件的数据");
                    } else {
                        Log.e("失败", "查询错误: " + e.getMessage());
                     }
                    arrayAdapter.notifyDataSetChanged();
                }
            });


        }catch (Exception ex){
            Log.e("avsearch","failed",ex);
            return;
        }







    }

    protected void onResume(){
        super.onResume();
        count = 0;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        try{
            setContentView(R.layout.activity_fullscreen);

            initData();
            fos = openFileOutput(dataFileName, MODE_APPEND);
            listView = (ListView)findViewById(R.id.list_view);
            faceImageView = (ImageView)findViewById(R.id.face_image);
            arrayAdapter.add("anything");
            listView.setAdapter(arrayAdapter);

            //register receiver for search bluetooth
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

            swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.refresh_layout);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if(bluetoothDevice != null) {
                        connectThread = new ConnectThread(bluetoothDevice, blueAdapter, handler, new ConnectedHandler(flowerID));
                        connectThread.start();
                    }
                }
            });
            flowerID = getIntent().getIntExtra("fid",1);
            if(!initBluetooth()) {
                blueAdapter.startDiscovery();
            }else{
                Toast.makeText(getApplicationContext(), "下拉即可更新数据", Toast.LENGTH_LONG).show();
            }

            binding();

            sp= new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
            music = sp.load(this, R.raw.water, 1);

        }catch (Exception ex){
            Log.e("asd",ex.getMessage());
        }
    }

    @Override
    protected void onDestroy(){
        try {
            unregisterReceiver(mReceiver);
            if(connectThread != null)
                connectThread.cancel();
            if(fos != null)
                fos.close();
            if(blueAdapter != null)
                blueAdapter.cancelDiscovery();
        } catch (Exception e) {
            Log.e("destroy", "error", e);
        }
        super.onDestroy();
    }

    private void binding(){
        ImageButton storeButton = (ImageButton)findViewById(R.id.store_button);
        storeButton.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("url","http://greeting2015.kuaizhan.com");
        /* 指定intent要启动的类 */
                intent.setClass(FullscreenActivity.this, StoreActivity.class);
        /* 启动一个新的Activity */
                FullscreenActivity.this.startActivity(intent);
        /* 关闭当前的Activity */
                FullscreenActivity.this.finish();
            }
        });

        ImageButton spaceButton = (ImageButton)findViewById(R.id.space_button);
        spaceButton.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent();
                    intent.putExtra("url", "http://lunchmeeting.kuaizhan.com");
        /* 指定intent要启动的类 */
                    intent.setClass(FullscreenActivity.this, StoreActivity.class);
        /* 启动一个新的Activity */
                    FullscreenActivity.this.startActivity(intent);
        /* 关闭当前的Activity */
                    FullscreenActivity.this.finish();
                }catch (Exception ex){
                    Log.e("asd","asdasd",ex);
                }
            }
        });


        ImageButton lightButton = (ImageButton)findViewById(R.id.light_button);
        lightButton.setOnClickListener(new ImageButton.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ConnectThread(bluetoothDevice, blueAdapter, handler,new LightHandler()).start();
                Toast.makeText(getApplicationContext(),"成功发出命令",Toast.LENGTH_LONG).show();
            }
        });

        ImageButton waterButton = (ImageButton)findViewById(R.id.water_button);
        waterButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new ConnectThread(bluetoothDevice, blueAdapter, handler, new WaterHandler()).start();
                sp.play(music, 1, 1, 0, 0, 1);
                Toast.makeText(getApplicationContext(),"成功发出命令",Toast.LENGTH_LONG).show();
                return true;
            }
        });


        ImageButton comboButton = (ImageButton)findViewById(R.id.combo);
        comboButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent();
                    intent.putExtra("url", "http://lunchmeeting.kuaizhan.com");
        /* 指定intent要启动的类 */
                    intent.setClass(FullscreenActivity.this, ChartActivity.class);
        /* 启动一个新的Activity */
                    FullscreenActivity.this.startActivity(intent);
        /* 关闭当前的Activity */
                    FullscreenActivity.this.finish();
                }catch (Exception ex){
                    Log.e("asd","asdasd",ex);
                }
            }
        });

        ImageButton shareButton = (ImageButton)findViewById(R.id.share_button);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
                intent.putExtra(Intent.EXTRA_TEXT, "快来一起用肉肉吧！！" + " http://42.96.164.129/app-release.apk");
                //intent.putExtra(Intent.EXTRA_HTML_TEXT, "<a href='http://42.96.164.129/app-release.apk'>猛击此处下载</a>");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(intent, getTitle()));
            }
        });

    }


    static Integer count = 0;
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(++count == 1){
                Toast.makeText(getApplicationContext(),"再次点击退出客户端",Toast.LENGTH_LONG).show();
                return true;
            }else {
                FullscreenActivity.this.getApplicationContext().sendBroadcast(new Intent("finish"));
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }





}
