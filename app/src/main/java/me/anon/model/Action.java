package me.anon.model;

import lombok.Data;
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
