package com.masonware.openbatterysaver;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.ContentResolver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;

public class DataUtils {
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void setMobileDataEnabled(Context context, boolean enabled) {
		Log.v("DataUtils", "setMobileDataEnabled=" + enabled);
	    final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    Class conmanClass;
		try {
			conmanClass = Class.forName(conman.getClass().getName());
			
		    final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
		    iConnectivityManagerField.setAccessible(true);
		    final Object iConnectivityManager = iConnectivityManagerField.get(conman);
		    final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
		    final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
		    setMobileDataEnabledMethod.setAccessible(true);
	
		    setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
		} catch (Exception e) {
			Log.e("DataUtils", e.getStackTrace().toString());
		}
	}
	
	public static void forceSync(Context context) {
		Bundle extras = new Bundle();
		extras.putBoolean(ContentResolver.SYNC_EXTRAS_FORCE, true);
		context.getContentResolver().startSync(null, extras); 
	}
	
}
