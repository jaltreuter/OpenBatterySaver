package com.masonware.openbatterysaver.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.ContentResolver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;

import com.masonware.openbatterysaver.settings.Settings;
import com.masonware.openbatterysaver.settings.Settings.SettingKey;

public class DataUtils {
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void setMobileDataEnabled(Context context, boolean enabled) {
		if(!Settings.hasSetting(SettingKey.DATA_USER_SETTING)) {
			Settings.putBoolean(SettingKey.DATA_USER_SETTING, getMobileDataEnabled(context));
		}
		Log.v("DataUtils", "setMobileDataEnabled=" + enabled);
	    final ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		try {
		    Class cmClass = Class.forName(cm.getClass().getName());
			
		    final Field iConnectivityManagerField = cmClass.getDeclaredField("mService");
		    iConnectivityManagerField.setAccessible(true);
		    final Object iConnectivityManager = iConnectivityManagerField.get(cm);
		    final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
		    final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
		    setMobileDataEnabledMethod.setAccessible(true);
	
		    setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
		} catch (Exception e) {
			Log.e("DataUtils", e.getStackTrace().toString());
		}
		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static boolean getMobileDataEnabled(Context context) {
	    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    try {
	        Class cmClass = Class.forName(cm.getClass().getName());
	        Method getMobileDataEnabledMethod = cmClass.getDeclaredMethod("getMobileDataEnabled");
	        getMobileDataEnabledMethod.setAccessible(true);
	        return (Boolean)getMobileDataEnabledMethod.invoke(cm);
	    } catch (Exception e) {
			Log.e("DataUtils", e.getStackTrace().toString());
	    }
	    return false;
	}
	
	public static void resetMobileDataEnabled(Context context) {
		if(!Settings.hasSetting(SettingKey.DATA_USER_SETTING)) {
			return;
		}
		setMobileDataEnabled(context, Settings.getBoolean(SettingKey.DATA_USER_SETTING, false));
		Settings.removeSetting(SettingKey.DATA_USER_SETTING);
	}
	
	@SuppressWarnings("deprecation")
	public static void forceSync(Context context) {
		Bundle extras = new Bundle();
		extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
		context.getContentResolver().startSync(null, extras); 
	}
	
}
