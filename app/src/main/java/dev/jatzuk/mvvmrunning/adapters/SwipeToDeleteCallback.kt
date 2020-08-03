package dev.jatzuk.mvvmrunning.adapters

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dev.jatzuk.mvvmrunning.R
import dev.jatzuk.mvvmrunning.db.Run

class SwipeToDeleteCallback(
    private val adapter: RunAdapter,
    private val view: View
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private val icon = ContextCompat.getDrawable(view.context, R.drawable.ic_delete_big)
    private val background = ColorDrawable(Color.RED)
    private var deletedItem: Run? = null
    private var deletedItemPosition = 0

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        val list = adapter.currentList.toMutableList()
        deletedItem = list[position]
        deletedItemPosition = position
        list.removeAt(position)
        adapter.submitList(list)
        showUndo()

        // TODO: 03/08/20 remove from db
    }

    private fun showUndo() {
        Snackbar.make(
            view,
            view.context.getString(R.string.run_deleted),
            Snackbar.LENGTH_LONG
        ).run {
            setAction(R.string.undo) { undoDeletion() }
            setActionTextColor(ContextCompat.getColor(view.context, R.color.colorAccent))
            show()
        }
    }

    private fun undoDeletion() {
        val list = adapter.currentList.toMutableList()
        list.add(deletedItemPosition, deletedItem)
        adapter.submitList(list)
        deletedItem = null
        deletedItemPosition = 0

        // TODO: 03/08/20 add to db
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        val itemView = viewHolder.itemView
        val backgroundCornerOffset = 20

        val iconMargin = (itemView.height - icon!!.intrinsicHeight) / 2
        val iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
        val iconBottom = iconTop + icon.intrinsicHeight

        when {
            dX > 0 -> {
                val iconLeft = itemView.left
                val iconRight = itemView.left + iconMargin
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                background.setBounds(
                    itemView.left,
                    itemView.top,
                    itemView.left + dX.toInt() + backgroundCornerOffset,
                    itemView.bottom
                )
            }
            dX < 0 -> {
                val iconLeft = itemView.right - iconMargin
                val iconRight = itemView.right
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                background.setBounds(
                    (itemView.right + dX).toInt() - backgroundCornerOffset,
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
            }
            else -> {
                background.setBounds(0, 0, 0, 0)
            }
        }

        background.draw(c)
        icon.draw(c)
    }
}
