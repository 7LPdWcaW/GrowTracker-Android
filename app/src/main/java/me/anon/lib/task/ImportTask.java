package me.anon.lib.task;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.crypto.Cipher;

import androidx.core.app.NotificationCompat;
import kotlin.Pair;
import me.anon.grow.MainApplication;
import me.anon.grow.R;
import me.anon.lib.helper.NotificationHelper;
import me.anon.lib.manager.FileManager;
import me.anon.lib.manager.PlantManager;
import me.anon.lib.stream.EncryptOutputStream;
import me.anon.model.Plant;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class ImportTask extends AsyncTask<Pair<String, ArrayList<Uri>>, Integer, Void>
{
	protected NotificationCompat.Builder notification;
	protected NotificationManager notificationManager;
	private Cipher cipher = EncryptOutputStream.createCipher(MainApplication.getKey());
	private Context appContext;
	private AsyncCallback callback;

	public ImportTask(Context appContext, AsyncCallback callback)
	{
		this.callback = callback;
		this.appContext = appContext.getApplicationContext();
	}

	@Override protected void onPreExecute()
	{
		super.onPreExecute();
		NotificationHelper.createExportChannel(appContext);

		notificationManager = (NotificationManager)appContext.getSystemService(Context.NOTIFICATION_SERVICE);

		notification = new NotificationCompat.Builder(appContext, "export")
			.setContentText(appContext.getString(R.string.data_task))
			.setContentTitle(appContext.getString(R.string.import_progress_warning))
			.setContentIntent(PendingIntent.getActivity(appContext, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT))
			.setTicker(appContext.getString(R.string.import_progress_warning))
			.setSmallIcon(R.drawable.ic_stat_name)
			.setPriority(NotificationCompat.PRIORITY_LOW)
			.setAutoCancel(false)
			.setOngoing(true)
			.setSound(null);

		notificationManager.notify(1, notification.build());
	}

	@Override protected Void doInBackground(Pair<String, ArrayList<Uri>>... params)
	{
		MainApplication.dataTaskRunning.set(true);

		int count = 0;
		int total = params[0].getSecond().size();
		File to = new File(FileManager.IMAGE_PATH + params[0].getFirst() + "/");
		ArrayList<String> imagesToAdd = new ArrayList<>();
		for (Uri filePath : params[0].getSecond())
		{
			File toPath = new File(to, System.currentTimeMillis() + ".jpg");
			copyImage(appContext, filePath, toPath);
			imagesToAdd.add(toPath.getPath());
			publishProgress(++count, total);
		}

		Plant plant = PlantManager.getInstance().getPlant(params[0].getFirst());
		if (plant != null)
		{
			plant.getImages().addAll(imagesToAdd);
			PlantManager.getInstance().save();
		}

		MainApplication.dataTaskRunning.set(false);
		return null;
	}

	public void copyImage(Context context, Uri imageUri, File newLocation)
	{
		OutputStream eos = null;

		try
		{
			if (MainApplication.isEncrypted())
			{
				eos = new EncryptOutputStream(cipher, newLocation);
			}
			else
			{
				eos = new FileOutputStream(newLocation);
			}

			if (imageUri.getScheme().startsWith("content"))
			{
				if (!newLocation.exists())
				{
					newLocation.createNewFile();
				}

				ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(imageUri, "r");
				FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
				InputStream streamIn = new BufferedInputStream(new FileInputStream(fileDescriptor), 524288);

				OutputStream streamOut = new BufferedOutputStream(eos, 524288);

				int len;
				byte[] buffer = new byte[524288];
				while ((len = streamIn.read(buffer)) != -1)
				{
					streamOut.write(buffer, 0, len);
				}

				streamIn.close();
				streamOut.flush();
				streamOut.close();
			}
			else if (imageUri.getScheme().startsWith("file"))
			{
				if (!newLocation.exists())
				{
					newLocation.createNewFile();
				}

				String image = imageUri.getPath();

				InputStream streamIn = new BufferedInputStream(new FileInputStream(new File(image)), 524288);
				OutputStream streamOut = new BufferedOutputStream(eos, 524288);

				int len;
				byte[] buffer = new byte[524288];
				while ((len = streamIn.read(buffer)) != -1)
				{
					streamOut.write(buffer, 0, len);
				}

				streamIn.close();
				streamOut.flush();
				streamOut.close();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override protected void onPostExecute(Void aVoid)
	{
		appContext = null;

		if (callback != null)
		{
			callback.callback();
		}
	}

	@Override protected void onProgressUpdate(Integer... values)
	{
		if (values[1].equals(values[0]))
		{
			notification = new NotificationCompat.Builder(appContext, "export")
				.setContentText(appContext.getString(R.string.data_task))
				.setContentTitle(appContext.getString(R.string.task_complete))
				.setContentIntent(PendingIntent.getActivity(appContext, 0, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT))
				.setTicker(appContext.getString(R.string.task_complete))
				.setSmallIcon(R.drawable.ic_floting_done)
				.setPriority(NotificationCompat.PRIORITY_LOW)
				.setAutoCancel(false)
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
