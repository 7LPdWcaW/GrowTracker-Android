package me.anon.grow3

import kotlinx.coroutines.runBlocking
import me.anon.grow3.data.source.DiariesDataSource
import me.anon.grow3.data.source.nitrite.NitriteDiariesDataSource
import me.anon.grow3.util.component
import me.anon.grow3.util.loadAsDiaries
import java.io.File
import java.io.FileOutputStream

class MainApplication : BaseApplication()
{
	override fun setup()
	{
		with (resources.assets.open("diaries.json")) {
			val file = File("$dataPath/diaries.db")
			file.delete()

			val diaries = loadAsDiaries()

			runBlocking {
				NitriteDiariesDataSource("$dataPath/diaries.db")
					.apply {
						diaries.forEach { addDiary(it) }
					}
			}
		}
	}
}
