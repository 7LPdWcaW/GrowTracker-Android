package me.anon.grow3.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Log
import me.anon.grow3.databinding.StubCardFooterBinding
import me.anon.grow3.databinding.StubCropSmallBinding
import me.anon.grow3.ui.common.Extras
import me.anon.grow3.ui.crops.fragment.ViewCropFragment
import me.anon.grow3.util.*

class LogFooterView(context: Context, attrs: AttributeSet? = null) : ConstraintLayout(context, attrs)
{
	private val bindings: StubCardFooterBinding

	init {
		bindings = StubCardFooterBinding.inflate(LayoutInflater.from(context), this, true)
	}

	private var diary: Diary? = null
	private var log: Log? = null

	override fun onFinishInflate()
	{
		super.onFinishInflate()
		populate()
	}

	override fun onAttachedToWindow()
	{
		super.onAttachedToWindow()
		populate()
	}

	private fun populate()
	{
		diary ?: return
		log ?: return

		whenNotNull(log, diary) { log, diary ->
			bindings.notes.text = log.notes
			bindings.notes.isVisible = log.notes.isNotBlank()

			bindings.cropsContainer.removeAllViews()
			log.cropIds
				.mapToView<String, StubCropSmallBinding>(bindings.cropsContainer) { cropId, cropView ->
					cropView.cropImage.onClick {
						it.navigateTo<ViewCropFragment> {
							bundleOf(
								Extras.EXTRA_DIARY_ID to diary.id,
								Extras.EXTRA_CROP_ID to cropId
							)
						}
					}
				}
				.hideIfEmpty()
		}
	}

	public fun setLog(diary: Diary, log: Log)
	{
		this.diary = diary
		this.log = log
		requestLayout()
		populate()
	}
}
