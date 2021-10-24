package me.anon.grow3.ui.action.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.anon.grow3.data.model.*
import me.anon.grow3.databinding.FragmentActionLogTransplantBinding
import me.anon.grow3.util.asEditable
import me.anon.grow3.util.asStringOrNull
import me.anon.grow3.util.toDoubleOrNull

class TransplantLogView : LogView<FragmentActionLogTransplantBinding>
{
	private lateinit var diary: Diary
	private lateinit var log: Transplant

	constructor() : super()
	constructor(diary: Diary, log: Transplant) : super()
	{
		this.diary = diary
		this.log = log
	}

	override fun createView(inflater: LayoutInflater, parent: ViewGroup): FragmentActionLogTransplantBinding
		= FragmentActionLogTransplantBinding.inflate(inflater, parent, false)

	override fun bindView(view: View): FragmentActionLogTransplantBinding
		= FragmentActionLogTransplantBinding.bind(view)

	override fun bind(view: FragmentActionLogTransplantBinding)
	{
		view.mediumTypeOptions.singleSelection = true
		view.mediumTypeOptions.setMenu(MediumType.toMenu())
		view.mediumTypeOptions.itemSelectListener = { item ->
			log.medium = MediumType.ofId(item.itemId)
		}

		view.mediumSizeUnitOptions.singleSelection = true
		view.mediumSizeUnitOptions.setMenu(VolumeUnit.toMenu())
		view.mediumSizeUnitOptions.itemSelectListener = { item ->
			view.mediumSize.editText!!.text.toDoubleOrNull()?.let { amount ->
				log.size = Volume(amount, VolumeUnit.ofId(item.itemId))
			}
		}

		view.mediumTypeOptions.checkItems(log.medium.strRes)
		log.size?.let { size ->
			view.mediumSizeUnitOptions.checkItems(size.unit.strRes)
			view.mediumSize.editText!!.text = size.amount.asStringOrNull()?.asEditable()
		}
	}

	override fun save(view: FragmentActionLogTransplantBinding): Log
	{
		view.root.clearFocus()
		val type = view.mediumTypeOptions.getSelectedItems().firstOrNull()
		val sizeUnit = view.mediumSizeUnitOptions.getSelectedItems().firstOrNull()
		val size = view.mediumSize.editText!!.text.toDoubleOrNull()

		log.apply {
			this.medium = MediumType.ofId(type!!.itemId)
			this.size = Volume(size!!, VolumeUnit.ofId(sizeUnit!!.itemId))
		}

		return log
	}

	override fun provideTitle(): String = "Medium transplant"
}
