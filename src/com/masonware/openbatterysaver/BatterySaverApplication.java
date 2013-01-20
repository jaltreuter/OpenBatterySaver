package com.masonware.openbatterysaver;

import android.app.Application;
import android.content.Context;

public class BatterySaverApplication extends Application {
	
	private static BatterySaverApplication instance;
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
	}
	
	public static Context getAppContext() {
		return instance.getApplicationContext();
	}
	
}
