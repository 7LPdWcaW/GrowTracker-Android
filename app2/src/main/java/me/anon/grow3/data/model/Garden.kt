package me.anon.grow3.data.model

import java.util.*

/**
 * // TODO: Add class description
 */
class Garden(
	val id: String = UUID.randomUUID().toString(),
	val name: String,
	val plants: ArrayList<Plant> = arrayListOf()
)
