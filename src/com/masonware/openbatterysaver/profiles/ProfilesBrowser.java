package com.masonware.openbatterysaver.profiles;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class ProfilesBrowser extends SherlockFragmentActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getSupportFragmentManager().findFragmentById(android.R.id.content)==null) {
			getSupportFragmentManager().beginTransaction().add(android.R.id.content, new ProfilesFragment()).commit();
		}
	}
}
