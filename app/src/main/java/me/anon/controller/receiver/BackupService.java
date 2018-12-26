package me.anon.controller.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import me.anon.lib.helper.BackupHelper;

/**
 * // TODO: Add class description
 */
public class BackupService extends BroadcastReceiver
{
	@Override public void onReceive(Context context, Intent intent)
	{
		BackupHelper.backupJson();
	}
}
