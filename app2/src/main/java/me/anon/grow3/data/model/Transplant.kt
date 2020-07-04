package me.anon.grow3.data.model

import com.squareup.moshi.JsonClass
import me.anon.grow3.util.string

typealias Medium = Transplant

@JsonClass(generateAdapter = true)
data class Transplant(
	public var medium: MediumType,
	public var size: Double? = null
) : Log(action = "Transplant")
{
	override fun summary(): CharSequence
	{
		return medium.strRes.string() + " " + size
	}
}
