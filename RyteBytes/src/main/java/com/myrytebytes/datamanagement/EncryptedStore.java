package com.myrytebytes.datamanagement;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;

import com.myrytebytes.remote.Base64;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptedStore {

	private static final String DEFAULT_FILENAME = "RyteBytes";
	
	private static final String IV = "deaskfodasjifupolfpdsaofshaufiawszf";
	private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
	private static final String KEY_XOR_VALUE = "xl#la*;lo0(!~n#zwep][/.//(FI)*hi;";
	
	private static byte[] sKey;

	private static EncryptedStore sharedInstance;

	private final Cipher mPrefWriter;
	private final Cipher mPrefReader;
	private final Cipher mKeyWriter;
	private final SharedPreferences mPreferences;
	private final Map<String, String> mValueBuffer;

	public EncryptedStore(Context context, String preferenceName) throws EncryptedStoreException {
		mValueBuffer = new HashMap<>();
		try {
			mPrefWriter = Cipher.getInstance(TRANSFORMATION);
			mPrefReader = Cipher.getInstance(TRANSFORMATION);
			mKeyWriter = Cipher.getInstance("AES/ECB/PKCS5Padding");

			if (sKey == null) {
				sKey = generatePassword(context);
			}
			
			byte[] iv = new byte[mPrefWriter.getBlockSize()];
			System.arraycopy(IV.getBytes(), 0, iv, 0, mPrefWriter.getBlockSize());
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.reset();
			SecretKeySpec secretKeySpec = new SecretKeySpec(md.digest(sKey), TRANSFORMATION);

			mPrefWriter.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);
			mPrefReader.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);
			mKeyWriter.init(Cipher.ENCRYPT_MODE, secretKeySpec);

			mPreferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
		} catch (Exception e) {
			throw new EncryptedStoreException(e);
		}
	}

	public static EncryptedStore getSharedInstance(Context context, String preferenceName) {
		if (sharedInstance == null) {
			sharedInstance = new EncryptedStore(context, preferenceName);
		}

		return sharedInstance;
	}
	
	private byte[] generatePassword(Context context) {
		UUID uuid;
		
		final SharedPreferences prefs = context.getSharedPreferences(DEFAULT_FILENAME, 0);
		final String id = prefs.getString("did", null);

		if (id != null) {
			uuid = UUID.fromString(id);
		} else {
			final String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);

			try {
				if (androidId != null && !"9774d56d682e549c".equals(androidId)) {
					uuid = UUID.nameUUIDFromBytes(androidId.getBytes("UTF-8"));
				} else {
					uuid = UUID.randomUUID();
					prefs.edit().putString("did", uuid.toString()).commit();
				}
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
		
		try {
			byte[] uuidBytes = uuid.toString().getBytes("UTF-8");
			byte[] toXORBytes = UUID.nameUUIDFromBytes(KEY_XOR_VALUE.getBytes()).toString().getBytes();
			
			byte[] keyBytes = new byte[uuidBytes.length];
			
			for (int i = 0; i < keyBytes.length; i++) {
				keyBytes[i] = (byte)(uuidBytes[i] ^ toXORBytes[i]);
			}
			
			return keyBytes;
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	private String getEncryptedKey(String key) {
		return encrypt(key, mKeyWriter);
	}

	public void putValue(String key, String value) throws EncryptedStoreException {
		if (key == null || key.equals("")) {
			return;
		}

		final String encryptedKey = getEncryptedKey(key);
		if (value == null) {
			mValueBuffer.remove(key);
			mPreferences.edit().remove(encryptedKey).commit();
		} else {
			mValueBuffer.put(key, value);
			final String encryptedValue = encrypt(value, mPrefWriter);
			mPreferences.edit().putString(encryptedKey, encryptedValue).commit();
		}
	}
	
	public String getString(String key) throws EncryptedStoreException {
		if (mValueBuffer.containsKey(key)) {
			return mValueBuffer.get(key);
		}

		final String encryptedKey = getEncryptedKey(key);
		if (mPreferences.contains(encryptedKey)) {
			String encryptedValue = mPreferences.getString(encryptedKey, "");
			if (encryptedValue != null && !encryptedValue.equals("")) {
				String value = decrypt(encryptedValue);
				mValueBuffer.put(key, value);
				return value;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	private String encrypt(String value, Cipher writer) throws EncryptedStoreException {
		try {
			byte[] encryptedValue = convert(writer, value.getBytes("UTF-8"));
			return Base64.encodeBytes(encryptedValue);
		} catch (Exception e) {
			throw new EncryptedStoreException(e);
		}
	}

	private String decrypt(String encodedEncryptedValue) {
		try {
			byte[] encryptedValue = Base64.decode(encodedEncryptedValue.getBytes());
			byte[] clearText = convert(mPrefReader, encryptedValue);
			return new String(clearText, "UTF-8");
		} catch (Exception e) {
			throw new EncryptedStoreException(e);
		}
	}

	private static byte[] convert(Cipher cipher, byte[] bs) throws EncryptedStoreException {
		try {
			return cipher.doFinal(bs);
		} catch (Exception e) {
			throw new EncryptedStoreException(e);
		}
	}
	
	public static class EncryptedStoreException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public EncryptedStoreException(Throwable e) {
			super(e);
		}
	}
	
	public static String getAuthenticatedUser(Context context) {
		EncryptedStore prefs = getSharedInstance(context, DEFAULT_FILENAME);
		try {
			return prefs.getString("authenticatedUser");
		} catch (Exception e) {
			Log.e(e);
			return null;
		}
	}
	
	public static void setAuthenticatedUser(String username, Context context) {
		EncryptedStore prefs = getSharedInstance(context, DEFAULT_FILENAME);
		try {
			prefs.putValue("authenticatedUser", username);
		} catch (Exception e) {
			Log.e(e);
		}
	}
	
	public static String getPassword(String username, Context context) {
		EncryptedStore prefs = getSharedInstance(context, DEFAULT_FILENAME);
		try {
			return prefs.getString("password+"+username);
		} catch (Exception e) {
			Log.e(e);
			return null;
		}
	}
	
	public static void setPassword(String username, String password, Context context) {
		EncryptedStore prefs = getSharedInstance(context, DEFAULT_FILENAME);

		try {
			prefs.putValue("password+"+username, password);
		} catch (Exception e) {
			Log.e(e);
		}
	}
}
