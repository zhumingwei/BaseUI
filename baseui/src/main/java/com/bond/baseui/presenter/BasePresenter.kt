package com.bond.baseui.presenter

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subscribers.DisposableSubscriber
import org.reactivestreams.Subscription
import java.lang.ref.Reference
import java.lang.ref.WeakReference

/**
 * @author zhumingwei
 * @date 2018/6/29 下午3:18
 * @email zdf312192599@163.com
 */
class BasePresenter<T : Any> {
    protected var mViewRef: Reference<T>? = null
    private var subscriptions: CompositeDisposable

    init {
        subscriptions = CompositeDisposable()
    }

    fun attachViewRef(view: T) {
        if (mViewRef == null) {
            mViewRef = WeakReference(view)
        }
    }

    fun addDisposable(disposable: Disposable) {
        subscriptions.add(disposable)
    }

    fun dettachViewRef() {
        subscriptions.clear()
    }

}
