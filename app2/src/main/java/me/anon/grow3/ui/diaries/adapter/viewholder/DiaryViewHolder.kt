package me.anon.grow3.ui.diaries.adapter.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import me.anon.grow3.databinding.ListDiaryBinding
import me.anon.grow3.ui.diaries.adapter.DiaryListAdapter

class DiaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
{
	private val name = ListDiaryBinding.bind(itemView).name

	public fun bind(diary: DiaryListAdapter.DiaryStub)
	{
		name.text = diary.name
	}
}
