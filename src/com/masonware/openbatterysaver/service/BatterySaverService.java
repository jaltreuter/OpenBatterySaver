package com.masonware.openbatterysaver.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.masonware.openbatterysaver.R;
import com.masonware.openbatterysaver.settings.SettingsUtil;
import com.masonware.openbatterysaver.settings.SettingsUtil.SettingKey;
import com.masonware.openbatterysaver.settings.SettingsActivity;
import com.masonware.openbatterysaver.utils.DataUtils;

public class BatterySaverService extends Service implements DataManager.Listener, OnSharedPreferenceChangeListener {
	
	private static final IntentFilter filter;
	private static final String STOP_SERVICE = "com.masonware.stopbatterysaver";
	private static final int NOTIFICATION_ID = 1;
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
		setServiceNotification();
		launchSettings = PendingIntent.getActivity(this, 0, new Intent(this, SettingsActivity.class), 0);
		stopService = PendingIntent.getBroadcast(this, 0, new Intent(STOP_SERVICE), 0);
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				handleBroadcastIntent(intent);
			}
		};
		registerReceiver(receiver, filter);
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(receiver);
		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
	}
	
	@Override
	public void onDataStatusChanged(boolean enabled) {
		determineIfActive(enabled);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
		if(SettingKey.BATTERY_SAVER_SERVICE_ACTIVE.name().equals(key)) {
			setServiceNotification();
		}
	}
	
	private void handleBroadcastIntent(Intent intent) {
		Log.v("BatterySaverService", "Intent received: " + intent);
		if(intent.getAction() == Intent.ACTION_SCREEN_OFF) {
			if(!DataManager.getInstance().isRunning() && DataUtils.getMobileDataEnabled(this)) {
				DataManager.getInstance().start(this);
			}
		} else if (intent.getAction() == Intent.ACTION_SCREEN_ON) {
			if(DataManager.getInstance().isRunning()) {
				DataManager.getInstance().stop(this);
			}
		} else if (intent.getAction() == STOP_SERVICE) {
			DataUtils.resetMobileDataEnabled(this);
			stopSelf();
		}
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private Notification getNotification() {
		int priority = getNotificationPriority();
		Log.v("BatterySaverService", "Notification priority=" + priority);
		if(priority < Notification.PRIORITY_MIN) {
			return null;
		}
		Notification.Builder builder = new Notification.Builder(this)
        .setContentTitle("Data ???")
        .setContentText("Say why...")
//        .setLargeIcon(icon)
        .setSmallIcon(R.drawable.notification_icon)
        .setContentIntent(launchSettings);
		Notification notification;
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			notification = builder
					.addAction(R.drawable.notification_action_stop, "Stop", stopService)
					.setPriority(priority)
					.build();
		} else {
			notification = builder.getNotification();
		}
		return notification;
	}
	
	private int getNotificationPriority() {
		return SettingsUtil.getBoolean(SettingKey.BATTERY_SAVER_SERVICE_ACTIVE, false) ?
			Integer.parseInt(SettingsUtil.getString(SettingKey.NOTIFICATION_PRIORITY_ACTIVE, "0")) :
			Integer.parseInt(SettingsUtil.getString(SettingKey.NOTIFICATION_PRIORITY_IDLE, "-2"));
	}
	
	private void setServiceNotification() {
		Notification notification = getNotification();
		stopForeground(true);
		if(notification != null) {
			startForeground(NOTIFICATION_ID, getNotification());
		}
	}
	
	private void determineIfActive(boolean enabled) {
		boolean userSetting = SettingsUtil.getBoolean(SettingKey.DATA_USER_SETTING, false);
		boolean curSetting = enabled;
		Log.v("BatterySaverService", "userSetting=" + userSetting + " curSetting=" + curSetting);
		boolean active = SettingsUtil.getBoolean(SettingKey.DATA_USER_SETTING, enabled)
				!= enabled;
		Log.v("BatterySaverService", "Battery saver service active=" + active);
		SettingsUtil.putBoolean(SettingKey.BATTERY_SAVER_SERVICE_ACTIVE, active);
	}
}
