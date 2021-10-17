package me.anon.grow3.view

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.core.view.ViewCompat
import androidx.core.view.children
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import me.anon.grow3.R
import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.databinding.StubSelectCropBinding
import me.anon.grow3.util.childViews
import me.anon.grow3.util.drawable
import me.anon.grow3.util.mapToView
import me.anon.grow3.util.onClick

class CropSelectView : ChipGroup
{
	private var diary: Diary? = null
	private var stateCheck = arrayListOf<String>()

	public var selectedCrops = hashSetOf<String>()
	public var onCropSelected: (Crop, Boolean) -> Unit = { _, _ -> }
	public var selectAll: Boolean = true

	constructor(context: Context) : this(context, null)
	constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

	override fun onSaveInstanceState(): Parcelable?
	{
		val state = Bundle()
		state.putParcelable("super", super.onSaveInstanceState())

		val checked = childViews.map { chip ->
			if ((chip as? Chip)?.isChecked == true) (chip.tag as? Crop)?.id else null
		}.toList()
		state.putStringArrayList("checked", checked.filterNotNull() as ArrayList<String>)

		return state
	}

	override fun onRestoreInstanceState(state: Parcelable?)
	{
		val state = state as Bundle
		super.onRestoreInstanceState(state.getParcelable("super"))

		stateCheck = state.getStringArrayList("checked") as ArrayList<String>
		childViews.forEach { chip ->
			if ((chip.tag as? Crop)?.id in stateCheck)
			{
				(chip as? Chip)?.isChecked = true
				selectedCrops.add((chip.tag as Crop).id)
			}
			else
			{
				(chip as? Chip)?.isChecked = false
				selectedCrops.remove((chip.tag as Crop).id)
			}
		}

		stateCheck.clear()
	}

	public fun setDiary(diary: Diary)
	{
		this.diary = diary
		layoutCrops()
	}

	private fun layoutCrops()
	{
		removeAllViews()

		val allChip = StubSelectCropBinding.inflate(LayoutInflater.from(context), this, false)
		allChip.chip.id = ViewCompat.generateViewId()
		allChip.chip.text = "All"
		allChip.chip.tag = "All"
		allChip.chip.isChecked = selectAll
		allChip.chip.onClick {
			selectAll = it.isChecked
			selectedCrops.clear()
			children.forEach { child ->
				if (child != allChip.root)
				{
					val chipBinding = StubSelectCropBinding.bind(child)
					chipBinding.chip.isChecked = it.isChecked
					chipBinding.chip.isEnabled = !it.isChecked
				}
			}
		}
		addView(allChip.root)

		diary?.crops?.mapToView<Crop, StubSelectCropBinding>(this) { crop, binding ->
			binding.chip.id = ViewCompat.generateViewId()
			binding.chip.text = crop.name
			binding.chip.tag = crop
			binding.chip.isEnabled = !selectAll
			binding.chip.isChecked = selectedCrops.contains(crop.id) || selectAll
			if (binding.chip.isChecked && !selectAll)
			{
				selectedCrops.add(crop.id)
			}

			val image = R.drawable.ic_coloured_icon.drawable(context)
			binding.chip.chipIcon = image

			binding.chip.onClick {
				if (it.isChecked) selectedCrops.add(crop.id)
				else selectedCrops.remove(crop.id)

				onCropSelected(crop, it.isChecked)
			}
		}
	}
}
