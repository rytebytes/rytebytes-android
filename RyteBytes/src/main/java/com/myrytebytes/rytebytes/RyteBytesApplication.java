package com.myrytebytes.rytebytes;

import android.app.Application;

import com.myrytebytes.remote.ApiInterface;
import com.parse.Parse;

public class RyteBytesApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		Parse.initialize(this, "zaZmkcjbGLCrEHagb8uJPt5TKyiFgCg9WffA6c6M", "DltIu9MSxC9k1ly58gpdpXMkGlPI6KkfSeTkjwYa");
		ApiInterface.init(this);
	}

}
