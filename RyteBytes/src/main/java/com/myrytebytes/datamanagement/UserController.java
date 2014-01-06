package com.myrytebytes.datamanagement;

import android.content.Context;
import android.preference.PreferenceManager;

import com.myrytebytes.datamodel.Location;
import com.myrytebytes.datamodel.User;
import com.myrytebytes.remote.JsonRequest;
import com.myrytebytes.remote.SafeJsonParser;
import com.parse.ParseUser;

public class UserController {

    private static final String PREF_ACTIVE_USER = "active_user";
	private static final Object LOGIN_LOCK = new Object();

	private static User user;
	private static Context context;

	public static void init(Context context) {
		UserController.context = context;
        String userJSON = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_ACTIVE_USER, null);
        if (userJSON != null) {
            try {
                user = new User(new SafeJsonParser(JsonRequest.JSON_FACTORY.createParser(userJSON)), true);
            } catch (Exception e) { }
        }
	}

	public static User getActiveUser() {
		synchronized (LOGIN_LOCK) {
			return user;
		}
	}

	public static void setActiveUser(ParseUser parseUser, Location location) {
        setActiveUser(new User(parseUser, location));
	}

    public static void setActiveUser(User user) {
        synchronized (LOGIN_LOCK) {
            UserController.user = user;
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_ACTIVE_USER, user.toJSON()).commit();
        }
    }

	public static void logOut(Context context) {
		synchronized (LOGIN_LOCK) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().remove(PREF_ACTIVE_USER).commit();
			user = null;
		}
	}
}
