package me.anon.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Additive(
	var amount: Double? = null,
	var description: String? = null
) : Parcelable
