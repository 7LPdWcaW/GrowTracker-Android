package me.anon.grow3

import me.anon.grow3.data.model.Diary
import me.anon.grow3.util.parseAsDiaries
import java.io.File

object TestConstants
{
	public val diaries_json: String by lazy {
		File("src/sample/assets/diaries.json").readText()
	}

	public val diaries get() = diaries_json.parseAsDiaries()

	public val newDiary get() = Diary(
		id = "test-uuid",
		name = "New Diary"
	)
}
