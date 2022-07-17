package me.anon.grow3.util

import me.anon.grow3.TestConstants
import org.amshove.kluent.`should be greater than`
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should not be empty`
import org.amshove.kluent.`should not be null`
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File

@RunWith(JUnit4::class)
class ModuleUtilsTest
{
	init {
		initThreeTen()
	}

	@Test
	public fun `test parse diaries string`()
	{
		val diariesString = TestConstants.diaries_json
		val parsed = diariesString.parseAsDiaries()
		parsed.`should not be empty`()
	}

	@Test
	public fun `test load diaries file`()
	{
		// write file to temp
		val diariesString = TestConstants.diaries_json
		val file = File.createTempFile("diaries", "json")
		file.deleteOnExit()
		file.writeBytes(diariesString.toByteArray())

		// load file
		val parsed = file.loadAsDiaries()
		parsed
			.`should not be null`()
			.`should not be empty`()
	}

	@Test
	public fun `test save diaries`()
	{
		val file = File.createTempFile("test-save-diaries", "json")
		file.deleteOnExit()

		TestConstants.diaries.saveAsDiaries(file)
		with (file) {
			exists().`should be`(true)
			length().`should be greater than`(0)
		}
	}
}
