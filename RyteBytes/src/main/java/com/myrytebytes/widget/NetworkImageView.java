package com.myrytebytes.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;
import android.util.AttributeSet;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.Volley;
import com.myrytebytes.remote.OkHttpStack;

import java.io.InputStream;

public class NetworkImageView extends com.android.volley.toolbox.NetworkImageView {

	private static RequestQueue sRequestQueue;
	private static ImageLoader sImageLoader;

	public NetworkImageView(Context context) {
		super(context);
	}

	public NetworkImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NetworkImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public static void init(Context context) {
		sRequestQueue = Volley.newRequestQueue(context, new OkHttpStack());
		sImageLoader = new ImageLoader(sRequestQueue, new ImageCache() {
			private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(10);

			@Override
			public Bitmap getBitmap(String url) {
				return mCache.get(url);
			}

			@Override
			public void putBitmap(String url, Bitmap bitmap) {
				mCache.put(url, bitmap);
			}
		});
	}

    public void setImageReference(String directory, String imageReference) {
        try {
            InputStream is = getContext().getAssets().open(directory + "/" + imageReference);
            Drawable d = Drawable.createFromStream(is, null);
            setImageDrawable(d);
        } catch (Exception e) {
            // We don't have this in the assets folder. Fetch remotely?
        }

        //TODO: this is how it SHOULD work. the code above is just there until we get a better system in place
//		if (menuItem.imageResourceId != null) {
//			setImageResource(menuItem.imageResourceId);
//		} else {
//			setImageUrl("http://www.myrytebytes/images/" + menuItem.imageName);
//		}
    }

    @Override
    public void setImageResource(int resId) {
        // no-op
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        // no-op
    }
}
