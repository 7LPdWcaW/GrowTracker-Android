package me.anon.lib.task;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.crypto.Cipher;

import androidx.core.app.NotificationCompat;
import me.anon.grow.MainApplication;
import me.anon.grow.R;
import me.anon.lib.helper.NotificationHelper;
import me.anon.lib.stream.EncryptOutputStream;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class EncryptTask extends AsyncTask<ArrayList<String>, Integer, Void>
{
	protected NotificationCompat.Builder notification;
	protected NotificationManager notificationManager;
	private Cipher cipher = EncryptOutputStream.createCipher(MainApplication.getKey());
	private Context appContext;

	public EncryptTask(Context appContext)
	{
		this.appContext = appContext;
	}

	@Override protected void onPreExecute()
	{
		super.onPreExecute();
		NotificationHelper.createExportChannel(appContext);

		notificationManager = (NotificationManager)appContext.getSystemService(Context.NOTIFICATION_SERVICE);

		notification = new NotificationCompat.Builder(appContext, "export")
			.setContentText(appContext.getString(R.string.app_name))
			.setContentTitle("Data task")
			.setContentIntent(PendingIntent.getActivity(appContext, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT))
			.setTicker(appContext.getString(R.string.encrypt_progress_warning))
			.setSmallIcon(R.drawable.ic_stat_name)
			.setPriority(NotificationCompat.PRIORITY_LOW)
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
			if (!new File(filePath).exists()) continue;

			FileInputStream fis = null;
			EncryptOutputStream eos = null;
			try
			{
				new File(filePath).renameTo(new File(filePath + ".temp"));

				fis = new FileInputStream(new File(filePath + ".temp"));
				eos = new EncryptOutputStream(cipher, new File(filePath));

				byte[] buffer = new byte[8192];
				int len = 0;

				while ((len = fis.read(buffer)) != -1)
				{
					eos.write(buffer, 0, len);
				}

				new File(filePath + ".temp").delete();

				eos.flush();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				if (fis != null)
				{
					try
					{
						fis.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}

				if (eos != null)
				{
					try
					{
						eos.close();
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
		notificationManager.cancel(1);
	}

	@Override protected void onProgressUpdate(Integer... values)
	{
		if (values[1] == values[0])
		{
			notificationManager.cancel(1);
		}
		else
		{
			notification.setProgress(values[1], values[0], false);
			notificationManager.notify(1, notification.build());
		}
	}
}
