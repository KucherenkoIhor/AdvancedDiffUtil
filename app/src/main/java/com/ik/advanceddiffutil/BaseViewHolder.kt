package com.ik.advanceddiffutil

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by ihor on 11.05.17.
 */
abstract class BaseViewHolder<D>(itemView: View) : RecyclerView.ViewHolder(itemView) {

    abstract fun onBind(item: D)

}