package com.masonware.openbatterysaver.service;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.masonware.openbatterysaver.BatterySaverApplication;
import com.masonware.openbatterysaver.utils.DataMonitor;
import com.masonware.openbatterysaver.utils.DataUtils;

class DataManager implements DataMonitor.Listener {
	public interface Listener {
		public void onDataStatusChanged(boolean enabled);
	}

	private static final long DATA_MIN_THRESHOLD = 650;
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
		DataUtils.setMobileDataEnabled(context, true);
	}

	@Override
	public void onDataRateUpdate(long bytesPerSecond) {
		if(!shouldRun) {return;}
		Log.v("DataManager", "onDataRateUpdate bps=" + bytesPerSecond);
		if(bytesPerSecond < DATA_MIN_THRESHOLD) {
			DataUtils.setMobileDataEnabled(context, false);
			DataMonitor.getInstance().unregisterListener(this);
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					enableDataAndSync();
					prepareDisableData();
				}
			}, WAKEUP_PERIOD);
		}
	}
	
	private void enableDataAndSync() {
		if(!shouldRun) {return;}
		Log.v("DataManager", "enableDataAndSync");
		DataUtils.setMobileDataEnabled(context, true);
		DataUtils.forceSync(context);
		listener.onDataStatusChanged(true);
	}
	
	private void prepareDisableData() {
		if(!shouldRun) {return;}
		Log.v("DataManager", "prepareDisableData");
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				disableDataAfterThreshold();
			}
		}, MIN_SYNC_TIME);
	}
	
	private void disableDataAfterThreshold() {
		if(!shouldRun) {return;}
		Log.v("DataManager", "disableDataAfterThreshold");
		DataMonitor.getInstance().registerListener(this);
	}
	
}
