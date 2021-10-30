package me.anon.grow3.ui.logs.view

import android.view.Gravity
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.viewbinding.ViewBinding
import me.anon.grow3.R
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Log
import me.anon.grow3.ui.action.fragment.DeleteActionFragment
import me.anon.grow3.ui.action.fragment.LogActionBottomSheetFragment
import me.anon.grow3.ui.action.fragment.LogActionFragment
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.util.nameOf
import me.anon.grow3.util.navigateTo
import me.anon.grow3.util.onLongClick
import me.anon.grow3.view.LogFooterView
import me.anon.grow3.view.LogHeaderView
import me.anon.grow3.view.model.Card

abstract class LogCard<T : ViewBinding, L: Log> : Card<T>
{
	protected lateinit var diary: Diary
	protected lateinit var log: L

	constructor() : super()
	constructor(diary: Diary, log: L) : super()
	{
		this.diary = diary
		this.log = log
	}

	final override fun bind(view: T)
	{
		view.root.isClickable = true
		view.root.isFocusable = true
		view.root.findViewById<LogHeaderView>(R.id.header).setLog(diary, log)
		view.root.findViewById<LogFooterView>(R.id.footer).setLog(diary, log)
		view.root.onLongClick { view ->
			val menu = PopupMenu(view.context, view, Gravity.BOTTOM or Gravity.END)
			menu.setOnMenuItemClickListener {
				when (it.itemId)
				{
					R.id.action_edit -> {
						view.navigateTo<LogActionBottomSheetFragment>(true) {
							bundleOf(
								Extras.EXTRA_DIARY_ID to diary.id,
								Extras.EXTRA_LOG_ID to log.id,
								Extras.EXTRA_LOG_TYPE to log.nameOf(),
							)
						}

						true
					}

					R.id.action_create -> {
						view.navigateTo<LogActionBottomSheetFragment>(true) {
							bundleOf(
								Extras.EXTRA_DIARY_ID to diary.id,
								Extras.EXTRA_LOG_ID to log.id,
								Extras.EXTRA_LOG_TYPE to log.nameOf(),
								LogActionFragment.EXTRA_COPY to true,
							)
						}

						true
					}

					R.id.action_delete -> {
						view.navigateTo<DeleteActionFragment>(true) {
							bundleOf(
								Extras.EXTRA_DIARY_ID to diary.id,
								Extras.EXTRA_LOG_ID to log.id,
							)
						}
						true
					}

					else -> false
				}
			}
			menu.inflate(R.menu.menu_log)
			menu.show()
			true
		}
		bindLog(view)
	}

	abstract fun bindLog(view: T)
}