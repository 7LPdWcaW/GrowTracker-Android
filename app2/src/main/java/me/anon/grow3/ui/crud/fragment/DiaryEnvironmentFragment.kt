package me.anon.grow3.ui.crud.fragment

import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import kotlinx.android.synthetic.main.fragment_crud_diary_environment.*
import me.anon.grow3.R
import me.anon.grow3.data.model.EnvironmentType
import me.anon.grow3.data.model.Light
import me.anon.grow3.data.model.LightType
import me.anon.grow3.data.model.Size
import me.anon.grow3.di.ApplicationComponent
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.crud.viewmodel.DiaryViewModel
import me.anon.grow3.util.*
import me.anon.grow3.util.states.asSuccess
import me.anon.grow3.util.states.isSuccess
import me.anon.grow3.view.DropDownEditText
import javax.inject.Inject

class DiaryEnvironmentFragment : BaseFragment(R.layout.fragment_crud_diary_environment)
{
	override val inject: (ApplicationComponent) -> Unit = { it.inject(this) }

	@Inject internal lateinit var viewModelFactory: DiaryViewModel.Factory
	private val viewModel: DiaryViewModel by activityViewModels { ViewModelProvider(viewModelFactory, this) }

	override fun bindUi()
	{
		bindEnvironmentUi()
		bindSizeUi()
		bindLightUi()
	}

	override fun bindVm()
	{
		viewModel.diary.observe(viewLifecycleOwner) { diary ->
			if (!diary.isSuccess) return@observe

			val diary = diary.asSuccess()
			diary.environment()?.let { environment ->
				environment_type_options.checkItems(environment.strRes)
			}
			diary.size()?.let { size ->
				environment_size_width.editText!!.text = size.width?.asString()?.asEditable()
				environment_size_height.editText!!.text = size.height?.asString()?.asEditable()
				environment_size_depth.editText!!.text = size.depth?.asString()?.asEditable()
			}
			diary.light()?.let { light ->
				light_type_options.checkItems(light.type.strRes)

				val isSunlight = light.type == LightType.Sunlight
				light_brand.isVisible = !isSunlight
				light_wattage.isVisible = !isSunlight

				if (!isSunlight)
				{
					light_wattage.editText!!.text = light.wattage?.asString()?.asEditable()
					light_brand.editText!!.text = light.brand?.asEditable()
				}
			}
		}
	}

	public fun bindEnvironmentUi()
	{
		environment_type_options.setMenu(EnvironmentType.toMenu())
		environment_type_options.itemSelectListener = { item ->
			viewModel.setEnvironment(
				type = ValueHolder(item.isChecked then EnvironmentType.ofId(item.itemId))
			)
		}
	}

	public fun bindSizeUi()
	{
		fun updateSize()
		{
			val width = environment_size_width.editText?.text?.toDoubleOrNull()
			val widthUnit = (environment_size_width_unit.editText as DropDownEditText).getSelectedItems().firstOrNull()
			val height = environment_size_height.editText?.text?.toDoubleOrNull()
			val heightUnit = (environment_size_height_unit.editText as DropDownEditText).getSelectedItems().firstOrNull()
			val depth = environment_size_depth.editText?.text?.toDoubleOrNull()
			val depthUnit = (environment_size_depth_unit.editText as DropDownEditText).getSelectedItems().firstOrNull()

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
			environment_size_width.editText!!,
			environment_size_height.editText!!,
			environment_size_depth.editText!!
		).forEach {
			it.onFocusLoss {
				updateSize()
			}
		}

		arrayOf(
			environment_size_width_unit.editText!!,
			environment_size_height_unit.editText!!,
			environment_size_depth_unit.editText!!
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
			light_type_options.getSelectedItems().firstOrNull()?.let {
				val type = LightType.ofId(it.itemId)
				viewModel.setEnvironment(
					light = ValueHolder(Light(
						type = type
					).apply {
						if (type != LightType.Sunlight)
						{
							wattage = light_wattage.editText!!.text.toDoubleOrNull()
							brand = light_brand.editText!!.text.toStringOrNull()
						}
					})
				)
			}
		}

		light_type_options.setMenu(LightType.toMenu())
		light_brand.isVisible = false
		light_wattage.isVisible = false

		light_type_options.itemSelectListener = { item ->
			val isSunlight = LightType.ofId(item.itemId) == LightType.Sunlight
			light_brand.isVisible = !isSunlight
			light_wattage.isVisible = !isSunlight

			updateLight()
		}

		arrayOf(
			light_wattage.editText!!,
			light_brand.editText!!
		).forEach {
			it.onFocusLoss {
				updateLight()
			}
		}
	}
}
