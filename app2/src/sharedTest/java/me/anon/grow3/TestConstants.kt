package me.anon.grow3

import me.anon.grow3.data.model.Garden
import me.anon.grow3.util.parseAsGardens
import java.io.File

object TestConstants
{
	public val gardens_json: String by lazy {
		File("src/sample/assets/gardens.json").readText()
	}

	public val gardens get() = gardens_json.parseAsGardens()

	public val newGarden get() = Garden(
		id = "test-uuid",
		name = "New Garden"
	)
}
