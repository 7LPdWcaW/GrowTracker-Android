package me.anon.lib;

import android.content.Context;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Unit class used for measurement input
 */
@AllArgsConstructor
public enum Unit
{
	MLPL("ml/l");

	@Getter private String label;

	public static double getMlPlForUnit(double inputValue, Unit fromUnit)
	{
		return inputValue;
	}

	public static Unit getSelectedUnit(Context context)
	{
		return MLPL;
	}
}
