package me.anon.model;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public abstract class Action
{
	private long date = System.currentTimeMillis();
	private String notes;

	public long getDate()
	{
		return date;
	}

	public void setDate(long date)
	{
		this.date = date;
	}

	public String getNotes()
	{
		return notes;
	}

	public void setNotes(String notes)
	{
		this.notes = notes;
	}

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

		private String printString;
		private int colour;

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

		public String getPrintString()
		{
			return printString;
		}

		public int getColour()
		{
			return colour;
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
