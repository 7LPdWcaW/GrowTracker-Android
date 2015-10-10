package me.anon.lib.helper;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class TimeHelper
{
	public static double toDays(long millis)
	{
		return millis / (1000d * 60d * 60d * 24d);
	}
}
