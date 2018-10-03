package com.bond.baseui.ui

import android.app.Activity
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import com.bond.baseui.util.AndroidUtil
import java.util.*
import kotlin.collections.LinkedHashMap

class AppManager(private val activityStack: Stack<Activity>, private val activityMap: LinkedHashMap<String, Activity>) {

    companion object {
        val instance by lazy {
            AppManager(Stack(), LinkedHashMap());
        }
    }

    fun getStack(): Stack<Activity> = activityStack

    fun getActivitySize(): Int = activityStack.size

    fun addActivity(activity: Activity, key: String?) {
        key?.let {
            if (activityMap.containsKey(key)) {
                activityMap.get(key)?.finish()
                activityMap.put(key, activity)
            }
            addActivity(activity)
        } ?: kotlin.run {
            addActivity(activity)
        }
    }

    private fun addActivity(activity: Activity) {
        if (activityStack.contains(activity)) {
            activityStack.remove(activity)
        }
        activityStack.add(activity)
    }

    fun currentActivity(): Activity {
        return activityStack.lastElement()
    }

    fun finishActivity(activity: Activity, key: String?) {
        key?.let {
            finishActivity(activity)
        } ?: kotlin.run {
            if (activityMap.containsKey(key)) {
                activityMap.remove(key)
            }
            finishActivity(activity)
        }

    }

    private fun finishActivity(activity: Activity?) {
        activityStack.remove(activity)
    }

    fun finishActivity() {
        val activity = currentActivity()
        finishActivity(activity)
        removeIfInMap(activity)
    }

    private fun removeIfInMap(activity: Activity) {
        activityMap.values.forEach {
            if (it == activity) {
                it.finish()
            }
        }
    }

    /**
     * 结束指定类名的Activity
     */
    fun finishActivity(cls: Class<*>) {
        synchronized(activityStack) {
            val iterator = activityStack.iterator()
            while (iterator.hasNext()) {
                val activity = iterator.next()
                if (activity.javaClass == cls) {
                    iterator.remove()
                    if (!activity.isFinishing) {
                        removeIfInMap(activity)
                        activity.finish()
                    }
                }
            }

        }
    }

    /**
     * 结束所有Activity
     */
    fun finishAllActivity() {
        if (activityStack == null || activityStack.size == 0)
            return

        var i = 0
        val size = activityStack.size
        while (i < size) {
            if (null != activityStack[i]) {
                val activity = activityStack[i]
                if (activity != null && !activity.isFinishing) {
                    activity.finish()
                }
                //                activityStack.get(i).finish();
            }
            i++
        }
        activityStack.clear()
        activityMap.clear()
    }

    fun removeAllWithout(target: Activity) {
        synchronized(activityStack) {
            val iterator = activityStack.iterator()
            while (iterator.hasNext()) {
                val activity = iterator.next()
                if (activity !== target) {
                    iterator.remove()
                    if (!activity.isFinishing) {
                        removeIfInMap(activity)
                        activity.finish() // 否则会对一个Activity调用两次finish
                    }
                }
            }
        }
    }

    /**
     * 退出应用程序
     */
    fun AppExit(context: Context) {
        try {
            finishAllActivity()
            val activityMgr = context
                    .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityMgr.killBackgroundProcesses(context.packageName)
            System.exit(0)
        } catch (e: Exception) {
        }

    }

    /**
     * 退出应用程序并发布重启的广播
     * 在宿主中注册{}
     * action为"com.wallstreetcn.library.app.RESTARTAPP"的服务
     */
    fun AppRestart(context: Context) {
        try {
            finishAllActivity()
            if (AndroidUtil.isAppOnForeground(context)) {
                val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                val restartIntent = PendingIntent.getActivity(context.applicationContext, 0, intent, PendingIntent.FLAG_ONE_SHOT)
                val mgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 10, restartIntent)
            }
            android.os.Process.killProcess(android.os.Process.myPid())
        } catch (e: Exception) {
        }
    }

    fun getPreActivity(): Activity? {
        val size = activityStack.size
        return if (size < 2) null else activityStack.elementAt(size - 2)
    }


}
