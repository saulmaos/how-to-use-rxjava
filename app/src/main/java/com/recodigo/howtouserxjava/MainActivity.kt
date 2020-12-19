package com.recodigo.howtouserxjava

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
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
//        secondImpl()
//        thirdImpl()
        fourthImpl()
        setListener()
    }

    private fun fourthImpl() {
        // Mostly used for connecting to APIs
        val databaseSingle = Single.create<User> { emitter ->
            val user = Database().getUser()
            emitter.onSuccess(user)
        }
        databaseSingle
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            Log.d(TAG, "onSuccess: ${it.email}")
                        },
                        {
                            it.printStackTrace()
                        }
                ).let { compositeDisposable.add(it) }

        // used when we're not interested on emitting data to the observer
        val databaseCompletable = Completable.create { emitter ->
            val user = User("hola@recodigo.com", "saul")
            val success = Database().updateUser(user)
            if (success)
                emitter.onComplete()
            else
                emitter.onError(Throwable("Error updating db"))
        }
        databaseCompletable
                .subscribeOn(Schedulers.io())
                .subscribe(
                        {
                            Log.d(TAG, "onComplete")
                        },
                        {
                            it.printStackTrace()
                        }
                ).let { compositeDisposable.add(it) }

        // Used when we're not sure if a value will be emitted
        val databaseMaybe = Maybe.create<User> { emitter ->
            val user = Database().getUser("hola@recodigo.com")
            user?.let {
                emitter.onSuccess(it)
                emitter.onComplete()
            } ?: emitter.onComplete()
        }
        databaseMaybe
                .isEmpty // if the user did not appear, it will return true. Otherwise false
                .map { isEmpty ->
                    if (isEmpty) "User does not exist"
                    else "User exists"
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            Log.d(TAG, it)
                        },
                        {
                            it.printStackTrace()
                        }
                ).let { compositeDisposable.add(it) }
    }

    private fun thirdImpl() {
        val databaseObservable = Observable.create<User> { emitter ->
            if (emitter.isDisposed) return@create
            val user = Database().getUser()
            emitter.onNext(user)
            emitter.onComplete()
        }

        databaseObservable
                .flatMap {
                    getRemoteUserObservable(it.email)
                }
                .map {
                    "This is the url = ${it.profilePicUrl}"
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            Log.d(TAG, "onNext: $it")
//                            Log.d(TAG, "onNext: ${it.name} - ${it.profilePicUrl}")
                        },
                        {
                            it.printStackTrace()
                        }
                )
                .let { compositeDisposable.add(it) }
    }

    private fun getRemoteUserObservable(email: String): Observable<RemoteUser> {
        return Observable.create<RemoteUser> { emitter ->
            if (emitter.isDisposed) return@create
            val remoteUser = ApiService().getRemoteUser(email)
            emitter.onNext(remoteUser)
            emitter.onComplete()
        }
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