package com.bond.baseui.widget

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.support.annotation.ColorRes
import android.support.annotation.IntDef
import android.support.annotation.RestrictTo
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPager
import android.support.v7.content.res.AppCompatResources
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.bond.baseui.R
import com.bond.baseui.color
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * @author zhumingwei
 * @date 2019/3/21 上午11:00
 * @email zdf312192599@163.com
 * 一个带禁用功能的tabLayout
 *
 * 先写基本布局
 * 添加滑动点击事件
 * 添加hook添加自定义tab
 *
 */
class DisableTabLayout @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr) {
    var viewPager: ViewPager? = null
    var delegeteAdater: BaseVpAdapter? = null
    var sourceAdapter: PagerAdapter? = null
    lateinit var tabsContainer: LinearLayout


    var indicatorHeight: Int = 8
    //indicatorWidth为0
    var indicatorWidth: Int = 0 //指示器宽度

    val indicatorPaint: Paint by lazy {
        Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
    }
    //默认tab布局
    val defaultTabLayoutParams: LinearLayout.LayoutParams by lazy { LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT) }
    val expandedTabLayoutParams: LinearLayout.LayoutParams by lazy { LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f) }
    private var tabBackgroundResId = 0
    var tabTextSize = 0
    var tabColorStateList: ColorStateList? = null
    var tabDefaultWidth: Int = 0


    private var currentPosition = 0
    private var currentPositionOffset: Float = 0f
    var mode: Int = MODE_FIXED
    val disableSet: MutableSet<Int> = mutableSetOf()
    val scrollOffset = 52
    //默认禁用颜色，xml文件可以改
    val defaultDisableColor by lazy {
        Color.parseColor("#C0C0C0")
    }
    var indicatorDrawable: Drawable? = null

    var marginEnd: Int = 0

    init {
        if (!isInEditMode) {
            isFillViewport = true
            setWillNotDraw(false)
            tabsContainer = LinearLayout(context)
            tabsContainer.orientation = LinearLayout.HORIZONTAL
            tabsContainer.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            tabsContainer.clipChildren = false
            tabsContainer.gravity = Gravity.CENTER
            addView(tabsContainer)
            val ta = context.obtainStyledAttributes(attrs, R.styleable.DisableTabLayout)
            val taa = context.obtainStyledAttributes(ta.getResourceId(R.styleable.DisableTabLayout_disable_tabTextAppearance, android.support.design.R.style.TextAppearance_Design_Tab),
                    android.support.v7.appcompat.R.styleable.TextAppearance)
            try {
                tabTextSize = taa.getDimensionPixelSize(android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize, 0)
                tabColorStateList = taa.getColorStateList(
                        android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor)

            } catch (e: Exception) {

            } finally {
                taa.recycle()
            }
            try {
                if (ta.hasValue(R.styleable.DisableTabLayout_disable_tabTextColor)) {
                    // If we have an explicit text color set, use it instead
                    tabColorStateList = ta.getColorStateList(R.styleable.DisableTabLayout_disable_tabTextColor)
                }

                if (ta.hasValue(R.styleable.DisableTabLayout_disable_tabSelectedTextColor) || ta.hasValue(R.styleable.DisableTabLayout_disable_tabDisableTextColor)) {
                    var selected = 0
                    if (ta.hasValue(R.styleable.DisableTabLayout_disable_tabSelectedTextColor)) {
                        selected = tabColorStateList?.getColorForState(View.SELECTED_STATE_SET, selected)
                                ?: selected
                        selected = ta.getColor(R.styleable.DisableTabLayout_disable_tabSelectedTextColor, selected)
                    }

                    var disableColor = defaultDisableColor
                    if (ta.hasValue(R.styleable.DisableTabLayout_disable_tabDisableTextColor)) {
                        disableColor = tabColorStateList?.getColorForState(intArrayOf(-android.R.attr.state_enabled), disableColor)
                                ?: disableColor
                        disableColor = ta.getColor(R.styleable.DisableTabLayout_disable_tabDisableTextColor, disableColor)
                    }
                    tabColorStateList = createColorStateList(tabColorStateList?.defaultColor
                            ?: Color.BLACK, selected, disableColor)
                }


                if (ta.hasValue(R.styleable.DisableTabLayout_disable_tabBackground)) {
                    tabBackgroundResId = ta.getResourceId(R.styleable.DisableTabLayout_disable_tabBackground, 0)
                }
//                控件宽度，需要等宽的时候用到
                if (ta.hasValue(R.styleable.DisableTabLayout_disable_tabDefaultWidth)) {
                    tabDefaultWidth = ta.getDimensionPixelSize(R.styleable.DisableTabLayout_disable_tabDefaultWidth, 0)
                }

                if (ta.hasValue(R.styleable.DisableTabLayout_disable_tabIndicatorHeight)) {
                    indicatorHeight = ta.getDimensionPixelSize(R.styleable.DisableTabLayout_disable_tabIndicatorHeight, 8)
                }
                if (ta.hasValue(R.styleable.DisableTabLayout_disable_tabIndicatorWidth)) {
                    //默认是控件宽度，如果有则使用设置宽度
                    indicatorWidth = ta.getDimensionPixelSize(R.styleable.DisableTabLayout_disable_tabIndicatorWidth, 0)
                }
                if (ta.hasValue(R.styleable.DisableTabLayout_disable_tabIndicatorColor)) {
                    val indicatorColor = ta.getColor(R.styleable.DisableTabLayout_disable_tabIndicatorColor, Color.BLACK)
                    indicatorPaint.color = indicatorColor
                }
                if (ta.hasValue(R.styleable.DisableTabLayout_disable_tabMode)) {
                    mode = ta.getInt(R.styleable.DisableTabLayout_disable_tabMode, MODE_FIXED)
                }
                if (ta.hasValue(R.styleable.DisableTabLayout_disable_tabIndicatorDrawable)) {
                    indicatorDrawable = ta.getDrawable(R.styleable.DisableTabLayout_disable_tabIndicatorDrawable)
                }
                if (ta.hasValue(R.styleable.DisableTabLayout_disable_tabMarginEnd)) {
                    marginEnd = ta.getDimensionPixelSize(R.styleable.DisableTabLayout_disable_tabMarginEnd, 0)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                ta.recycle()
            }
        }
    }

    var isFragmentModel: Boolean = false
    fun setupWithViewPagerAdapter(vp: ViewPager, adapter: PagerAdapter) {
        isFragmentModel = false
        this.viewPager = vp
        this.sourceAdapter = adapter
        delegeteAdater = DelegatePagerAdapter(adapter, disableSet)
        vp.adapter = delegeteAdater as DelegatePagerAdapter
        viewPager?.addOnPageChangeListener(pagListener)
        resetView()
        notifyDataSetChanged()
    }

    fun setupWithViewFragmentAdapter(vp: ViewPager, adapter: FragmentPagerAdapter, fm: FragmentManager) {
        isFragmentModel = true
        this.viewPager = vp
        this.sourceAdapter = adapter
        delegeteAdater = DelegateFragmentPagerAdapter(adapter, disableSet, fm)
        vp.adapter = delegeteAdater as DelegateFragmentPagerAdapter
        viewPager?.addOnPageChangeListener(pagListener)
        resetView()
        notifyDataSetChanged()
    }

    private fun resetView() {
        tabsContainer.removeAllViews()
        for (i in 0 until getTabCount()) {
            addTextTab(i, sourceAdapter?.getPageTitle(i))
        }
    }

    private fun notifyDataSetChanged() {
        //刷新界面
        //更新tab的style
        updateTabStyles()

        for (i in 0 until getTabCount()) {
            tabsContainer.getChildAt(i).isEnabled = !disableSet.contains(i)
        }


        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                } else {
                    viewTreeObserver.removeGlobalOnLayoutListener(this)
                }
                currentPosition = delegeteAdater?.realPosition(viewPager?.currentItem ?: 0) ?: 0
                val child = tabsContainer.getChildAt(currentPosition)
                child?.let {
                    child.isSelected = true
                    scrollToChild(currentPosition, 0)
                }
            }
        })
    }

    //背景颜色，字体大小，默认字体颜色
    private fun updateTabStyles() {
        for (i in 0 until getTabCount()) {
            val v = tabsContainer.getChildAt(i)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (isFragmentModel) {
                    if (tabBackgroundResId != 0) {
                        ViewCompat.setBackground(
                                v, AppCompatResources.getDrawable(context, tabBackgroundResId))
                    }
                } else {
                    ViewCompat.setBackground(v, RippleDrawable(ColorStateList.valueOf(Color.parseColor("#E7E7E7")), if (tabBackgroundResId == 0) null else AppCompatResources.getDrawable(context, tabBackgroundResId), null))
                }
            } else {
                if (tabBackgroundResId != 0) {
                    ViewCompat.setBackground(
                            v, AppCompatResources.getDrawable(context, tabBackgroundResId))
                }
            }

        }


    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (isInEditMode || getTabCount() == 0) return

        val height = height

        val currentView: View = tabsContainer.getChildAt(currentPosition)
        val lineOffset = getLineOffset(currentView)
        var lineLeft = currentView.left + tabsContainer.left + lineOffset
        var lineRight = currentView.right + tabsContainer.left - lineOffset

        if (lineRight - lineLeft < indicatorWidth) {
            return
        }

        if (currentPositionOffset > 0 && currentPosition < getTabCount() - 1) {
            val nextPosition = getNextEnablePosition(currentPosition)
            if (nextPosition != -1) {
                val nextTab = tabsContainer.getChildAt(nextPosition)
                val nextLineOffset = getLineOffset(nextTab)
                val nextLineLeft = nextTab.left + tabsContainer.left + nextLineOffset
                val nextLineRight = nextTab.right + tabsContainer.left - nextLineOffset
                lineLeft = (currentPositionOffset * nextLineLeft + (1f - currentPositionOffset) * lineLeft)
                lineRight = (currentPositionOffset * nextLineRight + (1f - currentPositionOffset) * lineRight)
            }
        }
        if (indicatorDrawable != null) {
            indicatorDrawable!!.apply {
                setBounds(lineLeft.toInt(), (height - indicatorHeight.toFloat()).toInt(), lineRight.toInt(), height)
            }.draw(canvas)
        } else {
            canvas?.drawPath(Path().apply {
                val hih = indicatorHeight / 2
                val top = height - indicatorHeight.toFloat()
                val bottom = height.toFloat()
                moveTo(lineLeft + hih, top)
                lineTo(lineRight - hih, top)
                arcTo(RectF(lineRight - indicatorHeight, top, lineRight, bottom), 270f, 180f)
                lineTo(lineLeft + hih, bottom)
                arcTo(RectF(lineLeft, top, lineLeft + indicatorHeight, bottom), 90f, 180f)
                close()
            }, indicatorPaint)
        }


    }

    private fun getNextEnablePosition(position: Int): Int {
        var mposition = position
        while (position < getTabCount()) {
            if (tabsContainer.getChildAt(++mposition).isEnabled) {
                return mposition
            }
        }
        return -1
    }

    private fun getLineOffset(currentView: View): Float {

        if (indicatorWidth != 0) {
            return (currentView.measuredWidth - indicatorWidth) / 2f
        } else {
            return 0f
        }
    }


    private fun addTextTab(position: Int, pageTitle: CharSequence?) {
        addTab(position, generateTextTab(pageTitle))
    }

    private fun addTab(position: Int, tab: View) {
        tab.isFocusable = true
        tab.tag = position
        tab.setOnClickListener(mTabClick)
        tabsContainer.addView(tab, position, if (mode == MODE_FIXED) expandedTabLayoutParams else defaultTabLayoutParams.apply {
            if (tabDefaultWidth != 0) {
                width = tabDefaultWidth
            }
            if (this@DisableTabLayout.marginEnd > 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    this.marginEnd = marginEnd
                }else{
                    setMargins(0,0,this@DisableTabLayout.marginEnd,0)
                }
            }

        })
    }

    private fun generateTextTab(pageTitle: CharSequence?): View {
        var tab: TextView = TextView(context)
        tab.text = pageTitle
        tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, tabTextSize.toFloat())
        tabColorStateList?.let {
            tab.setTextColor(tabColorStateList)
        }
        tab.gravity = Gravity.CENTER
        tab.ellipsize = TextUtils.TruncateAt.END
        tab.setSingleLine()
        return tab
    }


    private fun getTabCount(): Int {
        return sourceAdapter?.count ?: 0
    }

    private fun scrollToChild(position: Int, offset: Int) {
        if (getTabCount() == 0 || mode == MODE_FIXED) {
            return
        }
        val child = tabsContainer.getChildAt(position)
        var newScrollX = 0
        if (child == null) {
            newScrollX = offset
        } else {
            newScrollX = child.left + offset
        }
        if (position > 0 || offset > 0) {
            newScrollX -= scrollOffset;
        }
        scrollTo(newScrollX, 0)

    }

    private fun createColorStateList(defaultColor: Int, selectedColor: Int, disableColor: Int): ColorStateList {
        val states = arrayOfNulls<IntArray>(3)
        val colors = IntArray(3)
        var i = 0

        states[i] = intArrayOf(-android.R.attr.state_enabled)
        colors[i] = disableColor
        i++

        states[i] = View.SELECTED_STATE_SET
        colors[i] = selectedColor
        i++

        // Default enabled state
        states[i] = View.EMPTY_STATE_SET
        colors[i] = defaultColor
//        i++

        return ColorStateList(states, colors)
    }


    private val pagListener: ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {
//            if (state == ViewPager.SCROLL_STATE_IDLE) {
//                scrollToChild(viewPager?.currentItem?:0, 0)
//            }
            customPageListener?.onPageScrollStateChanged(state)
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            val realPosition: Int = delegeteAdater!!.realPosition(position)
            if (realPosition >= tabsContainer.childCount) {
                return
            }

            currentPosition = realPosition
            currentPositionOffset = positionOffset

            val child = tabsContainer.getChildAt(realPosition)
            var offset = 0
            if (child != null) {
                offset = (positionOffset * child.width).toInt()
            }
            scrollToChild(realPosition, offset)

            invalidate()
            customPageListener?.onPageScrolled(realPosition,positionOffset,positionOffsetPixels)
        }

        override fun onPageSelected(position: Int) {
            Log.d("vp", "onPageSelected $position")
            val realPosition: Int = delegeteAdater!!.realPosition(position)
            if (realPosition >= tabsContainer.childCount) {

            }
            for (i in 0 until tabsContainer.childCount) {
                tabsContainer.getChildAt(i).isSelected = realPosition == i
            }
            customPageListener?.onPageSelected(realPosition)
        }
    }

    val mTabClick: OnClickListener = OnClickListener { v ->
        val position: Int = v?.getTag() as? Int ?: 0
        val vpPosition: Int = delegeteAdater!!.vpPosition(position)
        if (vpPosition == -1) {
            return@OnClickListener
        }
        val current = viewPager?.currentItem
        if (current != vpPosition) {
            viewPager?.setCurrentItem(vpPosition, true)
        }
    }

    private fun dp2px(dpVal: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, resources.displayMetrics)
    }

    interface BaseVpAdapter {
        fun realPosition(position: Int): Int
        fun vpPosition(position: Int): Int

    }

    class DelegateFragmentPagerAdapter(val pa: FragmentPagerAdapter, val disableSet: Set<Int>, fm: FragmentManager) : FragmentPagerAdapter(fm), BaseVpAdapter {
        override fun getItem(position: Int): Fragment {
            return pa.getItem(realPosition(position))
        }

        override fun getCount(): Int {
            return mCount()
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return pa.getPageTitle(realPosition(position))
        }

        fun mCount(): Int {
            var result = pa.count
            for (i in 0 until pa.count) {
                if (disableSet.contains(i)) {
                    result--
                }
            }
            return result
        }


        //TODO 算法希望优化
        override fun realPosition(position: Int): Int {
            //vp的position转真实的position
            var result = position
            for (i in 0 until pa.count) {
                if (disableSet.contains(i) && result >= i) {
                    result++
                }
            }
            return Math.min(pa.count - 1, result)
        }

        //-1表示已经被禁用
        //todo 算法希望优化
        override fun vpPosition(position: Int): Int {
            if (disableSet.contains(position)) {
                return -1
            }
            //真实的position转vp position
            var result = position
            for (i in 0..position) {
                if (disableSet.contains(i)) {
                    result--
                }
            }
            return Math.max(0, result)
        }

    }

    class DelegatePagerAdapter(val pa: PagerAdapter, val disableSet: Set<Int>) : PagerAdapter(), BaseVpAdapter {

        override fun isViewFromObject(view: View, `object`: Any): Boolean = pa.isViewFromObject(view, `object`)

        override fun getCount(): Int {
            return mCount()
        }

        fun mCount(): Int {
            var result = pa.count
            for (i in 0 until pa.count) {
                if (disableSet.contains(i)) {
                    result--
                }
            }
            return result
        }

        //TODO 算法希望优化
        override fun realPosition(position: Int): Int {
            //vp的position转真实的position
            var result = position
            for (i in 0 until pa.count) {
                if (disableSet.contains(i) && result >= i) {
                    result++
                }
            }
            return Math.min(pa.count - 1, result)
        }

        //-1表示已经被禁用
        //todo 算法希望优化
        override fun vpPosition(position: Int): Int {
            if (disableSet.contains(position)) {
                return -1
            }
            //真实的position转vp position
            var result = position
            for (i in 0..position) {
                if (disableSet.contains(i)) {
                    result--
                }
            }
            return Math.max(0, result)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return pa.getPageTitle(realPosition(position))
        }

        override fun getItemPosition(`object`: Any): Int {
            return pa.getItemPosition(`object`)
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            return pa.instantiateItem(container, realPosition(position))
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            pa.destroyItem(container, realPosition(position), `object`)
        }


    }

    //    ============= 工具方法
    fun setCurrentItem(position: Int) {
        val position = delegeteAdater!!.vpPosition(position)
        if (position >= 0) {
            viewPager?.currentItem = position
        }

    }


    fun setEnable(index: Int, enable: Boolean) {
        if (enable) {
            setEnable(index)
        } else {
            setDisable(index)
        }
    }

    fun setDisable(index: Int) {
        disableSet.add(index)
        (delegeteAdater as? PagerAdapter)?.notifyDataSetChanged()
        notifyDataSetChanged()
    }

    fun setEnable(index: Int) {
        disableSet.remove(index)
        (delegeteAdater as? PagerAdapter)?.notifyDataSetChanged()
        notifyDataSetChanged()
    }

    fun setIndicatorColor(@ColorRes colorRes: Int) {
        indicatorPaint.color = ContextCompat.getColor(context, colorRes)
        invalidate()
    }

    fun setTabMode(mode: Int) {
        this.mode = mode
        resetView()
        notifyDataSetChanged()
    }

    fun setTextColor(defaultColor: Int, selectedColor: Int, disableColor: Int) {
        tabColorStateList = createColorStateList(defaultColor, selectedColor, disableColor)
        for (i in 0 until tabsContainer.childCount) {
            (tabsContainer.getChildAt(i) as? TextView)?.setTextColor(tabColorStateList)
        }
    }

    fun getCurrentRealPosition(): Int {
        return delegeteAdater?.realPosition(viewPager!!.currentItem) ?: -1
    }

    var customPageListener:ViewPager.OnPageChangeListener? = null
}

const val MODE_SCROLLABLE = 0
const val MODE_FIXED = 1