package me.anon.lib;

import android.content.Context;

import lombok.AllArgsConstructor;

/**
 * Unit class used for measurement input
 */
@AllArgsConstructor
public enum Unit
{
	MLPL;

	public static double getMlPlForUnit(double inputValue, Unit fromUnit)
	{
		return inputValue;
	}

	public static Unit getSelectedUnit(Context context)
	{
		return MLPL;
	}
}
