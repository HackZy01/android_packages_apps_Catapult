/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.tv.launcher.adapter

import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.lineageos.tv.launcher.R
import org.lineageos.tv.launcher.model.MainRowItem
import org.lineageos.tv.launcher.view.MainRowItemView

class MainVerticalAdapter :
    ListAdapter<Pair<Long, MainRowItem>, MainVerticalAdapter.ViewHolder>(diffCallback) {
    class ViewHolder(
        private val mainRowItemView: MainRowItemView,
    ) : RecyclerView.ViewHolder(mainRowItemView) {
        /**
         * @return true if the row took focus
         */
        fun bind(item: Pair<Long, MainRowItem>, requestFocus: Boolean): Boolean {
            mainRowItemView.setData(item.second)
            mainRowItemView.updateLayoutParams {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = mainRowItemView.resources.getDimension(
                    when (item.second.adapter) {
                        is AllAppsAdapter -> {
                            R.dimen.main_all_apps_row_height
                        }

                        is TvAdapter<*, *> -> {
                            R.dimen.main_app_row_height
                        }

                        else -> {
                            R.dimen.main_row_height
                        }
                    }
                ).toInt()
            }

            return requestFocus && mainRowItemView.requestFocus()
        }
    }

    private var recyclerView: RecyclerView? = null

    // The first row only grabs focus until something in the grid has it. Doing it on every bind
    // stole focus whenever that row was re-bound after scrolling back to it, which bounced
    // the selection around while navigating up.
    private var initialFocusDone = false

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val gotFocus = holder.bind(getItem(position), position == 0 && !initialFocusDone)
        if (gotFocus || recyclerView?.hasFocus() == true) {
            initialFocusDone = true
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        MainRowItemView(parent.context)
    )

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Pair<Long, MainRowItem>>() {
            override fun areItemsTheSame(
                oldItem: Pair<Long, MainRowItem>,
                newItem: Pair<Long, MainRowItem>
            ) = oldItem.first == newItem.first

            @Suppress("DiffUtilEquals")
            override fun areContentsTheSame(
                oldItem: Pair<Long, MainRowItem>,
                newItem: Pair<Long, MainRowItem>
            ) = oldItem.second.label == newItem.second.label
                    && oldItem.second.adapter === newItem.second.adapter
        }
    }
}
