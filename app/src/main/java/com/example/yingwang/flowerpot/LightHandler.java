package com.example.yingwang.flowerpot;

/**
 * Created by yingwang on 15/1/23.
 */
public class LightHandler extends AbstractConnectedHandler {
    @Override
    public void inner_task() {
        this.write("l".getBytes());
    }
}
