package com.SensorStreamer.Utils;

/**
 * 模式匹配工具函数
 * @author chen
 * @version 1.0
 * */

public class PatternMatch {
    /**
     * IPV4简单模式匹配
     * 只检查最基本的格式
     * @param ipv4 待验证字符串
     * @return 匹配结果
     * */
    public static boolean ipv4SimpleMatch(String ipv4) {
        String ipv4Regex = "(\\d{1,3}\\.){3}\\d{1,3}";
        return ipv4.matches(ipv4Regex);
    }
}
