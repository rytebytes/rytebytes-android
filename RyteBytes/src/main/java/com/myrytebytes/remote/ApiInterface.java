package com.myrytebytes.remote;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.core.JsonGenerator;
import com.myrytebytes.datamanagement.Log;
import com.myrytebytes.datamanagement.LoginController;
import com.myrytebytes.datamodel.Location;
import com.myrytebytes.datamodel.MenuItem;
import com.myrytebytes.datamodel.Order;
import com.myrytebytes.datamodel.StripeCustomer;
import com.myrytebytes.remote.ApiListener.CreateAccountListener;
import com.myrytebytes.remote.ApiListener.GetLocationsListener;
import com.myrytebytes.remote.ApiListener.GetMenuListener;
import com.myrytebytes.remote.ApiListener.LoginListener;
import com.myrytebytes.remote.JsonRequest.JsonRequestListener;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.io.ByteArrayOutputStream;
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
				for (MenuItem menuItem : response.result) {
					menuItem.insertOrUpdateByObjectId(context);
				}
				return response;
			}

			@Override
			public void onResponse(List<MenuItem> response, int statusCode, VolleyError error) {
				listener.onComplete(response, statusCode);
			}
		}));
	}

	public static void getLocations(final GetLocationsListener listener) {
		requestQueue.add(new RyteBytesRequest<>(Method.POST, "location", null, "result", Location.class, new JsonRequestListener<List<Location>>() {
			@Override
			public void onResponse(List<Location> response, int statusCode, VolleyError error) {
				listener.onComplete(response, statusCode);
			}
		}));
	}

	public static void createUser(StripeCustomer customer, String password, final CreateAccountListener listener) {
		ParseUser parseUser = new ParseUser();
		parseUser.setEmail(customer.email);
		parseUser.setUsername(customer.email);
		parseUser.setPassword(password);
		parseUser.add("stripeId", customer.id);
		parseUser.signUpInBackground(new SignUpCallback() {
			@Override
			public void done(ParseException e) {
				LoginController.setUser(ParseUser.getCurrentUser());
				listener.onComplete(ParseUser.getCurrentUser(), e);
			}
		});
	}

	public static void login(String email, String password, final LoginListener listener) {
		ParseUser.logInInBackground(email, password, new LogInCallback() {
			@Override
			public void done(ParseUser parseUser, ParseException e) {
				LoginController.setUser(parseUser);
				listener.onComplete(parseUser, e);
			}
		});
	}

	public static void placeOrder(Order order, String locationId) {
		byte[] params;
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			JsonGenerator generator = JsonRequest.JSON_FACTORY.createGenerator(os);
			generator.writeStartObject();
			generator.writeStringField("locationId", locationId);
			generator.writeNumberField("totalInCents", order.getTotalPrice());
			generator.writeStringField("userId", "tmp"); //TODO: put this back: LoginController.getSessionUser().getObjectId());
			generator.writeArrayFieldStart("orderItems");
			order.writeJson(generator);
			generator.writeEndObject();
			generator.close();

			params = os.toByteArray();
		} catch (Exception e) {
			params = null;
			Log.e(e);
		}

		requestQueue.add(new RyteBytesRequest<>(35000, Method.POST, "order", params, null, String.class, new JsonRequestListener<String>() {
			@Override
			public void onResponse(String response, int statusCode, VolleyError error) {
				Log.d("response = " + response + "; sc = " + statusCode);
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

		public RyteBytesRequest(int timeout, int method, String endpoint, byte[] body, String returnTag, Class returnType, JsonRequestListener<T> listener) {
			super(timeout, method, HOST_BASE_URL, endpoint, body, returnTag, returnType, listener);
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
