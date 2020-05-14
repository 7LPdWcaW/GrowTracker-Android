package me.anon.lib.ext

public fun String.normalise(): String
{
	return toLowerCase().replace("[^a-zA-Z0-9+]".toRegex(), "_")
}
