package me.anon.grow3.util

import me.anon.grow3.TestConstants
import org.amshove.kluent.*
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
	public fun `test parse gardens string`()
	{
		val gardensString = TestConstants.gardens_json
		val parsed = gardensString.parseAsGardens()
		parsed.`should not be empty`()
	}

	@Test
	public fun `test load gardens file`()
	{
		// write file to temp
		val gardensString = TestConstants.gardens_json
		val file = File.createTempFile("gardens", "json")
		file.deleteOnExit()
		file.writeBytes(gardensString.toByteArray())

		// load file
		val parsed = file.loadAsGardens()
		parsed
			.`should not be null`()
			.`should not be empty`()
	}

	@Test
	public fun `test load gardens fallback`()
	{
		val file = File("does-not-exist")
		val parsed = file.loadAsGardens { TestConstants.gardens }

		// all items should be the same
		parsed
			.`should not be null`()
			.`should contain all`(TestConstants.gardens)
	}

	@Test
	public fun `test save gardens`()
	{
		val file = File.createTempFile("test-save-gardens", "json")
		file.deleteOnExit()

		TestConstants.gardens.saveAsGardens(file)
		with (file) {
			exists().`should be`(true)
			length().`should be greater than`(0)
		}
	}
}
