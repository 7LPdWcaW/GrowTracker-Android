package me.anon.grow3.data.model

import com.squareup.moshi.JsonClass

typealias Medium = Transplant

@JsonClass(generateAdapter = true)
data class Transplant(
	public var medium: MediumType,
	public var size: Double?
) : Log(action = "Transplant")
