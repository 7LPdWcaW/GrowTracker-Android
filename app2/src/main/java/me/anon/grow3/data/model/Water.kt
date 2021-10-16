package me.anon.grow3.data.model

import com.squareup.moshi.JsonClass
import me.anon.grow3.ui.action.view.WaterLogView
import me.anon.grow3.ui.logs.view.WaterLogCard

/**
 * See [me.anon.grow3.ui.logs.view.WaterLogCard]
 */
@JsonClass(generateAdapter = true)
data class Water(
	public var inPH: PHUnit? = null,
	public var outPH: PHUnit? = null,
	public var tds: TdsUnit? = null,
	public var amount: Volume? = null,
	public var temperature: Double? = null,
	public val additives: ArrayList<Additive> = arrayListOf()
) : Log(action = "Water")
{
	class PHUnit(
		public var amount: Double
	)

	class TdsUnit(
		public var amount: Double,
		public var type: TdsType
	)

	class Additive(
		public var description: String = "",
		public var amount: Double
	)

	override val typeRes: Int = me.anon.grow3.R.string.log_type_water
}
public fun Water.logView(diary: Diary) = WaterLogView(diary, this)
public fun Water.logCard(diary: Diary) = WaterLogCard(diary, this)
