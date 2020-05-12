package me.anon.grow3.data.model

import me.anon.grow3.TestConstants
import me.anon.grow3.util.initThreeTen
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test

class GardenTest
{
	private lateinit var gardens: List<Garden>

	init
	{
		initThreeTen()
	}

	@Before
	public fun setupGarden()
	{
		gardens = TestConstants.gardens
	}

	@Test
	public fun `test garden loaded`()
	{
		val garden = gardens.first()
		garden.`should not be null`()

		garden.size().`should not be null`()

		with(garden.light().`should not be null`())
		{
			type.`should be`(LightType.LED)
			wattage.`should be equal to`(250.0)
			brand.`should be equal to`("Custom")
		}

		garden.type().`should not be null`()
			.`should be`(EnvironmentType.Outdoors)
		garden.size().`should not be null`()
			.`should be equal to`(Size(1000.0, 1000.0, 1000.0))

		garden.plants.`should not be empty`()
		val plantStages = garden.mapPlantStages().`should not be empty`()
		plantStages[garden.plants[0]].`should not be null`()
			.type.`should be equal to`(StageType.Planted)
		plantStages[garden.plants[1]].`should not be null`()
			.type.`should be equal to`(StageType.Vegetation)
		plantStages[garden.plants[2]].`should not be null`()
			.type.`should be equal to`(StageType.Flower)
		plantStages[garden.plants[3]].`should not be null`()
			.type.`should be equal to`(StageType.Vegetation)

		// garden.stage always adopts latest change
		garden.stage().`should not be null`()
			.type.`should be equal to`(StageType.Vegetation)
	}

	@Test
	public fun `test default garden adds planted stage`()
	{
		val newGarden = Garden(name = "Test")
		newGarden.stage().`should not be null`()
			.type.`should be equal to`(StageType.Planted)
	}

	@Test
	public fun `test plant stage lengths`()
	{
		val garden = gardens.first()
		val plantStages = garden.calculatePlantStageLengths()
		plantStages.keys.`should not be empty`()

		// planted
		with (plantStages[garden.plant("0000-000-0000-000001")].`should not be null`())
		{
			size.`should be equal to`(1)
			val key = keys.elementAt(0)
			key.type.`should be equal to`(StageType.Planted)
			get(key)
				.`should not be null`()
				.`should be greater than`(0.0)
		}

		// planted -> veg
		with (plantStages[garden.plant("0000-000-0000-000002")].`should not be null`())
		{
			size.`should be equal to`(2)
			var key = keys.elementAt(0)
			key.type.`should be equal to`(StageType.Planted)
			get(key)
				.`should not be null`()
				.`should be greater than`(1.0)

			key = keys.elementAt(1)
			key.type.`should be equal to`(StageType.Vegetation)
			get(key)
				.`should not be null`()
				.`should be greater than`(0.0)
		}

		// planted -> veg -> flower -> veg
		with (plantStages[garden.plant("0000-000-0000-000004")].`should not be null`())
		{
			size.`should be equal to`(3)
			var key = keys.elementAt(0)
			key.type.`should be equal to`(StageType.Planted)
			get(key)
				.`should not be null`()
				.`should be greater than`(1.0)

			key = keys.elementAt(1)
			key.type.`should be equal to`(StageType.Vegetation)
			get(key)
				.`should not be null`()
				.`should be greater than`(3.0)

			key = keys.elementAt(2)
			key.type.`should be equal to`(StageType.Flower)
			get(key)
				.`should not be null`()
				.`should be greater than`(2.0)
		}
	}
}
