package com.example.yingwang.flowerpot;

/**
 * Created by yingwang on 15/1/23.
 */
public class Checker {
    public static Integer water = 50;
    public static Integer temp = 25;

    public static final int STATUS_HAPPY = 0;
    public static final int STATUS_SOSO = 1;
    public static final int STATUS_DIE = 2;
    public static final int STATUS_NULL = -1;
    public static final int STATUS_WARTER = 3;
    public static Integer checkStatus(Integer water,Integer temp, Integer isDry){
        Integer total = 100;
        total -= Checker.water - water;
        total -= Math.abs(temp - Checker.temp);
        total -= isDry == 0 ? 10 : 0;

        if(isDry == 1){
            return STATUS_WARTER;
        }
        if(total >= 90){
            return STATUS_HAPPY;
        }else if(total >= 70){
            return STATUS_SOSO;
        }else{
            return STATUS_DIE;
        }
    }
}
