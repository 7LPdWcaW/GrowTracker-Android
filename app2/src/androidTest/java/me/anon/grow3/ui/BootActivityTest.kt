package me.anon.grow3.ui

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import org.amshove.kluent.shouldBeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BootActivityTest
{
	@get:Rule
	public var activityRule: ActivityTestRule<BootActivity> = ActivityTestRule(BootActivity::class.java, true, false)

	@Test
	public fun testBootActivityLaunchesFirstUse()
	{
		val instrumentation = InstrumentationRegistry.getInstrumentation()

		activityRule.launchActivity(Intent(instrumentation.targetContext, BootActivity::class.java))
		activityRule.activity.isFinishing
			.shouldBeTrue()
	}
}
