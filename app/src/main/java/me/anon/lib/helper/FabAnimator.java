package me.anon.lib.helper;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;

/**
 * // TODO: Add class description
 *
 * @author 
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class FabAnimator
{
	public static void animateUp(View fab)
	{
		if (fab == null) return;

		fab.clearAnimation();

		Animation animation = new TranslateAnimation(
			Animation.RELATIVE_TO_SELF, 0f,
			Animation.RELATIVE_TO_SELF, 0f,
			Animation.RELATIVE_TO_SELF, 0f,
			Animation.RELATIVE_TO_SELF, -0.85f
		);
		animation.setFillAfter(true);
		animation.setFillEnabled(true);
		animation.setDuration(200);
		animation.setInterpolator(new LinearInterpolator());

		fab.startAnimation(animation);
	}

	public static void animateDown(View fab)
	{
		if (fab == null) return;

		fab.clearAnimation();

		Animation animation = new TranslateAnimation(
			Animation.RELATIVE_TO_SELF, 0f,
			Animation.RELATIVE_TO_SELF, 0f,
			Animation.RELATIVE_TO_SELF, -0.85f,
			Animation.RELATIVE_TO_SELF, 0f
		);
		animation.setFillAfter(true);
		animation.setFillEnabled(true);
		animation.setDuration(200);
		animation.setInterpolator(new LinearInterpolator());

		fab.startAnimation(animation);
	}
}
