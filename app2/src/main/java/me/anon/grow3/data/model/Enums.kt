package me.anon.grow3.data.model

enum class EnvironmentType
{
	Tent,
	Outdoors,
	Indoors,
	Greenhouse
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
	FIMM,
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
