package com.ik.advanceddiffutil

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.Calendar.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var rv : RecyclerView

    private val adapter = TimesAdapter()

    var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val timeZonesIds = resources.getStringArray(R.array.timezones_ids).asIterable()
        rv = findViewById(R.id.rv)
        rv.setHasFixedSize(true)
        rv.adapter = adapter

        val flowable = Flowable.fromIterable(timeZonesIds)
                .map { getInstance(TimeZone.getTimeZone(it)) }
                .map { Time(it.timeZone.id, it.get(HOUR_OF_DAY), it.get(MINUTE), it.get(SECOND)) }
                .toList()
                .repeatWhen { it.delay(1, TimeUnit.SECONDS) }
                .subscribeOn(Schedulers.computation())

        disposable = adapter.setDataSource(flowable)

    }

    override fun onDestroy() {
        if(disposable?.isDisposed ?: false) {
            disposable?.dispose()
        }
        super.onDestroy()
    }
}
