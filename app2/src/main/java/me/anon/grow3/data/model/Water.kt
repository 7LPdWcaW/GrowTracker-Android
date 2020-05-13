package me.anon.grow3.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class Water(
	public var inPH: PHUnit?,
	public var outPH: PHUnit?,
	public var tds: TdsUnit?,
	public var amount: Double?,
	public var temperature: Double?,
	public val additives: ArrayList<Additive> = arrayListOf()
) : Log(action = "Water")
{
	class PHUnit(
		public var amount: Double?
	)

	class TdsUnit(
		public var type: TdsType,
		public var amount: Double?
	)

	class Additive(
		public var description: String = "",
		public var amount: Double?
	)
}
