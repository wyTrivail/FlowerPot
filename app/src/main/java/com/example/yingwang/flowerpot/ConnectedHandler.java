package com.example.yingwang.flowerpot;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.os.Handler;

import com.avos.avoscloud.AVObject;


/**
 * Created by yingwang on 15/1/21.
 */
public class ConnectedHandler extends AbstractConnectedHandler{
    Integer flowerID;
    public ConnectedHandler(Integer flowerID){
        super();
        this.flowerID = flowerID;
    }
    @Override
    public void inner_task() {
        try {
            String r = "r" ;//+ this.getTime();
            Log.e("asd time",r);
            this.write(r.getBytes());
            Integer count = Integer.valueOf(this.read());
            List<String> lines = new ArrayList<String>();

            Integer status = Checker.STATUS_NULL;
            for (int i = 0; i != count; ++i) {
                String result = this.read();
                String[] contents = result.split(",");
                String line = generateLine(contents[0], contents[1], contents[2], Integer.valueOf(contents[3]));
                Log.e("asd", line);
                lines.add(line);
                AVObject record = new AVObject("FlowerRecord");
                record.put("water", Integer.valueOf(contents[1]));
                record.put("temp", Integer.valueOf(contents[2]));
                record.put("time", contents[0]);
                record.put("uid", Constants.UID);
                record.put("fid", flowerID);
                record.put("is_dry", Integer.valueOf(contents[3]));
                if (i == count - 1) {
                    status = Checker.checkStatus(Integer.valueOf(contents[1]), Integer.valueOf(contents[2]), Integer.valueOf(contents[3]));
                    this.mmHandler.obtainMessage(Constants.MESSAGE_PIC_CHANGE, status).sendToTarget();
                }
                try {
                    record.save();
                } catch (Exception ex) {
                    Log.e("AVOS", "save error", ex);
                }
            }
            this.mmHandler.obtainMessage(Constants.MESSAGE_READ,lines).sendToTarget();

        }catch (Exception ex){
            Log.e("asd","asd",ex);
        }


    }

    public static String generateLine(String time,String water,String temp,Integer isDry){
        String line = time + "\t湿度:"+ water + "％\t温度：" + temp + "摄氏度";
        if( isDry == 1){
            line += " 需要加水";
        }
        return line;
    }



}
