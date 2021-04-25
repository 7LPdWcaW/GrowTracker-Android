package me.anon.grow3.util.handler

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import me.anon.grow3.BaseApplication
import me.anon.grow3.util.application
import me.anon.grow3.util.component
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldNotBeEmpty
import org.amshove.kluent.shouldNotBeNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExceptionHandlerTest
{
	private lateinit var app: BaseApplication

	@Before
	public fun setup()
	{
		val instrumentation = InstrumentationRegistry.getInstrumentation()
		app = instrumentation.targetContext.application
		app.shouldNotBeNull()
	}

	@Test
	public fun testExceptionHandler()
	{
		var stackTraces = app.component.exceptionHandler().searchForStackTraces()
		stackTraces.shouldBeEmpty()
		app.component.exceptionHandler().sendException(IllegalArgumentException("test"))
		stackTraces = app.component.exceptionHandler().searchForStackTraces()
		stackTraces.shouldNotBeEmpty()
	}
}
