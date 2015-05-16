package me.anon.model;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Data
@Accessors(prefix = {"m", ""}, chain = true)
public abstract class Action
{
	private long date = System.currentTimeMillis();
	private String notes;

	public enum ActionName
	{
		FEED("Feed", 0xff90CAF9),
		WATER("Water", 0xffBBDEFB),
		FIM("Fuck I Missed", 0xffFFCC80),
		FLIPPED("Flipped to flower", 0xffB39DDB),
		FLUSH("Flush", 0xffFFE082),
		LST("Low Stress Training", 0xffFFF59D),
		LOLLIPOP("Lollipop", 0xffFFD180),
		TOP("Topped", 0xffBCAAA4),
		TRANSPLANTED("Transplanted", 0xffFFFF8D),
		TRIM("Trim", 0xffFFAB91);

		@Getter private String printString;
		@Getter private int colour;

		private ActionName(String name, int colour)
		{
			this.printString = name;
			this.colour = colour;
		}

		public static String[] names()
		{
			String[] names = new String[values().length];
			for (int index = 0; index < names.length; index++)
			{
				names[index] = values()[index].getPrintString();
			}

			return names;
		}
	}
}
