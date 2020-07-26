package me.anon.grow3.ui.crud.fragment

import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import me.anon.grow3.data.model.EnvironmentType
import me.anon.grow3.data.model.Light
import me.anon.grow3.data.model.LightType
import me.anon.grow3.data.model.Size
import me.anon.grow3.databinding.FragmentCrudDiaryEnvironmentBinding
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.crud.viewmodel.DiaryViewModel
import me.anon.grow3.util.*
import me.anon.grow3.view.DropDownEditText
import javax.inject.Inject

class DiaryEnvironmentFragment : BaseFragment(FragmentCrudDiaryEnvironmentBinding::class)
{
	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: DiaryViewModel.Factory
	private val viewModel: DiaryViewModel by activityViewModels { ViewModelProvider(viewModelFactory, this) }
	private val viewBindings by viewBinding<FragmentCrudDiaryEnvironmentBinding>()

	override fun bindUi()
	{
		bindEnvironmentUi()
		bindSizeUi()
		bindLightUi()
	}

	override fun bindVm()
	{
		viewModel.diary.observe(viewLifecycleOwner) { diary ->
			diary.environment()?.let { environment ->
				viewBindings.environmentTypeOptions.checkItems(environment.strRes)
			}
			diary.size()?.let { size ->
				viewBindings.environmentSizeWidth.editText!!.text = size.width?.asString()?.asEditable()
				viewBindings.environmentSizeHeight.editText!!.text = size.height?.asString()?.asEditable()
				viewBindings.environmentSizeDepth.editText!!.text = size.depth?.asString()?.asEditable()
			}
			diary.light()?.let { light ->
				viewBindings.lightTypeOptions.checkItems(light.type.strRes)

				val isSunlight = light.type == LightType.Sunlight
				viewBindings.lightBrand.isVisible = !isSunlight
				viewBindings.lightWattage.isVisible = !isSunlight

				if (!isSunlight)
				{
					viewBindings.lightWattage.editText!!.text = light.wattage?.asString()?.asEditable()
					viewBindings.lightBrand.editText!!.text = light.brand?.asEditable()
				}
			}
		}
	}

	public fun bindEnvironmentUi()
	{
		viewBindings.environmentTypeOptions.setMenu(EnvironmentType.toMenu())
		viewBindings.environmentTypeOptions.itemSelectListener = { item ->
			viewModel.setEnvironment(
				type = ValueHolder(item.isChecked then EnvironmentType.ofId(item.itemId))
			)
		}
	}

	public fun bindSizeUi()
	{
		fun updateSize()
		{
			val width = viewBindings.environmentSizeWidth.editText?.text?.toDoubleOrNull()
			val widthUnit = (viewBindings.environmentSizeWidthUnit.editText as DropDownEditText).getSelectedItems().firstOrNull()
			val height = viewBindings.environmentSizeHeight.editText?.text?.toDoubleOrNull()
			val heightUnit = (viewBindings.environmentSizeHeightUnit.editText as DropDownEditText).getSelectedItems().firstOrNull()
			val depth = viewBindings.environmentSizeDepth.editText?.text?.toDoubleOrNull()
			val depthUnit = (viewBindings.environmentSizeDepthUnit.editText as DropDownEditText).getSelectedItems().firstOrNull()

			val size = if (width != null || height != null || depth != null)
				Size(
					width = width?.let { it /* conversion logic for units */ },
					height = height?.let { it /* conversion logic for units */ },
					depth = depth?.let { it /* conversion logic for units */ }
				)
			else null

			viewModel.setEnvironment(
				size = ValueHolder(size)
			)
		}

		arrayOf(
			viewBindings.environmentSizeWidth.editText!!,
			viewBindings.environmentSizeHeight.editText!!,
			viewBindings.environmentSizeDepth.editText!!
		).forEach {
			it.onFocusLoss {
				updateSize()
			}
		}

		arrayOf(
			viewBindings.environmentSizeWidthUnit.editText!!,
			viewBindings.environmentSizeHeightUnit.editText!!,
			viewBindings.environmentSizeDepthUnit.editText!!
		).forEach {
			(it as? DropDownEditText)?.itemSelectListener = {
				updateSize()
			}
		}
	}

	public fun bindLightUi()
	{
		fun updateLight()
		{
			viewBindings.lightTypeOptions.getSelectedItems().firstOrNull()?.let {
				val type = LightType.ofId(it.itemId)
				viewModel.setEnvironment(
					light = ValueHolder(Light(
						type = type
					).apply {
						if (type != LightType.Sunlight)
						{
							wattage = viewBindings.lightWattage.editText!!.text.toDoubleOrNull()
							brand = viewBindings.lightBrand.editText!!.text.toStringOrNull()
						}
					})
				)
			}
		}

		viewBindings.lightTypeOptions.setMenu(LightType.toMenu())
		viewBindings.lightBrand.isVisible = false
		viewBindings.lightWattage.isVisible = false

		viewBindings.lightTypeOptions.itemSelectListener = { item ->
			val isSunlight = LightType.ofId(item.itemId) == LightType.Sunlight
			viewBindings.lightBrand.isVisible = !isSunlight
			viewBindings.lightWattage.isVisible = !isSunlight

			updateLight()
		}

		arrayOf(
			viewBindings.lightWattage.editText!!,
			viewBindings.lightBrand.editText!!
		).forEach {
			it.onFocusLoss {
				updateLight()
			}
		}
	}
}
