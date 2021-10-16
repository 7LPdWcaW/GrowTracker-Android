package me.anon.grow3.ui.crud.fragment

import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.freelapp.flowlifecycleobserver.collectWhileStarted
import me.anon.grow3.data.model.*
import me.anon.grow3.databinding.FragmentCrudDiaryEnvironmentBinding
import me.anon.grow3.ui.base.BaseFragment
import me.anon.grow3.ui.crud.viewmodel.DiaryCrudViewModel
import me.anon.grow3.util.*
import me.anon.grow3.view.DropDownEditText
import javax.inject.Inject

class DiaryEnvironmentFragment : BaseFragment(FragmentCrudDiaryEnvironmentBinding::class)
{
	override val injector: Injector = { it.inject(this) }

	@Inject internal lateinit var crudViewModelFactory: DiaryCrudViewModel.Factory
	private val crudViewModel: DiaryCrudViewModel by activityViewModels { ViewModelProvider(crudViewModelFactory, this) }
	private val viewBindings by viewBinding<FragmentCrudDiaryEnvironmentBinding>()

	override fun onStop()
	{
		requireView().clearFocus()
		super.onStop()
	}

	override fun bindUi()
	{
		bindEnvironmentUi()
		bindSizeUi()
		bindLightUi()
	}

	override fun bindVm()
	{
		crudViewModel.state
			.collectWhileStarted(this) { state ->
				val diary = (state as? DiaryCrudViewModel.UiResult.Loaded)?.diary ?: return@collectWhileStarted
				diary.environment()?.let { environment ->
					environment.type?.let { type ->
						viewBindings.environmentTypeOptions.checkItems(type.strRes)
					}

					environment.size?.let { size ->
						size.width?.let { width ->
							viewBindings.environmentSizeWidth.editText!!.text = width.amount.asString().asEditable()
							(viewBindings.environmentSizeWidthUnit.editText as DropDownEditText).checkItems(DimensionUnit.idOf(width.unit))
						}

						size.height?.let { height ->
							viewBindings.environmentSizeHeight.editText!!.text = height.amount.asString().asEditable()
							(viewBindings.environmentSizeHeightUnit.editText as DropDownEditText).checkItems(DimensionUnit.idOf(height.unit))
						}

						size.depth?.let { depth ->
							viewBindings.environmentSizeDepth.editText!!.text = depth.amount.asString().asEditable()
							(viewBindings.environmentSizeDepthUnit.editText as DropDownEditText).checkItems(DimensionUnit.idOf(depth.unit))
						}
					}

					environment.light?.let { light ->
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
	}

	public fun bindEnvironmentUi()
	{
		viewBindings.environmentTypeOptions.setMenu(EnvironmentType.toMenu())
		viewBindings.environmentTypeOptions.itemSelectListener = { item ->
			crudViewModel.mutateEnvironment {
				patch(type = ValueHolder(item.isChecked then EnvironmentType.ofId(item.itemId)))
			}
		}
	}

	public fun bindSizeUi()
	{
		fun updateSize()
		{
			val width = viewBindings.environmentSizeWidth.editText?.text?.toDoubleOrNull()
			val widthUnit = (viewBindings.environmentSizeWidthUnit.editText as DropDownEditText).getSelectedItems().first()
			val height = viewBindings.environmentSizeHeight.editText?.text?.toDoubleOrNull()
			val heightUnit = (viewBindings.environmentSizeHeightUnit.editText as DropDownEditText).getSelectedItems().first()
			val depth = viewBindings.environmentSizeDepth.editText?.text?.toDoubleOrNull()
			val depthUnit = (viewBindings.environmentSizeDepthUnit.editText as DropDownEditText).getSelectedItems().first()

			val size = if (width != null || height != null || depth != null)
				Size(
					width = width?.let { Dimension(it, DimensionUnit.ofId(widthUnit.itemId)) },
					height = height?.let { Dimension(it, DimensionUnit.ofId(heightUnit.itemId)) },
					depth = depth?.let { Dimension(it, DimensionUnit.ofId(depthUnit.itemId)) }
				)
			else null

			crudViewModel.mutateEnvironment {
				patch(size = ValueHolder(size))
			}
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
			with (it as DropDownEditText) {
				setMenu(DimensionUnit.toMenu())

				itemSelectListener = {
					updateSize()
				}
			}
		}
	}

	public fun bindLightUi()
	{
		fun updateLight()
		{
			viewBindings.lightTypeOptions.getSelectedItems().firstOrNull()?.let {
				val type = LightType.ofId(it.itemId)
				crudViewModel.mutateEnvironment {
					patch(
						light = ValueHolder(Light(type = type).apply {
							if (type != LightType.Sunlight)
							{
								wattage = viewBindings.lightWattage.editText!!.text.toDoubleOrNull()
								brand = viewBindings.lightBrand.editText!!.text.toStringOrNull()
							}
						})
					)
				}
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
