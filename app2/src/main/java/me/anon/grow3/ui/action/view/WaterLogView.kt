package me.anon.grow3.ui.action.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.anon.grow3.data.model.Water
import me.anon.grow3.databinding.FragmentActionLogWaterBinding

class WaterLogView(
	log: Water = Water {  }
) : LogView<Water>(log)
{
	override fun createView(inflater: LayoutInflater, parent: ViewGroup): View
		= FragmentActionLogWaterBinding.inflate(inflater, parent, false).root

	override fun bindView(view: View)
	{
		val bindings = FragmentActionLogWaterBinding.bind(view)
	}
}
