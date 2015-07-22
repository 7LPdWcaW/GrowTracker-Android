package me.anon.lib.helper;

import android.view.View;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class FabAnimator
{
	public static void animateUp(View fab)
	{
		if (fab == null) return;

		fab.animate().yBy(-(fab.getHeight() * 0.85f)).setDuration(200).start();
	}

	public static void animateDown(View fab)
	{
		if (fab == null) return;

		fab.animate().yBy((fab.getHeight() * 0.85f)).setDuration(200).start();
	}
}
