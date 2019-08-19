package me.anon.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
class Additive(
	var amount: Double? = null,
	var description: String? = null
) : Parcelable
