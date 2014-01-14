package com.myrytebytes.remote;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.myrytebytes.datamanagement.MenuQuantityManager;
import com.myrytebytes.datamanagement.UserController;
import com.myrytebytes.datamodel.Location;
import com.myrytebytes.datamodel.LocationItem;
import com.myrytebytes.datamodel.MenuItem;
import com.myrytebytes.datamodel.Order;
import com.myrytebytes.datamodel.StripeCustomer;
import com.myrytebytes.datamodel.User;
import com.myrytebytes.remote.ApiListener.CreateAccountListener;
import com.myrytebytes.remote.ApiListener.GetLocationListener;
import com.myrytebytes.remote.ApiListener.GetLocationsListener;
import com.myrytebytes.remote.ApiListener.GetMenuListener;
import com.myrytebytes.remote.ApiListener.LoginListener;
import com.myrytebytes.remote.ApiListener.PurchaseListener;
import com.myrytebytes.remote.ApiListener.ResetPasswordListener;
import com.myrytebytes.remote.ApiListener.UpdateUserListener;
import com.myrytebytes.remote.JsonRequest.JsonRequestListener;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.util.ArrayList;
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

    public static void getMenuAtLocation(final GetMenuListener listener, String locationId) {
        Map<String, Object> params = new HashMap<>();
        params.put("locationId", locationId);
        requestQueue.add(new RyteBytesRequest<>(Method.POST, "retrievemenu", params, "result", LocationItem.class, new JsonRequestListener<List<LocationItem>>() {
            @Override
            public Response<List<LocationItem>> onParseResponseComplete(Response<List<LocationItem>> response) {
                for (LocationItem locationItem : response.result) {
                    locationItem.menuItem.insertOrUpdateByObjectId(context);
                }
                return response;
            }

            @Override
            public void onResponse(List<LocationItem> response, int statusCode, VolleyError error) {
                List<MenuItem> menuItems;
                if (response != null) {
                    MenuQuantityManager.setLocationItems(response);
                    menuItems = new ArrayList<>();
                    for (LocationItem locationItem : response) {
                        menuItems.add(locationItem.menuItem);
                    }
                } else {
                    menuItems = null;
                }
                listener.onComplete(menuItems, statusCode);
            }
        }));
    }

	public static void getMenu(final GetMenuListener listener) {
        User user = UserController.getActiveUser();
        if (user != null && user.location != null) {
            getMenuAtLocation(listener, user.location.objectId);
        } else {
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
	}

	public static void getLocations(final GetLocationsListener listener) {
		requestQueue.add(new RyteBytesRequest<>(Method.POST, "location", null, "result", Location.class, new JsonRequestListener<List<Location>>() {
			@Override
			public void onResponse(List<Location> response, int statusCode, VolleyError error) {
				listener.onComplete(response, statusCode);
			}
		}));
	}

	public static void createUser(StripeCustomer customer, final Location location, final String password, final CreateAccountListener listener) {
        ParseUser parseUser = new ParseUser();
		parseUser.setEmail(customer.email);
		parseUser.setUsername(customer.email);
		parseUser.setPassword(password);
		parseUser.put("stripeId", customer.id);
        parseUser.put("locationId", ParseObject.createWithoutData("Location", location.objectId));
		parseUser.signUpInBackground(new SignUpCallback() {
			@Override
			public void done(ParseException e) {
				UserController.setActiveUser(ParseUser.getCurrentUser(), location);
				listener.onComplete(ParseUser.getCurrentUser(), e);
			}
		});
	}

    public static void updateUserLocation(final Location location, final UpdateUserListener listener) {
        ParseUser user = UserController.getActiveUser().parseUser;
        user.put("locationId", ParseObject.createWithoutData("Location", location.objectId));
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    UserController.getActiveUser().location = location;
                }
                listener.onComplete(e == null);
            }
        });
    }

    public static void getLocation(String objectId, final GetLocationListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("objectId", objectId);

        requestQueue.add(new RyteBytesRequest<>(Method.POST, "getlocation", params, "result", Location.class, new JsonRequestListener<List<Location>>() {
            @Override
            public void onResponse(List<Location> response, int statusCode, VolleyError error) {
                if (response != null && response.size() > 0) {
                    listener.onComplete(response.get(0), statusCode);
                } else {
                    listener.onComplete(null, statusCode);
                }
            }
        }));
    }

	public static void login(final String email, final String password, final LoginListener listener) {
		ParseUser.logInInBackground(email, password, new LogInCallback() {
			@Override
			public void done(ParseUser parseUser, ParseException e) {
                listener.onComplete(parseUser, e);
			}
		});
	}

	public static void placeOrder(Order order, String locationId, final PurchaseListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("locationId", locationId);
        params.put("totalInCents", order.getTotalPrice());
        params.put("userId", UserController.getActiveUser().parseUser.getObjectId());
        params.put("orderItemDicionary", order);

		requestQueue.add(new RyteBytesRequest<>(35000, Method.POST, "order", params, null, String.class, new JsonRequestListener<String>() {
			@Override
			public void onResponse(String response, int statusCode, VolleyError error) {
				listener.onComplete(statusCode == 200, statusCode);
			}
		}));
	}

    public static void resetPassword(String username, final ResetPasswordListener listener) {
        ParseUser.requestPasswordResetInBackground(username, new RequestPasswordResetCallback() {
            @Override
            public void done(ParseException e) {
                listener.onComplete(e == null);
            }
        });
    }

	private static class RyteBytesRequest<T> extends JsonRequest<T> {
		public RyteBytesRequest(int method, String endpoint, Map<String, Object> params, String returnTag, Class returnType, JsonRequestListener<T> listener) {
			super(method, HOST_BASE_URL, endpoint, params, returnTag, returnType, true, listener);
		}

		public RyteBytesRequest(int timeout, int method, String endpoint, Map<String, Object> params, String returnTag, Class returnType, JsonRequestListener<T> listener) {
			super(timeout, method, HOST_BASE_URL, endpoint, params, returnTag, returnType, true, listener);
		}

		public RyteBytesRequest(int method, String endpoint, Map<String, Object> params, String returnTag, Class returnType, JsonRequestListener<T> listener, Object tag) {
			super(method, HOST_BASE_URL, endpoint, params, returnTag, returnType, true, listener);
			setTag(tag);
		}

		public RyteBytesRequest(int timeout, int method, String endpoint, Map<String, Object> params, String returnTag, Class returnType, JsonRequestListener<T> listener, Object tag) {
			super(timeout, method, HOST_BASE_URL, endpoint, params, returnTag, returnType, true, listener);
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
