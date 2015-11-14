package me.anon.lib.stream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import me.anon.lib.helper.EncryptionHelper;

public class DecryptInputStream extends InputStream
{
	private CipherInputStream cis;
	private FileInputStream fis;

	public DecryptInputStream(String key, File file) throws FileNotFoundException
	{
		try
		{
			SecretKey secretKey = EncryptionHelper.generateKey(key);

			if (secretKey != null)
			{
				fis = new FileInputStream(file);

				Cipher cipher = Cipher.getInstance("AES");
				cipher.init(Cipher.DECRYPT_MODE, secretKey);

				cis = new CipherInputStream(fis, cipher);
			}
		}
		catch (NoSuchAlgorithmException | NoSuchPaddingException | IOException | InvalidKeyException e)
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
		cis.close();
		fis.close();
	}
}
