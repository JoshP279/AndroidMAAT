package com.radaee.decorators

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

/**
 * A [ItemDecoration] that adds equal space to all sides of the item for a RecyclerView
 * @param spaceHeight The height of the space to be added to all sides of the item
 * This code belongs to Prof Dieter Vogts
 */
class EqualSpaceItemDecoration(private val spaceHeight: Int) : ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.bottom = spaceHeight
        outRect.top = spaceHeight
        outRect.left = spaceHeight
        outRect.right = spaceHeight
    }
}