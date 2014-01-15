package com.myrytebytes.remote;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.myrytebytes.datamanagement.Log;
import com.myrytebytes.datamodel.JacksonParser;
import com.myrytebytes.datamodel.JacksonWriter;
import com.myrytebytes.remote.JsonHandler.JsonHandlerListenerAdapter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonRequest<T> extends Request<T> {

	public static final int DEFAULT_TIMEOUT = 30000; // 30 seconds
	public static final JsonFactory JSON_FACTORY = new JsonFactory();

	private Map<String, Object> mParamMap;
	private final JsonRequestListener<T> mListener;
	private int mStatusCode;
	private Class mReturnType;
	private String mReturnTag;
	private T mResponseObject;
    private boolean mEncodeAsJson;

	public JsonRequest(int method, String baseUrl, String endpoint, Map<String, Object> params, String returnTag, Class returnType, boolean encodeAsJson, JsonRequestListener<T> listener) {
		this(DEFAULT_TIMEOUT, method, baseUrl, endpoint, params, returnTag, returnType, encodeAsJson, listener);
	}

	public JsonRequest(int timeout, int method, String baseUrl, String endpoint, Map<String, Object> params, String returnTag, Class returnType, boolean encodeAsJson, JsonRequestListener<T> listener) {
		this(timeout, method, getUrl(method, baseUrl, endpoint, params), params, returnTag, returnType, encodeAsJson, listener);
	}

	public JsonRequest(int timeout, int method, String url, Map<String, Object> params, String returnTag, Class returnType, boolean encodeAsJson, JsonRequestListener<T> listener) {
		super(method, url, null);

		setShouldCache(false);

		mListener = listener;
		mReturnType = returnType;
		mReturnTag = returnTag;

		setRetryPolicy(new DefaultRetryPolicy(timeout, 1, 1));

        mEncodeAsJson = encodeAsJson;

		if (method == Method.POST || method == Method.PUT) {
            mParamMap = params;
		}
	}

	private static String getUrl(int method, String baseUrl, String endpoint, Map<String, Object> params) {
		if (params != null) {
			for (Map.Entry<String, Object> entry : params.entrySet()) {
				if (entry.getValue() == null || entry.getValue().equals("null")) {
					entry.setValue("");
				}
			}
		}

		if (method == Method.GET && params != null && !params.isEmpty()) {
			final StringBuilder result = new StringBuilder(baseUrl + endpoint);
			final int startLength = result.length();
			for (String key : params.keySet()) {
				try {
					final String encodedKey = URLEncoder.encode(key, "UTF-8");
					final String encodedValue = URLEncoder.encode((String)params.get(key), "UTF-8");
					if (result.length() > startLength) {
						result.append("&");
					} else {
						result.append("?");
					}
					result.append(encodedKey);
					result.append("=");
					result.append(encodedValue);
				} catch (Exception e) { }
			}
			return result.toString();
		} else {
			return baseUrl + endpoint;
		}
	}

	@Override
	public byte[] getBody() throws AuthFailureError {
		if (mParamMap != null && mEncodeAsJson) {
            byte[] body;
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                JsonGenerator generator = JsonRequest.JSON_FACTORY.createGenerator(os);
                generator.writeStartObject();

                for (String key : mParamMap.keySet()) {
                    Object value = mParamMap.get(key);
                    if (value instanceof String) {
                        generator.writeStringField(key, (String)value);
                    } else if (value instanceof Integer) {
                        generator.writeNumberField(key, (Integer)value);
                    } else if (value instanceof JacksonWriter) {
                        generator.writeObjectFieldStart(key);
                        ((JacksonWriter)value).writeJSON(generator);
                        generator.writeEndObject();
                    }
                }

                generator.writeEndObject();
                generator.close();
                body = os.toByteArray();
            } catch (Exception e) {
                body = null;
                Log.e(e);
            }

//            Log.d("body = " + new String(body));
			return body;
		} else {
			return super.getBody();
		}
	}

    @Override
    public String getBodyContentType() {
        if (mParamMap != null && mEncodeAsJson) {
            return "application/json; charset=" + getParamsEncoding();
        } else {
            return super.getBodyContentType();
        }
    }

	@Override
	protected void deliverResponse(T response) {
		mListener.onResponse(response, mStatusCode, null);
	}

	@Override
	public void deliverError(VolleyError error) {
		int statusCode;
		if (error != null && error.networkResponse != null) {
			statusCode = error.networkResponse.statusCode;
		} else {
			statusCode = 0;
		}

        //Uncomment to print the raw JSON response. Leaving this uncommented will break everything.
//        Log.d("error = " + new java.util.Scanner(((JsonNetworkResponse)error.networkResponse).inputStream).useDelimiter("\\A").next());
        mListener.onResponse(null, statusCode, error);
	}

	@Override
	protected Response<T> parseNetworkResponse(NetworkResponse networkResponse) {
		mStatusCode = networkResponse.statusCode;

		if (mReturnType != null && mStatusCode >= 200 && mStatusCode < 300) {
			try {
				if (networkResponse.statusCode >= 200 && networkResponse.statusCode < 300) {
					//Uncomment to print the raw JSON response. Leaving this uncommented will break everything.
//					Log.d("json = " + new java.util.Scanner(((JsonNetworkResponse)networkResponse).inputStream).useDelimiter("\\A").next());

					SafeJsonParser jsonParser = new SafeJsonParser(JSON_FACTORY.createParser(((JsonNetworkResponse)networkResponse).inputStream));
					parseJson(jsonParser);
                }
			} catch (Exception e) {
				Log.e("An error occurred while parsing network response:", e);
				e.printStackTrace();
				return Response.error(new ParseError(networkResponse));
			}
		}

		Response<T> response = Response.success(mResponseObject, HttpHeaderParser.parseCacheHeaders(networkResponse));
		if (mReturnType == null || mResponseObject != null) {
			return mListener.onParseResponseComplete(response);
		} else {
			return response;
		}
	}

	private void parseJson(SafeJsonParser jsonParser) {
		try {
			if (mReturnTag != null) {
				JsonHandler.handleJson(jsonParser, new JsonHandlerListenerAdapter() {
					@Override
					public boolean onObject(String tag, SafeJsonParser jsonParser) throws IOException {
						if (tag.equals(mReturnTag)) {
							assignResponseObject(getResponseObject(jsonParser, false));
							return true;
						} else {
							return false;
						}
					}

					@Override
					public boolean onArray(String tag, SafeJsonParser jsonParser) throws IOException {
						if (tag.equals(mReturnTag)) {
							List<Object> list = new ArrayList<>();
							while (jsonParser.nextToken() != JsonToken.END_ARRAY && jsonParser.getCurrentToken() != null) {
								list.add(getResponseObject(jsonParser, false));
							}
							assignResponseObject(list);
							return true;
						} else {
							return false;
						}
					}
				}, true);
			} else {
				assignResponseObject(getResponseObject(jsonParser, true));
			}
		} catch (Exception e) {
			Log.e("Exception parsing json:", e);
		}
	}

	private void assignResponseObject(Object returnObject) throws IOException {
		try {
			@SuppressWarnings("unchecked")
			T uncheckedAssignment = (T)returnObject; //Needed to suppress the unchecked warning
			mResponseObject = uncheckedAssignment;
		} catch (ClassCastException e) {
			throw new IOException("Can't cast " + returnObject.getClass() + " to " + mReturnType);
		}
	}

	/*package*/ Object getResponseObject(SafeJsonParser jsonParser, boolean closeWhenComplete) {
		try {
			if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
                Object data = mReturnType.newInstance();
                if (!(data instanceof JacksonParser)) {
                    throw new Exception("Unparsable object!");
                } else {
                    ((JacksonParser)data).fillFromJSON(jsonParser, closeWhenComplete);
                }
                return data;
			} else {
				return null;
			}
		} catch (Exception e) {
			Log.e(e);
			return null;
		}
	}

	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		Map<String, String> headers = new HashMap<>();
		headers.put("Accept", "application/json");

		if (getMethod() == Method.POST || getMethod() == Method.PUT) {
			headers.put("Content-Type", getBodyContentType());
		}

		return headers;
	}

    @Override
    public Map<String, String> getParams() {
        if (mEncodeAsJson) {
            return null;
        } else {
            Map<String, String> params = new HashMap<>(mParamMap.size());
            for (String key : mParamMap.keySet()) {
                params.put(key, mParamMap.get(key).toString());
            }
            return params;
        }
    }

    public static abstract class JsonRequestListener<T> {
		public abstract void onResponse(T response, int statusCode, VolleyError error);
		public Response<T> onParseResponseComplete(Response<T> response) { return response; }
	}
}
