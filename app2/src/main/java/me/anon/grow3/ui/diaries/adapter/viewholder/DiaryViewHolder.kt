package me.anon.grow3.ui.diaries.adapter.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import me.anon.grow3.databinding.ListDiaryBinding
import me.anon.grow3.ui.diaries.adapter.DiaryListAdapter

class DiaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
{
	private val binding = ListDiaryBinding.bind(itemView)
	private val name = binding.name
	private val summary = binding.shortSummary

	public fun bind(stub: DiaryListAdapter.DiaryStub)
	{
		name.text = stub.name
		summary.text = stub.summary
	}
}
