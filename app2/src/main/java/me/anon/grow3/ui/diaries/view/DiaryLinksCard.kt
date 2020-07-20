package me.anon.grow3.ui.diaries.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import me.anon.grow3.data.model.Diary
import me.anon.grow3.databinding.CardDiaryLinksBinding
import me.anon.grow3.ui.common.Extras.EXTRA_DIARY_ID
import me.anon.grow3.ui.logs.fragment.LogListFragment
import me.anon.grow3.util.navigateTo
import me.anon.grow3.util.onClick
import me.anon.grow3.view.model.Card

class DiaryLinksCard : Card<CardDiaryLinksBinding>
{
	private lateinit var diary: Diary

	constructor() : super(null)
	constructor(diary: Diary, title: String? = null) : super(title)
	{
		this.diary = diary
	}

	override fun createView(inflater: LayoutInflater, parent: ViewGroup): CardDiaryLinksBinding
		= CardDiaryLinksBinding.inflate(inflater, parent, false)
	override fun bindView(view: View): CardDiaryLinksBinding = CardDiaryLinksBinding.bind(view)

	override fun bind(view: CardDiaryLinksBinding)
	{
		view.viewLogs.onClick {
			it.navigateTo<LogListFragment> {
				bundleOf(EXTRA_DIARY_ID to diary.id)
			}
		}
	}
}
