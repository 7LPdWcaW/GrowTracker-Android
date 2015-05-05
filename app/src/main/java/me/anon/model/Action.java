package me.anon.model;

/**
 * // TODO: Add class description
 *
 * @author 
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public abstract class Action
{
	private long date;

	public enum ActionName
	{
		FEED,
		WATER,
		TRIM,
		TOP,
		FIM,
		LST;
	}
}
