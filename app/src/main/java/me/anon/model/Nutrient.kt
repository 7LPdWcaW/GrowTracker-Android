package me.anon.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Deprecated("")
@Parcelize
class Nutrient : Parcelable
{
	var npc: Double? = null // nitrogen
	var ppc: Double? = null // phosphorus
	var kpc: Double? = null // potassium
	var capc: Double? = null // calcium
	var spc: Double? = null // sulfur
	var mgpc: Double? = null // magnesium
}
