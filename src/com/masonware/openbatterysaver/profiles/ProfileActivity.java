package com.masonware.openbatterysaver.profiles;

import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.masonware.openbatterysaver.R;
import com.masonware.openbatterysaver.settings.DurationPreference;
import com.masonware.openbatterysaver.settings.SettingsUtil;
import com.masonware.openbatterysaver.settings.SettingsUtil.SettingKey;

public class ProfileActivity extends PreferenceActivity {
	
	public static final int ADD_REQUEST = 0;
	public static final int EDIT_REQUEST = 1;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.profile);
		if(getIntent().hasExtra(ProfilesFragment.EXTRA_PROFILE_TITLE)) {
			setTitle("Edit \"" + getIntent().getStringExtra(ProfilesFragment.EXTRA_PROFILE_TITLE) + "\"");
		} else {
			setTitle("New Profile");
		}
		
		String defTitle   = SettingsUtil.getString(SettingKey.NEW_PROFILE_TITLE, "New Profile");
		int defDowntime   = SettingsUtil.getInt(SettingKey.NEW_PROFILE_DOWNTIME, 900000);
		int defUptime     = SettingsUtil.getInt(SettingKey.NEW_PROFILE_UPTIME, 10000);
		int defRateCutoff = SettingsUtil.getInt(SettingKey.NEW_PROFILE_RATE_CUTOFF, 600);
		
		bindPreferenceSummaryToValue(findPreference(SettingKey.NEW_PROFILE_TITLE.name()), defTitle);
		findPreference(SettingKey.NEW_PROFILE_DOWNTIME.name()).setDefaultValue(defDowntime);
		findPreference(SettingKey.NEW_PROFILE_UPTIME.name()).setDefaultValue(defUptime);
		bindPreferenceSummaryToValue(findPreference(SettingKey.NEW_PROFILE_RATE_CUTOFF.name()), defRateCutoff);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.actionbar_save_cancel, menu);
	    return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.save:
			save();
			return true;
		case R.id.cancel:
			cancel();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void save() {
		Intent returnIntent = new Intent();
		if(getIntent().hasExtra(ProfilesFragment.EXTRA_PROFILE_ID)) {
			returnIntent.putExtra(ProfilesFragment.EXTRA_PROFILE_ID, getIntent().getIntExtra(ProfilesFragment.EXTRA_PROFILE_ID, -1));
		}
		setResult(RESULT_OK, returnIntent);     
		finish();
	}

	private void cancel() {
		Intent returnIntent = new Intent();
		setResult(RESULT_CANCELED, returnIntent);        
		finish();
	}
	
	private static void bindPreferenceSummaryToValue(Preference preference, Object defaultValue) {
		bindPreferenceSummaryToValue(preference);
		sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, defaultValue);
	}
	
	private static void bindPreferenceSummaryToValue(Preference preference) {
		preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
	}
	
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if(preference instanceof ListPreference) {
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);
				preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
			} else if(preference instanceof DurationPreference) {
//				DurationPreference durationPreference = (DurationPreference)preference; 
//				durationPreference.setSummaryForValue((Integer)value);
			} else {
				preference.setSummary(stringValue);
			}
			return true;
		}
	};
	
}
