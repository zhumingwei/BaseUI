package com.bond.baselib

import android.graphics.Color
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.bond.baseui.widget.DisableTabLayout
import kotlinx.android.synthetic.main.activity_custom_tab.*

/**
 * @author zhumingwei
 * @date 2019/3/21 下午2:50
 * @email zdf312192599@163.com
 */
class CustomTabActivity : AppCompatActivity() {
     lateinit var customTab: DisableTabLayout
     lateinit var viewPager: ViewPager
     lateinit var pagerAdapter: PagerAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_tab)
        customTab = findViewById(R.id.customTab)
        viewPager = findViewById(R.id.viewpager)
        pagerAdapter = MyPagerAdapter(5)
        customTab.setupWithViewPagerAdapter(viewpager,pagerAdapter)
        customTab.setDisable(0)
        customTab.setDisable(1)
        customTab.setEnable(0)
    }

     inner class MyPagerAdapter (size:Int):PagerAdapter(){
         var views:List<View> = List(size) {
             TextView(this@CustomTabActivity).apply {
                 setText("$it")
                 textSize = 40f
                 setBackgroundColor(Color.GRAY)
                 setTextColor(Color.BLACK)
                 gravity = Gravity.CENTER
                 layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT)
             }
         }

         override fun instantiateItem(container: ViewGroup, position: Int): Any {
             container.addView(views.get(position))
             return views.get(position);
         }

         override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
             container.removeView(views[position])
         }

         override fun getPageTitle(position: Int): CharSequence? {
             return "tab$position"
         }
         override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
         }

         override fun getCount(): Int {
            return views.size
         }

     }
}
