package com.myrytebytes.remote;

import android.content.Context;

import com.android.volley.Network;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ByteArrayPool;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.PoolingByteArrayOutputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.conn.ConnectTimeoutException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

public class JsonNetwork implements Network {
	private static final int DEFAULT_POOL_SIZE = 4096;

    private final HttpStack mHttpStack;
	private final ByteArrayPool mPool;

	/**
	 * @param httpStack The HTTP stack that requests are performed with.
	 */
    public JsonNetwork(HttpStack httpStack) {
		this(httpStack, DEFAULT_POOL_SIZE);
    }

	/**
	 * @param httpStack The HTTP stack that requests are performed with.
	 * @param poolSize The size of the pool buffer used for cached requests. NOTE: caching
	 *                 requests will have a significant adverse affect on parsing speed!
	 */
	public JsonNetwork(HttpStack httpStack, int poolSize) {
		mHttpStack = httpStack;
		mPool = new ByteArrayPool(poolSize);
	}

    @Override
    public NetworkResponse performRequest(Request<?> request) throws VolleyError {
		try {
			HttpResponse httpResponse = mHttpStack.performRequest(request, new HashMap<String, String>());
			final int statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity entity = httpResponse.getEntity();

			if (statusCode == -1) {
				throw new NoConnectionError();
			} else if (statusCode == HttpStatus.SC_NOT_MODIFIED && request.getCacheEntry().data != null) {
				return new JsonNetworkResponse(statusCode, request.getCacheEntry().data, null, convertHeaders(httpResponse.getAllHeaders()));
			} else {
				Map<String, String> headers = convertHeaders(httpResponse.getAllHeaders());
				byte[] data = null;
				InputStream inputStream = null;
				if (request.shouldCache()) {
					if (entity != null) {
						data = entityToBytes(entity);
					} else {
						data = new byte[0];
					}
				} else {
					inputStream = entity.getContent();
				}

				if ((statusCode >= 200 && statusCode < 300) || statusCode == 409 || statusCode == 422 || statusCode == 428) {
					return new JsonNetworkResponse(statusCode, data, inputStream, headers);
				} else {
					VolleyLog.d("Unacceptable statusCode: " + statusCode + " returned for url: " + request.getUrl());
					throw new NetworkError(new JsonNetworkResponse(statusCode, data, inputStream, headers));
				}
			}
		} catch (SocketTimeoutException | ConnectTimeoutException e) {
			throw new TimeoutError();
		} catch (MalformedURLException e) {
			throw new RuntimeException("Bad URL " + request.getUrl(), e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new NoConnectionError(e);
		}
    }

	/**
	 * Copied from {@link com.android.volley.toolbox.BasicNetwork}
	 *
	 * Reads the contents of HttpEntity into a byte[].
	 *
	 */
	private byte[] entityToBytes(HttpEntity entity) throws IOException, ServerError {
		PoolingByteArrayOutputStream bytes = new PoolingByteArrayOutputStream(mPool, (int) entity.getContentLength());
		byte[] buffer = null;
		try {
			InputStream in = entity.getContent();
			if (in == null) {
				throw new ServerError();
			}
			buffer = mPool.getBuf(1024);
			int count;
			while ((count = in.read(buffer)) != -1) {
				bytes.write(buffer, 0, count);
			}
			return bytes.toByteArray();
		} finally {
			try {
				// Close the InputStream and release the resources by "consuming the content".
				entity.consumeContent();
			} catch (IOException e) {
				// This can happen if there was an exception above that left the entity in
				// an invalid state.
				VolleyLog.v("Error occured when calling consumingContent");
			}
			mPool.returnBuf(buffer);
			bytes.close();
		}
	}

	/**
	 * Copied from {@link com.android.volley.toolbox.BasicNetwork}
	 *
	 * Converts Headers[] to Map<String, String>.
	 *
	 */
    private static Map<String, String> convertHeaders(Header[] headers) {
        Map<String, String> result = new HashMap<>(headers.length);
		for (Header header : headers) {
			result.put(header.getName(), header.getValue());
		}
        return result;
    }

	/**
	 * Creates a default instance of the worker pool and calls {@link RequestQueue#start()} on it.
	 *
	 * @param context A {@link Context} to use for creating the cache dir.
	 * @param stack An {@link com.android.volley.toolbox.HttpStack} to use for handling network calls
	 * @return A started {@link RequestQueue} instance.
	 */
	public static RequestQueue newRequestQueue(Context context, HttpStack stack) {
		RequestQueue queue = new RequestQueue(new DiskBasedCache(new File(context.getCacheDir(), "volley")), new JsonNetwork(stack));
		queue.start();
		return queue;
	}
}
