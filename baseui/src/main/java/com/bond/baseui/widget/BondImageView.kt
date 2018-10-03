package com.bond.baseui.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.support.annotation.DrawableRes
import android.support.v4.app.Fragment
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions

open class BondImageView @kotlin.jvm.JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ImageView(context, attrs, defStyleAttr) {
    val OPTION_UNSET = -1
    private lateinit var withWho: Any
    private var placeHolder: Int = 0
    private var errorHolder: Int = 0
     var radius = 0

    private var transformation: Transformation<Bitmap>? = null
    fun with(fragment: Fragment): BondImageView {
        withWho = fragment
        return this
    }

    fun with(view: View): BondImageView {
        withWho = view
        return this
    }

    fun setTransformer(transformation: Transformation<Bitmap>): BondImageView {
        this.transformation = transformation
        return this
    }


    fun placeholder(@DrawableRes placeHolder: Int): BondImageView {
        this.placeHolder = placeHolder
        return this
    }

    fun error(@DrawableRes errorHolder: Int): BondImageView {
        this.errorHolder = errorHolder
        return this
    }

    fun load(url: String) {
        loadObj(url)
    }

    fun load(uri:Uri){
        loadObj(uri)
    }

    private fun loadObj(url: Any) {
        getRequestManager()
                .load(url)
                .apply(createOption())
                .into(this)
    }

    private fun getRequestManager(): RequestManager {
        var requestManager: RequestManager
        try {
            if (withWho is Fragment)
                requestManager = Glide.with(withWho as Fragment)
            else if (withWho is View)
                requestManager = Glide.with(withWho as View)
            else
                requestManager = Glide.with(context)
        } catch (e: Exception) {
            requestManager = Glide.with(context)
        }

        return requestManager
    }

    private fun createOption(): RequestOptions {
        val options = RequestOptions()

        if (errorHolder > OPTION_UNSET)
            if (placeHolder > 0) {
                options.placeholder(placeHolder)
            }


        if (errorHolder > OPTION_UNSET)
            if (errorHolder > 0) {
                options.error(errorHolder)
            }


        if (optionHeight > 0 && optionWidth > 0) options.override(optionWidth, optionHeight)

        if (radius > 0) {
            options.optionalTransform(RoundedCorners(radius))
        }


        if (circleCrop)
            options.optionalCircleCrop()

        if (centerCrop)
            options.optionalCenterCrop()

        transformation?.let {
            options.optionalTransform(it)
        }


        return options
    }

    private var optionWidth: Int = 0
    private var optionHeight: Int = 0

    fun override(width: Int, height: Int): BondImageView {
        this.optionWidth = width
        this.optionHeight = height
        return this
    }

    private var circleCrop = false

    private var centerCrop = true

    fun circle(circleCrop: Boolean): BondImageView {
        this.circleCrop = circleCrop
        return this
    }

    fun centerCrop(centerCrop: Boolean): BondImageView {
        this.centerCrop = centerCrop
        return this
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)

    }


}
