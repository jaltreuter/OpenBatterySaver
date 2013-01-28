package com.masonware.openbatterysaver.profiles;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.masonware.openbatterysaver.R;

public class ProfilesFragment extends SherlockListFragment implements DialogInterface.OnClickListener {

	private ProfileDBHelper db = null;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		setRetainInstance(true);
		db = new ProfileDBHelper(getActivity());
		new LoadCursorTask().execute();
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

	private void add() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View addView = inflater.inflate(R.layout.profile_add_edit, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setTitle(R.string.add_profile_title).setView(addView)
			.setPositiveButton(R.string.ok, this)
			.setNegativeButton(R.string.cancel, null).show();
	}

	public void onClick(DialogInterface di, int whichButton) {
		ContentValues values = new ContentValues(2);
		AlertDialog dlg = (AlertDialog)di;
		EditText title = (EditText)dlg.findViewById(R.id.title);
		EditText value = (EditText)dlg.findViewById(R.id.downtime);

		values.put(ProfileDBHelper.TITLE, title.getText().toString());
		values.put(ProfileDBHelper.DOWNTIME, Integer.parseInt(value.getText().toString()));

		new InsertTask().execute(values);
	}

	private Cursor doQuery() {
		return(db.getReadableDatabase().rawQuery("SELECT _id" +
				", " + ProfileDBHelper.TITLE +
				", " + ProfileDBHelper.DOWNTIME +
				", " + ProfileDBHelper.UPTIME +
				", " + ProfileDBHelper.RATE_CUTOFF +
				" FROM " + ProfileDBHelper.TABLE_NAME + " ORDER BY " + ProfileDBHelper.TITLE,
				null));
	}

	private class LoadCursorTask extends AsyncTask<Void, Void, Void> {
		private Cursor profilesCursor = null;
		private final String[] from = {ProfileDBHelper.TITLE};
		private final int[]    to   = {R.id.title};

		@Override
		protected Void doInBackground(Void... params) {
			profilesCursor = doQuery();
			profilesCursor.getCount();
			return(null);
		}

		@Override
		public void onPostExecute(Void arg0) {
			SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), R.layout.profile_row, profilesCursor, from, to, 0);
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

}
