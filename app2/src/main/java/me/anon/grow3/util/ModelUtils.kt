package me.anon.grow3.util

import com.squareup.moshi.Types
import me.anon.grow3.data.model.Garden
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

public fun String.parseAsGardens(): List<Garden> = MoshiHelper.parse(
	json = this,
	type = Types.newParameterizedType(ArrayList::class.java, Garden::class.java)
)

public fun File.loadAsGardens(default: () -> List<Garden> = { arrayListOf<Garden>() }): List<Garden>
{
	try
	{
		with(FileInputStream(this)) {
			val data = MoshiHelper.parse<ArrayList<Garden>>(
				json = this,
				type = Types.newParameterizedType(ArrayList::class.java, Garden::class.java)
			)
			close()
			return data ?: default()
		}
	}
	catch (e: Exception)
	{
		return default()
	}
}

public fun List<Garden>.saveAsGardens(file: File)
{
	with (FileOutputStream(file)) {
		MoshiHelper.toJson(
			obj = this@saveAsGardens,
			type = Types.newParameterizedType(ArrayList::class.java, Garden::class.java),
			outputStream = this@with
		)
		flush()
		close()
	}
}
