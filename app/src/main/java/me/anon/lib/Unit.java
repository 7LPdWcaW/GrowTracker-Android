package me.anon.lib;

import android.content.Context;
import android.preference.PreferenceManager;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Unit class used for measurement input
 */
public enum Unit
{
	ML("ml")
	{
		@Override public double to(Unit to, double fromValue)
		{
			switch (to)
			{
				case L: return toTwoDecimalPlaces(fromValue * 0.001d);
				case GAL: return toTwoDecimalPlaces(fromValue * 0.000219969d);
				case QUART: return toTwoDecimalPlaces(fromValue * 0.000879877d);
				case TSP: return toTwoDecimalPlaces(fromValue * 0.168936d);
				case USGAL: return toTwoDecimalPlaces(fromValue * 0.000264172d);
				case USQUART: return toTwoDecimalPlaces(fromValue * 0.00105669d);
				case USTSP: return toTwoDecimalPlaces(fromValue * 0.202884d);
			}

			return toTwoDecimalPlaces(fromValue);
		}
	},

	L("l")
	{
		@Override public double to(Unit to, double fromValue)
		{
			switch (to)
			{
				case ML: return toTwoDecimalPlaces(fromValue * 1000d);
				case GAL: return toTwoDecimalPlaces(fromValue * 0.219969d);
				case QUART: return toTwoDecimalPlaces(fromValue * 0.879877d);
				case TSP: return toTwoDecimalPlaces(fromValue * 168.936d);
				case USGAL: return toTwoDecimalPlaces(fromValue * 0.264172d);
				case USQUART: return toTwoDecimalPlaces(fromValue * 1.05669d);
				case USTSP: return toTwoDecimalPlaces(fromValue * 202.884d);
			}

			return toTwoDecimalPlaces(fromValue);
		}
	},

	GAL("gal")
	{
		@Override public double to(Unit to, double fromValue)
		{
			switch (to)
			{
				case ML: return toTwoDecimalPlaces(fromValue * 4546.09d);
				case L: return toTwoDecimalPlaces(fromValue * 4.54609d);
				case QUART: return toTwoDecimalPlaces(fromValue * 4d);
				case TSP: return toTwoDecimalPlaces(fromValue * 768d);
				case USGAL: return toTwoDecimalPlaces(fromValue * 1.20095d);
				case USQUART: return toTwoDecimalPlaces(fromValue * 4.8038d);
				case USTSP: return toTwoDecimalPlaces(fromValue * 922.33d);
			}

			return toTwoDecimalPlaces(fromValue);
		}
	},

	QUART("quart")
	{
		@Override public double to(Unit to, double fromValue)
		{
			switch (to)
			{
				case ML: return toTwoDecimalPlaces(fromValue * 1136.52d);
				case L: return toTwoDecimalPlaces(fromValue * 1.13652d);
				case GAL: return toTwoDecimalPlaces(fromValue * 0.25d);
				case TSP: return toTwoDecimalPlaces(fromValue * 192d);
				case USGAL: return toTwoDecimalPlaces(fromValue * 0.300237d);
				case USQUART: return toTwoDecimalPlaces(fromValue * 1.20095d);
				case USTSP: return toTwoDecimalPlaces(fromValue * 230.582d);
			}

			return toTwoDecimalPlaces(fromValue);
		}
	},

	TSP("tsp")
	{
		@Override public double to(Unit to, double fromValue)
		{
			switch (to)
			{
				case ML: return toTwoDecimalPlaces(fromValue * 5.91939d);
				case L: return toTwoDecimalPlaces(fromValue * 0.00591939d);
				case GAL: return toTwoDecimalPlaces(fromValue * 0.00130208d);
				case QUART: return toTwoDecimalPlaces(fromValue * 0.00520834d);
				case USGAL: return toTwoDecimalPlaces(fromValue * 0.00156374d);
				case USQUART: return toTwoDecimalPlaces(fromValue * 0.00625495d);
				case USTSP: return toTwoDecimalPlaces(fromValue * 1.20095d);
			}

			return toTwoDecimalPlaces(fromValue);
		}
	},

	USGAL("us gal")
	{
		@Override public double to(Unit to, double fromValue)
		{
			switch (to)
			{
				case ML: return toTwoDecimalPlaces(fromValue * 3785.41d);
				case L: return toTwoDecimalPlaces(fromValue * 3.78541d);
				case GAL: return toTwoDecimalPlaces(fromValue * 0.832674d);
				case QUART: return toTwoDecimalPlaces(fromValue * 4d);
				case TSP: return toTwoDecimalPlaces(fromValue * 639.494d);
				case USQUART: return toTwoDecimalPlaces(fromValue * 0.00130208d);
				case USTSP: return toTwoDecimalPlaces(fromValue * 0.00130208d);
			}

			return toTwoDecimalPlaces(fromValue);
		}
	},

	USQUART("us quart")
	{
		@Override public double to(Unit to, double fromValue)
		{
			switch (to)
			{
				case ML: return toTwoDecimalPlaces(fromValue * 946.353d);
				case L: return toTwoDecimalPlaces(fromValue * 0.946353d);
				case GAL: return toTwoDecimalPlaces(fromValue * 0.208169d);
				case QUART: return toTwoDecimalPlaces(fromValue * 0.832674d);
				case TSP: return toTwoDecimalPlaces(fromValue * 159.873d);
				case USGAL: return toTwoDecimalPlaces(fromValue * 0.25d);
				case USTSP: return toTwoDecimalPlaces(fromValue * 192d);
			}

			return toTwoDecimalPlaces(fromValue);
		}
	},

	USTSP("us tsp")
	{
		@Override public double to(Unit to, double fromValue)
		{
			switch (to)
			{
				case ML: return toTwoDecimalPlaces(fromValue * 4.92892d);
				case L: return toTwoDecimalPlaces(fromValue * 0.00492892d);
				case GAL: return toTwoDecimalPlaces(fromValue * 0.00108421d);
				case QUART: return toTwoDecimalPlaces(fromValue * 0.00433684d);
				case TSP: return toTwoDecimalPlaces(fromValue * 0.832674d);
				case USGAL: return toTwoDecimalPlaces(fromValue * 0.00130208d);
				case USQUART: return toTwoDecimalPlaces(fromValue * 0.00520833d);
			}

			return toTwoDecimalPlaces(fromValue);
		}
	};

	private String label;

	private Unit(String label)
	{
		this.label = label;
	}

	public String getLabel()
	{
		return label;
	}

	public static Double toTwoDecimalPlaces(double input)
	{
		return new BigDecimal(input).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
	}

	/**
	 * x to given unit
	 * @param to Unit to convert to
	 * @param fromValue ml value
	 * @return converted value
	 */
	public abstract double to(Unit to, double fromValue);

	public static Unit getSelectedDeliveryUnit(Context context)
	{
		int index;
		return values()[(index = PreferenceManager.getDefaultSharedPreferences(context).getInt("delivery_unit", -1)) == -1 ? L.ordinal() : index];
	}

	public static Unit getSelectedMeasurementUnit(Context context)
	{
		int index;
		return values()[(index = PreferenceManager.getDefaultSharedPreferences(context).getInt("measurement_unit", -1)) == -1 ? ML.ordinal() : index];
	}
}
