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
	MLPL("ml", "ml/l")
	{
		/**
		 * Unit to ml/l
		 * @param from Unit to convert to
		 * @param fromValue Unit value
		 * @return converted value
		 */
		public double from(Unit from, double fromValue)
		{
			return from.to(this, fromValue);
		}

		/**
		 * ml/l to given unit
		 * @param to Unit to convert to
		 * @param fromValue ml/l value
		 * @return converted value
		 */
		public double to(Unit to, double fromValue)
		{
			return fromValue;
		}
	};

	@Getter private String unit;
	@Getter private String label;

	public abstract double to(Unit to, double fromValue);
	public abstract double from(Unit from, double fromValue);

	public static Unit getSelectedUnit(Context context)
	{
		return MLPL;
	}
}
