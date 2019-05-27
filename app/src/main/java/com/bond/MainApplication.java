package com.bond;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

/**
 * @author zhumingwei
 * @date 2019/4/9 下午1:54
 * @email zdf312192599@163.com
 */
public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), "725dee4cb1", true);
    }
}
