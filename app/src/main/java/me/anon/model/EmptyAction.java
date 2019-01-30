package me.anon.model;

import android.support.annotation.Nullable;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class EmptyAction extends Action
{
	@Nullable private ActionName action;

	public EmptyAction()
	{
	}

	public EmptyAction(Action.ActionName action)
	{
		this.setDate(System.currentTimeMillis());
		this.setAction(action);
	}

	public void setAction(@Nullable ActionName action)
	{
		this.action = action;
	}

	@Nullable public ActionName getAction()
	{
		return action;
	}
}
