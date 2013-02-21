package com.masonware.openbatterysaver.profiles;

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
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SettingsUtil.removeSetting(SettingKey.NEW_PROFILE_TITLE);
		SettingsUtil.removeSetting(SettingKey.NEW_PROFILE_DOWNTIME);
		SettingsUtil.removeSetting(SettingKey.NEW_PROFILE_UPTIME);
		SettingsUtil.removeSetting(SettingKey.NEW_PROFILE_RATE_CUTOFF);
		addPreferencesFromResource(R.xml.profile);
		bindPreferenceSummaryToValue(findPreference(SettingKey.NEW_PROFILE_TITLE.name()), "New Profile");
		bindPreferenceSummaryToValue(findPreference(SettingKey.NEW_PROFILE_RATE_CUTOFF.name()), 600);
        setTitle("New Profile");
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
		
	}
	
	private void cancel() {
		
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
