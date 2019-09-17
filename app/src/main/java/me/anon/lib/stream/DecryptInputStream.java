package me.anon.lib.stream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;

import me.anon.lib.helper.EncryptionHelper;

public class DecryptInputStream extends InputStream
{
	private CipherInputStream cis;
	private FileInputStream fis;

	public static Cipher createCipher(String key)
	{
		SecretKey secretKey = EncryptionHelper.generateKey(key);

		try
		{
			if (secretKey != null)
			{
				Cipher cipher = Cipher.getInstance("AES");
				cipher.init(Cipher.DECRYPT_MODE, secretKey);
				return cipher;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public DecryptInputStream(String key, File file) throws FileNotFoundException
	{
		this(createCipher(key), file);
	}

	public DecryptInputStream(Cipher cipher, File file) throws FileNotFoundException
	{
		try
		{
			fis = new FileInputStream(file);
			cis = new CipherInputStream(fis, cipher);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override public int available() throws IOException
	{
		return cis.available();
	}

	@Override public void mark(int readlimit)
	{
		cis.mark(readlimit);
	}

	@Override public boolean markSupported()
	{
		return cis.markSupported();
	}

	@Override public int read(byte[] buffer) throws IOException
	{
		return cis.read(buffer);
	}

	@Override public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException
	{
		return cis.read(buffer, byteOffset, byteCount);
	}

	@Override public synchronized void reset() throws IOException
	{
		cis.reset();
	}

	@Override public long skip(long byteCount) throws IOException
	{
		return cis.skip(byteCount);
	}

	@Override public int read() throws IOException
	{
		return cis.read();
	}

	@Override public void close() throws IOException
	{
		try
		{
			cis.close();
			fis.close();
		}
		catch (NullPointerException e)
		{
			e.printStackTrace();
		}
	}
}
