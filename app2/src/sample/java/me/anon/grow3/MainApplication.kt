package me.anon.grow3

import java.io.File
import java.io.FileOutputStream

class MainApplication : BaseApplication()
{
	override fun setup()
	{
		with (resources.assets.open("gardens.json"))
		{
			val file = File("$dataPath/gardens.json")
			file.delete()

			val out = FileOutputStream(file)
			copyTo(out)
			out.close()
			close()
		}
	}
}
