package me.anon.grow3.ui.action.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.isVisible
import me.anon.grow3.data.model.*
import me.anon.grow3.databinding.FragmentActionLogEnvironmentBinding
import me.anon.grow3.util.*
import me.anon.grow3.view.DropDownEditText

class EnvironmentLogView : LogView<FragmentActionLogEnvironmentBinding>
{
	private lateinit var diary: Diary
	private lateinit var log: Environment

	constructor() : super()
	constructor(diary: Diary, log: Environment) : super()
	{
		this.diary = diary
		this.log = log
	}

	override fun createView(inflater: LayoutInflater, parent: ViewGroup): FragmentActionLogEnvironmentBinding
		= FragmentActionLogEnvironmentBinding.inflate(inflater, parent, false)

	override fun bindView(view: View): FragmentActionLogEnvironmentBinding
		= FragmentActionLogEnvironmentBinding.bind(view)

	override fun bind(view: FragmentActionLogEnvironmentBinding)
	{
		val flipper = view.root
		view.environmentTypeButton.onClick {
			flipper.children.forEach { it.isVisible = false }
			view.environmentTypeContainer.isVisible = true
		}
//		view.environmentTypeButton.onClick {
//			flipper.children.onEach { it.isVisible = false }
//			view.environmentTypeContainer.isVisible = true
//		}

		bindType(view)
		bindLight(view)
	}

	private fun bindType(view: FragmentActionLogEnvironmentBinding)
	{
		view.environmentTypeOptions.setMenu(EnvironmentType.toMenu())

		arrayOf(
			view.environmentSizeWidthUnit.editText!!,
			view.environmentSizeHeightUnit.editText!!,
			view.environmentSizeDepthUnit.editText!!
		).forEach {
			with (it as DropDownEditText) {
				setMenu(DimensionUnit.toMenu())
			}
		}

		log.type?.let { type ->
			view.environmentTypeOptions.checkItems(type.strRes)
		}

		log.size?.let { size ->
			size.width?.let { width ->
				view.environmentSizeWidth.editText!!.text = width.amount.asString().asEditable()
				(view.environmentSizeWidthUnit.editText as DropDownEditText).checkItems(DimensionUnit.idOf(width.unit))
			}

			size.height?.let { height ->
				view.environmentSizeHeight.editText!!.text = height.amount.asString().asEditable()
				(view.environmentSizeHeightUnit.editText as DropDownEditText).checkItems(DimensionUnit.idOf(height.unit))
			}

			size.depth?.let { depth ->
				view.environmentSizeDepth.editText!!.text = depth.amount.asString().asEditable()
				(view.environmentSizeDepthUnit.editText as DropDownEditText).checkItems(DimensionUnit.idOf(depth.unit))
			}
		}
	}

	private fun bindLight(view: FragmentActionLogEnvironmentBinding)
	{
		log.light?.let { light ->
//			view.lightTypeOptions.checkItems(light.type.strRes)
//
//			val isSunlight = light.type == LightType.Sunlight
//			view.lightBrand.isVisible = !isSunlight
//			view.lightWattage.isVisible = !isSunlight
//
//			if (!isSunlight)
//			{
//				view.lightWattage.editText!!.text = light.wattage?.asString()?.asEditable()
//				view.lightBrand.editText!!.text = light.brand?.asEditable()
//			}
		}
	}

	override fun save(view: FragmentActionLogEnvironmentBinding): Log
	{
		view.root.clearFocus()

		val width = view.environmentSizeWidth.editText?.text?.toDoubleOrNull()
		val widthUnit = (view.environmentSizeWidthUnit.editText as DropDownEditText).getSelectedItems().first()
		val height = view.environmentSizeHeight.editText?.text?.toDoubleOrNull()
		val heightUnit = (view.environmentSizeHeightUnit.editText as DropDownEditText).getSelectedItems().first()
		val depth = view.environmentSizeDepth.editText?.text?.toDoubleOrNull()
		val depthUnit = (view.environmentSizeDepthUnit.editText as DropDownEditText).getSelectedItems().first()

		val size = if (width != null || height != null || depth != null)
			Size(
				width = width?.let { Dimension(it, DimensionUnit.ofId(widthUnit.itemId)) },
				height = height?.let { Dimension(it, DimensionUnit.ofId(heightUnit.itemId)) },
				depth = depth?.let { Dimension(it, DimensionUnit.ofId(depthUnit.itemId)) }
			)
		else null

		val envType = view.environmentTypeOptions.getSelectedItems().firstOrNull()
		val type = envType?.let { EnvironmentType.ofId(it.itemId) }

		log.patch(
			type = ValueHolder(type),
			size = ValueHolder(size),
		)

		return log
	}

	override fun provideTitle(): String = "Environment change"
}
