package com.masonware.openbatterysaver.service;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.masonware.openbatterysaver.BatterySaverApplication;
import com.masonware.openbatterysaver.settings.SettingsUtil;
import com.masonware.openbatterysaver.settings.SettingsUtil.SettingKey;
import com.masonware.openbatterysaver.utils.DataMonitor;
import com.masonware.openbatterysaver.utils.DataUtils;

public class DataManager implements DataMonitor.Listener {
	public interface Listener {
		public void onDataStatusChanged(boolean enabled);
	}

	private static final int DATA_MIN_THRESHOLD = 650;
	private static final long WAKEUP_PERIOD = 1000 * 60 * 15;
	private static final long MIN_SYNC_TIME = 1000 * 15;
	
	private static DataManager instance;
	private Listener listener;
	private Handler handler = new Handler();
	private Context context;
	private boolean shouldRun;
	
	private DataManager() {
		context = BatterySaverApplication.getAppContext();
		shouldRun = false;
	}
	
	public static DataManager getInstance() {
		if (instance == null) {
			instance = new DataManager();
		}
		return instance;
	}
	
	public void start(Listener listener) throws IllegalArgumentException {
		Log.v("DataManager", "Starting DataManager");
		if (this.listener != null) {
			throw new IllegalArgumentException("Listener already exists");
		}
		this.listener = listener;
		shouldRun = true;
		prepareDisableData();
	}
	
	public boolean isRunning() {
		return shouldRun;
	}
	
	public void stop(Listener listener) throws IllegalArgumentException {
		Log.v("DataManager", "Stopping DataManager");
		if (this.listener != listener) {
			throw new IllegalArgumentException("Must pass original listener to unregister");
		}
		this.listener = null;
		shouldRun = false;
		DataUtils.resetMobileDataEnabled(context, listener);
	}

	@Override
	public void onDataRateUpdate(long bytesPerSecond) {
		if(!shouldRun) {return;}
		Log.v("DataManager", "onDataRateUpdate bps=" + bytesPerSecond);
		if(bytesPerSecond < SettingsUtil.getInt(SettingKey.DATA_MIN_THRESHOLD, DATA_MIN_THRESHOLD)) {
			DataUtils.setMobileDataEnabled(context, false, listener);
			DataMonitor.getInstance().unregisterListener(this);
			
			long wakeup_period = SettingsUtil.getLong(SettingKey.WAKEUP_PERIOD, WAKEUP_PERIOD); 
			Log.v("DataManager", "Disabling data for " + wakeup_period);
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					enableDataAndSync();
					prepareDisableData();
				}
			}, wakeup_period);
		}
	}
	
	private void enableDataAndSync() {
		if(!shouldRun) {return;}
		Log.v("DataManager", "enableDataAndSync");
		DataUtils.setMobileDataEnabled(context, true, listener);
		DataUtils.forceSync(context);
	}
	
	private void prepareDisableData() {
		if(!shouldRun) {return;}
		Log.v("DataManager", "prepareDisableData");
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				disableDataAfterThreshold();
			}
		}, SettingsUtil.getLong(SettingKey.MIN_SYNC_TIME, MIN_SYNC_TIME));
	}
	
	private void disableDataAfterThreshold() {
		if(!shouldRun) {return;}
		Log.v("DataManager", "disableDataAfterThreshold");
		DataMonitor.getInstance().registerListener(this);
	}
	
}
