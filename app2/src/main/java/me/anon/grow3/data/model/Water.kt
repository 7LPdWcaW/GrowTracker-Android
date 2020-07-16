package me.anon.grow3.data.model

import com.squareup.moshi.JsonClass

/**
 * See [me.anon.grow3.ui.logs.view.WaterLogCard]
 */
@JsonClass(generateAdapter = true)
data class Water(
	public var inPH: PHUnit?,
	public var outPH: PHUnit?,
	public var tds: TdsUnit?,
	public var amount: Double?,
	public var temperature: Double?,
	public val additives: ArrayList<Additive> = arrayListOf()
) : Log(action = "Water")
{
	class PHUnit(
		public var amount: Double
	)

	class TdsUnit(
		public var type: TdsType,
		public var amount: Double
	)

	class Additive(
		public var description: String = "",
		public var amount: Double
	)

	override fun equals(other: Any?): Boolean = id == (other as? Log)?.id || super.equals(other)
}

public fun Water(block: Water.() -> Unit): Water = Water(null, null, null, null, null).apply(block)
