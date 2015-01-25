package com.example.yingwang.flowerpot;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yingwang on 15/1/22.
 */
public abstract class AbstractConnectedHandler {
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    private BufferedReader in;
    protected BluetoothSocket mmSocket;
    protected Handler mmHandler;
    private final String TAG = "connected handler";
    public abstract void inner_task();
    public void task(BluetoothSocket socket, Handler handler){
        mmHandler = handler;
        mmSocket = socket;
        readyStream(socket);
        inner_task();
    }
    public void write(byte[] buffer) {
        try {
            mmOutStream.write(buffer);
        } catch (IOException e) {
            Log.e(TAG, "Exception during write", e);
        }
    }

    public String read(){
        Log.e("asdasd","read");
        try{
            String result = in.readLine();
            Log.e("asd",result);
            return result;
        }catch (Exception ex){
            Log.e(TAG, "Exception during read", ex);
        }
        return "";
    }
    private void readyStream(BluetoothSocket socket){
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the BluetoothSocket input and output streams
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "temp sockets not created", e);
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
        in = new BufferedReader(new InputStreamReader(mmInStream));
    }

    protected  static String getTime(){
        return new SimpleDateFormat("HH:mm").format(new Date());
    }
}
