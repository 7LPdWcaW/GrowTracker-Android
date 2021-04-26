package me.anon.grow3.view

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import androidx.core.view.ViewCompat
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer
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
	public var selectAll: Boolean = false

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

		diary?.crops?.mapToView<Crop, StubSelectCropBinding>(this) { crop, binding ->
			binding.chip.id = ViewCompat.generateViewId()
			binding.chip.text = crop.name
			binding.chip.tag = crop
			binding.chip.isChecked = selectedCrops.contains(crop.id)
			if (binding.chip.isChecked)
			{
				selectedCrops.add(crop.id)
			}

			val image = R.drawable.sample.drawable(context)
			binding.chip.chipIcon = CircleBitmapDisplayer.CircleDrawable((image as BitmapDrawable).bitmap, 0, 0f)

			binding.chip.onClick {
				if (it.isChecked) selectedCrops.add(crop.id)
				else selectedCrops.remove(crop.id)

				onCropSelected(crop, it.isCheckable)
			}
		}
	}
}
