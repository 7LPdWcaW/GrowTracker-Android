package me.anon.grow3.util

import com.squareup.moshi.Types
import me.anon.grow3.data.model.Diary
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream

public fun String.parseAsDiaries(): List<Diary> = MoshiHelper.parse(
	json = this,
	type = Types.newParameterizedType(ArrayList::class.java, Diary::class.java)
)

public fun File.loadAsDiaries(default: () -> List<Diary> = { arrayListOf() }): List<Diary> = FileInputStream(this).loadAsDiaries(default)
public fun InputStream.loadAsDiaries(default: () -> List<Diary> = { arrayListOf() }): List<Diary>
{
	return try
	{
		val data = MoshiHelper.parse<ArrayList<Diary>>(
			json = this,
			type = Types.newParameterizedType(ArrayList::class.java, Diary::class.java)
		)
		close()
		data
	}
	catch (e: Exception)
	{
		default()
	}
}

public fun List<Diary>.saveAsDiaries(file: File)
{
	with (FileOutputStream(file)) {
		MoshiHelper.toJson(
			obj = this@saveAsDiaries,
			type = Types.newParameterizedType(ArrayList::class.java, Diary::class.java),
			outputStream = this@with
		)
		flush()
		close()
	}
}
