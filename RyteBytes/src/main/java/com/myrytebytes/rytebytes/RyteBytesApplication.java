package com.myrytebytes.rytebytes;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.myrytebytes.datamanagement.UserController;
import com.myrytebytes.remote.ApiInterface;
import com.myrytebytes.widget.NetworkImageView;
import com.parse.Parse;

public class RyteBytesApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
        Crashlytics.start(this);
		Parse.initialize(this, Config.PARSE_APP_ID, Config.PARSE_CLIENT_KEY);
		ApiInterface.init(this);
		NetworkImageView.init(this);
        UserController.init(this);
	}
}
