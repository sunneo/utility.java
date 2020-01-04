package com.example;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import cn.longcloud.aes.Rfc2898DeriveBytes;

public class StringCipher {
	public static class Base32 {

		// the valid chars for the encoding
		private static String ValidChars = "QAZ2WSX3" + "EDC4RFV5" + "TGB6YHN7" + "UJM8K9LP";

		/// <summary>
		/// Converts an array of bytes to a Base32-k String.
		/// </summary>
		public static String ToBase32String(byte[] bytes) {
			StringBuilder sb = new StringBuilder(); // holds the base32 chars
			byte index;
			int hi = 5;
			int currentByte = 0;

			while (currentByte < bytes.length) {
				// do we need to use the next byte?
				if (hi > 8) {
					// get the last piece from the current byte, shift it to the right
					// and increment the byte counter
					index = (byte) (bytes[currentByte++] >> (hi - 5));
					if (currentByte != bytes.length) {
						// if we are not at the end, get the first piece from
						// the next byte, clear it and shift it to the left
						index = (byte) (((byte) (bytes[currentByte] << (16 - hi)) >> 3) | index);
					}

					hi -= 3;
				} else if (hi == 8) {
					index = (byte) (bytes[currentByte++] >> 3);
					hi -= 3;
				} else {

					// simply get the stuff from the current byte
					index = (byte) ((byte) (bytes[currentByte] << (8 - hi)) >> 3);
					hi += 5;
				}

				sb.append(ValidChars.charAt(index));
			}

			return sb.toString();
		}

		/// <summary>
		/// Converts a Base32-k String into an array of bytes.
		/// </summary>
		/// <exception cref="System.ArgumentException">
		/// Input String <paramref name="s">s</paramref> contains invalid Base32-k
		/// characters.
		/// </exception>
		public static byte[] FromBase32String(String str) {
			int strLen = str.length();
			int numBytes = str.length() * 5 / 8;
			byte[] bytes = new byte[numBytes];

			// all UPPERCASE chars
			str = str.toUpperCase();

			int bit_buffer;
			int currentCharIndex;
			int bits_in_buffer;

			if (strLen < 3) {
				bytes[0] = (byte) (ValidChars.indexOf(str.charAt(0)) | ValidChars.indexOf(str.charAt(1)) << 5);
				return bytes;
			}

			bit_buffer = (ValidChars.indexOf(str.charAt(0)) | ValidChars.indexOf(str.charAt(1)) << 5);
			bits_in_buffer = 10;
			currentCharIndex = 2;
			for (int i = 0; i < bytes.length; i++) {
				bytes[i] = (byte) bit_buffer;
				bit_buffer >>= 8;
				bits_in_buffer -= 8;
				while (bits_in_buffer < 8 && currentCharIndex < strLen) {
					bit_buffer |= ValidChars.indexOf(str.charAt(currentCharIndex++)) << bits_in_buffer;
					bits_in_buffer += 5;
				}
			}

			return bytes;
		}
	}

	public static class Cipher {
		public static String Encode(String content, String phaseKey) {
			try {
				return StringCipher.Encrypt(content, phaseKey);
			} catch (Exception ee) {
				ee.printStackTrace();
				return "ERROR";
			}
		}

		public static String Decode(String content, String phaseKey) {
			try {
				return StringCipher.Decrypt(content, phaseKey);
			} catch (Exception ee) {
				ee.printStackTrace();
				return "ERROR";
			}
		}
	}

	public static String PassKey;

	public static String Encrypt(String plainText) {
		return Encrypt(plainText, PassKey);
	}

	public static String Decrypt(String cipher) {
		return Decrypt(cipher, PassKey);
	}

	public static String Encrypt(String strSource, String saltString) {
		byte[] result = null;
		try {
			byte[] encryptBytes = strSource.getBytes("UTF-8");
			byte[] salt = saltString.getBytes();
			// ����漣������ (AES) 撖寧妍蝞���恣摰��
			// 1.�������嚗��蛹AES蝞��,銝��之撠��
			KeyGenerator aes = KeyGenerator.getInstance("AES");
			// ��蝙��鈭� System.Security.Cryptography.HMACSHA1
			// ��憚������嚗��鈭����瘣曄��
			// (PBKDF2)��
			Rfc2898DeriveBytes rfc = new Rfc2898DeriveBytes(saltString, salt);
			// ����挽蝵桃鈭笆蝘啁������
			aes.init(128, new SecureRandom(rfc.getBytes(128 / 8)));
			// ����挽蝵桃鈭笆蝘啁��������� (IV)��
			SecretKey key = aes.generateKey();
			javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, key, new IvParameterSpec((rfc.getBytes(128 / 8))));
			result = cipher.doFinal(encryptBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 撠����敺���蓮�銝箏�泵銝�
		return Base32.ToBase32String(result);
	}

	public static String Decrypt(String strSource, String pWDString) {
		byte[] result = null;
		try {
			byte[] encryptBytes = Base32.FromBase32String(strSource);
			byte[] salt = pWDString.getBytes();
			// ����漣������ (AES) 撖寧妍蝞���恣摰��
			KeyGenerator aes = KeyGenerator.getInstance("AES");
			// ��蝙��鈭� System.Security.Cryptography.HMACSHA1
			// ��憚������嚗��鈭����瘣曄��
			// (PBKDF2)��
			Rfc2898DeriveBytes rfc = new Rfc2898DeriveBytes(pWDString, salt);
			// ����挽蝵桃鈭笆蝘啁������
			aes.init(128, new SecureRandom(rfc.getBytes(128 / 8)));
			// ����挽蝵桃鈭笆蝘啁��������� (IV)��
			SecretKey key = aes.generateKey();
			javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(javax.crypto.Cipher.DECRYPT_MODE, key, new IvParameterSpec((rfc.getBytes(128 / 8))));
			result = cipher.doFinal(encryptBytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 撠圾撖��敺���蓮�銝箏�泵銝�
		try {
			return new String(result, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String byteToHexString(byte[] bytes) {
		StringBuffer sb = new StringBuffer(bytes.length);
		String sTemp;
		for (int i = 0; i < bytes.length; i++) {
			sTemp = Integer.toHexString(0xFF & bytes[i]);
			if (sTemp.length() < 2)
				sb.append(0);
			sb.append(sTemp.toUpperCase());
		}
		return sb.toString();
	}
}
