package com.myrytebytes.remote;

import android.content.Context;
import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.myrytebytes.datamanagement.Logr;
import com.myrytebytes.datamanagement.MenuQuantityManager;
import com.myrytebytes.datamanagement.UserController;
import com.myrytebytes.datamodel.ErrorResponse;
import com.myrytebytes.datamodel.HeatingInstructions;
import com.myrytebytes.datamodel.Location;
import com.myrytebytes.datamodel.LocationItem;
import com.myrytebytes.datamodel.MenuItem;
import com.myrytebytes.datamodel.Order;
import com.myrytebytes.datamodel.PurchaseResponse;
import com.myrytebytes.datamodel.StripeCustomer;
import com.myrytebytes.datamodel.StripeToken;
import com.myrytebytes.datamodel.User;
import com.myrytebytes.remote.ApiListener.CreateAccountListener;
import com.myrytebytes.remote.ApiListener.GetLocationListener;
import com.myrytebytes.remote.ApiListener.GetLocationsListener;
import com.myrytebytes.remote.ApiListener.GetMenuListener;
import com.myrytebytes.remote.ApiListener.GetUserInfoListener;
import com.myrytebytes.remote.ApiListener.LoginListener;
import com.myrytebytes.remote.ApiListener.PurchaseListener;
import com.myrytebytes.remote.ApiListener.ResetPasswordListener;
import com.myrytebytes.remote.ApiListener.UpdateHeatingInstructionsListener;
import com.myrytebytes.remote.ApiListener.UpdateUserInfoListener;
import com.myrytebytes.remote.ApiListener.UpdateUserListener;
import com.myrytebytes.remote.JsonRequest.JsonRequestListener;
import com.myrytebytes.rytebytes.Config;
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

    private static Context context;
    private static RequestQueue requestQueue;

    public static void init(Context context) {
        ApiInterface.context = context;

        if (requestQueue == null) {
            requestQueue = JsonNetwork.newRequestQueue(context, new OkHttpStack());
        }
    }

    private static void updateMenuDatabase(List<MenuItem> menuItems) {
        List<String> currentObjectIds = MenuItem.getAllObjectIds(context);
        for (String objectId : currentObjectIds) {
            boolean contains = false;
            for (MenuItem menuItem : menuItems) {
                if (menuItem.objectId.equals(objectId)) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                MenuItem.deleteByObjectId(objectId, context);
            }
        }

        for (MenuItem menuItem : menuItems) {
            menuItem.insertOrUpdateByObjectId(context);
        }
    }

    private static void getMenuAtLocation(final GetMenuListener listener, String locationId) {
        Map<String, Object> params = new HashMap<>();
        params.put("locationId", locationId);
        requestQueue.add(new RyteBytesRequest<>(Method.POST, "retrievemenu", params, "result", LocationItem.class, new JsonRequestListener<List<LocationItem>>() {
            @Override
            public Response<List<LocationItem>> onParseResponseComplete(Response<List<LocationItem>> response) {
                List<MenuItem> menuItems;
                if (response != null) {
                    MenuQuantityManager.setLocationItems(response.result);
                    menuItems = new ArrayList<>();
                    for (LocationItem locationItem : response.result) {
                        menuItems.add(locationItem.menuItem);
                    }
                    updateMenuDatabase(menuItems);
                }
                return response;
            }

            @Override
            public void onResponse(List<LocationItem> response, int statusCode, VolleyError error) {
                listener.onComplete(response != null, statusCode);
            }
        }));
    }

    public static void getMenu(final GetMenuListener listener) {
        if (UserController.getPickupLocation() != null) {
            getMenuAtLocation(listener, UserController.getPickupLocation().objectId);
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
                    listener.onComplete(response != null, statusCode);
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

    public static void createUser(StripeToken token, String email, final Location location, final String password, final CreateAccountListener listener) {
        ParseUser parseUser = new ParseUser();
        parseUser.setEmail(email);
        parseUser.setUsername(email);
        parseUser.setPassword(password);
        parseUser.put("stripeId", token.id);
        parseUser.put("locationId", ParseObject.createWithoutData("Location", location.objectId));
        parseUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null && ParseUser.getCurrentUser() != null) {
                    UserController.setActiveUser(ParseUser.getCurrentUser(), location);
                }
                listener.onComplete(ParseUser.getCurrentUser(), e);
            }
        });
    }

    public static void getUserInfo(final GetUserInfoListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", UserController.getActiveUser().parseUser.getObjectId());
        requestQueue.add(new RyteBytesRequest<>(Method.POST, "userinfo", params, "result", StripeCustomer.class, new JsonRequestListener<StripeCustomer>() {
            @Override
            public void onResponse(StripeCustomer response, int statusCode, VolleyError error) {
                listener.onComplete(response, statusCode);
            }
        }));
    }

    public static void updateUserStripeToken(StripeToken stripeToken, final UpdateUserInfoListener listener) {
        User user = UserController.getActiveUser();
        Map<String, Object> params = new HashMap<>();
        params.put("userId", user.parseUser.getObjectId());
        params.put("stripeId", user.stripeId);
        params.put("token", stripeToken.id);
        Logr.d("userId = " + params.get("userId") + "; stripeId = " + params.get("stripeId") + "; tokenId = " + params.get("token"));
        requestQueue.add(new RyteBytesRequest<>(Method.POST, "updateuser", params, null, StripeCustomer.class, new JsonRequestListener<StripeCustomer>() {
            @Override
            public void onResponse(StripeCustomer response, int statusCode, VolleyError error) {
                Logr.d("sc = " + statusCode);
                listener.onComplete(error == null, statusCode);
            }
        }));
    }

    public static void updateUserLocation(final Location location, final UpdateUserListener listener) {
        ParseUser user = UserController.getActiveUser().parseUser;
        user.put("locationId", ParseObject.createWithoutData("Location", location.objectId));
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    User user = UserController.getActiveUser();
                    user.location = location;
                    UserController.setActiveUser(user);
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
        params.put("orderItemDictionary", order);

        requestQueue.add(new RyteBytesRequest<>(35000, Method.POST, "order", params, null, PurchaseResponse.class, new JsonRequestListener<PurchaseResponse>() {
            @Override
            public void onResponse(PurchaseResponse response, int statusCode, VolleyError error) {
                String errorMessage = null;
                if (error != null) {
                    errorMessage = getErrorMessage(error.networkResponse);
                }
                listener.onComplete(response != null && "success".equalsIgnoreCase(response.result), errorMessage, statusCode);
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

    public static void updateHeatingInstructions(final UpdateHeatingInstructionsListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("heating", "heating");
        params.put("content", "heating");

        requestQueue.add(new RyteBytesRequest<>(35000, Method.GET, "content", params, null, HeatingInstructions.class, new JsonRequestListener<HeatingInstructions>() {
            boolean updated;
            @Override
            public Response<HeatingInstructions> onParseResponseComplete(Response<HeatingInstructions> response) {
                if (response.result != null && !TextUtils.isEmpty(response.result.text)) {
                    Logr.d("text = " + response.result.text);
                    updated = response.result.persistIfNeeded(context);
                }
                return response;
            }

            @Override
            public void onResponse(HeatingInstructions response, int statusCode, VolleyError error) {
                Logr.d("sc = " + statusCode);
                Logr.d("onResponse: " + response);
                if (response != null) {
                    Logr.d("text: " + response.text);
                }
                if (error != null) {
                    error.printStackTrace();
                }
                listener.onComplete(updated);
            }
        }));
    }

    private static String getErrorMessage(NetworkResponse networkResponse) {
        try {
            return new ErrorResponse(new SafeJsonParser(JsonRequest.JSON_FACTORY.createParser(((JsonNetworkResponse)networkResponse).inputStream)), true).message;
        } catch (Exception e) {
            return null;
        }
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
            headers.put("X-Parse-Application-Id", Config.PARSE_APP_ID);
            headers.put("X-Parse-REST-API-Key", Config.PARSE_API_KEY);
            headers.put("Accept", "application/json");

            if (getMethod() == Method.POST || getMethod() == Method.PUT) {
                headers.put("Content-Type", getBodyContentType());
            }

            return headers;
        }
    }
}
