package me.anon.grow3.ui.setup.fragment

import androidx.fragment.app.testing.launchFragmentInContainer
import me.anon.grow3.ui.crud.fragment.DiaryDetailsFragment
import org.junit.Test

class DiaryDetailsFragmentTest
{
	@Test
	public fun testFragment()
	{
		val scenario = launchFragmentInContainer<DiaryDetailsFragment>()
//		scenario.moveToState(Lifecycle.State.CREATED)
		scenario.recreate()
		scenario.onFragment { fragment ->

		}
	}
}
