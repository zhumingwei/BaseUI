package com.bond.baseui.util;

/**
 * @author zhumingwei
 * @date 2018/6/29 下午6:55
 * @email zdf312192599@163.com
 */
public class DoubleClickHelper {
    private static final long DEFAULT = 300L;
    private static long lastClickTime = 0;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        lastClickTime = time;
        return 0 < timeD && timeD < DEFAULT;
    }
}
