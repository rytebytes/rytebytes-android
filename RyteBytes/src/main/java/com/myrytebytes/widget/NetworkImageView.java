package com.myrytebytes.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.AttributeSet;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.Volley;
import com.myrytebytes.remote.OkHttpStack;

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

    public void setImageFilename(String imageFilename) {
        int resId = getResources().getIdentifier(imageFilename, "drawable", getContext().getPackageName());
        if (resId != 0) {
            setImageResource(resId);
        } else {
            setImageUrl("http://res.cloudinary.com/rytebytes/image/upload/" + imageFilename, sImageLoader);
        }
    }

    @Override
    public void setImageResource(int resId) {
        if (resId != 0) {
            super.setImageResource(resId);
        }
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        if (bm != null) {
            super.setImageBitmap(bm);
        }
    }
}
