package me.anon.grow3.util

import androidx.test.platform.app.InstrumentationRegistry
import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.zone.TzdbZoneRulesProvider
import org.threeten.bp.zone.ZoneRulesProvider

public fun initAndroidThreeTen()
{
	val appContext = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
	AndroidThreeTen.init(appContext)
}

public fun Any.initThreeTen()
{
	if (ZoneRulesProvider.getAvailableZoneIds().isEmpty())
	{
		val stream = this.javaClass.classLoader!!.getResourceAsStream("TZDB.dat")
		stream.use(::TzdbZoneRulesProvider).apply {
			ZoneRulesProvider.registerProvider(this)
		}
	}
}
