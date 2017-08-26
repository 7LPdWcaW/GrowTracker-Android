package me.anon.lib.helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * // TODO: Add class description
 *
 * @author 7LPdWcaW
 * @documentation // TODO Reference flow doc
 * @project GrowTracker
 */
public class EncryptionHelper
{
	public static SecretKey generateKey(String key)
	{
		try
		{
			key = key == null ? "" : key;
			int keyLength = 128;
			byte[] keyBytes = new byte[keyLength / 8];
			Arrays.fill(keyBytes, (byte)0x0);

			byte[] passwordBytes = key.getBytes("UTF-8");
			int length = passwordBytes.length < keyBytes.length ? passwordBytes.length : keyBytes.length;
			System.arraycopy(passwordBytes, 0, keyBytes, 0, length);

			return new SecretKeySpec(keyBytes, "AES");
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public static byte[] encrypt(String key, String input)
	{
		try
		{
			SecretKey secretKey = generateKey(key);
			input = input == null ? "" : input;

			if (secretKey != null)
			{
				ByteArrayOutputStream fos = new ByteArrayOutputStream(8192);

				Cipher cipher = Cipher.getInstance("AES");
				cipher.init(Cipher.ENCRYPT_MODE, secretKey);

				CipherOutputStream cos = new CipherOutputStream(fos, cipher);
				byte[] bytes = input.getBytes("UTF-8");

				for (byte b : bytes)
				{
					cos.write(b);
				}

				cos.flush();
				cos.close();

				return fos.toByteArray();
			}
		}
		catch (NoSuchAlgorithmException | IOException | InvalidKeyException | NoSuchPaddingException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public static String decrypt(String key, byte[] input)
	{
		try
		{
			SecretKey secretKey = generateKey(key);

			if (secretKey != null)
			{
				ByteArrayInputStream fis = new ByteArrayInputStream(input);
				StringBuilder output = new StringBuilder();

				Cipher cipher = Cipher.getInstance("AES");
				cipher.init(Cipher.DECRYPT_MODE, secretKey);
				CipherInputStream cis = new CipherInputStream(fis, cipher);

				int len;
				byte[] b = new byte[8192];
				while ((len = cis.read(b)) != -1)
				{
					output.append(new String(b, 0, len, "UTF-8"));
				}

				cis.close();

				return output.toString();
			}
		}
		catch (NoSuchAlgorithmException | NoSuchPaddingException | IOException | InvalidKeyException e)
		{
			e.printStackTrace();
		}

		return null;
	}
}
