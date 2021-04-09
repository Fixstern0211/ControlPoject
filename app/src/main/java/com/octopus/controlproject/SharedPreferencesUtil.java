package com.octopus.controlproject;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @Author： zh
 * @Date： 10/12/20
 */

/**
 * SharedPreferences: Used for data storage, through xml, storage of marked data and setting information
 */
public class SharedPreferencesUtil {

    //The file name is config
    private static final String PREFERENCE_NAME = "config";
    // declare  constants here and use them as keys
    //version
    public static final String APK_VERSION = "APK_VERSION";
    //url for download
    public static final String APK_DOWNLOAD_URL = "APK_DOWNLOAD_URL";

    private static SharedPreferences sharedPreferences;

    /**
     * put Boolean variable to sharedPreferences
     *
     * @param context
     * @param key The name of the preference to modify
     * @param value   The new value for the preference
     */
    public static void putBoolean(Context context, String key, boolean value) {

        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        }
        sharedPreferences.edit().putBoolean(key, value).commit();
    }

    /**
     * get boolean from sharedPreferences
     *
     * @param context
     * @param key
     * @param value
     * @return default or get result
     */
    public static boolean getBoolean(Context context, String key, boolean value) {

        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        }
        return sharedPreferences.getBoolean(key, value);
    }

    /**
     * write String in sharedPreferences
     *
     * @param context
     * @param key The name of the preference to modify.
     * @param value The new value for the preference.
     */
    public static void putString(Context context, String key, String value) {

        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        }
        sharedPreferences.edit().putString(key, value).commit();
    }

    /**
     * get String from sharedPreferences
     *
     * @param context
     * @param key The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return default
     */
    public static String getString(Context context, String key, String defValue) {

        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        }
        return sharedPreferences.getString(key, defValue);
    }

    /**
     * put int in sharedPreferences
     *
     * @param context
     * @param key
     * @param value
     */
    public static void putInt(Context context, String key, int value) {

        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        }
        sharedPreferences.edit().putInt(key, value).commit();
    }

    /**
     * get int from sharedPreferences
     *
     * @param context  Interface to global information about an application environment.
     * @param key      The name of the preference to retrieve.
     * @param defValue Value to return if this preference does not exist.
     * @return default or result fro this key
     */
    public static int getInt(Context context, String key, int defValue) {

        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        }
        return sharedPreferences.getInt(key, defValue);
    }

    /**
     * remove the key from sharedPreferences
     *
     * @param context Interface to global information about an application environment.
     * @param key     The name of the preference to remove.
     */
    public static void remove(Context context, String key) {

        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
        }
        sharedPreferences.edit().remove(key).commit();
    }
}

