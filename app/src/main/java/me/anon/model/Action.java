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
		FEED("Feed"),
		WATER("Water"),
		FIM("Fuck I Missed"),
		FLIPPED("Flipped to flower"),
		FLUSH("Flush"),
		LST("Low Stress Training"),
		LOLLIPOP("Lollipop"),
		TOP("Topped"),
		TRANSPLANTED("Transplanted"),
		TRIM("Trim");

		@Getter private String printString;

		private ActionName(String name)
		{
			this.printString = name;
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
