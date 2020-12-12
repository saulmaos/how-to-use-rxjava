package com.recodigo.howtouserxjava

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }
    private val compositeDisposable = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        simpleImpl()
        secondImpl()
        setListener()
    }

    private fun setListener() {
        btnCancel.setOnClickListener { compositeDisposable.dispose() } // this cancels all the observables
    }

    private fun secondImpl() {
        val observable = Observable.create<Int> { emitter ->
            (1..5).forEach {
                Log.d(TAG, "Observable - thread: ${Thread.currentThread().name}")
                Log.d(TAG, "Observable - dispose: ${emitter.isDisposed}") // to know if the observable has been canceled
                // If the observable has been disposed, it will return
                if (emitter.isDisposed) return@create
                emitter.onNext(it)
                try {
                    Thread.sleep(3000) // it will sleep for 3 seconds
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    if (!emitter.isDisposed)
                        emitter.onError(e) // It will never be called because the observable has already been disposed when this error occurs
                }
            }
            if (!emitter.isDisposed)
                emitter.onComplete()
        }
        observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            // onNext
                            Log.d(TAG, "onNext: $it - ${Thread.currentThread().name}")
                        },
                        {
                            // onError
                            Log.d(TAG, "onError")
                            it.printStackTrace()
                        },
                        {
                            // onComplete
                            Log.d(TAG, "onComplete: ${Thread.currentThread().name}")
                        }
                ).let { compositeDisposable.add(it) }
    }

    private fun simpleImpl() {
        val observable = Observable.create<String> {
            // here is where the job is done, it works on the thread specified in subscribeOn()
            // if no thread is given, it works on main thread which is not recommended
            Log.d(TAG, "Observable - thread: ${Thread.currentThread().name}")
            it.onNext("Hola")
            it.onNext("recodigo")
            it.onNext("fin")
            it.onComplete()
        }

        val observer = object: Observer<String> {
            // here is where we receive the data emitted by the observable. It will be received on the thread
            // specified in observeOn() if not thread is given, it will use the one in subscribeOn()

            override fun onComplete() {
                // it is called when the observable finished
                Log.d(TAG, "onComplete: ${Thread.currentThread().name}")
            }

            override fun onSubscribe(d: Disposable) {
                // it is called when the observer subscribed to the observable
                Log.d(TAG, "onSubscribe: ${Thread.currentThread().name}")
            }

            override fun onNext(t: String) {
                // it is called every time a new String is received
                val text = textView.text
                textView.text =  "$text $t"
                Log.d(TAG, "onNext: $t - ${Thread.currentThread().name}")
            }

            override fun onError(e: Throwable) {
                // it is called when an error occurs
                e.printStackTrace()
            }
        }

        observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)
    }
}