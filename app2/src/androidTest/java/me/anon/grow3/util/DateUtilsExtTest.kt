package me.anon.grow3.util

import androidx.test.ext.junit.runners.AndroidJUnit4
import me.anon.grow3.data.model.Diary
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DateUtilsExtTest
{
	@Test
	public fun testModelDateParse()
	{
		val diary = Diary(date = "2020-01-01T00:00:00+00:00", name = "test")
		val date = with (diary.date.asDateTime())
		{
			year.shouldBeEqualTo(2020)
			monthValue.shouldBeEqualTo(1)
			dayOfMonth.shouldBeEqualTo(1)
			hour.shouldBeEqualTo(0)
			minute.shouldBeEqualTo(0)
			second.shouldBeEqualTo(0)
			offset.totalSeconds.shouldBeEqualTo(0)
			this
		}

		date.asApiString()
			.shouldBeEqualTo("2020-01-01T00:00:00+00:00")
	}

	@Test
	public fun testModelDateDisplay()
	{
		val diary = Diary(date = "2020-01-01T00:00:00+00:00", name = "test")
		val date = diary.date.asDateTime().asDisplayString()
			.shouldBeEqualTo("01 Jan 2020 00:00")

		with (date.fromDisplayString())
		{
			year.shouldBeEqualTo(2020)
			monthValue.shouldBeEqualTo(1)
			dayOfMonth.shouldBeEqualTo(1)
			hour.shouldBeEqualTo(0)
			minute.shouldBeEqualTo(0)
			second.shouldBeEqualTo(0)
			offset.totalSeconds.shouldBeEqualTo(0)
		}
	}
}
