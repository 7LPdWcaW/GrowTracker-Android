package me.anon.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Accessors(prefix = {"m", ""}, chain = true)
public abstract class Action
{
	@Getter @Setter private long date = System.currentTimeMillis();
	@Getter @Setter private String notes;

	public enum ActionName
	{
		FIM("Fuck I Missed", 0x9AFFCC80),
		FLUSH("Flush", 0x9AFFE082),
		FOLIAR_FEED("Foliar Feed", 0x9AE6EE9C),
		LST("Low Stress Training", 0x9AFFF59D),
		LOLLIPOP("Lollipop", 0x9AFFD180),
		PESTICIDE_APPLICATION("Pesticide Application", 0x9AEF9A9A),
		TOP("Topped", 0x9ABCAAA4),
		TRANSPLANTED("Transplanted", 0x9AFFFF8D),
		TRIM("Trim", 0x9AFFAB91);

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

	@Override public boolean equals(Object o)
	{
		if (o == this) return true;
		if (!(o instanceof Action)) return false;
		Action other = (Action)o;
		if (!super.equals(o)) return false;
		if (this.date != other.getDate()) return false;

		return true;
	}
}
