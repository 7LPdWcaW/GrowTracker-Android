package me.anon.grow3.data.model

import com.squareup.moshi.JsonClass

/**
 * See [me.anon.grow3.ui.logs.view.PhotoLogCard]
 */
@JsonClass(generateAdapter = true)
data class Photo(
	public val imagePath: ArrayList<String> = arrayListOf()
) : Log(action = "Photo")
