package com.masonware.openbatterysaver.settings;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.masonware.openbatterysaver.BatterySaverApplication;

public class Settings {
	
	public static enum SettingKey {
		BATTERY_SAVER_SERVICE_ON,
		BATTERY_SAVER_SERVICE_ACTIVE,
		NOTIFICATION_PRIORITY_ACTIVE,
		NOTIFICATION_PRIORITY_IDLE,
		DATA_USER_SETTING;
	}
	
	public static SharedPreferences getSharedPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(BatterySaverApplication.getAppContext());
	}
	
	public static boolean hasSetting(SettingKey key) {
		return getSharedPreferences().contains(key.name());
	}
	
	public static void removeSetting(SettingKey key) {
		SharedPreferences prefs = getSharedPreferences();
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(key.name());
		editor.commit();
	}
	
	public static int getInt(SettingKey key, int defaultValue) {
		return getSharedPreferences().getInt(key.name(), defaultValue);
	}
	
	public static boolean getBoolean(SettingKey key, boolean defaultValue) {
		return getSharedPreferences().getBoolean(key.name(), defaultValue);
	}
	
	public static float getFloat(SettingKey key, float defaultvalue) {
		return getSharedPreferences().getFloat(key.name(), defaultvalue);
	}	

	public static String getString(SettingKey key, String defaultValue) {
		return getSharedPreferences().getString(key.name(), defaultValue);
	}	
	
	public static long getLong(SettingKey key, long defaultValue) {
		return getSharedPreferences().getLong(key.name(), defaultValue);
	}	
	
	public static void putInt(SettingKey key, int value) {
		SharedPreferences prefs = getSharedPreferences();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(key.name(), value);
		editor.commit();
	}
	
	public static void putLong(SettingKey key, long value) {
		SharedPreferences prefs = getSharedPreferences();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong(key.name(), value);
		editor.commit();
	}
	
	public static void putFloat(SettingKey key, float value) {
		SharedPreferences prefs = getSharedPreferences();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putFloat(key.name(), value);
		editor.commit();
	}
	
	public static void putString(SettingKey key, String value) {
		SharedPreferences prefs = getSharedPreferences();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(key.name(), value);
		editor.commit();
	}

	public static void putBoolean(SettingKey key, boolean value) {
		SharedPreferences prefs = getSharedPreferences();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(key.name(), value);
		editor.commit();
	}
	
	public static void putProfile(BatteryProfile profile) {
		String base = "BATTERY_PROFILE_" + profile.name;
		SharedPreferences prefs = getSharedPreferences();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(base + "_name", profile.name);
		editor.putLong(base + "_downTime", profile.downTime);
		editor.putLong(base + "_minUpTime", profile.minUpTime);
		editor.putInt(base + "_dataRateCutoff", profile.dataRateCutoff);
		editor.commit();
	}
	
	public class BatteryProfile {
		private String name;
		private long downTime;
		private long minUpTime;
		private int dataRateCutoff;
		
		public BatteryProfile(String name, long downTime, long minUpTime, int dataRateCutoff) {
			this.name = name;
			this.downTime = downTime;
			this.minUpTime = minUpTime;
			this.dataRateCutoff = dataRateCutoff;
		}
		
		
	}
}
