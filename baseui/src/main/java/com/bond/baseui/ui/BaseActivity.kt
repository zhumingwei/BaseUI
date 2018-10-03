package com.bond.baseui.ui

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import com.bond.baseui.R
import com.bond.baseui.logger.Logger
import com.bond.baseui.util.LightStatusBarCompat
import com.bond.baseui.util.UIUtil
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BaseActivity : AppCompatActivity(), SwipeWindowHelper.SlideBackManager{
    private var subscriptions: CompositeDisposable = CompositeDisposable()

    private val mConnectivityManager: ConnectivityManager by lazy {
        applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    private val mSwipeWindowHelper: SwipeWindowHelper by lazy {

        SwipeWindowHelper(this).apply {
            setOnSwipeListener(object : SwipeWindowHelper.OnSwipeListener {
                override fun onSwipe(status: Int) {}
            })
        }
    }

//    //设置页面状态
//    stateOperator.bind(framelayout) 自己调用吧绑定到那个内容处理那个内容 参数为ViewGroup
//    val stateOperator:StateOperator by lazy {
//        StateOperator()
//    }
//    fun  netErrorView(){
//        stateOperator.setState(StateOperator.STATE_NETWORK_ERROR)
//    }
//
//    fun emptyDataView(){
//        stateOperator.setState(StateOperator.STATE_DATA_EMPTY)
//    }
//
//    fun loadError(){
//        stateOperator.setState(StateOperator.STATE_LOAD_ERROR)
//    }
//
//    fun showNormal(){
//        stateOperator.setState(StateOperator.STATE_NORMAL)
//    }


    private lateinit var framelayout: FrameLayout

    lateinit var view: View
    var mSavedInstanceState: Bundle? = null
    protected var activityKey: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dealIntent();
        this.mSavedInstanceState = savedInstanceState
        activityKey = getMyActivityKey()
        if (activityKey.isNullOrEmpty()) {
            AppManager.instance.addActivity(this, activityKey)
        }

        framelayout = LayoutInflater.from(this).inflate(R.layout.activity_base, null) as FrameLayout;

        view = LayoutInflater.from(this).inflate(getContentViewRes(), null)
        view.setFocusable(true)
        view.setFocusableInTouchMode(true)
        framelayout.addView(view, 0)


        setContentView(framelayout)
        UIUtil.setStatusBarTranslucentCompat(this)

        LightStatusBarCompat.setLightStatusBar(window, supportLightStatusBar())
        if (closeScreenShots()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        initView()
    }

    open  fun initData() {

    }


    abstract fun initView()
    fun getMyActivityKey(): String? {
        return activityKey
    }

    private fun closeScreenShots(): Boolean {
        return false
    }

    open fun supportLightStatusBar(): Boolean {
        return false
    }


    abstract fun getContentViewRes(): Int
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        dealIntent()
    }
    open fun dealIntent() {}


    /**
     * 查询网络状态
     */
    private fun isNetworkAvailable(): Boolean {
        val info = mConnectivityManager.activeNetworkInfo
        return info?.isConnected ?: false
    }

    /**
     * 监听网络状态变化
     */
    private val mConnectivityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!isNetworkAvailable()) {
                Log.d("页面", "网络掉线...")
            } else {
                Log.d("页面", "网络恢复...")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            registerReceiver(mConnectivityReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
        }
        return true
    }


    override fun onPause() {
        super.onPause()
        try {
            UIUtil.hiddenKeyboard(this)
            unregisterReceiver(mConnectivityReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getSlideActivity(): Activity {
        return this;
    }

    override fun supportSlideBack(): Boolean {
        return true
    }

    override fun canBeSlideBack(): Boolean {
        return true
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return if (!supportSlideBack() || mSwipeWindowHelper == null) {
            super.dispatchTouchEvent(ev)
        } else mSwipeWindowHelper.processTouchEvent(ev) || super.dispatchTouchEvent(ev)
    }

    override fun onDestroy() {
        super.onDestroy()
        dettachViewRef()
        AppManager.instance.finishActivity(this, activityKey)
    }

    /**
     * 请尽量使用字符串或数字的unique id
     *
     * @param activityKey
     */
    fun setActivityKey(activityKey: Any) {
        this.activityKey = this.javaClass.simpleName + "_" + activityKey
        Logger.d(TAG, "activityKey: " + this.activityKey)
        AppManager.instance.addActivity(this, this.activityKey)
    }

    val TAG: String = "BaseActivity"

    fun startActivity(clazz: Class<*>) {
        startActivity(Intent(this, clazz))
    }

    fun addDisposable(disposable: Disposable) {
        subscriptions.add(disposable)
    }

    fun dettachViewRef() {
        if (!subscriptions.isDisposed) {
            subscriptions.clear()
        }
    }
}

