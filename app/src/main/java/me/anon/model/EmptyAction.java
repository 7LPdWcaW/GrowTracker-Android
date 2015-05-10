package me.anon.model;

import lombok.Data;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
@Data
public class EmptyAction extends Action
{
	private ActionName action;

	public EmptyAction(Action.ActionName action)
	{
		this.setDate(System.currentTimeMillis());
		this.setAction(action);
	}
}
