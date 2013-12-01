package com.myrytebytes.datamanagement;

import android.content.Context;

import com.parse.ParseUser;

public class LoginController {

	public static class LoginStatus {
		public static final int SUCCESS = 0;
		public static final int UNAUTHORIZED = 1;
		public static final int UNKNOWN_ERROR = 2;
	}

	public static final String ACTION_LOGIN_COMPLETED = "login_completed";
	public static final String EXTRA_LOGIN_STATUS = "login_status";

	private static final Object LOGIN_LOCK = new Object();

	private static ParseUser user;
	private static boolean isLoggingIn = false;
	/*package*/ static Context context;

	public static void init(Context context) {
		LoginController.context = context;
		String loggedInUser = EncryptedStore.getAuthenticatedUser(context);
		if (loggedInUser != null) {
			ParseUser user = new ParseUser();
			user.setUsername(loggedInUser);
			user.setEmail(loggedInUser);
			user.setPassword(EncryptedStore.getPassword(loggedInUser, context));
		}
	}

	public static ParseUser getSessionUser() {
		synchronized (LOGIN_LOCK) {
			return user;
		}
	}

	public static void setUser(ParseUser user) {
		synchronized (LOGIN_LOCK) {
			LoginController.user = user;
		}
	}

	public static void logOut(Context context) {
		synchronized (LOGIN_LOCK) {
			EncryptedStore.setAuthenticatedUser(null, context);
			user = null;
		}
	}
}
