package me.anon.grow3.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class Photo(
	public val imagePath: String
) : Log(action = "Photo")
