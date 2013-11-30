package com.myrytebytes.remote;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.myrytebytes.datamodel.MenuItem;
import com.myrytebytes.remote.ApiListener.GetMenuListener;
import com.myrytebytes.remote.JsonRequest.JsonRequestListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiInterface {

	private static String HOST_BASE_URL = "https://api.parse.com/1/functions/";
	private static String PARSE_APP_ID = "zaZmkcjbGLCrEHagb8uJPt5TKyiFgCg9WffA6c6M";
	private static String PARSE_API_KEY = "ZjCVp64qsDxYWw6PktZgc5PFZLdLmRuHe9oOF3q9";

	private static Context context;
	private static RequestQueue requestQueue;

	public static void init(Context context) {
		ApiInterface.context = context;

		if (requestQueue == null) {
			requestQueue = JsonNetwork.newRequestQueue(context, new OkHttpStack());
		}
	}

	public static void getMenu(final GetMenuListener listener) {
		requestQueue.add(new RyteBytesRequest<>(Method.POST, "retrievemenu", null, "result", MenuItem.class, new JsonRequestListener<List<MenuItem>>() {
			@Override
			public Response<List<MenuItem>> onParseResponseComplete(Response<List<MenuItem>> response) {
				//TODO: add to a database?
				return response;
			}

			@Override
			public void onResponse(List<MenuItem> response, int statusCode, VolleyError error) {
				listener.onComplete(response, statusCode);
			}
		}));
	}

	private static class RyteBytesRequest<T> extends JsonRequest<T> {
		public RyteBytesRequest(int method, String endpoint, Map<String, String> params, String returnTag, Class returnType, JsonRequestListener<T> listener) {
			super(method, HOST_BASE_URL, endpoint, params, returnTag, returnType, listener);
		}

		public RyteBytesRequest(int timeout, int method, String endpoint, Map<String, String> params, String returnTag, Class returnType, JsonRequestListener<T> listener) {
			super(timeout, method, HOST_BASE_URL, endpoint, params, returnTag, returnType, listener);
		}

		public RyteBytesRequest(int method, String endpoint, Map<String, String> params, String returnTag, Class returnType, JsonRequestListener<T> listener, Object tag) {
			super(method, HOST_BASE_URL, endpoint, params, returnTag, returnType, listener);
			setTag(tag);
		}

		public RyteBytesRequest(int timeout, int method, String endpoint, Map<String, String> params, String returnTag, Class returnType, JsonRequestListener<T> listener, Object tag) {
			super(timeout, method, HOST_BASE_URL, endpoint, params, returnTag, returnType, listener);
			setTag(tag);
		}

		@Override
		public Map<String, String> getHeaders() throws AuthFailureError {
			Map<String, String> headers = new HashMap<>();
			headers.put("X-Parse-Application-Id", PARSE_APP_ID);
			headers.put("X-Parse-REST-API-Key", PARSE_API_KEY);
			headers.put("Accept", "application/json");

			if (getMethod() == Method.POST || getMethod() == Method.PUT) {
				headers.put("Content-Type", "application/x-www-form-urlencoded; charset=utf8");
			}

			return headers;
		}
	}
}
