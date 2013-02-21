package com.masonware.openbatterysaver;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.masonware.openbatterysaver.settings.SettingsUtil;
import com.masonware.openbatterysaver.settings.SettingsUtil.SettingKey;

public class BatterySaverApplication extends Application {
	
	private static BatterySaverApplication instance;
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		if(SettingsUtil.getBoolean(SettingKey.BATTERY_SAVER_SERVICE_ON, false)) {
			startService(new Intent("com.masonware.batteryservice"));
		}
	}
	
	public static Context getAppContext() {
		return instance.getApplicationContext();
	}
	
}
