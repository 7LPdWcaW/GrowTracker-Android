package me.anon.grow;

import android.app.Application;

import me.anon.lib.manager.PlantManager;

/**
 * // TODO: Add class description
 *
 * @author 
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class MainApplication extends Application
{
	@Override public void onCreate()
	{
		super.onCreate();

		PlantManager.getInstance().initialise(this);
	}
}
