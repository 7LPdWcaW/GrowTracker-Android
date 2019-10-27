package me.anon.lib;

import android.content.Context;
import androidx.annotation.WorkerThread;

import java.io.File;

public class ExportCallback
{
	@WorkerThread
	public void onCallback(Context context, File file){}
}
