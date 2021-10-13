package me.anon.grow3.ui.action.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.dhaval2404.imagepicker.ImagePicker
import com.nostra13.universalimageloader.core.ImageLoader
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Photo
import me.anon.grow3.databinding.FragmentActionLogPhotoBinding
import me.anon.grow3.util.onClick

class PhotoLogView(
	diary: Diary,
	log: Photo
) : LogView<Photo>(diary, log)
{
	private lateinit var bindings: FragmentActionLogPhotoBinding

	override fun createView(inflater: LayoutInflater, parent: ViewGroup): View
		= FragmentActionLogPhotoBinding.inflate(inflater, parent, false).root

	override fun bindView(view: View)
	{
		bindings = FragmentActionLogPhotoBinding.bind(view)
		bindings.photo.onClick {
			ImagePicker.with(fragment)
				.createIntent { intent ->
					fragment.intentCallback = { result ->
						val fileUri = result.data!!
						log.imagePaths += fileUri.toString()
					}
					fragment.intentResult.launch(intent)
				}
		}

		log.imagePaths.firstOrNull()?.let { image ->
			ImageLoader.getInstance()
				.displayImage(image, bindings.photo)
		}
	}

	override fun saveView(): Photo
	{
		bindings.root.clearFocus()
		return log
	}

	override fun provideTitle(): String? = "Edit photo log"
}
