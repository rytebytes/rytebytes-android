package com.myrytebytes.datamanagement;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.myrytebytes.datamodel.Location;
import com.myrytebytes.datamodel.User;
import com.myrytebytes.remote.JsonRequest;
import com.myrytebytes.remote.SafeJsonParser;
import com.parse.ParseUser;

public class UserController {

    private static final String PREF_ACTIVE_USER = "active_user";
    private static final String PREF_DEFAULT_LOCATION = "default_location";
	private static final Object LOGIN_LOCK = new Object();

	private static User user;
    private static Location defaultLocation;
	private static Context context;

	public static void init(Context context) {
		UserController.context = context;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String userJSON = prefs.getString(PREF_ACTIVE_USER, null);
        if (userJSON != null) {
            try {
                user = new User(new SafeJsonParser(JsonRequest.JSON_FACTORY.createParser(userJSON)), true);
                defaultLocation = user.location;
            } catch (Exception e) { }
        }

        if (defaultLocation == null) {
            String locationJSON = prefs.getString(PREF_DEFAULT_LOCATION, null);
            Logr.d("locationJson = " + locationJSON);
            if (locationJSON != null) {
                try {
                    defaultLocation = new Location(new SafeJsonParser(JsonRequest.JSON_FACTORY.createParser(locationJSON)), true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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

    public static void setDefaultPickupLocation(Location location) {
        defaultLocation = location;
        if (location != null) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_DEFAULT_LOCATION, location.toJSON()).commit();
        }
    }

    public static Location getPickupLocation() {
        if (user == null) {
            return defaultLocation;
        } else {
            return user.location;
        }
    }
}
