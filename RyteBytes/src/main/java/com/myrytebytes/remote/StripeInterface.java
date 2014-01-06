package com.myrytebytes.remote;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.myrytebytes.datamodel.StripeCustomer;
import com.myrytebytes.remote.ApiListener.CreateStripeAccountListener;
import com.myrytebytes.remote.ApiListener.UpdateCreditCardListener;
import com.myrytebytes.remote.JsonRequest.JsonRequestListener;

import java.util.HashMap;
import java.util.Map;

public class StripeInterface {

	private static final String API_BASE = "https://api.stripe.com/";
	private static final String API_KEY = "sk_test_0eORjVUmVNJxwTHqMLLCogZr";

	private static RequestQueue requestQueue;

    public static void updateCardForUser(String stripeId, String cardNumber, String cardholderName, String expMonth, String expYear, Context context, final UpdateCreditCardListener listener) {
        if (requestQueue == null) {
            requestQueue = JsonNetwork.newRequestQueue(context, new OkHttpStack());
        }

        Map<String, Object> customerMap = new HashMap<>();
        customerMap.put("card[name]", cardholderName);
        customerMap.put("card[number]", cardNumber);
        customerMap.put("card[exp_month]", expMonth);
        customerMap.put("card[exp_year]", expYear);

        requestQueue.add(new StripeRequest<>(Method.POST, "v1/customers/" + stripeId, customerMap, null, StripeCustomer.class, new JsonRequestListener<StripeCustomer>() {
            @Override
            public void onResponse(StripeCustomer response, int statusCode, VolleyError error) {
                listener.onComplete(response, statusCode);
            }
        }));
    }

	public static void createCustomer(String email, String cardHolderName, String cardNumber, String expMonth, String expYear, Context context, final CreateStripeAccountListener listener) {
		if (requestQueue == null) {
			requestQueue = JsonNetwork.newRequestQueue(context, new OkHttpStack());
		}

		Map<String, Object> customerMap = new HashMap<>();
		customerMap.put("email", email);
        customerMap.put("card[name]", cardHolderName);
		customerMap.put("card[number]", cardNumber);
		customerMap.put("card[exp_month]", expMonth);
		customerMap.put("card[exp_year]", expYear);

		requestQueue.add(new StripeRequest<>(Method.POST, "v1/customers", customerMap, null, StripeCustomer.class, new JsonRequestListener<StripeCustomer>() {
			@Override
			public void onResponse(StripeCustomer response, int statusCode, VolleyError error) {
				listener.onComplete(response, statusCode);
			}
		}));
	}

	private static class StripeRequest<T> extends JsonRequest<T> {
		public StripeRequest(int method, String endpoint, Map<String, Object> params, String returnTag, Class returnType, JsonRequestListener<T> listener) {
			super(method, API_BASE, endpoint, params, returnTag, returnType, false, listener);
		}

		@Override
		public Map<String, String> getHeaders() throws AuthFailureError {
			Map<String, String> headers = new HashMap<>();
			try {
				headers.put("Authorization", "Basic " + Base64.encodeBytes((API_KEY + ":").getBytes("UTF-8")));
			} catch (Exception e) { }

			headers.put("Accept", "application/json");

			if (getMethod() == Method.POST || getMethod() == Method.PUT) {
				headers.put("Content-Type", "application/x-www-form-urlencoded; charset=utf8");
			}

			return headers;
		}
	}
}
