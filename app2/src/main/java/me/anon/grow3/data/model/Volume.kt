package me.anon.grow3.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Volume(
	var amount: Double = 0.0,
	var unit: VolumeUnit =  VolumeUnit.Ml
)