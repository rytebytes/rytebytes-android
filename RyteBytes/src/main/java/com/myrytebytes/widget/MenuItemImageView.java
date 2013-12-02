package com.myrytebytes.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.myrytebytes.datamodel.MenuItem;

import java.io.InputStream;

public class MenuItemImageView extends NetworkImageView {

	private static final float IMAGE_ASPECT_RATIO = 139f / 332f;

	public MenuItemImageView(Context context) {
		super(context);
	}

	public MenuItemImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MenuItemImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setMenuItem(MenuItem menuItem) {
		try {
			InputStream is = getContext().getAssets().open("menuimages/" + menuItem.imageName);
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

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = (int)(width * IMAGE_ASPECT_RATIO);

		setMeasuredDimension(width, height);
	}
}
