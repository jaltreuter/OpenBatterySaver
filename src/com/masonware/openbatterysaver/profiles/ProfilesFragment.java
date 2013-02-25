package com.masonware.openbatterysaver.profiles;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.masonware.openbatterysaver.R;
import com.masonware.openbatterysaver.settings.SettingsUtil;
import com.masonware.openbatterysaver.settings.SettingsUtil.SettingKey;

public class ProfilesFragment extends SherlockListFragment {
	
	private static final int CONTEXT_MENU_EDIT = 1;
	private static final int CONTEXT_MENU_DELETE = 2;
	
	public static final String EXTRA_PROFILE_ID = "profile_id";
	public static final String EXTRA_PROFILE_TITLE = "profile_title";

	private ProfileDBHelper db = null;
	private Cursor contextCursor = null;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		setRetainInstance(true);
		db = new ProfileDBHelper(getActivity());
		new LoadCursorTask().execute();
		registerForContextMenu(getListView());
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		((CursorAdapter)getListAdapter()).getCursor().close();
		db.close();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.actionbar_add, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add:
			add();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@SuppressLint("SimpleDateFormat")
	@Override
	public void onListItemClick(ListView listView, View v, int position, long id) {
		Date currentTime = new Date();
		Cursor cursor = (Cursor) getListAdapter().getItem(position);
		if(ProfileDBHelper.getLong(cursor, ProfileDBHelper.ACTIVATED) > 0) {return;}
		
		int downtime = ProfileDBHelper.getInt(cursor, ProfileDBHelper.DOWNTIME);
		int uptime = ProfileDBHelper.getInt(cursor, ProfileDBHelper.UPTIME);
		int rate_cutoff = ProfileDBHelper.getInt(cursor, ProfileDBHelper.RATE_CUTOFF);
		SettingsUtil.putLong(SettingKey.WAKEUP_PERIOD, downtime);
		SettingsUtil.putLong(SettingKey.MIN_SYNC_TIME, uptime);
		SettingsUtil.putInt(SettingKey.DATA_MIN_THRESHOLD, rate_cutoff);
		
		int _id = ProfileDBHelper.getInt(cursor, ProfileDBHelper._ID);
		ContentValues values = new ContentValues(1);
		values.put(ProfileDBHelper.ACTIVATED, "0");
		new EditTask().execute(new EditRequest(_id * -1, values));
		
		ContentValues values2 = new ContentValues(1);
		values2.put(ProfileDBHelper.ACTIVATED, currentTime.getTime());
		new EditTask().execute(new EditRequest(_id, values2));
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
	    AdapterView.AdapterContextMenuInfo info;
	    try {
	        info = (AdapterView.AdapterContextMenuInfo) menuInfo;
	    } catch (ClassCastException e) {
	        Log.e("ProfilesFragment", "bad menuInfo", e);
	    return;
	    }

	    contextCursor = (Cursor) getListAdapter().getItem(info.position);
	    menu.setHeaderTitle("Profile: " + ProfileDBHelper.getString(contextCursor, ProfileDBHelper.TITLE));
	    menu.add(Menu.NONE, CONTEXT_MENU_EDIT, 0, "Edit");
	    menu.add(Menu.NONE, CONTEXT_MENU_DELETE, 0, "Delete");
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		if(item.getMenuInfo() instanceof AdapterContextMenuInfo) {
		    switch (item.getItemId()) {
		        case CONTEXT_MENU_EDIT:
		        	edit(contextCursor);
		            return true;
		        case CONTEXT_MENU_DELETE:
		        	delete(contextCursor);
		        	return true;
		        default:
		        	return false;
		    }
		}
        Log.e("ProfilesFragment", "bad menuInfo");
        return false;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(Activity.RESULT_OK != resultCode) {return;}
		ContentValues values = new ContentValues(5);
		values.put(ProfileDBHelper.TITLE, SettingsUtil.getString(SettingKey.NEW_PROFILE_TITLE, "New Profile"));
		values.put(ProfileDBHelper.DOWNTIME, SettingsUtil.getInt(SettingKey.NEW_PROFILE_DOWNTIME, 900000));
		values.put(ProfileDBHelper.UPTIME, SettingsUtil.getInt(SettingKey.NEW_PROFILE_UPTIME, 10000));
		values.put(ProfileDBHelper.RATE_CUTOFF, SettingsUtil.getInt(SettingKey.NEW_PROFILE_RATE_CUTOFF, 600));
		switch(requestCode) {
		case ProfileActivity.ADD_REQUEST:
			values.put(ProfileDBHelper.ACTIVATED, 0);
			new InsertTask().execute(values);
			break;
		case ProfileActivity.EDIT_REQUEST:
			int id = data.getIntExtra(EXTRA_PROFILE_ID, -1);
			new EditTask().execute(new EditRequest(id, values));
			break;
		}
	}
	
	private void add() {
		SettingsUtil.removeSetting(SettingKey.NEW_PROFILE_TITLE);
		SettingsUtil.removeSetting(SettingKey.NEW_PROFILE_DOWNTIME);
		SettingsUtil.removeSetting(SettingKey.NEW_PROFILE_UPTIME);
		SettingsUtil.removeSetting(SettingKey.NEW_PROFILE_RATE_CUTOFF);
		
		startActivityForResult(new Intent(getActivity(), ProfileActivity.class), ProfileActivity.ADD_REQUEST);
	}
	
	private void edit(final Cursor cursor) {
		SettingsUtil.putString(SettingKey.NEW_PROFILE_TITLE, ProfileDBHelper.getString(cursor, ProfileDBHelper.TITLE));
		SettingsUtil.putInt(SettingKey.NEW_PROFILE_DOWNTIME, ProfileDBHelper.getInt(cursor, ProfileDBHelper.DOWNTIME));
		SettingsUtil.putInt(SettingKey.NEW_PROFILE_UPTIME, ProfileDBHelper.getInt(cursor, ProfileDBHelper.UPTIME));
		SettingsUtil.putInt(SettingKey.NEW_PROFILE_RATE_CUTOFF, ProfileDBHelper.getInt(cursor, ProfileDBHelper.RATE_CUTOFF));
		
		Intent editIntent = new Intent(getActivity(), ProfileActivity.class);
		editIntent.putExtra(EXTRA_PROFILE_ID, cursor.getInt(cursor.getColumnIndex(ProfileDBHelper._ID)));
		editIntent.putExtra(EXTRA_PROFILE_TITLE, cursor.getString(cursor.getColumnIndex(ProfileDBHelper.TITLE)));
		startActivityForResult(editIntent, ProfileActivity.EDIT_REQUEST);
	}

	private void delete(final Cursor cursor) {
		final int id = cursor.getInt(cursor.getColumnIndex(ProfileDBHelper._ID));
		final String title = cursor.getString(cursor.getColumnIndex(ProfileDBHelper.TITLE));
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.dialog_delete_profile_title))
			.setNegativeButton(R.string.cancel, null)
			.setMessage(getString(R.string.dialog_delete_profile_message, title))
			.setPositiveButton(R.string.ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					new DeleteTask().execute(id);
				}
			})
			.show();
	}

	private Cursor doQuery() {
		return(db.getReadableDatabase().rawQuery("SELECT _id" +
				", " + ProfileDBHelper.TITLE +
				", " + ProfileDBHelper.DOWNTIME +
				", " + ProfileDBHelper.UPTIME +
				", " + ProfileDBHelper.RATE_CUTOFF +
				", " + ProfileDBHelper.ACTIVATED +
				" FROM " + ProfileDBHelper.TABLE_NAME + " ORDER BY " + ProfileDBHelper.TITLE,
				null));
	}

	private class LoadCursorTask extends AsyncTask<Void, Void, Void> {
		private Cursor profilesCursor = null;
		private final String[] from = {ProfileDBHelper.TITLE, ProfileDBHelper.ACTIVATED};
		private final int[]    to   = {R.id.title,			  R.id.activated};

		@Override
		protected Void doInBackground(Void... params) {
			profilesCursor = doQuery();
			profilesCursor.getCount();
			return(null);
		}

		@Override
		public void onPostExecute(Void arg0) {
			SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), R.layout.profile_row, profilesCursor, from, to, 0);
			adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
			    @Override
			    public boolean setViewValue(View view, Cursor cursor, int column) {
			        if(column == cursor.getColumnIndex(ProfileDBHelper.ACTIVATED)) {
			            long activationTime = ProfileDBHelper.getLong(cursor, ProfileDBHelper.ACTIVATED);
			            if(activationTime <= 0) {
			            	view.setVisibility(View.GONE);
			            } else {
			            	String pattern = DateFormat.is24HourFormat(getActivity()) ? "H:mm, MMM d"
			            															  : "K:mm a, MMM d";
			        		String date = new SimpleDateFormat(pattern, Locale.US).format(new Date(activationTime));
			        		TextView activated = (TextView)view; 
			        		activated.setText("Activated at " + date);
			        		activated.setVisibility(View.VISIBLE);
			            }
			            return true;
			        }
			        return false;
			    }
			});
			setListAdapter(adapter);
		}
	}

	private class InsertTask extends AsyncTask<ContentValues, Void, Void> {
		private Cursor profilesCursor=null;

		@Override
		protected Void doInBackground(ContentValues... values) {
			db.getWritableDatabase().insert(ProfileDBHelper.TABLE_NAME, ProfileDBHelper.TITLE, values[0]);
			profilesCursor = doQuery();
			profilesCursor.getCount();
			return null;
		}

		@Override
		public void onPostExecute(Void result) {
			((CursorAdapter)getListAdapter()).changeCursor(profilesCursor);
		}
	}
	
	private class EditRequest {
		private int id;
		private ContentValues newContentValues;
		
		private EditRequest(int id, ContentValues newContentValues) {
			this.id = id;
			this.newContentValues = newContentValues;
		}
	}

	private class EditTask extends AsyncTask<EditRequest, Void, Void> {
		private Cursor profilesCursor=null;

		@Override
		protected Void doInBackground(EditRequest... requests) {
			EditRequest request = requests[0];
			String where = (request.id > 0) ? ProfileDBHelper._ID + "="  + Math.abs(request.id)
											: ProfileDBHelper._ID + "<>" + Math.abs(request.id); 
			db.getWritableDatabase().update(ProfileDBHelper.TABLE_NAME, 
											request.newContentValues, 
											where,
											null);
			profilesCursor = doQuery();
			profilesCursor.getCount();
			return null;
		}

		@Override
		public void onPostExecute(Void result) {
			((CursorAdapter)getListAdapter()).changeCursor(profilesCursor);
		}
	}

	private class DeleteTask extends AsyncTask<Integer, Void, Void> {
		private Cursor profilesCursor=null;

		@Override
		protected Void doInBackground(Integer... ids) {
			int id = ids[0];
			db.getWritableDatabase().delete(ProfileDBHelper.TABLE_NAME, 
											ProfileDBHelper._ID + "=" + id, 
											null);
			profilesCursor = doQuery();
			profilesCursor.getCount();
			return null;
		}

		@Override
		public void onPostExecute(Void result) {
			((CursorAdapter)getListAdapter()).changeCursor(profilesCursor);
		}
	}

}
