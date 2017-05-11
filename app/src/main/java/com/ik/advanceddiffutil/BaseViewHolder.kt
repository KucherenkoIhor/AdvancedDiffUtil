package com.ik.advanceddiffutil

import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * Created by ihor on 11.05.17.
 */
abstract class BaseViewHolder<D>(itemView: View?) : RecyclerView.ViewHolder(itemView) {

    abstract fun onBind(item: D)

}