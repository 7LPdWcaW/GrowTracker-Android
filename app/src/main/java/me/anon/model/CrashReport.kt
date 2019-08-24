package me.anon.model

import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
class CrashReport : Serializable
{
	// App information
	var version = "unknown"
	var packageName = "unknown"
	var versionCode = "0"

	// Device information
	var model = "unknown"
	var manufacturer = "unknown"
	var osVersion = "unknown"

	var exception: String? = null
	var additionalMessage = ""
	var timestamp = 0L
}
