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

enum class StageType : Type
{
	Planted {
		override val strRes = R.string.stage_type_planted
		override val iconRes = R.drawable.ic_coloured_icon
	},
	Germination {
		override val strRes = R.string.stage_type_germination
		override val iconRes = R.drawable.ic_coloured_icon
	},
	Seedling {
		override val strRes = R.string.stage_type_seedling
		override val iconRes = R.drawable.ic_coloured_icon
	},
	Cutting {
		override val strRes = R.string.stage_type_cutting
		override val iconRes = R.drawable.ic_coloured_icon
	},
	Vegetation {
		override val strRes = R.string.stage_type_vegetation
		override val iconRes = R.drawable.ic_coloured_icon
	},
	Budding {
		override val strRes = R.string.stage_type_budding
		override val iconRes = R.drawable.ic_coloured_icon
	},
	Flower {
		override val strRes = R.string.stage_type_flower
		override val iconRes = R.drawable.ic_coloured_icon
	},
	Ripening {
		override val strRes = R.string.stage_type_ripening
		override val iconRes = R.drawable.ic_coloured_icon
	},
	Drying {
		override val strRes = R.string.stage_type_drying
		override val iconRes = R.drawable.ic_coloured_icon
	},
	Curing {
		override val strRes = R.string.stage_type_curing
		override val iconRes = R.drawable.ic_coloured_icon
	},
	Harvested {
		override val strRes = R.string.stage_type_harvested
		override val iconRes = R.drawable.ic_coloured_icon
	};

	abstract val iconRes: Int

	companion object
	{
		fun ofId(id: Int): StageType = values().first { it.strRes == id }

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

enum class VolumeUnit : Type
{
	Ml {
		override val strRes: Int = R.string.volume_unit_ml
	},
	L {
		override val strRes: Int = R.string.volume_unit_l
	};

	companion object
	{
		fun ofId(id: Int): VolumeUnit = VolumeUnit.values().first { it.strRes == id }

		fun toMenu(): List<DropDownEditText.DropDownMenuItem>
			= VolumeUnit.values().map {
				DropDownEditText.DropDownMenuItem(
					it.strRes,
					false,
					false,
					titleRes = it.strRes,
					iconRes = -1
				)
		}
	}
}