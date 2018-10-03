package com.bond.baseui.widget

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import com.bond.baseui.R
import com.bond.baseui.util.UIUtil
import kotlinx.android.synthetic.main.base_toolbar.view.*

class BaseToolBar @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    var titleString: String? = ""

    init {
        View.inflate(context, R.layout.base_toolbar, this)
        navigation_icon.setOnClickListener {
            if (context is Activity) {
                context.finish()
            }
        }

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BaseToolBar)
        titleString = typedArray.getString(R.styleable.BaseToolBar_base_title)
        setTitle(titleString)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var mheightMeasureSpec = heightMeasureSpec
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val statusHeight = UIUtil.getStatusBarHeight(context)
            var statusLayoutParams: LayoutParams = new_status_bar.layoutParams as LayoutParams
            statusLayoutParams.height = statusHeight
            new_status_bar.layoutParams = statusLayoutParams

            var containerParams = toolar_container.layoutParams as LayoutParams
            containerParams.topMargin = statusHeight
            containerParams.gravity = Gravity.BOTTOM or Gravity.CENTER
            toolar_container.layoutParams = containerParams

            mheightMeasureSpec = MeasureSpec.makeMeasureSpec(containerParams.height + statusHeight, MeasureSpec.EXACTLY)

        }
        super.onMeasure(widthMeasureSpec, mheightMeasureSpec)
    }


    fun init(title: String?) {
        setTitle(title)
    }

    fun setTitle(title: String?) {
        titleString = title
        navigation_title.text = titleString
    }

    fun setAction(actionString:String){
        action.text = actionString
    }

    fun setAction(actionString:String ,block: (View?) -> Unit){
        action.text = actionString
        action.setOnClickListener { block(action) }
    }

    fun hideBack() {
        navigation_icon.visibility = View.GONE
    }

    fun navigationIconClick(block:(View?)->Unit){
        navigation_icon.setOnClickListener(object :OnClickListener{
            override fun onClick(v: View?) {
                block(v)
            }
        })
    }
    fun navigationIconClick(click:OnClickListener){
        navigation_icon.setOnClickListener(click)
    }

}

