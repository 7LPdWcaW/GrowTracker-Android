package me.anon.grow3.data.model

import me.anon.grow3.TestConstants
import me.anon.grow3.util.T
import me.anon.grow3.util.initThreeTen
import me.anon.grow3.util.uniqueBy
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import org.threeten.bp.ZonedDateTime
import java.util.*
import kotlin.collections.ArrayList

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
			type.`should be`(LightType.HID)
			wattage.`should be equal to`(1000.0)
			brand.`should be equal to`("Sungro")
		}

		diary.environment().`should not be null`()
			.`should be`(EnvironmentType.Greenhouse)
		diary.size().`should not be null`()
			.`should be equal to`(Size(10000.0, 3000.0, 5000.0))

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
	public fun `test default diary adds crop stage`()
	{
		val newDiary = Diary(name = "Test")
		newDiary.stage().`should not be null`()
			.type.`should be equal to`(StageType.Planted)
	}

	@Test
	public fun `test crop stage lengths`()
	{
		val diary = diaries.first()
		val cropStages = diary.mapCropStageLengths()
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

	@Test
	public fun `test harvested crop`()
	{
		val diary = diaries.first()
		diary.stageOf("0002-000005").`should not be null`()
			.type.`should be equal to`(StageType.Harvested)

		diary.harvestedOf("0002-000005").`should not be null`()
			.amount.`should be equal to`(876.0)
	}

	@Test
	public fun `benchmark test`()
	{
		var timelineStart = ZonedDateTime.now()
		var logCounter = 100000
		var cropCounter = 100
		val diary = Diary {
			name = "Test diary"
		}

		do
		{
			diary.crops += Crop {
				name = "Test Crop $cropCounter"
				genetics = "Unknown"
				numberOfPlants = (Math.random() * 10).toInt()
			}
		} while (cropCounter-- >= 0)

		do
		{
			// pick random number of crops to select
			var cropSelector = (0..diary.crops.size).random()
			val selectedCrops = ArrayList<String>(cropSelector)
			while (cropSelector-- >= 0)
			{
				selectedCrops.add(diary.crops[(0 until diary.crops.size).random()].id)
			}

			val crops = selectedCrops.uniqueBy { it }

			val actionGenerators = arrayOf(
				::generateWater,
				::generateStageChange,
				::generateMaintenance
			)

			val log = actionGenerators[(actionGenerators.indices).random()].invoke()
			log.cropIds.addAll(crops)
			diary.log += log

			timelineStart.plusDays((0L..10L).random())
		} while (logCounter-- > 0)

		// test methods
		diary.water()
		diary.stage()
	}

	private fun generateWater(): Water
	{
		return Water {
			inPH = ((1..2).random() % 2 == 0) T Water.PHUnit((40..70).random() / 10.0)
			outPH = ((1..2).random() % 2 == 0) T Water.PHUnit((40..70).random() / 10.0)
			tds = ((1..2).random() % 2 == 0) T Water.TdsUnit(TdsType.values().random(), (0..1000).random().toDouble())
			amount = ((1..2).random() % 2 == 0) T (1000..10_000).random().toDouble()
			temperature = ((1..2).random() % 2 == 0) T (20..35).random().toDouble()
			additives.addAll(ArrayList<Water.Additive>().apply {
				((1..2).random() % 2 == 0) T {
					var counter = (0..5).random()
					while (counter-- >= 0)
					{
						add(Water.Additive(UUID.randomUUID().toString(), (10..50).random() / 10.0))
					}
				}()
			})
		}
	}

	private fun generateStageChange(): StageChange = StageChange(StageType.values().random())
	private fun generateMaintenance(): Maintenance = Maintenance(MaintenanceType.values().random())
}
