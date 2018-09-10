package com.bond.baseui.list.simpleRV


import android.content.Context
import android.support.annotation.LayoutRes
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * @author zhumingwei
 * @date 2018/9/10 下午5:51
 * @email zdf312192599@163.com
 */
class SimpleRecyclerHelper<D, H : RecyclerView.ViewHolder> {

    companion object Factory {
        operator fun invoke(xxx: Any) = xxx
    }

    lateinit var simpleRecyclerHelperCallBack: SimpleRecyclerHelperCallBack<D, H>
    @LayoutRes
    var itemLayout: Int = 0
    lateinit var data: List<D>
    lateinit var recyclerView: RecyclerView
    lateinit var context: Context

    var adapter: RecyclerView.Adapter<H>

    init {

        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        adapter = object : RecyclerView.Adapter<H>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): H {
                var view = LayoutInflater.from(context).inflate(itemLayout, parent, false)
                return simpleRecyclerHelperCallBack.createViewHolder(view)
            }

            override fun getItemCount(): Int {
                return data.size
            }

            override fun onBindViewHolder(holder: H, position: Int) {
                simpleRecyclerHelperCallBack.onBindViewHolder(holder, data[position], position)
            }

        }
    }


    open interface SimpleRecyclerHelperCallBack<D, H : RecyclerView.ViewHolder> {
        fun createViewHolder(view: View): H

        fun onBindViewHolder(holder: H, t: D, position: Int)
    }

    open class SimpleRecyclerHelperBuilder<D, H : RecyclerView.ViewHolder> {
        lateinit var simpleRecyclerHelperCallBack: SimpleRecyclerHelperCallBack<D, H>
        @LayoutRes
        var itemLayout: Int = 0
        lateinit var data: List<D>
        lateinit var recyclerView: RecyclerView
        lateinit var context: Context
        fun build(): SimpleRecyclerHelper<D, H> {
            return SimpleRecyclerHelper<D, H>().apply {
                simpleRecyclerHelperCallBack = this@SimpleRecyclerHelperBuilder.simpleRecyclerHelperCallBack
                itemLayout = this@SimpleRecyclerHelperBuilder.itemLayout
                data = this@SimpleRecyclerHelperBuilder.data
                recyclerView = this@SimpleRecyclerHelperBuilder.recyclerView
                context = this@SimpleRecyclerHelperBuilder.context
            }
        }

    }


}
