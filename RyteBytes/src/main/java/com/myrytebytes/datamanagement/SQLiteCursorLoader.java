package com.myrytebytes.datamanagement;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.support.v4.content.AsyncTaskLoader;

import com.myrytebytes.datamodel.RyteBytesSQLiteOpenHelper;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;

public class SQLiteCursorLoader extends AsyncTaskLoader<Cursor> {

	private Cursor mCursor;
	private SQLiteOpenHelper mSQLiteOpenHelper;
	private String mRawQuery;
	private String[] mArgs;

	public SQLiteCursorLoader(Context context) {
		super(context);
		mSQLiteOpenHelper = RyteBytesSQLiteOpenHelper.getInstance(context);
	}

	public boolean setQuery(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
		return setQuery(false, table, columns, selection, selectionArgs, groupBy, having, orderBy, null);
	}

	public boolean setQuery(boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
		return setQuery(SQLiteQueryBuilder.buildQueryString(distinct, table, columns, selection, groupBy, having, orderBy, limit), selectionArgs);
	}

	public boolean setQuery(String rawQuery, String[] args) {
		if (mRawQuery == null || !mRawQuery.equals(rawQuery) || !Arrays.equals(mArgs, args)) {
			mRawQuery = rawQuery;
			mArgs = args;
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Cursor loadInBackground() {
		Cursor cursor = mSQLiteOpenHelper.getReadableDatabase().rawQuery(mRawQuery, mArgs);

		if (cursor != null) {
			cursor.getCount();
		}

		return cursor;
	}

	@Override
	public void deliverResult(Cursor cursor) {
		if (isReset()) {
			if (cursor != null) {
				cursor.close();
			}
		} else {
			Cursor oldCursor = mCursor;
			mCursor = cursor;

			if (isStarted()) {
				super.deliverResult(cursor);
			}

			if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
				oldCursor.close();
			}
		}
	}

	@Override
	protected void onStartLoading() {
		if (mCursor != null) {
			deliverResult(mCursor);
		}

		if (takeContentChanged() || mCursor == null) {
			forceLoad();
		}
	}

	@Override
	protected void onStopLoading() {
		cancelLoad();
	}

	@Override
	public void onCanceled(Cursor cursor) {
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
	}

	@Override
	protected void onReset() {
		super.onReset();

		onStopLoading();

		if (mCursor != null && !mCursor.isClosed()) {
			mCursor.close();
		}

		mCursor = null;
	}

	@Override
	public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
		super.dump(prefix, fd, writer, args);
		writer.print(prefix);
		writer.print("rawQuery=");
		writer.println(mRawQuery);
		writer.print(prefix);
		writer.print("args=");
		writer.println(Arrays.toString(args));
	}
}
