package com.myrytebytes.datamanagement;

public class Logr {

	private static final String TAG = "RyteBytes";

	public static void d(String tag, String s) {
		android.util.Log.d(tag, s);
	}

	public static void d(String s) {
		d(TAG, s);
	}

	public static void e(String tag, String s) {
		android.util.Log.e(tag, s);
	}

	public static void e(Exception e) {
		e.printStackTrace();
	}

	public static void e(String s) {
		e(TAG, s);
	}

	public static void e(String message, Exception e) {
		e(message);
		e(e);
	}
}
