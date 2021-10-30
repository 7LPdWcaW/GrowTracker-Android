package me.anon.grow3.ui.action.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.plusAssign
import androidx.fragment.app.findFragment
import com.github.dhaval2404.imagepicker.ImagePicker
import com.nostra13.universalimageloader.core.ImageLoader
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Photo
import me.anon.grow3.databinding.FragmentActionLogPhotoBinding
import me.anon.grow3.databinding.StubCardLogPhotoBinding
import me.anon.grow3.ui.action.fragment.LogActionFragment
import me.anon.grow3.util.onClick
import me.anon.grow3.util.onLongClick
import me.anon.grow3.util.promptRemove

class PhotoLogView : LogView<FragmentActionLogPhotoBinding>
{
	private lateinit var diary: Diary
	private lateinit var log: Photo

	constructor() : super()
	constructor(diary: Diary, log: Photo) : super()
	{
		this.diary = diary
		this.log = log
	}

	override fun createView(inflater: LayoutInflater, parent: ViewGroup): FragmentActionLogPhotoBinding
		= FragmentActionLogPhotoBinding.inflate(inflater, parent, false)

	override fun bindView(view: View): FragmentActionLogPhotoBinding
		= FragmentActionLogPhotoBinding.bind(view)

	override fun bind(view: FragmentActionLogPhotoBinding)
	{
		fun createImageView(): StubCardLogPhotoBinding
		{
			val photo = StubCardLogPhotoBinding.inflate(LayoutInflater.from(view.root.context), view.photosContainer, false)
			photo.photoContainer.onClick {
				view.root.findFragment<LogActionFragment>().let { fragment ->
					ImagePicker.with(fragment)
						.createIntent { intent ->
							fragment.intentCallback = { result ->
								val fileUri = result.data!!
								log.imagePaths += fileUri.toString()
							}
							fragment.intentResult.launch(intent)
						}
				}
			}
			return photo
		}

		view.photosContainer.removeAllViews()
		view.photosContainer += createImageView().root
		log.imagePaths.forEach { image ->
			val photo = createImageView()
			view.photosContainer.addView(photo.root, view.photosContainer.childCount - 1)

			ImageLoader.getInstance()
				.displayImage(image, photo.photo)

			photo.root.onLongClick {
				view.root.findFragment<LogActionFragment>().let { fragment ->
					fragment.promptRemove {
						if (it)
						{
							log.imagePaths.removeAll { it == image }
							view.photosContainer.removeView(photo.root)
						}
					}
				}
				true
			}
		}
	}

	override fun save(view: FragmentActionLogPhotoBinding): Photo
	{
		return log
	}

	override fun provideTitle(): String = "Edit photo log"
}
