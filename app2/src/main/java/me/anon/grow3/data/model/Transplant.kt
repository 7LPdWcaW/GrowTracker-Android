package me.anon.grow3.data.model

import com.squareup.moshi.JsonClass
import me.anon.grow3.util.*

typealias Medium = Transplant

/**
 * See [me.anon.grow3.ui.logs.view.TransplantLogCard]
 */
@JsonClass(generateAdapter = true)
data class Transplant(
	public var medium: MediumType,
	public var size: Double? = null
) : Log(action = "Transplant")
{
	override fun summary(): CharSequence
	{
		return medium.strRes.string() + "\n" + size.asString() + "ml"
	}
}

data class TransplantChange(
	var days: Int,
	var mediumType: Duo<MediumType>,
	var size: Duo<Double?>
) : Delta()

public fun Duo<Transplant>.difference(): TransplantChange
{
	return TransplantChange(
		(first.date and second!!.date).dateDifferenceDays(),
		first.medium and second!!.medium,
		first.size and second!!.size
	)
}
