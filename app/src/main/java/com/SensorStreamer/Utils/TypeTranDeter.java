package com.SensorStreamer.Utils;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;


/**
 * 判断类型转换是否成功的工具函数
 * @author chen
 * @version 1.0
 * */

public class TypeTranDeter {
    private final static String LOG_TAG = "TypeTranDeter";
    private static final Gson gson = new Gson();

    public static boolean canStr2Num(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            Log.e(TypeTranDeter.LOG_TAG, "canStr2Num:NumberFormatException", e);
            return false;
        }
    }

    public static boolean canStr2JsonData(String str, Class<?> object) {
        try {
            gson.fromJson(str, object);
            return true;
        } catch (JsonSyntaxException e) {
            Log.e(TypeTranDeter.LOG_TAG, "canStr2JsonData:JsonSyntaxException", e);
            return false;
        }
    }
}
