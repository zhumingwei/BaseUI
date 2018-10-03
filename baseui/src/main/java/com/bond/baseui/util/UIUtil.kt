package com.bond.baseui.util

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Looper
import android.provider.Settings
import android.support.annotation.StringRes
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast

object UIUtil {


    fun showToast(context: Context, @StringRes msg: Int) {
        showShortToast(context, context.getString(msg))
    }

    fun showShortToast(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun showOrHiddenKeyBoard(view: View) {
        view.requestFocus()
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED)//SHOW_FORCED，RESULT_HIDDEN
    }

    fun hiddenKeyboard(activity: Activity) {
        val inputMethod = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) view = activity.window.decorView
        if (view != null) {
            inputMethod.hideSoftInputFromWindow(view.windowToken, 0)
        }

    }

    fun showKeyBoard(view: View) {
        view.requestFocus()
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED)//SHOW_FORCED，RESULT_HIDDEN
    }

    /**
     * 设置statusBar透明兼容4.4 / 5.x / 6.x
     * 适用于有图片为头部的页面
     *
     * @param activity flag_status：0表示取消，WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS 表示透
     */
    fun setStatusBarTranslucentCompat(activity: Activity) {
        if (SDK_INT < Build.VERSION_CODES.KITKAT)
            return

        val window = activity.window

        if (SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        } else {
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }


    fun setNavigationBarTranslucent(activity: Activity) {
        if (SDK_INT < Build.VERSION_CODES.KITKAT)
            return

        if (!checkDeviceHasNavigationBar(activity))
            return

        val window = activity.window
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
    }

    /**
     * 判断设备是否有NavigationBar
     *
     * @param activity
     * @return
     */
    fun checkDeviceHasNavigationBar(activity: Context): Boolean {

        //通过判断设备是否有返回键、菜单键(不是虚拟键,是手机屏幕外的按键)来确定是否有navigation bar
        val hasMenuKey = ViewConfiguration.get(activity)
                .hasPermanentMenuKey()
        val hasBackKey = KeyCharacterMap
                .deviceHasKey(KeyEvent.KEYCODE_BACK)

        return if (!hasMenuKey && !hasBackKey) {
            // 做任何你需要做的,这个设备有一个导航栏
            true
        } else false
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    fun dip2px(context: Context?, dpValue: Float): Int {
        if (context == null)
            return 0
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    fun px2dip(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    /**
     * 获取状态栏高度
     *
     * @param context
     * @return
     */
    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier(
                "status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    /**
     * 获得屏幕
     *
     * @param context
     * @return
     */
    fun getDisplayWH(context: Context): IntArray {
        val screenWidth: Int
        val screenHeight: Int
        val wh = IntArray(2)
        val dm = getDpi(context)
        val density = dm.density // 屏幕密度（像素比例：0.75/1.0/1.5/2.0）
        val densityDPI = dm.densityDpi // 屏幕密度（每寸像素：120/160/240/320）
        val xdpi = dm.xdpi
        val ydpi = dm.ydpi

        screenWidth = dm.widthPixels // 屏幕宽（像素，如：480px）
        screenHeight = dm.heightPixels // 屏幕高（像素，如：800px）

        //Logger.d("density:"+density +"\n"+"densityDPI:"+densityDPI+"\n"+"xdpi:"+xdpi+"\n"+"ydpi:"+ydpi +"\n"+"screenWidth:"+screenWidth+"\n"+"screenHeight:"+screenHeight);
        wh[0] = screenWidth
        wh[1] = screenHeight
        return wh
    }


    fun getScreenwidth(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }

    fun getScreenHeight(context: Context): Int {
        return context.resources.displayMetrics.heightPixels
    }

    // 获取屏幕DPI
    fun getDpi(context: Context): DisplayMetrics {
        var dm = DisplayMetrics()
        dm = context.resources.displayMetrics
        return dm
    }

    fun sp2px(context: Context, spValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }

    /**
     * 设置当前屏幕亮度值 0--255，并使之生效
     */
    fun setBrightness(act: Activity, value: Int) {
        try {
            Log.d("setBrightness", value.toString() + "")
            val lp = act.window.attributes
            lp.screenBrightness = (if (value <= 0) 1 else value) / 255f
            act.window.attributes = lp
        } catch (e: Exception) {
//        ToastUtil.show("无法改变亮度")
            //            Toast.makeText(act, "无法改变亮度", Toast.LENGTH_SHORT).show();
        }

    }

    fun getBrightness(act: Activity): Int {
        try {
            return Settings.System.getInt(act.contentResolver, Settings.System.SCREEN_BRIGHTNESS, -1)
        } catch (e: Exception) {
            return 1
        }

    }

    fun getWindowBrightness(act: Activity): Int {
        try {
            val lp = act.window.attributes
            return (lp.screenBrightness * 255).toInt()
        } catch (e: Exception) {
            return 1
        }

    }

    fun isOnMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }
}