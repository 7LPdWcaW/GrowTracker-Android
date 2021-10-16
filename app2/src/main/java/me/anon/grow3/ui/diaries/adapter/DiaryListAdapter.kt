package me.anon.grow3.ui.diaries.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.anon.grow3.R
import me.anon.grow3.ui.diaries.adapter.viewholder.DiaryViewHolder
import me.anon.grow3.util.inflate
import me.anon.grow3.util.onClick

class DiaryListAdapter : RecyclerView.Adapter<DiaryViewHolder>()
{
	/**
	 * Lightweight data struct for navigation
	 */
	data class DiaryStub(
		val id: String,
		val name: String,
		val summary: String
	)

	public var items: List<DiaryStub> = arrayListOf()
	public var onItemClick: (DiaryStub) -> Unit = {}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder
		= DiaryViewHolder(parent.inflate(R.layout.list_diary))

	override fun getItemCount(): Int = items.size

	override fun onBindViewHolder(holder: DiaryViewHolder, position: Int)
	{
		holder.bind(items[position])
		holder.itemView.onClick {
			onItemClick(items[position])
		}
	}
}
