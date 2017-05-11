package com.ik.advanceddiffutil

import android.support.v7.util.DiffUtil

/**
 * Created by ihor on 11.05.17.
 */
class TimeDiffCallback(val oldList: List<Time>, val newList: List<Time>) : DiffUtil.Callback() {

    companion object {
        const val ID = "ID"
        const val HOURS = "HOURS"
        const val MINUTES = "MINUTES"
        const val SECONDS = "SECONDS"
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean
        = oldList[oldItemPosition].id == newList[newItemPosition].id

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val new = newList[newItemPosition]
        val old = oldList[oldItemPosition]
        val isIdTheSame = new.id == old.id
        val isHoursTheSame = new.h == old.h
        val isMinutesTheSame = new.m == old.m
        val isSecondsTheSame = new.s == old.s
        return isIdTheSame && isHoursTheSame && isMinutesTheSame && isSecondsTheSame
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        val new = newList[newItemPosition]
        val old = oldList[oldItemPosition]
        val set = mutableSetOf<String>()
        val isIdTheSame = new.id == old.id
        val isHoursTheSame = new.h == old.h
        val isMinutesTheSame = new.m == old.m
        val isSecondsTheSame = new.s == old.s
        if(isIdTheSame.not()) {
            set.add(ID)
        }
        if(isHoursTheSame.not()) {
            set.add(HOURS)
        }
        if(isMinutesTheSame.not()) {
            set.add(MINUTES)
        }
        if(isSecondsTheSame.not()) {
            set.add(SECONDS)
        }
        return set
    }
}