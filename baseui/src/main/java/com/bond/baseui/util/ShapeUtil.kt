package com.bond.baseui.util

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.view.View

/**
 * @author zhumingwei
 * @date 2018/9/15 上午11:29
 * @email zdf312192599@163.com
 */
class ShapeUtil

fun View.setShape(radius: Float, color: Int, strokeWidth: Float = 0f) {
    val shapeDrawable = getShapeDrawable(radius, color, strokeWidth)
    shapeDrawable.paint.color = color
    background = shapeDrawable
}


fun getShapeDrawable(radio: Float, color: Int, strokeWidth: Float = 0f): ShapeDrawable {
    val shape = if (strokeWidth == 0f) {
        RoundRectShape(floatArrayOf(radio, radio, radio, radio, radio, radio, radio, radio),
                null,
                null
        )
    } else {
        val ir = if (radio > strokeWidth) {
            radio - strokeWidth
        } else 0f
        RoundRectShape(floatArrayOf(radio, radio, radio, radio, radio, radio, radio, radio),
                RectF(strokeWidth, strokeWidth, strokeWidth, strokeWidth),
                floatArrayOf(ir, ir, ir, ir, ir, ir, ir, ir)
        )
    }
    val shapeDrawable = ShapeDrawable(shape)
    shapeDrawable.paint.color = color
    return shapeDrawable
}

fun View.setRipple(radio: Float, color: Int, strokeWidth: Float = 0f) {
    var rippleDrawable: Drawable? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        RippleDrawable(ColorStateList.valueOf(Color.GREEN), getShapeDrawable(radio, color, strokeWidth), null)
    } else {
        getShapeDrawable(radio, color, strokeWidth)
    }
    background = rippleDrawable

}