package me.anon.lib.task;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.crypto.Cipher;

import androidx.core.app.NotificationCompat;
import me.anon.grow.MainApplication;
import me.anon.grow.R;
import me.anon.lib.helper.NotificationHelper;
import me.anon.lib.stream.DecryptInputStream;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class DecryptTask extends AsyncTask<ArrayList<String>, Integer, Void>
{
	protected NotificationCompat.Builder notification;
	protected NotificationManager notificationManager;
	private Cipher cipher = DecryptInputStream.createCipher(MainApplication.getKey());
	private Context appContext;

	public DecryptTask(Context appContext)
	{
		this.appContext = appContext.getApplicationContext();
	}

	@Override protected void onPreExecute()
	{
		super.onPreExecute();
		NotificationHelper.createExportChannel(appContext);

		notificationManager = (NotificationManager)appContext.getSystemService(Context.NOTIFICATION_SERVICE);

		notification = new NotificationCompat.Builder(appContext, "export")
			.setContentText(appContext.getString(R.string.data_task))
			.setContentTitle(appContext.getString(R.string.decrypt_progress_warning))
			.setContentIntent(PendingIntent.getActivity(appContext, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT))
			.setTicker(appContext.getString(R.string.decrypt_progress_warning))
			.setSmallIcon(R.drawable.ic_stat_name)
			.setPriority(NotificationCompat.PRIORITY_LOW)
			.setAutoCancel(false)
			.setOngoing(true)
			.setSound(null);

		notificationManager.notify(1, notification.build());
	}

	@Override protected Void doInBackground(ArrayList<String>... params)
	{
		MainApplication.dataTaskRunning.set(true);

		int count = 0;
		int total = params[0].size();
		for (String filePath : params[0])
		{
			File file = new File(filePath);
			File temp = new File(filePath + ".temp");
			if (!file.exists()) continue;

			FileOutputStream fos = null;
			DecryptInputStream dis = null;
			try
			{
				file.renameTo(temp);

				dis = new DecryptInputStream(cipher, temp);
				fos = new FileOutputStream(file);

				byte[] buffer = new byte[8192];
				int len;

				while ((len = dis.read(buffer)) != -1)
				{
					fos.write(buffer, 0, len);
				}

				fos.flush();
				temp.delete();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			finally
			{
				if (fos != null)
				{
					try
					{
						fos.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}

				if (dis != null)
				{
					try
					{
						dis.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}

			publishProgress(++count, total);
		}

		MainApplication.dataTaskRunning.set(false);
		return null;
	}

	@Override protected void onPostExecute(Void aVoid)
	{
		appContext = null;
	}

	@Override protected void onProgressUpdate(Integer... values)
	{
		if (values[1].equals(values[0]))
		{
			notification = new NotificationCompat.Builder(appContext, "export")
				.setContentText(appContext.getString(R.string.decrypt_task_complete))
				.setContentTitle(appContext.getString(R.string.data_task))
				.setContentIntent(PendingIntent.getActivity(appContext, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT))
				.setTicker(appContext.getString(R.string.decrypt_task_complete))
				.setSmallIcon(R.drawable.ic_floting_done)
				.setPriority(NotificationCompat.PRIORITY_DEFAULT)
				.setAutoCancel(true)
				.setOngoing(false)
				.setSound(null)
				.setProgress(0, 0, false);
			notificationManager.notify(1, notification.build());
		}
		else
		{
			notification.setTicker(values[0] + "/" + values[1]);
			notification.setProgress(values[1], values[0], false);
			notificationManager.notify(1, notification.build());
		}
	}
}
