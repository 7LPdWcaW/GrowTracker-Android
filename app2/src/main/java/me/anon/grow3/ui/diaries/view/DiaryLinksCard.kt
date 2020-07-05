package me.anon.grow3.ui.diaries.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.anon.grow3.data.model.Diary
import me.anon.grow3.databinding.CardDiaryLinksBinding
import me.anon.grow3.view.model.Card

class DiaryLinksCard(
	val diary: Diary,
	title: String? = null
) : Card<CardDiaryLinksBinding>(title)
{
	override fun createView(inflater: LayoutInflater, parent: ViewGroup): CardDiaryLinksBinding
		= CardDiaryLinksBinding.inflate(inflater, parent, false)
	override fun bindView(view: View): CardDiaryLinksBinding = CardDiaryLinksBinding.bind(view)

	override fun bind(view: CardDiaryLinksBinding)
	{

	}
}
