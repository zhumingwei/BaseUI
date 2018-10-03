package com.bond.baseui.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.AnyThread;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.widget.Toast;

/**
 * Created by chanlevel on 2017/5/31.
 */

public class ToastUtil {

    protected static Handler handler = new Handler(Looper.getMainLooper());

//    public static void show(String notice) {
//        show(notice,Toast.LENGTH_SHORT);
//    }
//    public static void show(String notice,int duration) {
//        if(!TextUtils.isEmpty(notice))
//            handler
//                .post(() -> Toast.makeText(AppContextProvider.get(), notice, duration).show());
//    }
//    public static void show(@StringRes int resId) {
//        if (resId==0) return;
//        handler
//                .post(new Runnable() {
//                    @Override
//                    public void run() {
//                        Toast.makeText(AppContextProvider.get(), AppContextProvider.get().getResources().getString(resId), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }




    private static Toast mToast = null;



    /**
     * 显示Toast，多次调用此函数时，Toast显示的时间不会累计，并且显示内容为最后一次调用时传入的内容
     * 持续时间默认为short
     * @param notice 要显示的内容
     *            {@link Toast#LENGTH_LONG}
     */
    @AnyThread
    public static void show(final String notice){
        show(notice, Toast.LENGTH_SHORT);
    }

    @AnyThread
    public static void show(@StringRes final int tips){
        show(AppContextProvider.get().getString(tips), Toast.LENGTH_SHORT);
    }

    @AnyThread
    public static void show(Context context, @StringRes int resid){
        show(context.getString(resid), Toast.LENGTH_SHORT);
    }

    @AnyThread
    public static void show(Context context, @StringRes int resid, int duration){
        show(context.getString(resid),duration);
    }



    /**
     * 显示Toast，多次调用此函数时，Toast显示的时间不会累计，并且显示内容为最后一次调用时传入的内容
     *
     * @param tips 要显示的内容
     * @param duration 持续时间，参见{@link Toast#LENGTH_SHORT}和
     *            {@link Toast#LENGTH_LONG}
     */
    @SuppressLint("WrongThread")
    @AnyThread
    public static void show(final String tips, final int duration) {
        if (android.text.TextUtils.isEmpty(tips)) {
            return;
        }
        if (isOnMainThread()){
            internalShow(tips, duration);
        }else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    internalShow(tips,duration);
                }
            });
        }
    }


    @UiThread
     static void internalShow(String tips, int duration){
        if (mToast == null) {
            mToast = Toast.makeText(AppContextProvider.get(), tips, duration);
            mToast.show();
        } else {
            //mToast.cancel();
            //mToast.setView(mToast.getView());
            mToast.setText(tips);
            mToast.setDuration(duration);
            mToast.show();
        }
    }

    public static boolean isOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

}
