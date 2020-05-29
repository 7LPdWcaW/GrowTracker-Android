package me.anon.grow3.data.model

import me.anon.grow3.R
import me.anon.grow3.view.DropDownEditText

interface Type
{
	val strRes: Int
	//val iconRes: Int
}

enum class EnvironmentType : Type
{
	Tent {
		override val strRes: Int = R.string.environment_type_tent
	},
	Outdoors {
		override val strRes: Int = R.string.environment_type_outdoors
	},
	Indoors {
		override val strRes: Int = R.string.environment_type_indoors
	},
	Greenhouse {
		override val strRes: Int = R.string.environment_type_greenhouse
	};

	companion object
	{
		fun toMenu(): List<DropDownEditText.DropDownMenuItem>
			= values().map {
				DropDownEditText.DropDownMenuItem(
					it.strRes,
					false,
					true,
					titleRes = it.strRes
				)
			}
	}
}

enum class MediumType
{
	Soil,
	Hydro,
	Coco,
	Aqua
}

enum class TdsType
{
	PPM500,
	PPM700,
	EC
}

enum class LightType
{
	LED,
	HID,
	CFL,
	DE,
	CMH,
	Sunlight,
}

enum class StageType
{
	Planted,
	Germination,
	Seedling,
	Cutting,
	Vegetation,
	Budding,
	Flower,
	Ripening,
	Drying,
	Curing,
	Harvested
}

enum class MaintenanceType
{
	Topped,
	Trimmed,
	LowStressTraining,
	FIM,
	ScrogTuck,
	Lollipop,
	Supercrop,
	Monstercrop
}

enum class PesticideType
{
	Spray,
	Liquid,
	Solid
}
