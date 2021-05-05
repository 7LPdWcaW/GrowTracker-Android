package me.anon.grow3.ui.diaries.adapter.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_diary.view.*
import me.anon.grow3.ui.diaries.adapter.DiaryListAdapter

class DiaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
{
	private val name = itemView.name

	public fun bind(diary: DiaryListAdapter.DiaryStub)
	{
		name.text = diary.name
	}
}
