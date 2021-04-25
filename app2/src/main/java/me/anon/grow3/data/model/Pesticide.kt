package me.anon.grow3.data.model

import com.squareup.moshi.JsonClass

/**
 * See [me.anon.grow3.ui.logs.view.PesticideLogCard]
 */
@JsonClass(generateAdapter = true)
data class Pesticide(
	public val type: PesticideType,
	public var name: String = "",
	public var amount: Double?
) : Log(action = "Pesticide")
