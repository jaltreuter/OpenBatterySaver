package com.masonware.openbatterysaver.settings;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.masonware.openbatterysaver.BatterySaverApplication;

public class Settings {
	public static SharedPreferences getSharedPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(BatterySaverApplication.getAppContext());
	}
	
	public static boolean hasSetting(String key) {
		Object value = getSharedPreferences().getAll().get(key);
		return value != null;
	}
	
	public static int getInt(String key, int defaultValue) {
		return getSharedPreferences().getInt(key, defaultValue);
	}
	
	public static boolean getBoolean(String key, boolean defaultValue) {
		return getSharedPreferences().getBoolean(key, defaultValue);
	}

	public static String getEnum(String key, String defaultValue) {
		return getSharedPreferences().getString(key, defaultValue);
	}
	
	public static float getFloat(String key, float defaultvalue) {
		return getSharedPreferences().getFloat(key, defaultvalue);
	}	

	public static String getString(String key, String defaultValue) {
		return getSharedPreferences().getString(key, defaultValue);
	}	
	
	public static long getLong(String key, long defaultValue) {
		return getSharedPreferences().getLong(key, defaultValue);
	}	
	
	public static void putInt(String key, int value) {
		SharedPreferences prefs = getSharedPreferences();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(key, value);
		editor.commit();
	}
	
	public static void putLong(String key, long value) {
		SharedPreferences prefs = getSharedPreferences();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong(key, value);
		editor.commit();
	}
	
	public static void putFloat(String key, float value) {
		SharedPreferences prefs = getSharedPreferences();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putFloat(key, value);
		editor.commit();
	}
	
	public static void putString(String key, String value) {
		SharedPreferences prefs = getSharedPreferences();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public static void putBoolean(String key, boolean value) {
		SharedPreferences prefs = getSharedPreferences();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
}
