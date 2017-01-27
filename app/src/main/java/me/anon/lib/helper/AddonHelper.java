package me.anon.lib.helper;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains all methods and action types for available addons for Grow Tracker
 */
public class AddonHelper
{
	/**
	 * Array of broadcast actions
	 */
	public static final String[] ADDON_BROADCAST = {
		"me.anon.grow.ACTION_SAVE_PLANTS"
	};

	public static final Map<String, String> ADDON_DESCRIPTIONS = new HashMap<>();

	static
	{
		ADDON_DESCRIPTIONS.put(ADDON_BROADCAST[0], "Listens for save events, i.e. when a plant is saved, or a photo taken/deleted.<br />Not triggered for garden edits.");
	}

	/**
	 * Array of startable activities
	 */
	public static final String[] ADDON_ACTIVITIES = {
		"me.anon.grow.ADDON_CONFIGURATION"
	};
}
