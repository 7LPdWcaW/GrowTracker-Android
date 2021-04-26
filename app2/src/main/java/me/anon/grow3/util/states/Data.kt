package me.anon.grow3.util.states

import me.anon.grow3.data.model.Crop
import me.anon.grow3.data.model.Diary
import me.anon.grow3.data.model.Log

class Data(
	val diary: Diary? = null,
	val crop: Crop? = null,
	val log: Log? = null
)
