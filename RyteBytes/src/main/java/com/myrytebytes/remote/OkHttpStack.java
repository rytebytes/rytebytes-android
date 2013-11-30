package com.myrytebytes.remote;

import com.android.volley.toolbox.HurlStack;
import com.myrytebytes.datamanagement.Log;
import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;

public class OkHttpStack extends HurlStack {
	private final OkHttpClient client;

	public OkHttpStack() {
		client = new OkHttpClient();
		SSLContext sslContext;
		try {
			sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, null, null);
			client.setSslSocketFactory(sslContext.getSocketFactory());
		} catch (GeneralSecurityException e) {
			Log.e(e);
		}
	}

	@Override
	protected HttpURLConnection createConnection(URL url) throws IOException {
		return client.open(url);
	}
}
