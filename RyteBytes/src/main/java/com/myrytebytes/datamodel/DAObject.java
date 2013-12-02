package com.myrytebytes.datamodel;

import android.content.ContentValues;
import android.database.Cursor;

public abstract class DAObject {

	public abstract void fillFromCursor(Cursor c);
	public abstract ContentValues toContentValues();

	public static Boolean getBooleanFromCursor(Cursor c, int columnIndex) {
		return c.isNull(columnIndex) ? null : (c.getInt(columnIndex) == 1);
	}

	public static Short getShortFromCursor(Cursor c, int columnIndex) {
		return c.isNull(columnIndex) ? null : c.getShort(columnIndex);
	}

	public static Integer getIntFromCursor(Cursor c, int columnIndex) {
		return c.isNull(columnIndex) ? null : c.getInt(columnIndex);
	}

	public static Long getLongFromCursor(Cursor c, int columnIndex) {
		return c.isNull(columnIndex) ? null : c.getLong(columnIndex);
	}

	public static Float getFloatFromCursor(Cursor c, int columnIndex) {
		return c.isNull(columnIndex) ? null : c.getFloat(columnIndex);
	}

	public static Boolean getBooleanFromCursor(Cursor c, String columnName) {
		int columnIndex = c.getColumnIndex(columnName);
		return c.isNull(columnIndex) ? null : (c.getInt(columnIndex) == 1);
	}

	public static boolean getBooleanFromCursor(Cursor c, String columnName, boolean defaultValue) {
		Boolean valueInDb = getBooleanFromCursor(c, columnName);
		return valueInDb == null ? defaultValue : valueInDb;
	}

	public static Short getShortFromCursor(Cursor c, String columnName) {
		int columnIndex = c.getColumnIndex(columnName);
		return c.isNull(columnIndex) ? null : c.getShort(columnIndex);
	}

	public static Integer getIntFromCursor(Cursor c, String columnName) {
		int columnIndex = c.getColumnIndex(columnName);
		return c.isNull(columnIndex) ? null : c.getInt(columnIndex);
	}

	public static Long getLongFromCursor(Cursor c, String columnName) {
		int columnIndex = c.getColumnIndex(columnName);
		return c.isNull(columnIndex) ? null : c.getLong(columnIndex);
	}

	public static Float getFloatFromCursor(Cursor c, String columnName) {
		int columnIndex = c.getColumnIndex(columnName);
		return c.isNull(columnIndex) ? null : c.getFloat(columnIndex);
	}

	public static String getStringFromCursor(Cursor c, String columnName) {
		int columnIndex = c.getColumnIndex(columnName);
		return c.getString(columnIndex);
	}

	public static boolean objectsAreEqual(Object a, Object b) {
		if (a == null && b != null) {
			return false;
		} else if (a != null && b == null) {
			return false;
		} else if (a == null && b == null) {
			return true;
		} else {
			return a.equals(b);
		}
	}
}
