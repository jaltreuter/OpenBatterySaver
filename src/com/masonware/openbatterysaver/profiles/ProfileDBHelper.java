package com.masonware.openbatterysaver.profiles;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ProfileDBHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "profiles.db";
	private static final int SCHEMA_VERSION = 1;
	static final String TABLE_NAME	= "profiles";
	static final String _ID		= "_id";
	static final String TITLE		= "title";
	static final String DOWNTIME	= "downtime";
	static final String UPTIME		= "uptime";
	static final String RATE_CUTOFF	= "rate_cutoff";
	static final String ACTIVATED	= "activated";
	
	public ProfileDBHelper(Context context) {
		super(context, DATABASE_NAME, null, SCHEMA_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			db.beginTransaction();
			db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
					       _ID			+ " INTEGER PRIMARY KEY AUTOINCREMENT" +
					", " + TITLE		+ " TEXT" +
					", " + DOWNTIME		+ " INTEGER" +
					", " + UPTIME		+ " INTEGER" +
					", " + RATE_CUTOFF	+ " INTEGER" +
					", " + ACTIVATED	+ " INTEGER" +
					");");
			
			ContentValues cv = new ContentValues();
			cv.put(TITLE, "Default");
			cv.put(DOWNTIME, 1000 * 60 * 15);	// 15 minutes
			cv.put(UPTIME, 1000 * 15);			// 15 seconds
			cv.put(RATE_CUTOFF, 650);			// 650 bytes/second
			cv.put(ACTIVATED, System.currentTimeMillis());
			
			db.insert(TABLE_NAME, TITLE, cv);
			db.setTransactionSuccessful();
		}
		finally {
			db.endTransaction();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		throw new RuntimeException("How did we get here?");
	}
	
	public static int getInt(Cursor cursor, String column) {
		return cursor.getInt(cursor.getColumnIndex(column));
	}
	
	public static long getLong(Cursor cursor, String column) {
		return cursor.getLong(cursor.getColumnIndex(column));
	}
	
	public static String getString(Cursor cursor, String column) {
		return cursor.getString(cursor.getColumnIndex(column));
	}
	
	public static boolean getBoolean(Cursor cursor, String column) {
		return cursor.getString(cursor.getColumnIndex(column)).equalsIgnoreCase("true");
	}

}
