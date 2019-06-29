package me.anon.lib.ext

import java.lang.Exception

public fun String?.toSafeInt(): Int
{
	try
	{
		if (this?.indexOf('.') ?: -1 > -1)
		{
			return this?.toDouble()?.toInt() ?: 0
		}

		return this?.toInt() ?: 0
	}
	catch (e: Exception)
	{
		return 0
	}
}
