package me.anon.grow3.ui.action.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.nostra13.universalimageloader.core.ImageLoader
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Photo
import me.anon.grow3.databinding.FragmentActionLogPhotoBinding
import me.anon.grow3.util.onClick

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
		view.photo.onClick {
//			ImagePicker.with(fragment)
//				.createIntent { intent ->
//					fragment.intentCallback = { result ->
//						val fileUri = result.data!!
//						log.imagePaths += fileUri.toString()
//					}
//					fragment.intentResult.launch(intent)
//				}
		}

		log.imagePaths.firstOrNull()?.let { image ->
			ImageLoader.getInstance()
				.displayImage(image, view.photo)
		}
	}

	override fun save(view: FragmentActionLogPhotoBinding): Photo
	{
		return log
	}

	override fun provideTitle(): String = "Edit photo log"
}
