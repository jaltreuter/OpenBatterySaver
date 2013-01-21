package com.masonware.openbatterysaver.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.masonware.openbatterysaver.R;
import com.masonware.openbatterysaver.settings.SettingsActivity;

public class BatterySaverService extends Service implements DataManager.Listener {
	
	private static final IntentFilter filter;
	private static final String STOP_SERVICE = "com.masonware.stopbatterysaver";
	private static final int NOTIFICATION_ID = 33853385;
	static{
		filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		filter.addAction(STOP_SERVICE);
	}

	private PendingIntent launchSettings;
	private PendingIntent stopService;
	private BroadcastReceiver receiver;
	
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
		startForeground(NOTIFICATION_ID, getNotification());
		launchSettings = PendingIntent.getActivity(this, 0, new Intent(this, SettingsActivity.class), 0);
		stopService = PendingIntent.getBroadcast(this, 0, new Intent(STOP_SERVICE), 0);
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				handleBroadcastIntent(intent);
			}
		};
		registerReceiver(receiver, filter);
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(receiver);
	}
	
	@Override
	public void onDataStatusChanged(boolean enabled) {
		// TODO Auto-generated method stub
		
	}
	
	private void handleBroadcastIntent(Intent intent) {
		Log.v("BatterySaverService", "Intent received: " + intent);
		if(intent.getAction() == Intent.ACTION_SCREEN_OFF) {
			if(!DataManager.getInstance().isRunning()) {
				DataManager.getInstance().start(this);
			}
		} else if (intent.getAction() == Intent.ACTION_SCREEN_ON) {
			DataManager.getInstance().stop(this);
		} else if (intent.getAction() == STOP_SERVICE) {
			stopSelf();
		}
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private Notification getNotification() {
		Notification.Builder builder = new Notification.Builder(this)
        .setContentTitle("Data ???")
        .setContentText("Say why...")
//        .setLargeIcon(icon)
        .setSmallIcon(R.drawable.notification_icon)
        .setContentIntent(launchSettings);
		Notification notification;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			notification = builder.addAction(R.drawable.notification_action_stop, "Stop", stopService).build();
		} else {
			notification = builder.getNotification();
		}
		return notification;
	}
}
