package me.anon.lib.task;

import android.os.AsyncTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import me.anon.grow.MainApplication;
import me.anon.lib.stream.EncryptOutputStream;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class EncryptTask extends AsyncTask<ArrayList<String>, Void, Void>
{
	@Override protected Void doInBackground(ArrayList<String>... params)
	{
		for (String filePath : params[0])
		{
			FileInputStream fis = null;
			EncryptOutputStream eos = null;
			try
			{
				new File(filePath).renameTo(new File(filePath + ".temp"));

				fis = new FileInputStream(new File(filePath + ".temp"));
				eos = new EncryptOutputStream(MainApplication.getKey(), new File(filePath));

				byte[] buffer = new byte[8192];
				int len = 0;

				while ((len = fis.read(buffer)) != -1)
				{
					eos.write(buffer, 0, len);
				}

				new File(filePath + ".temp").delete();

				eos.flush();
			}
			catch (IOException e)
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
		}

		return null;
	}
}
