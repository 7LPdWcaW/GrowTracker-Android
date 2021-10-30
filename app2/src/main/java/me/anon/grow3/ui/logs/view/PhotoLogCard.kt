package me.anon.grow3.ui.logs.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.nostra13.universalimageloader.core.ImageLoader
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Photo
import me.anon.grow3.databinding.CardPhotoLogBinding
import me.anon.grow3.databinding.StubPhotoLogPhotoBinding
import me.anon.grow3.util.mapToView

class PhotoLogCard : LogCard<CardPhotoLogBinding, Photo>
{
	constructor() : super()
	constructor(diary: Diary, log: Photo) : super(diary, log)

	inner class PhotoLogCardHolder(view: View) : CardViewHolder(view)
	override fun createViewHolder(inflater: LayoutInflater, parent: ViewGroup): CardViewHolder
		= PhotoLogCardHolder(CardPhotoLogBinding.inflate(inflater, parent, false).root)

	override fun bindView(view: View): CardPhotoLogBinding = CardPhotoLogBinding.bind(view)

	override fun bindLog(view: CardPhotoLogBinding)
	{
		view.content.removeAllViews()
		log.imagePaths.mapToView<String, StubPhotoLogPhotoBinding>(view.content) { image, view ->
			ImageLoader.getInstance()
				.displayImage(image, view.photo)
		}

		view.content.isVisible = view.content.childCount > 0
	}
}
