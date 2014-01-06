package com.myrytebytes.widget;

import android.content.Context;
import android.util.AttributeSet;

import com.myrytebytes.datamodel.MenuItem;

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
        setImageReference("menuimages", menuItem.imageName);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = (int)(width * IMAGE_ASPECT_RATIO);

		setMeasuredDimension(width, height);
	}
}
