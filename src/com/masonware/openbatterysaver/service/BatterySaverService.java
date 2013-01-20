package com.masonware.openbatterysaver.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class BatterySaverService extends Service implements DataManager.Listener {
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}
	
	@Override
	public void onCreate() {
		DataManager.getInstance().start(this); 
	}
	
	@Override
	public void onDestroy() {
		DataManager.getInstance().stop(this);
	}
	
	@Override
	public void onDataStatusChanged(boolean enabled) {
		// TODO Auto-generated method stub
		
	}
}
