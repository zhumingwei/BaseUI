package com.bond.baseui.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bond.baseui.R


class StateOperator : View.OnClickListener {

    private val view_empty = R.layout.view_empty
    private val view_net_error = R.layout.view_net_error
    private val view_load_error = R.layout.view_load_error
    var content: ViewGroup? = null
    var context: Context? = null
    var childList: MutableList<View> = mutableListOf()
    fun bind(frameLayout: ViewGroup) {
        this.content = frameLayout;
        this.context = frameLayout.context

        for (i in 0 until frameLayout.childCount) {
            childList.add(frameLayout.getChildAt(i))
        }
    }

    private var displayView: View? = null

    fun setState(state: Int) {
        when (state) {
            STATE_NORMAL -> {
                showNormal()
            }
            STATE_LOAD_ERROR -> {
                showView(getLoadErrorView())
            }
            STATE_NETWORK_ERROR -> {
                showView(getNetErrorView())
            }
            STATE_DATA_EMPTY -> {
                showView(getDataEmptyView())
            }
        }
    }

    private fun showView(v: View) {
        childGone()
        displayView = v
        displayView?.visibility = View.VISIBLE
        content?.addView(displayView)

    }

    var viewEmptData: View? = null
    private fun getDataEmptyView(): View {
        viewEmptData = viewEmptData?.let {
            it
        } ?: LayoutInflater.from(context).inflate(view_empty, null)

        return viewEmptData!!
    }

    fun setDataEmptyView(v: View): StateOperator {
        viewEmptData = v
        return this
    }

    var viewNetError: View? = null
    private fun getNetErrorView(): View {
        viewNetError = viewNetError?.let {
            it
        } ?: LayoutInflater.from(context).inflate(view_net_error, null)

        return viewNetError!!
    }

    fun setNetErrorView(v: View): StateOperator {
        viewNetError = v
        return this
    }

    var viewLoadError: View? = null
    private fun getLoadErrorView(): View {
        viewLoadError = viewLoadError?.let {
            it
        } ?: LayoutInflater.from(context).inflate(view_load_error, null)

        return viewLoadError!!
    }

    fun setLoadErrorView(v: View): StateOperator {
        viewLoadError = v
        return this
    }

    fun showNormal() {
        childShow()
        displayView?.visibility == View.GONE
    }


    private fun childGone() {
        childList.forEach {
            it.visibility = View.GONE
        }
    }

    private fun childShow() {
        childList.forEach {
            it.visibility = View.VISIBLE
        }
    }

    override fun onClick(v: View) {
        when (v) {
            viewEmptData -> {
                callback?.onEmptyClick(v)
            }
            viewLoadError -> {
                callback?.onLoadErrorClick(v)
            }
            viewNetError -> {
                callback?.onNetErrorClick(v)
            }
        }
    }


    var callback: CallBack? = null
    fun setCallBack(callback: CallBack) {
        this.callback = callback
    }

    interface CallBack {
        fun onNetErrorClick(view: View)

        fun onLoadErrorClick(view: View)

        fun onEmptyClick(view: View)
    }

    companion object {
        val STATE_NORMAL = 1
        val STATE_LOAD_ERROR = 2 // 加载出现问题
        val STATE_NETWORK_ERROR = 3 // 网络出现问题
        val STATE_DATA_EMPTY = 4 // 网络出现问题
    }
}
