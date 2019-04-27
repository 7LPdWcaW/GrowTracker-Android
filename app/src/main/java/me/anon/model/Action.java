package me.anon.model;

import me.anon.grow.R;

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
		FIM(R.string.action_fim, 0x9AFFCC80),
		FLUSH(R.string.action_flush, 0x9AFFE082),
		FOLIAR_FEED(R.string.action_foliar_feed, 0x9AE6EE9C),
		LST(R.string.action_lst, 0x9AFFF59D),
		LOLLIPOP(R.string.action_lolipop, 0x9AFFD180),
		PESTICIDE_APPLICATION(R.string.action_pesticide_application, 0x9AEF9A9A),
		TOP(R.string.action_topped, 0x9ABCAAA4),
		TRANSPLANTED(R.string.action_transplanted, 0x9AFFFF8D),
		TRIM(R.string.action_trim, 0x9AFFAB91);

		private int printString;
		private int colour;

		private ActionName(int name, int colour)
		{
			this.printString = name;
			this.colour = colour;
		}

		public static int[] names()
		{
			int[] names = new int[values().length];
			for (int index = 0; index < names.length; index++)
			{
				names[index] = values()[index].getPrintString();
			}

			return names;
		}

		public int getPrintString()
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
