package me.anon.model

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

/**
 * // TODO: Add class description
 */
@Parcelize
@JsonClass(generateAdapter = true)
class Garden(
	var name: String = "",
	var plantIds: ArrayList<String> = arrayListOf()
) : Parcelable
