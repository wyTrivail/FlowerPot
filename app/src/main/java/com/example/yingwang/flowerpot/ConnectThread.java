package com.example.yingwang.flowerpot;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.util.Log;
import android.os.Handler;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by yingwang on 15/1/21.
 */
public class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private final BluetoothAdapter mmAdapter;
    private final Handler mmHandler;
    private AbstractConnectedHandler connectedHandler;
    private static final UUID uuid = UUID.randomUUID();
    private static String TAG = "connect thread";
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("ea87c0d0-afac-11de-8a39-0800200c9a66");
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public ConnectThread(BluetoothDevice device, BluetoothAdapter adapter,Handler handler, AbstractConnectedHandler chandler) {
        Log.e(TAG, "连接");
        mmDevice = device;
        mmAdapter = adapter;
        mmHandler = handler;
        BluetoothSocket tmp = null;
        connectedHandler = chandler;


        // Get a BluetoothSocket for a connection with the
        // given BluetoothDevice

        try {
            Method m = device.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
            tmp = (BluetoothSocket) m.invoke(device, 1);
            /*if(device.getUuids() == null){
                Log.e("asd","uuid null");
                tmp = device.createInsecureRfcommSocketToServiceRecord(uuid);
            }else {
                UUID myUUID = device.getUuids()[0].getUuid();
                Log.e("asd",myUUID.toString());
                tmp = device.createInsecureRfcommSocketToServiceRecord(myUUID);
            }*/

        } catch (Exception e) {
            Log.e(TAG, "connect failed", e);
        }
        mmSocket = tmp;

    }

    public void run() {
        setName("ConnectThread");

        // Always cancel discovery because it will slow down a connection
        mmAdapter.cancelDiscovery();

        // Make a connection to the BluetoothSocket
        try {
            // This is a blocking call and will only return on a
            // successful connection or an exception
            mmSocket.connect();
            Log.e("mm","start handle connect");
            connectedHandler.task(mmSocket,mmHandler);
        } catch (IOException e) {
            Log.e(TAG, "connect failed", e);
            mmHandler.obtainMessage(Constants.MESSAGE_CONNECT_FAILED).sendToTarget();
        }finally {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "unable to close() socket during connection failure", e);
            }
        }
    }

    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "close() of connect " +  " socket failed", e);
        }
    }
}
