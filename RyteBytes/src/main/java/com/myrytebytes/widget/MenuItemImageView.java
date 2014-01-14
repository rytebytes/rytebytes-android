package com.myrytebytes.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.TextPaint;
import android.util.AttributeSet;

import com.myrytebytes.datamanagement.FontManager;
import com.myrytebytes.datamodel.MenuItem;
import com.myrytebytes.rytebytes.R;

public class MenuItemImageView extends NetworkImageView {

    private static final float IMAGE_ASPECT_RATIO = 139f / 332f;
    private boolean mDrawItemName;

    private String mItemName;
    private int mTextPadding;
    private TextPaint mTextPaint;
    private boolean mTextNeedsResize;
    private int mLastResizeWidth;
    private int mMaxTextSize;
    private int mMinTextSize;
    private int mTextHeight;

    public MenuItemImageView(Context context) {
        super(context);
        init(context, null);
    }

    public MenuItemImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MenuItemImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MenuItemImageView);
        mDrawItemName = a.getBoolean(R.styleable.MenuItemImageView_drawItemName, false);
        if (mDrawItemName) {
            final float density = context.getResources().getDisplayMetrics().density;
            mMaxTextSize = (int)(20 * density);
            mMinTextSize = (int)(12 * density);
            mTextPadding = (int)(8 * density);
            mTextPaint = new TextPaint();
            mTextPaint.setColor(Color.BLACK);
            mTextPaint.setShadowLayer(4.5f * density, 0, 0, Color.WHITE);
            mTextPaint.setTextSize(mMaxTextSize);
            mTextPaint.setTextSkewX(-0.25f);
            mTextPaint.setTypeface(FontManager.getTypeFace(context, FontManager.DEFAULT_FONT_BOLD));
        }
        a.recycle();
    }

    public void setMenuItem(MenuItem menuItem) {
        mItemName = menuItem.name;
        resetTextSize();
        resizeText(getMeasuredWidth() - mTextPadding * 2);
        setImageFilename(menuItem.imageName);
        invalidate();
    }

    public void resetTextSize() {
        mTextPaint.setTextSize(mMaxTextSize);
        mLastResizeWidth = 0;
        mTextNeedsResize = true;
    }

    public void resizeText(final int width) {
        final String text = mItemName;

        if (text == null || text.length() == 0 || width <= 0) {
            return;
        }

        if (width != mLastResizeWidth) {
            mLastResizeWidth = width;
            float targetTextSize = mTextPaint.getTextSize();

            float length = mTextPaint.measureText(text);
            while ((length > width) && (targetTextSize > mMinTextSize)) {
                targetTextSize -= 2;
                mTextPaint.setTextSize(targetTextSize);
                length = mTextPaint.measureText(text);
            }
            final int ascent = (int)Math.ceil(-mTextPaint.ascent());
            final int descent = (int)Math.ceil(mTextPaint.descent());
            mTextHeight = ascent + descent;
        }

        mTextNeedsResize = false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != oldw || h != oldh) {
            mTextNeedsResize = true;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed || mTextNeedsResize) {
            int widthLimit = (right - left) - mTextPadding * 2;
            resizeText(widthLimit);
        }

        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mDrawItemName) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = (int)(width * IMAGE_ASPECT_RATIO);

            if (width > getMeasuredWidth()) {
                resetTextSize();
            }
            resizeText(width - mTextPadding * 2);
            setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mDrawItemName && mItemName != null) {
            canvas.drawText(mItemName, mTextPadding, mTextPadding + mTextHeight, mTextPaint);
        }
    }
}
