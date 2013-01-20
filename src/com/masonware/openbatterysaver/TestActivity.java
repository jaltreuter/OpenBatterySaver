package com.masonware.openbatterysaver;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class TestActivity extends Activity implements DataMonitor.Listener, OnClickListener {
	
	private TextView output;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test);
		output = (TextView)findViewById(R.id.output);
		((Button)findViewById(R.id.on)).setOnClickListener(this);
		((Button)findViewById(R.id.off)).setOnClickListener(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		DataMonitor.getInstance().registerListener(this);
	}
	
	@Override
	public void onPause() {
		DataMonitor.getInstance().unregisterListener(this);
		super.onPause();
	}

	@Override
	public void onDataRateUpdate(final long bytesPerSecond) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				output.setText(bytesPerSecond + "b/s");
			}
		});
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.on) {
			startService(new Intent("com.masonware.batteryservice"));
		} else if(v.getId() == R.id.off) {
			stopService(new Intent("com.masonware.batteryservice"));
		}
	}
}
