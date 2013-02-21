package com.masonware.openbatterysaver.profiles;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CursorAdapter;
import android.widget.EditText;
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

	private ProfileDBHelper db = null;
	private Cursor contextCursor = null;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		setRetainInstance(true);
		db = new ProfileDBHelper(getActivity());
		new LoadCursorTask().execute();
//		getListView().setOnItemLongClickListener(this);
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
	
	private void add() {
//		LayoutInflater inflater = getActivity().getLayoutInflater();
//		View addView = inflater.inflate(R.layout.profile_add_edit, null);
//		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//
//		builder.setTitle(R.string.add_profile_title).setView(addView)
//			.setPositiveButton(R.string.ok, new OnClickListener() {
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					ContentValues values = new ContentValues(5);
//					AlertDialog dlg = (AlertDialog)dialog;
//					EditText title = (EditText)dlg.findViewById(R.id.title);
//					EditText downtime = (EditText)dlg.findViewById(R.id.downtime);
//					EditText uptime = (EditText)dlg.findViewById(R.id.uptime);
//					EditText rate_cutoff = (EditText)dlg.findViewById(R.id.rate_cutoff);
//
//					values.put(ProfileDBHelper.TITLE, title.getText().toString());
//					values.put(ProfileDBHelper.DOWNTIME, Integer.parseInt(downtime.getText().toString()));
//					values.put(ProfileDBHelper.UPTIME, Integer.parseInt(uptime.getText().toString()));
//					values.put(ProfileDBHelper.RATE_CUTOFF, Integer.parseInt(rate_cutoff.getText().toString()));
//					values.put(ProfileDBHelper.ACTIVATED, 0);
//
//					new InsertTask().execute(values);
//				}
//			})
//			.setNegativeButton(R.string.cancel, null)
//			.show();
		
		startActivityForResult(new Intent(getActivity(), ProfileActivity.class), ProfileActivity.ADD_REQUEST);
	}
	
	private void delete(final Cursor cursor) {
		int id = cursor.getInt(cursor.getColumnIndex(ProfileDBHelper._ID));

		new DeleteTask().execute(id);
	}
	
	private void edit(final Cursor cursor) {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View addView = inflater.inflate(R.layout.profile_add_edit, null);
		((EditText)addView.findViewById(R.id.title)).setText(ProfileDBHelper.getString(cursor, ProfileDBHelper.TITLE));
		((EditText)addView.findViewById(R.id.downtime)).setText("" + ProfileDBHelper.getInt(cursor, ProfileDBHelper.DOWNTIME));
		((EditText)addView.findViewById(R.id.uptime)).setText("" + ProfileDBHelper.getInt(cursor, ProfileDBHelper.UPTIME));
		((EditText)addView.findViewById(R.id.rate_cutoff)).setText("" + ProfileDBHelper.getInt(cursor, ProfileDBHelper.RATE_CUTOFF));
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setTitle(R.string.add_profile_title)
			.setView(addView)
			.setPositiveButton(R.string.ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ContentValues values = new ContentValues(4);
					AlertDialog dlg = (AlertDialog)dialog;
					EditText titleView = (EditText)dlg.findViewById(R.id.title);
					EditText downtimeView = (EditText)dlg.findViewById(R.id.downtime);
					EditText uptimeView = (EditText)dlg.findViewById(R.id.uptime);
					EditText rate_cutoffView = (EditText)dlg.findViewById(R.id.rate_cutoff);

					int downtime = Integer.parseInt(downtimeView.getText().toString());
					int uptime = Integer.parseInt(uptimeView.getText().toString());
					int rate_cutoff = Integer.parseInt(rate_cutoffView.getText().toString());
					SettingsUtil.putLong(SettingKey.WAKEUP_PERIOD, downtime);
					SettingsUtil.putLong(SettingKey.MIN_SYNC_TIME, uptime);
					SettingsUtil.putInt(SettingKey.DATA_MIN_THRESHOLD, rate_cutoff);
					values.put(ProfileDBHelper.TITLE, titleView.getText().toString());
					values.put(ProfileDBHelper.DOWNTIME, downtime);
					values.put(ProfileDBHelper.UPTIME, uptime);
					values.put(ProfileDBHelper.RATE_CUTOFF, rate_cutoff);
					
					int id = cursor.getInt(cursor.getColumnIndex(ProfileDBHelper._ID));
					
					new EditTask().execute(new EditRequest(id, values));
				}
			})
			.setNegativeButton(R.string.cancel, null)
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
