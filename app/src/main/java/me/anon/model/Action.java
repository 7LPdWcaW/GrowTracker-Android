package me.anon.model;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * // TODO: Add class description
 *
 * @author 
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Data
@Accessors(prefix = {"m", ""}, chain = true)
public abstract class Action
{
	private long date = System.currentTimeMillis();

	public enum ActionName
	{
		FEED("Feed"),
		WATER("Water"),
		TRIM("Trim"),
		TOP("Topped"),
		FIM("Fuck I Missed"),
		LST("Low Stress Training"),
		LOLLIPOP("Lollipop"),
		FLUSH("Flush"),
		FLIPPED("Flipped to flower");

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
