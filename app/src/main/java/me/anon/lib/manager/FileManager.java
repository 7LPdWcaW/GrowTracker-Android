package me.anon.lib.manager;

import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.security.DigestInputStream;
import java.security.MessageDigest;

public class FileManager
{
	public static String IMAGE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/GrowTracker/";
	private static FileManager instance;

	/**
	 * Gets the file manager singleton or creates one if its null
	 *
	 * @return The file manager singleton
	 */
	public static FileManager getInstance()
	{
		if (instance == null)
		{
			synchronized (FileManager.class)
			{
				if (instance == null)
				{
					instance = new FileManager();
				}
			}
		}

		return instance;
	}

	/**
	 * Default private constructor
	 */
	private FileManager(){}

	/**
	 * Get the age of the file in millis
	 *
	 * @param filePath Absolute path to the file
	 *
	 * @return Age of file in millis (Compared against current system time)
	 */
	public long getFileAge(String filePath)
	{
		if (!TextUtils.isEmpty(filePath))
		{
			return System.currentTimeMillis() - new File(filePath).lastModified();
		}

		return 0;
	}

	/**
	 * Check if the file exists from an absolute file path
	 *
	 * @param filePath The absolute path to the file on the local filesystem
	 *
	 * @return {@code true} if file exists
	 */
	public boolean fileExists(String filePath)
	{
		return !TextUtils.isEmpty(filePath) && new File(filePath).exists();
	}

	/**
	 * Read the file and return a string of the contents
	 *
	 * @param filePath The absolute path to the file on the local filesystem
	 *
	 * @return String of file contents
	 */
	public String readFileAsString(String filePath)
	{
		return readFileAsString(new File(filePath));
	}

	/**
	 * Read from file and return a string representation of the contents
	 *
	 * @param file A file to read from
	 *
	 * @return String of file contents
	 */
	public String readFileAsString(File file)
	{
		return new String(readFile(file));
	}

	/**
	 * Read from file and return a string representation of the contents
	 *
	 * @param stream The stream to read from
	 *
	 * @return String of file contents
	 */
	public String readFileAsString(InputStream stream)
	{
		return new String(readFile(stream));
	}

	/**
	 * Read from file and return as a JSON element
	 *
	 * @param filePath The absolute path to the file on the local filesystem
	 *
	 * @return JSON representation of file contents
	 */
	public JsonElement readFileAsJson(String filePath)
	{
		return readFileAsJson(new File(filePath));
	}

	/**
	 * Read from file and return as a JSON element
	 *
	 * @param file The file on the local filesystem
	 *
	 * @return JSON representation of file contents
	 */
	public JsonElement readFileAsJson(File file)
	{
		return new JsonParser().parse(readFileAsString(file));
	}

	/**
	 * Read from file and return as a JSON element
	 *
	 * @param stream The stream to read from
	 *
	 * @return JSON representation of file contents
	 */
	public JsonElement readFileAsJson(InputStream stream)
	{
		return new JsonParser().parse(readFileAsString(stream));
	}

	/**
	 * Read the file and return the byte array of its contents
	 *
	 * @param filePath The absolute path to the file on the local filesystem
	 *
	 * @return Byte array of file contents
	 */
	public byte[] readFile(String filePath)
	{
		return readFile(new File(filePath));
	}

	/**
	 * Read the file and return the byte array of its contents
	 *
	 * @param file The absolute path to the file on the local filesystem
	 *
	 * @return Byte array of file contents
	 */
	public byte[] readFile(File file)
	{
		try
		{
			return readFile(new FileInputStream(file));
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Read the input stream and return the byte array of its contents
	 *
	 * @param input The input stream of the file to read
	 *
	 * @return Byte array of file contents
	 */
	public byte[] readFile(InputStream input)
	{
		ByteArrayOutputStream bos = null;

		try
		{
			int bufferSize = 8192;
			bos = new ByteArrayOutputStream(bufferSize);
			input = new BufferedInputStream(input, bufferSize);

			byte[] buffer = new byte[1024];

			int len = 0;
			while ((len = input.read(buffer)) > 0)
			{
				bos.write(buffer, 0, len);
			}

			return bos.toByteArray();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				input.close();

				if (bos != null)
				{
					bos.close();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * Copies a file from src to dest
	 * @param src The source input stream
	 * @param dest The dest output stream
	 */
	public void copyFile(String src, String dest)
	{
		try
		{
			copyFile(new FileInputStream(src), new FileOutputStream(dest));
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Copies a file from src to dest
	 * @param src The source input stream
	 * @param dest The dest output stream
	 */
	public void copyFile(InputStream src, OutputStream dest)
	{
		try
		{
			byte[] buffer = new byte[1024];
			int read;

			while ((read = src.read(buffer)) != -1)
			{
				dest.write(buffer, 0, read);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Writes a file to disk
	 * <p/>
	 * This will replace a file if it already exists
	 *
	 * @param fileName The name of the file to write.
	 * @param contents The byte data to write to the file
	 */
	public void writeFile(String fileName, byte[] contents)
	{
		writeFile("", fileName, contents);
	}

	/**
	 * Writes a file to disk
	 * <p/>
	 * This will replace a file if it already exists
	 *
	 * @param fileName The name of the file to write.
	 * @param contents The serializable data to write to the file
	 */
	public void writeFile(String fileName, Serializable contents)
	{
		try
		{
			File file = new File(fileName);

			if (contents instanceof String)
			{
				OutputStreamWriter fos = new OutputStreamWriter(new FileOutputStream(file));
				fos.write((String)contents);
				fos.close();
			}
			else
			{
				ObjectOutputStream fos = new ObjectOutputStream(new FileOutputStream(file));
				fos.writeObject(contents);
				fos.flush();
				fos.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Writes a file to disk
	 * <p/>
	 * This will replace a file if it already exists
	 *
	 * @param folderPath The folder of the file
	 * @param fileName The name of the file to write.
	 * @param contents The serializable data to write to the file
	 */
	public void writeFile(String folderPath, String fileName, Serializable contents)
	{
		try
		{
			File file = new File(folderPath, fileName);
			ObjectOutputStream fos = new ObjectOutputStream(new FileOutputStream(file));
			fos.writeObject(contents);
			fos.flush();
			fos.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Writes a file to disk
	 * <p/>
	 * This will replace a file if it already exists
	 *
	 * @param folderPath The folder of the file
	 * @param fileName The name of the file to write.
	 * @param contents The byte data to write to the file
	 */
	public void writeFile(String folderPath, String fileName, byte[] contents)
	{
		try
		{
			File file = new File(folderPath, fileName);

			if (file.exists())
			{
				file.delete();
			}

			FileOutputStream fos = new FileOutputStream(file);
			fos.write(contents);
			fos.flush();
			fos.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Deletes a file from the filesystem
	 *
	 * @param file The file to delete
	 *
	 * @return {@code true} if the file was successfully deleted
	 */
	public boolean removeFile(String file)
	{
		return removeFile("", file);
	}

	/**
	 * Deletes a file from the filesystem
	 *
	 * @param folderPath The base folder path
	 * @param fileName The file to delete
	 *
	 * @return {@code true} if the file was successfully deleted
	 */
	public boolean removeFile(String folderPath, String fileName)
	{
		File f = new File(folderPath, fileName);
		return f.delete();
	}

	/**
	 * Gets the file's MD5 hash
	 *
	 * @param filePath The file to calculate the hash of
	 *
	 * @return The calculated file hash, or null
	 */
	@Nullable
	public String getFileHash(String filePath)
	{
		InputStream is = null;
		try
		{
			StringBuilder signature = new StringBuilder();
			MessageDigest md = MessageDigest.getInstance("MD5");
			is = new DigestInputStream(new FileInputStream(filePath), md);

			byte[] contents = readFile(filePath);
			byte[] messageDigest = md.digest(contents);

			for (byte message : messageDigest)
			{
				String hex = Integer.toHexString(0xFF & message);
				if (hex.length() == 1)
				{
					signature.append('0');
				}

				signature.append(hex);
			}

			return String.valueOf(signature);
		}
		catch (Exception ignore){}
		finally
		{
			try
			{
				if (is != null)
				{
					is.close();
				}
			}
			catch (Exception ignore){}
		}

		return null;
	}
}
