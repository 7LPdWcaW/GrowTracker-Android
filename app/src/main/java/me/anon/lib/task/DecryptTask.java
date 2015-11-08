package me.anon.lib.task;

import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import me.anon.grow.MainApplication;
import me.anon.lib.stream.DecryptInputStream;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class DecryptTask extends AsyncTask<ArrayList<String>, Void, Void>
{
	@Override protected Void doInBackground(ArrayList<String>... params)
	{
		for (String filePath : params[0])
		{
			try
			{
				new File(filePath).renameTo(new File(filePath + ".temp"));

				DecryptInputStream dis = new DecryptInputStream(MainApplication.getKey(), new File(filePath + ".temp"));
				FileOutputStream fos = new FileOutputStream(new File(filePath));

				byte[] buffer = new byte[8192];
				int len = 0;

				while ((len = dis.read(buffer)) != -1)
				{
					fos.write(buffer, 0, len);
				}

				new File(filePath + ".temp").delete();

				fos.close();
				fos.flush();
				dis.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		return null;
	}
}
