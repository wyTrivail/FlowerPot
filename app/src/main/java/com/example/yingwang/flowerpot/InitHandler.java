package com.example.yingwang.flowerpot;

import android.util.Log;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;

import java.util.List;

/**
 * Created by yingwang on 15/1/23.
 */
public class InitHandler extends AbstractConnectedHandler {

    Integer flowerID = null;
    public InitHandler(Integer flowerID){
        this.flowerID = flowerID;
    }
    @Override
    public void inner_task() {
        //get flower biaozhun
        AVQuery<AVObject> query = new AVQuery<AVObject>("FlowerInfo");
        query.whereEqualTo("flower_id", flowerID);

        try {
            List<AVObject> avObjects = query.find();
            if(avObjects != null){
                AVObject avObject = avObjects.get(0);
                String line = generateLine(avObject.getInt("light"));
                Integer water = avObject.getInt("water");
                Integer temp = avObject.getInt("temp");
                Checker.temp = temp;
                Checker.water = water;
                Log.e("init",line);
                this.write(line.getBytes());
                this.mmHandler.obtainMessage(Constants.MESSAGE_PAIRED).sendToTarget();
            }

        } catch (AVException e) {
            Log.d("失败", "查询错误: " + e.getMessage());
        }


    }

    public static String generateLine(Integer lightTime){
        String line = "w," + getTime() + "," + lightTime.toString();
        return line;
    }
}
