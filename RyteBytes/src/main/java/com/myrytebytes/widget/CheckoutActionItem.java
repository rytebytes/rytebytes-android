package com.myrytebytes.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.myrytebytes.datamanagement.FontManager;
import com.myrytebytes.datamodel.Order;
import com.myrytebytes.rytebytes.R;

public class CheckoutActionItem extends ImageView {

	public interface CheckoutActionItemListener {
		public void onClick();
	}

	private CheckoutActionItemListener mListener;
	private String mItemCountString;

	private final int mSize;
	private final Order mOrder;
	private final TextPaint mTextPaint;
	private final int mTextX;
	private final int mTextY;

	private final OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mListener != null) {
				mListener.onClick();
			}
		}
	};

	private final OnLongClickListener mOnLongClickListener = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			final Context context = getContext();

			final int[] screenPos = new int[2];
			final Rect displayFrame = new Rect();
			getLocationOnScreen(screenPos);
			getWindowVisibleDisplayFrame(displayFrame);

			final int centerY = screenPos[1] + mSize / 2;
			final int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

			Toast toast = Toast.makeText(context, "Checkout", Toast.LENGTH_SHORT);
			if (centerY < displayFrame.height()) {
				toast.setGravity(Gravity.TOP | Gravity.RIGHT, screenWidth - screenPos[0] - mSize / 2, mSize);
			} else {
				toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, mSize);
			}
			toast.show();

			return true;
		}
	};

    public CheckoutActionItem(Context context) {
        this(context, null, 0);
    }

    public CheckoutActionItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CheckoutActionItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

		setImageResource(R.drawable.ic_checkout);

		Resources res = context.getResources();
		final float density = res.getDisplayMetrics().density;
		mOrder = Order.getSharedOrder();
		mSize = res.getDimensionPixelSize(R.dimen.abc_action_bar_default_height);
		mTextX = mSize / 2 - (int)(2 * density);
		mTextY = mSize / 2 + (int)(2 * density);
		mTextPaint = new TextPaint();
		mTextPaint.setTextSize((int)(20 * density));
		mTextPaint.setColor(0xFFFBBF2E);
		mTextPaint.setTypeface(FontManager.getTypeFace(context, FontManager.DEFAULT_FONT_BOLD));
		mTextPaint.setTextAlign(Align.CENTER);

		setClickable(true);
		setLongClickable(true);
		setFocusable(true);
		setOnClickListener(mOnClickListener);
		setOnLongClickListener(mOnLongClickListener);

		updateBadge();
    }

    public void updateBadge() {
		mItemCountString = ""+mOrder.getItemTotal();
        invalidate();
    }

	public void setListener(CheckoutActionItemListener listener) {
		mListener = listener;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(mSize, mSize);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (mItemCountString != null) {
			canvas.drawText(mItemCountString, mTextX, mTextY, mTextPaint);
		}
	}
}
