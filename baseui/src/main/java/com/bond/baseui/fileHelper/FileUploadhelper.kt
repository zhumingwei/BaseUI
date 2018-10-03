package com.bond.baseui.fileHelper

import android.util.Log
import com.bond.baseui.BuildConfig
import com.qiniu.android.common.AutoZone
import com.qiniu.android.storage.*
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter

/**
 * @author zhumingwei
 * @date 2018/7/18 上午10:33
 * @email zdf312192599@163.com
 */
object FileUploadhelper {

    fun log(msg: String) {
        if (BuildConfig.DEBUG) {
            Log.d("FileUploadhelper", msg)
        }
    }

    fun upload(file: String, token: String, key: String, e: FlowableEmitter<UploadResult>) {

        val configuration = Configuration.Builder()
                .chunkSize(512 * 1024)        // 分片上传时，每片的大小。 默认256K
                .putThreshhold(1024 * 1024)   // 启用分片上传阀值。默认512K
                .connectTimeout(10)           // 链接超时。默认10秒
                .useHttps(true)               // 是否使用https上传域名
                .responseTimeout(60)          // 服务器响应超时。默认60秒
                .zone(AutoZone.autoZone)        // 设置区域，指定不同区域的上传域名、备用域名、备用IP。
                .build()
        val uploadManager = UploadManager(configuration)
        uploadManager.put(file, key, token, { key, info, response ->
            if (info != null && info.isOK) {
                log("上传成功")
                e.onNext(UploadResult(key, 1.0, isOk = true))
            } else {
                log("上传失败")
            }
        }, UploadOptions(null, null, false, UpProgressHandler { key, percent ->
            log(key + "上传进度:" + percent)
            e.onNext(UploadResult(key, percent))
        }, UpCancellationSignal { false }, NetReadyHandler { }))
    }

    fun uploadQiniu(file: String, token: String, key: String): Flowable<UploadResult> {
        return Flowable.create({ e -> upload(file, token, key, e) }, BackpressureStrategy.LATEST)
    }

    data class UploadResult(var key: String, var percent: Double, var url: String = "", var isOk: Boolean = false)
}
