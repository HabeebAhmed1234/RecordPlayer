package com.cromiumapps.musicplayer.preferences;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public abstract class AbstractSharedPreferences {
    private static final String TAG = "TNSharedPreferences";

    private SharedPreferences mSharedPref;
    private Editor mEditor;
    /**
     * Encryption key for secure preference entries. The key will be different
     * for each preference file
     */
    private byte[] mAESKey;
    /**
     * static cache of shared preference entries
     */
    private static HashMap<String, Object> sCache;
    /**
     * shared pref key name for the encryption key
     */
    private static String sAESKeyName;

    private static HashSet<String> SECRET_KEYS = new HashSet<String>() {{
        add(UserInfo.USERINFO_APP_VERSION);
        add(UserInfo.USERINFO_VERSION);
    }};

    protected Context mContext;

    public AbstractSharedPreferences(Context context) {
        mContext = context;
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        mEditor = mSharedPref.edit();
        initializeKey(context);

        if (sCache == null) {
            sCache = new HashMap<String, Object>();
        }
    }

    public AbstractSharedPreferences(Context context, String preferenceName) {
        mContext = context;
        mSharedPref = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
        mEditor = mSharedPref.edit();
        initializeKey(context);

        if (sCache == null) {
            sCache = new HashMap<String, Object>();
        }
    }

    private void initializeKey(Context context) {
        try {
            if (sAESKeyName == null) {
                sAESKeyName = AbstractSharedPreferences.generateAesKeyName(context);
            }
            String value = mSharedPref.getString(sAESKeyName, null);
            if (value == null) {
                value = AbstractSharedPreferences.generateAesKeyValue();
                mSharedPref.edit().putString(sAESKeyName, value).commit();
            }
            mAESKey = AbstractSharedPreferences.decode(value);
        } catch (Throwable t) {
            // in case of any failture, we use the android id as key
            mAESKey = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID).getBytes();
        }
    }

    private static String encode(byte[] input) {
        return Base64.encodeToString(input, Base64.NO_PADDING | Base64.NO_WRAP);
    }

    private static byte[] decode(String input) {
        return Base64.decode(input, Base64.NO_PADDING | Base64.NO_WRAP);
    }

    private static String generateAesKeyName(Context context) throws InvalidKeySpecException, NoSuchAlgorithmException {
        final char[] password = context.getPackageName().toCharArray();
        final byte[] salt = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID).getBytes();

        // Number of PBKDF2 hardening rounds to use, larger values increase
        // computation time, you should select a value that causes
        // computation to take >100ms
        final int iterations = 1000;

        // Generate a 256-bit key
        final int keyLength = 256;

        final KeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        return AbstractSharedPreferences.encode(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
                .generateSecret(spec).getEncoded());
    }

    private static String generateAesKeyValue() throws NoSuchAlgorithmException {
        // Do *not* seed secureRandom! Automatically seeded from system entropy
        final SecureRandom random = new SecureRandom();

        // Use the largest AES key length which is supported by the OS
        final KeyGenerator generator = KeyGenerator.getInstance("AES");
        try {
            generator.init(256, random);
        } catch (Exception e) {
            try {
                generator.init(192, random);
            } catch (Exception e1) {
                generator.init(128, random);
            }
        }
        return AbstractSharedPreferences.encode(generator.generateKey().getEncoded());
    }

    private String encrypt(String cleartext) {
        if (cleartext == null || cleartext.length() == 0) {
            return cleartext;
        }
        try {
            final Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(mAESKey, "AES"));
            return AbstractSharedPreferences.encode(cipher.doFinal(cleartext.getBytes("UTF-8")));
        } catch (Exception e) {
            Log.w(AbstractSharedPreferences.class.getName(), "encrypt", e);
            return cleartext;
        }
    }

    private String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.length() == 0) {
            return ciphertext;
        }
        try {
            final Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(mAESKey, "AES"));
            return new String(cipher.doFinal(AbstractSharedPreferences.decode(ciphertext)), "UTF-8");
        } catch (Exception e) {
            Log.w(AbstractSharedPreferences.class.getName(), "decrypt", e);
            return ciphertext;
        }
    }

    public HashMap<String, Object> getChangesCache() {
        return new HashMap<String, Object>(sCache);
    }

    // Add changes from a changesCache that has been received in an Intent or something
    public void writeChangesFromCache(HashMap<String, Object> changesCache) {
        for (Map.Entry<String, Object> entry : changesCache.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String) setByKey(key, (String) value);
            else if (value instanceof Boolean) setByKey(key, (Boolean) value);
            else if (value instanceof Integer) setByKey(key, (Integer) value);
            else if (value instanceof Long) setByKey(key, (Long) value);
            else Log.e(TAG, "Bad value in changesCache - value:" + value);
        }
    }


    @SuppressLint("NewApi")
    public void commitChanges() {
        // apply() saves to shared memory preferences and then async to disk where as commit
        // does everything synchronously. apply() however is only supported GINGERBREAD and above
        mEditor.apply();
    }

    public void clearChanges() {
        mEditor.clear();
        sCache.clear();
    }

    // *** Key/Value interface for interaction with PreferenceActivity ***
    public void setByKey(String key, String value) {
        sCache.put(key, value);

        if (SECRET_KEYS.contains(key)) {
            final String encryptedKey = encrypt(key);
            final String encryptedValue = encrypt(value);
            mEditor.putString(encryptedKey, encryptedValue);
        } else {
            mEditor.putString(key, value);
        }
    }

    public void setByKey(String key, boolean value) {
        sCache.put(key, value);

        if (SECRET_KEYS.contains(key)) {
            final String encryptedKey = encrypt(key);
            final String encryptedValue = encrypt(Boolean.toString(value));
            mEditor.putString(encryptedKey, encryptedValue);
        } else {
            mEditor.putBoolean(key, value);
        }
    }

    public void setByKey(String key, int value) {
        sCache.put(key, value);

        if (SECRET_KEYS.contains(key)) {
            final String encryptedKey = encrypt(key);
            final String encryptedValue = encrypt(Integer.toString(value));
            mEditor.putString(encryptedKey, encryptedValue);
        } else {
            mEditor.putInt(key, value);
        }
    }

    public void setByKey(String key, long value) {
        sCache.put(key, value);

        if (SECRET_KEYS.contains(key)) {
            final String encryptedKey = encrypt(key);
            final String encryptedValue = encrypt(Long.toString(value));
            mEditor.putString(encryptedKey, encryptedValue);
        } else {
            mEditor.putLong(key, value);
        }
    }


    public String getStringByKey(String key) {
        return getStringByKey(key, "");
    }

    public String getStringByKey(String key, String defaultValue) {
        if (sCache.containsKey(key)) {
            Object val = sCache.get(key);
            if (val == null) return defaultValue;
            return (String)val;
        }

        if (SECRET_KEYS.contains(key)) {
            final String encryptedKey = encrypt(key);
            final String encryptedValue = mSharedPref.getString(encryptedKey, null);
            if (!TextUtils.isEmpty(encryptedValue)) {
                String decryptedValue = decrypt(encryptedValue);
                sCache.put(key, decryptedValue);
                return decryptedValue;
            }
        } else {
            return mSharedPref.getString(key, defaultValue);
        }

        return defaultValue;
    }

    public Boolean getBooleanByKey(String key) {
        return getBooleanByKey(key, false);
    }

    public Boolean getBooleanByKey(String key, boolean defaultValue) {
        if (sCache.containsKey(key)) {
            return (Boolean) sCache.get(key);
        }

        if (SECRET_KEYS.contains(key)) {
            final String encryptedKey = encrypt(key);
            final String encryptedValue = mSharedPref.getString(encryptedKey, null);
            try {
                Boolean decryptedValue = Boolean.parseBoolean(decrypt(encryptedValue));
                sCache.put(key, decryptedValue);
                return decryptedValue;
            } catch (Exception e) {
                return defaultValue;
            }
        } else {
            return mSharedPref.getBoolean(key, defaultValue);
        }
    }

    public int getIntByKey(String key) {
        return getIntByKey(key, 0);
    }

    public int getIntByKey(String key, int defaultValue) {
        if (sCache.containsKey(key)) {
            return (Integer) sCache.get(key);
        }

        if (SECRET_KEYS.contains(key)) {
            final String encryptedKey = encrypt(key);
            final String encryptedValue = mSharedPref.getString(encryptedKey, null);
            try {
                Integer decryptedValue = Integer.parseInt(decrypt(encryptedValue));
                sCache.put(key, decryptedValue);
                return decryptedValue;
            } catch (Exception e) {
                return defaultValue;
            }
        } else {
            return mSharedPref.getInt(key, defaultValue);
        }
    }

    public long getLongByKey(String key) {
        return getLongByKey(key, 0);
    }

    public long getLongByKey(String key, long defaultValue) {
        if (sCache.containsKey(key)) {
            return (Long) sCache.get(key);
        }

        if (SECRET_KEYS.contains(key)) {
            final String encryptedKey = encrypt(key);
            final String encryptedValue = mSharedPref.getString(encryptedKey, null);
            try {
                Long decryptedValue = Long.parseLong(decrypt(encryptedValue));
                sCache.put(key, decryptedValue);
                return decryptedValue;
            } catch (Exception e) {
                return defaultValue;
            }
        } else {
            return mSharedPref.getLong(key, defaultValue);
        }
    }
}
