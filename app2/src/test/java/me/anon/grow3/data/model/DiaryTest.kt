package me.anon.grow3.data.model

import me.anon.grow3.TestConstants
import me.anon.grow3.util.initThreeTen
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test

class DiaryTest
{
	private lateinit var diaries: List<Diary>

	init {
		initThreeTen()
	}

	@Before
	public fun setupDiaries()
	{
		diaries = TestConstants.diaries
	}

	@Test
	public fun `test diary loaded`()
	{
		val diary = diaries.first()
		diary.`should not be null`()

		diary.size().`should not be null`()

		with(diary.light().`should not be null`()) {
			type.`should be`(LightType.LED)
			wattage.`should be equal to`(250.0)
			brand.`should be equal to`("Custom")
		}

		diary.environment().`should not be null`()
			.`should be`(EnvironmentType.Outdoors)
		diary.size().`should not be null`()
			.`should be equal to`(Size(1000.0, 1000.0, 1000.0))

		diary.crops.`should not be empty`()
		val cropStages = diary.mapCropStages().`should not be empty`()
		cropStages[diary.crops[0]].`should not be null`()
			.type.`should be equal to`(StageType.Planted)
		cropStages[diary.crops[1]].`should not be null`()
			.type.`should be equal to`(StageType.Vegetation)
		cropStages[diary.crops[2]].`should not be null`()
			.type.`should be equal to`(StageType.Flower)
		cropStages[diary.crops[3]].`should not be null`()
			.type.`should be equal to`(StageType.Vegetation)

		// diary.stage always adopts latest change
		diary.stage().`should not be null`()
			.type.`should be equal to`(StageType.Vegetation)
	}

	@Test
	public fun `test default diary adds croped stage`()
	{
		val newDiary = Diary(name = "Test")
		newDiary.stage().`should not be null`()
			.type.`should be equal to`(StageType.Planted)
	}

	@Test
	public fun `test crop stage lengths`()
	{
		val diary = diaries.first()
		val cropStages = diary.calculateCropStageLengths()
		cropStages.keys.`should not be empty`()

		// planted
		with (cropStages[diary.crop("0002-000001")].`should not be null`()) {
			size.`should be equal to`(1)
			val key = keys.elementAt(0)
			key.type.`should be equal to`(StageType.Planted)
			get(key)
				.`should not be null`()
				.`should be greater than`(0.0)
		}

		// planted -> veg
		with (cropStages[diary.crop("0002-000002")].`should not be null`()) {
			size.`should be equal to`(2)
			var key = keys.elementAt(0)
			key.type.`should be equal to`(StageType.Planted)
			get(key)
				.`should not be null`()
				.`should be greater or equal to`(1.0)

			key = keys.elementAt(1)
			key.type.`should be equal to`(StageType.Vegetation)
			get(key)
				.`should not be null`()
				.`should be greater than`(0.0)
		}

		// planted -> veg -> flower -> veg
		with (cropStages[diary.crop("0002-000004")].`should not be null`()) {
			size.`should be equal to`(3)
			var key = keys.elementAt(0)
			key.type.`should be equal to`(StageType.Planted)
			get(key)
				.`should not be null`()
				.`should be greater or equal to`(1.0)
				.`should be less than`(2.0)

			key = keys.elementAt(1)
			key.type.`should be equal to`(StageType.Vegetation)
			get(key)
				.`should not be null`()
				.`should be greater or equal to`(3.0)
				.`should be less than`(4.0)

			key = keys.elementAt(2)
			key.type.`should be equal to`(StageType.Flower)
			get(key)
				.`should not be null`()
				.`should be greater or equal to`(2.0)
		}
	}
}
