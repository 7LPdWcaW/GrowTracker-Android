package me.anon.controller.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import java.io.File;

import me.anon.lib.manager.FileManager;

import static me.anon.lib.manager.PlantManager.FILES_DIR;

/**
 * // TODO: Add class description
 */
public class BackupService extends BroadcastReceiver
{
	@Override public void onReceive(Context context, Intent intent)
	{
		new File(Environment.getExternalStorageDirectory(), "/backups/GrowTracker/").mkdirs();
		FileManager.getInstance().copyFile(FILES_DIR + "/plants.json", Environment.getExternalStorageDirectory().getAbsolutePath() + "/backups/GrowTracker/" + System.currentTimeMillis() + ".bak");
	}
}
