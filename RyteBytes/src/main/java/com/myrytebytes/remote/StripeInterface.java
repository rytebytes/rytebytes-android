package com.myrytebytes.remote;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.myrytebytes.datamodel.StripeToken;
import com.myrytebytes.remote.ApiListener.CreateStripeTokenListener;
import com.myrytebytes.remote.JsonRequest.JsonRequestListener;
import com.myrytebytes.rytebytes.Config;

import java.util.HashMap;
import java.util.Map;

public class StripeInterface {

	private static final String API_BASE = "https://api.stripe.com/";

	private static RequestQueue requestQueue;

    public static void createToken(String cvc, String cardNumber, String expMonth, String expYear, Context context, final CreateStripeTokenListener listener) {
        if (requestQueue == null) {
            requestQueue = JsonNetwork.newRequestQueue(context, new OkHttpStack());
        }

        Map<String, Object> cardMap = new HashMap<>();
        cardMap.put("card[cvc]", cvc);
        cardMap.put("card[number]", cardNumber);
        cardMap.put("card[exp_month]", expMonth);
        cardMap.put("card[exp_year]", expYear);

        requestQueue.add(new StripeRequest<>(Method.POST, "v1/tokens", cardMap, null, StripeToken.class, new JsonRequestListener<StripeToken>() {
            @Override
            public void onResponse(StripeToken response, int statusCode, VolleyError error) {
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
				headers.put("Authorization", "Basic " + Base64.encodeBytes((Config.STRIPE_API_KEY + ":").getBytes("UTF-8")));
			} catch (Exception e) { }

			headers.put("Accept", "application/json");

			if (getMethod() == Method.POST || getMethod() == Method.PUT) {
				headers.put("Content-Type", "application/x-www-form-urlencoded; charset=utf8");
			}

			return headers;
		}
	}
}
