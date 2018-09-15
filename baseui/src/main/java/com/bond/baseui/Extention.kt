package com.bond.baseui

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bond.baseui.util.UIUtil

/**
 *
 *   @author zhumingwei
 *   @date 2018/9/15 上午11:33
 *   @email zdf312192599@163.com
 */

fun Context?.color(resourceId: Int) = ContextCompat.getColor(this!!, resourceId)

fun View?.color(resourceId: Int) = this?.context.color(resourceId)

fun Fragment.color(colorId: Int) = ContextCompat.getColor(activity as FragmentActivity, colorId)

fun Context?.dip2px(dimen: Number) = UIUtil.dip2px(this, dimen.toFloat())
fun View.lp(width: Int = ViewGroup.LayoutParams.MATCH_PARENT, height: Int = ViewGroup.LayoutParams.WRAP_CONTENT): View {
    if (layoutParams == null)
        layoutParams = ViewGroup.LayoutParams(width, height)
    else {
        layoutParams.height = height
        layoutParams.width = width
    }
    return this
}

infix fun <A, B, C> Pair<A, B>.tri(that: C): Triple<A, B, C> = Triple(this.first, this.second, that)

fun ViewGroup.inflate(resID: Int): View {
    return LayoutInflater.from(context).inflate(resID, this, false)
}