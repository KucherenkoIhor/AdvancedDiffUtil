package com.ik.advanceddiffutil

import android.animation.ValueAnimator
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable


/**
 * Created by ihor on 10.05.17.
 */
class TimesAdapter : BaseAdapter<Time, TimesAdapter.TimeViewHolder>() {

    companion object {
        @JvmStatic
        val valueAnimator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            this.repeatCount = ValueAnimator.INFINITE
            this.repeatMode = ValueAnimator.REVERSE
            this.duration = 400
            this.start()
        }
    }

    override fun getItemViewId() = R.layout.view_item

    override fun instantiateViewHolder(view: View) = TimeViewHolder(view)

    fun setDataSource(flowable: Flowable<List<Time>>) : Disposable {
        var newList: List<Time> = emptyList()
        return flowable
                .doOnNext { newList = it }
                .map { DiffUtil.calculateDiff(TimeDiffCallback(dataSource, it)) }
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { dataSource = newList }
                .subscribe { it.dispatchUpdatesTo(this@TimesAdapter) }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: TimeViewHolder, position: Int, payloads: MutableList<Any>) {
        if(payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val set = payloads.firstOrNull() as Set<String>?
            set?.forEach {
                when(it) {
                    TimeDiffCallback.ID -> {
                        holder.tvId?.text = getItem(position).id
                    }
                    TimeDiffCallback.HOURS -> {
                        holder.tvHours?.setTime(getItem(position).h)
                    }
                    TimeDiffCallback.MINUTES -> {
                        holder.tvMinutes?.setTime(getItem(position).m)
                    }
                    TimeDiffCallback.SECONDS -> {
                        holder.tvSeconds?.setTime(getItem(position).s)
                    }
                    else -> super.onBindViewHolder(holder, position, payloads)
                }
            }
        }
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    class TimeViewHolder(itemView: View) : BaseViewHolder<Time>(itemView) {

        val vFirstDivider: TextView = itemView.findViewById(R.id.vFirstDivider)
        val vSecondDivider : TextView = itemView.findViewById(R.id.vSecondDivider)

        val tvId by lazy { itemView.findViewById(R.id.tvId) as TextView? }
        val tvHours by lazy { itemView.findViewById(R.id.tvHours) as TextView? }
        val tvMinutes by lazy { itemView.findViewById(R.id.tvMinutes) as TextView? }
        val tvSeconds by lazy { itemView.findViewById(R.id.tvSeconds) as TextView? }


        init {
            valueAnimator.addUpdateListener {
                vFirstDivider.alpha = it.animatedFraction
                vSecondDivider.alpha = it.animatedFraction
            }
        }
        override fun onBind(time: Time) {
            time.let {
                tvId?.text = time.id
                tvHours?.setTime(time.h)
                tvMinutes?.setTime(time.m)
                tvSeconds?.setTime(time.s)
            }
        }
    }
}

fun TextView.setTime(digit: Int) {
    text = String.format("%02d", digit)
}