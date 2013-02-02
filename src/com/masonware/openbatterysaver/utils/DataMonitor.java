package com.masonware.openbatterysaver.utils;

import java.util.ArrayList;
import java.util.Iterator;

import android.net.TrafficStats;
import android.os.Looper;

public class DataMonitor {

	private static DataMonitor instance;
	private static ArrayList<DataMonitorThread> threads;
	
	private DataMonitor() {
		threads = new ArrayList<DataMonitorThread>();
	}
	
	public static DataMonitor getInstance() {
		if (instance == null) {
			instance = new DataMonitor();
		}
		return instance;
	}

	public void registerListener(Listener listener) {
		DataMonitorThread thread = new DataMonitorThread(listener);
		thread.start();
		threads.add(thread);
	}

	public void unregisterListener(Listener listener) {
		Iterator<DataMonitorThread> it = threads.iterator();
		while(it.hasNext()) {
			DataMonitorThread thread = it.next();
			if(thread.equals(listener)) {
				thread.end();
				it.remove();
			}
		}
	}
	
	public interface Listener {
		public void onDataRateUpdate(final long bytesPerSecond);
	}
	
	private class DataMonitorThread extends Thread {
		private static final long DELAY = 5000;
		private static final long INVALID = -1;
		private long loopStart;
		private long prevTotalBytes;
		private boolean shouldRun = true;
		private Listener listener;
		
		private DataMonitorThread(Listener listener) {
			this.listener = listener;
		}
		
		@Override
		public void run() {
			Looper.prepare();
			shouldRun = true;
			prevTotalBytes  = INVALID;
			while(shouldRun) {
				loopStart = System.currentTimeMillis();
				
				long rate = getDataRate();
				if(rate != INVALID) {
					listener.onDataRateUpdate(rate);
				}
				
				long sleepTime = loopStart + DELAY - System.currentTimeMillis();
				if (sleepTime > 0) {
					try {
						sleep(sleepTime);
					} catch (InterruptedException ex) {
						//thread is being interrupted
					}
				}
			}
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			} else if (obj == this) {
				return true;
			} else if (obj == listener) {
				return true;
			}
			return false;
		}
		
		private void end() {
			shouldRun = false;
		}
		
		/**
		 * @return the current total rate of data usage (in and out), in bytes per second
		 */
		private long getDataRate() {
			long totalBytes = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();
			long rate;
			if (prevTotalBytes == INVALID) {
				rate = INVALID;
			} else {
				rate = (totalBytes - prevTotalBytes) / (DELAY / 1000);
			}
			prevTotalBytes = totalBytes;
			return rate;
		}
	}
}