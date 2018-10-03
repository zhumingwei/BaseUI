package com.bond.baseui.util

import android.annotation.TargetApi
import android.os.Build
import android.os.Environment
import android.support.annotation.ColorInt
import android.view.View
import android.view.Window
import android.view.WindowManager
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*

class LightStatusBarCompat {

    companion object {
        private val IMPL: ILightStatusBar by lazy {
            if (MIUILightStatusBarImpl.isMe()) {
                MIUILightStatusBarImpl()
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                MLightStatusBarImpl()
            } else if (MeizuLightStatusBarImpl.isMe()) {
                MeizuLightStatusBarImpl()
            } else {
                object : ILightStatusBar {
                    override fun setLightStatusBar(window: Window, lightStatusBar: Boolean) {}
                }
            }
        }

        fun isSuppotLightMode(): Boolean {
            return MIUILightStatusBarImpl.isMe() || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || MeizuLightStatusBarImpl.isMe()
        }

        fun decorateFakeStatusBar(statusBar: View, actionBarLightMode: Boolean) {
            decorateFakeStatusBar(statusBar, actionBarLightMode, -1)
        }

        fun decorateFakeStatusBar(statusBar: View, actionBarLightMode: Boolean, @ColorInt color: Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                val statusHeight = UIUtil.getStatusBarHeight(statusBar.context)

                val statusBarParams = statusBar.layoutParams
                statusBarParams.height = statusHeight
                statusBar.layoutParams = statusBarParams

            }

            if (actionBarLightMode && LightStatusBarCompat.isSuppotLightMode()) {
                statusBar.setBackgroundColor(if (color <= 0) -0x1 else color)//0x33000000
            } else {
                statusBar.setBackgroundColor(0x33000000)//0x33000000
            }
        }

        //true 黑色 false 白色
        fun setLightStatusBar(window: Window, lightStatusBar: Boolean) {
            IMPL.setLightStatusBar(window, lightStatusBar)
        }

    }


    interface ILightStatusBar {
        abstract fun setLightStatusBar(window: Window, lightStatusBar: Boolean)
    }




    private class MLightStatusBarImpl : ILightStatusBar {

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        override fun setLightStatusBar(window: Window, lightStatusBar: Boolean) {
            // 设置浅色状态栏时的界面显示
            val decor = window.decorView
            var ui = decor.systemUiVisibility
            if (lightStatusBar) {
                ui = ui or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                ui = ui and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            }
            decor.systemUiVisibility = ui

        }
    }

    private class MIUILightStatusBarImpl : ILightStatusBar {

        override fun setLightStatusBar(window: Window, lightStatusBar: Boolean) {
            val clazz = window.javaClass
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val decor = window.decorView
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    var ui = decor.systemUiVisibility
                    if (lightStatusBar) {
                        ui = ui or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    } else {
                        ui = ui and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                    }
                    decor.systemUiVisibility = ui
                }
                val layoutParams = Class.forName("android.view.MiuiWindowManager\$LayoutParams")
                val field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE")
                val darkModeFlag = field.getInt(layoutParams)
                val extraFlagField = clazz.getMethod("setExtraFlags", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                extraFlagField.invoke(window, if (lightStatusBar) darkModeFlag else 0, darkModeFlag)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        companion object {

            private val KEY_MIUI_VERSION_CODE = "ro.miui.ui.version.code"
            private val KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name"
            private val KEY_MIUI_INTERNAL_STORAGE = "ro.miui.internal.storage"

            internal// ignore all exception
            fun isMe(): Boolean {
                var fis: FileInputStream? = null
                try {
                    fis = FileInputStream(File(Environment.getRootDirectory(), "build.prop"))
                    val prop = Properties()
                    prop.load(fis)
                    return (prop.getProperty(KEY_MIUI_VERSION_CODE) != null
                            || prop.getProperty(KEY_MIUI_VERSION_NAME) != null
                            || prop.getProperty(KEY_MIUI_INTERNAL_STORAGE) != null)
                } catch (e: IOException) {
                    return false
                } finally {
                    if (fis != null) {
                        try {
                            fis.close()
                        } catch (e: IOException) {
                        }

                    }
                }
            }
        }
    }

    private class MeizuLightStatusBarImpl : ILightStatusBar {

        override fun setLightStatusBar(window: Window, lightStatusBar: Boolean) {
            val params = window.attributes
            try {
                val darkFlag = WindowManager.LayoutParams::class.java.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON")
                val meizuFlags = WindowManager.LayoutParams::class.java.getDeclaredField("meizuFlags")
                darkFlag.isAccessible = true
                meizuFlags.isAccessible = true
                val bit = darkFlag.getInt(null)
                var value = meizuFlags.getInt(params)
                if (lightStatusBar) {
                    value = value or bit
                } else {
                    value = value and bit.inv()
                }
                meizuFlags.setInt(params, value)
                window.attributes = params
                darkFlag.isAccessible = false
                meizuFlags.isAccessible = false
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        companion object {
            fun isMe(): Boolean = Build.DISPLAY.startsWith("Flyme")
        }
    }

}
