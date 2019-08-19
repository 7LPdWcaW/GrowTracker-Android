package me.anon.model

import android.content.Context

import me.anon.grow.R

enum class PlantStage private constructor(val printString: Int)
{
	PLANTED(R.string.planted),
	GERMINATION(R.string.germination),
	SEEDLING(R.string.seedling),
	CUTTING(R.string.cutting),
	VEGETATION(R.string.vegetation),
	FLOWER(R.string.flowering),
	DRYING(R.string.drying),
	CURING(R.string.curing),
	HARVESTED(R.string.harvested);

	companion object
	{
		public fun names(context: Context): Array<String>
		{
			val names = arrayListOf<String>()
			values().forEach { stage ->
				names.add(context.getString(stage.printString))
			}

			return names.toTypedArray()
		}

		public fun valueOfPrintString(context: Context, printString: String): PlantStage?
		{
			for (plantStage in values())
			{
				if (context.getString(plantStage.printString) == printString)
				{
					return plantStage
				}
			}

			return null
		}
	}
}
