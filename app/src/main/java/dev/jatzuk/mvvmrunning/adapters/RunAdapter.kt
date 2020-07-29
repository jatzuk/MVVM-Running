package dev.jatzuk.mvvmrunning.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.jatzuk.mvvmrunning.databinding.ItemRunBinding
import dev.jatzuk.mvvmrunning.db.Run

class RunAdapter : ListAdapter<Run, RunAdapter.RunViewHolder>(RunDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RunViewHolder.from(parent)

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RunViewHolder private constructor(
        val binding: ItemRunBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Run) {
            binding.run = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): RunViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemRunBinding.inflate(layoutInflater, parent, false)
                return RunViewHolder(binding)
            }
        }
    }

    class RunDiffCallback : DiffUtil.ItemCallback<Run>() {

        override fun areItemsTheSame(oldItem: Run, newItem: Run) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Run, newItem: Run) =
            oldItem.hashCode() == newItem.hashCode()
    }
}
