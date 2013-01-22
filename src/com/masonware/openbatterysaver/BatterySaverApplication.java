package com.masonware.openbatterysaver;

import com.masonware.openbatterysaver.settings.Settings;
import com.masonware.openbatterysaver.settings.Settings.SettingKey;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

public class BatterySaverApplication extends Application {
	
	private static BatterySaverApplication instance;
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		if(Settings.getBoolean(SettingKey.BATTERY_SAVER_SERVICE_ON, false)) {
			startService(new Intent("com.masonware.batteryservice"));
		}
	}
	
	public static Context getAppContext() {
		return instance.getApplicationContext();
	}
	
}
