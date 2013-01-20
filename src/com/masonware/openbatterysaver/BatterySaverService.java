package com.masonware.openbatterysaver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class BatterySaverService extends Service implements DataMonitor.Listener {
	
	private ConnectivityReceiver connectivityReceiver;
	private static final long DATA_MIN_THRESHOLD = 650;
	private static final long WAKEUP_PERIOD = 1000 * 60 * 15;
	private static final long MIN_SYNC_TIME = 650;
	private Handler handler = new Handler();
	
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
		connectivityReceiver = new ConnectivityReceiver();
		disableDataAfterThreshold();
	}
	
	@Override
	public void onDestroy() {
		unregisterReceiver(connectivityReceiver);
		DataMonitor.getInstance().unregisterListener(this);
	}
	
	private class ConnectivityReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.v("BatterySaverService", "Connectivity intent received");
			Bundle extras = intent.getExtras();
			if (extras != null) {
				for (String key: extras.keySet()) {
					Log.v("BatterySaverService", "key [" + key + "]: " + extras.get(key));

				}
			} else {
				Log.v("BatterySaverService", "no extras");
			}
		}
	}

	@Override
	public void onDataRateUpdate(long bytesPerSecond) {
		if(bytesPerSecond < DATA_MIN_THRESHOLD) {
			DataUtils.setMobileDataEnabled(this, false);
			DataMonitor.getInstance().unregisterListener(this);
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					enableDataAndSync();
				}
			}, WAKEUP_PERIOD);
		}
	}
	
	private void enableDataAndSync() {
		DataUtils.setMobileDataEnabled(this, true);
		DataUtils.forceSync(this);
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				disableDataAfterThreshold();
			}
		}, MIN_SYNC_TIME);
	}
	
	private void disableDataAfterThreshold() {
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(connectivityReceiver, filter);
		DataMonitor.getInstance().registerListener(this);
	}
}
