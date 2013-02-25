package com.masonware.openbatterysaver.service;

import com.masonware.openbatterysaver.settings.SettingsUtil;
import com.masonware.openbatterysaver.settings.SettingsUtil.SettingKey;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(SettingsUtil.getBoolean(SettingKey.BATTERY_SAVER_SERVICE_ON, false)) {
			context.startService(new Intent("com.masonware.batteryservice"));
		}
	}

}
