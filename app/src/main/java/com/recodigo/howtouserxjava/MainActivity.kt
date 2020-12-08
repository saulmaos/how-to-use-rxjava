package com.recodigo.howtouserxjava

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        simpleImpl()
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