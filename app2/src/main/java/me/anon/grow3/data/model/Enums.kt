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
		fun ofId(id: Int): EnvironmentType = values().first { it.strRes == id }

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

enum class MediumType : Type
{
	Soil {
		override val strRes: Int = R.string.medium_type_soil
	},
	Hydro {
		override val strRes: Int = R.string.medium_type_hydro
	},
	Coco {
		override val strRes: Int = R.string.medium_type_coco
	},
	Aqua {
		override val strRes: Int = R.string.medium_type_aqua
	};

	companion object
	{
		fun ofId(id: Int): MediumType = MediumType.values().first { it.strRes == id }

		fun toMenu(): List<DropDownEditText.DropDownMenuItem>
			= MediumType.values().map {
				DropDownEditText.DropDownMenuItem(
					it.strRes,
					false,
					true,
					titleRes = it.strRes
				)
			}
	}
}

enum class TdsType
{
	PPM500,
	PPM700,
	EC
}

enum class LightType : Type
{
	LED {
		override val strRes = R.string.light_type_led
	},
	HID {
		override val strRes = R.string.light_type_hid
	},
	CFL {
		override val strRes = R.string.light_type_cfl
	},
	DE {
		override val strRes = R.string.light_type_de
	},
	CMH {
		override val strRes = R.string.light_type_cmh
	},
	Sunlight {
		override val strRes = R.string.light_type_sunlight
	};

	companion object
	{
		fun ofId(id: Int): LightType = LightType.values().first { it.strRes == id }

		fun toMenu(): List<DropDownEditText.DropDownMenuItem>
			= LightType.values().map {
				DropDownEditText.DropDownMenuItem(
					it.strRes,
					false,
					true,
					titleRes = it.strRes
				)
			}
	}
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
