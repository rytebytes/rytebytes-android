package com.myrytebytes.remote;

import com.android.volley.NetworkResponse;

import org.apache.http.HttpStatus;

import java.io.InputStream;
import java.util.Map;

public class JsonNetworkResponse extends NetworkResponse {
	public InputStream inputStream;

	public JsonNetworkResponse(int statusCode, byte[] dataIfCached, InputStream inputStream, Map<String, String> headers) {
		super(statusCode, dataIfCached, headers, statusCode == HttpStatus.SC_NOT_MODIFIED);
		this.inputStream = inputStream;
	}
}