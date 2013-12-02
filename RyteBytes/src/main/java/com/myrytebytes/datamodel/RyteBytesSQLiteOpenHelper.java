package com.myrytebytes.datamodel;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RyteBytesSQLiteOpenHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "rytebytes.db";
	private static final int DATABASE_VERSION = 1;

	private static RyteBytesSQLiteOpenHelper instance;
	private static SQLiteDatabase sWriteableDb;
	private static SQLiteDatabase sReadableDb;

	private RyteBytesSQLiteOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public static RyteBytesSQLiteOpenHelper getInstance(Context context) {
		if (instance == null) {
			instance = new RyteBytesSQLiteOpenHelper(context);
		}
		return instance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		MenuItem.Columns.createTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Fill when we have to upgrade db's
	}
	
	@Override
	public SQLiteDatabase getWritableDatabase() {
		if (sWriteableDb == null || !sWriteableDb.isOpen()) {
			sWriteableDb = super.getWritableDatabase();
		}
		return sWriteableDb;
	}
	
	@Override
	public SQLiteDatabase getReadableDatabase() {
		if (sReadableDb == null || !sReadableDb.isOpen()) {
			sReadableDb = super.getReadableDatabase();
		}
		return sReadableDb;
	}

	@Override
	public synchronized void close() {
		super.close();
		if (sWriteableDb != null) {
			sWriteableDb.close();
			sWriteableDb = null;
		}
		if (sReadableDb != null) {
			sReadableDb.close();
			sReadableDb = null;
		}
	}
}
