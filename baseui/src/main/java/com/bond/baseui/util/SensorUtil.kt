package com.bond.baseui.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import io.reactivex.FlowableOnSubscribe

/**
 * @author zhumingwei
 * @date 2018/7/4 下午1:59
 * @email zdf312192599@163.com
 */
class SensorUtil {
    var emitter: FlowableEmitter<SensorEvent>? = null
    fun observer(context: Context, type: Int): Flowable<SensorEvent> {
        var flowable = Flowable.create(object : FlowableOnSubscribe<SensorEvent> {
            override fun subscribe(e: FlowableEmitter<SensorEvent>?) {
                emitter = e
            }

        }, BackpressureStrategy.LATEST)
        var mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        var mSensor = mSensorManager.getDefaultSensor(type)
        mSensorManager.registerListener(object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                emitter?.onNext(event)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }, mSensor, SensorManager.SENSOR_DELAY_FASTEST)

        return flowable
    }
}

fun Context.lightSensor(): Flowable<SensorEvent> {
    return SensorUtil().observer(this, Sensor.TYPE_LIGHT)//亮度
}

fun Context.sensor(type: Int): Flowable<SensorEvent> {
    return SensorUtil().observer(this, type)
}