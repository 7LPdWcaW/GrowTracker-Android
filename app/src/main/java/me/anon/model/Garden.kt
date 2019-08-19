package me.anon.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * // TODO: Add class description
 */
@Parcelize
class Garden(
	var name: String = "",
	var plantIds: ArrayList<String> = arrayListOf()
) : Parcelable
