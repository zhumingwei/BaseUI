package com.bond.baseui.explorer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment

/**
 * @author zhumingwei
 * @date 2018/7/5 下午1:47
 * @email zdf312192599@163.com
 */
class PickConfig(var builder: Builder) {
    companion object {
        val DEFAULT_SPAN_COUNT: Int = 3
        val DEFAULT_PICK_SIZE: Int = 1
        var DEFAULT_CHECK_IMAGE = false
        var DEFAULT_SHOW_GIF = false

        val CROP_AREA_1_1: Int = 1
        val CROP_AREA_4_3: Int = 1
        val CROP_AREA_16_9: Int = 1

        var MODE_SINGLE_PICK = 1
        var MODE_MULTIPLE_PICK = 2

        val PACKAGE: CharSequence = "com.bond.bondnews"

        val PICK_REQUEST_CODE = 10607
        val CROP_REQUEST_CODE = 10011
        val CAMERA_REQUEST_CODE: Int = 10010

        val EXTRA_PICK_BUNDLE: String = "extra_pick_bundle"
        val EXTRA_SPAN_COUNT: String = "extra_span_count"
        val EXTRA_PICK_MODE: String = "extra_pick_mode"
        val EXTRA_MAX_SIZE: String = "extra_max_size"
        val EXTRA_USE_CROP: String = "extra_use_crop"
        val EXTRA_USE_CAMERA: String = "extra_use_camera"
        val EXTRA_CROP_AREA: String = "extra_crop_area"
        val EXTRA_TITLE: String = "extra_title"
        val EXTRA_CHECK_IMAGE: String = "extra_check_image"//是否检查图片
        val EXTRA_SHOW_GIF: String = "extra_show_gif" //是否展示gif

        val EXTRA_PARCELABLE_ARRAYLIST: String = "extra_parcelable_arraylist"
//        val EXTRA_STRING_ARRAYLIST: String = "extra_string_arraylist"
        val EXTRA_CORP_PATH: String = "extra_corp_path"


    }

    var spanCount: Int
    var pickMode: Int
    var maxPickSize: Int
    var useCrop: Boolean
    var userCamera: Boolean
    var crop_area: Int
    var title: String;
    var checkImage: Boolean
    var showGit: Boolean

    var bundle: Bundle


    private var context: Activity? = null
    private var fragment: Fragment? = null

    constructor(activity: Activity?, builder: Builder) : this(builder) {
        this.context = activity
    }

    constructor(fragment: Fragment?,builder: Builder):this(builder){
        this.fragment = fragment
    }


    init {
        spanCount = builder.spanCount;
        pickMode = builder.pickMode
        maxPickSize = builder.maxPickSize
        useCrop = builder.useCrop
        userCamera = builder.useCamera
        crop_area = builder.crop_area
        title = builder.title
        checkImage = builder.checkImage
        showGit = builder.showGit

        bundle = Bundle().apply {
            putInt(EXTRA_SPAN_COUNT, spanCount)
            putInt(EXTRA_PICK_MODE, if (useCrop) MODE_SINGLE_PICK else pickMode)
            putInt(EXTRA_MAX_SIZE, maxPickSize)
            putBoolean(EXTRA_USE_CROP, useCrop)
            putBoolean(EXTRA_USE_CAMERA, userCamera)
            putInt(EXTRA_CROP_AREA, crop_area)
            putString(EXTRA_TITLE, title)
            putBoolean(EXTRA_CHECK_IMAGE, checkImage)
            putBoolean(EXTRA_SHOW_GIF, showGit)
        }
    }

    fun startPick(){
        if (context !=null){
            startPick(context!!,bundle)
        } else if (fragment != null) {
            return startPick(fragment!!, bundle)
        }
    }



    private fun startPick(context: Activity, bundle: Bundle) {
        var intent: Intent = Intent()
        intent.putExtra(EXTRA_PICK_BUNDLE, bundle)
        intent.setClass(context, PickPhotosActivity::class.java)
        context.startActivityForResult(intent, if (useCrop) CROP_REQUEST_CODE else PICK_REQUEST_CODE)
    }

    private fun startPick(fragment: Fragment, bundle: Bundle) {
        var intent: Intent = Intent()
        intent.putExtra(EXTRA_PICK_BUNDLE, bundle)
        intent.setClass(fragment.context, PickPhotosActivity::class.java)
        fragment.startActivityForResult(intent, if (useCrop) CROP_REQUEST_CODE else PICK_REQUEST_CODE)
    }


     class Builder {
        private var context: Activity? = null
        private var fragment: Fragment? = null

        var spanCount: Int = DEFAULT_SPAN_COUNT
        var pickMode: Int = MODE_SINGLE_PICK
        var maxPickSize: Int = DEFAULT_PICK_SIZE
        var useCrop: Boolean = false
        var useCamera: Boolean = false
        var crop_area: Int = CROP_AREA_1_1
        var title: String = "选择图片"
        var checkImage: Boolean = DEFAULT_CHECK_IMAGE
        var showGit: Boolean = DEFAULT_SHOW_GIF

        constructor(context: Activity) {
            this.context = context
        }

        constructor(fragment: Fragment) {
            this.fragment = fragment
        }

        fun spanCount(spancount: Int): Builder {
            this.spanCount = spancount
            return this
        }

        fun setPickMode(pickMode: Int): Builder {
            this.pickMode = pickMode
            return this
        }

        fun maxPickSize(maxPickSize: Int): Builder {
            this.maxPickSize = maxPickSize
            return this;
        }

        fun useCrop(useCrop: Boolean): Builder {
            this.useCrop = useCrop
            return this
        }

        fun useCamera(useCamera: Boolean): Builder {
            this.useCamera = useCamera
            return this
        }

        fun cropArea(crop_area: Int): Builder {
            this.crop_area = crop_area;
            return this
        }

        fun checkImage(checkImage: Boolean): Builder {
            this.checkImage = checkImage
            return this
        }

        fun showGif(showGif: Boolean): Builder {
            this.showGit = showGit
            return this
        }

        public fun build(): PickConfig? {
            if (context != null) {
                return PickConfig(context!!, this)
            } else if (fragment != null) {
                return PickConfig(fragment!!, this)
            } else {
                return null
            }
        }

    }


}
