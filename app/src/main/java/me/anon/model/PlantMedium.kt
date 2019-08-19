package me.anon.model

import android.content.Context

import me.anon.grow.R

enum class PlantMedium private constructor(val printString: Int)
{
	SOIL(R.string.soil),
	HYDRO(R.string.hydroponics),
	COCO(R.string.coco_coir),
	AERO(R.string.aeroponics);

	companion object
	{
		fun names(context: Context): Array<String>
		{
			val names = arrayListOf<String>()
			values().forEach { medium ->
				names.add(context.getString(medium.printString))
			}

			return names.toTypedArray()
		}
	}
}
