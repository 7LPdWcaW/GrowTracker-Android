package me.anon.grow3.ui.action.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.StageChange
import me.anon.grow3.data.model.StageType
import me.anon.grow3.databinding.FragmentActionLogStageChangeBinding

class StageChangeLogView(
	diary: Diary,
	log: StageChange
) : LogView<StageChange>(diary, log)
{
	private lateinit var bindings: FragmentActionLogStageChangeBinding

	override fun createView(inflater: LayoutInflater, parent: ViewGroup): View
		= FragmentActionLogStageChangeBinding.inflate(inflater, parent, false).root

	override fun bindView(view: View)
	{
		bindings = FragmentActionLogStageChangeBinding.bind(view)
		bindings.common.setLog(diary, log)

		bindings.stages.setMenu(StageType.toMenu())
		bindings.stages.singleSelection = true
		bindings.stages.checkItems(diary.stage().type.strRes)
		bindings.stages.itemSelectListener = { item ->

		}
	}

	override fun saveView(): StageChange
	{
		bindings.root.clearFocus()
		bindings.common.saveTo(log)
		log.type = StageType.ofId(bindings.stages.getSelectedItems().first().itemId)
		return log
	}

	override fun provideTitle(): String? = "Plant stage change"
}
