package com.myrytebytes.widget;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.ImageView;

public class FlipImageView extends ImageView {

	private static final int ANIMATION_DURATION = 300;
	private FlipAnimator mAnimation;

	public FlipImageView(Context context) {
		super(context);
		init();
	}

	public FlipImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public FlipImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		mAnimation = new FlipAnimator();
		mAnimation.setInterpolator(new DecelerateInterpolator());
		mAnimation.setDuration(ANIMATION_DURATION);
	}

	public void toggleFlip(int newRes) {
		mAnimation.setNewRes(newRes);
		startAnimation(mAnimation);
	}

	public class FlipAnimator extends Animation {
		private Camera camera;
		private int newRes;
		private float centerX;
		private float centerY;
		private boolean visibilitySwapped;

		public void setNewRes(int res) {
			newRes = res;
			visibilitySwapped = false;
		}

		public FlipAnimator() {
			setFillAfter(true);
		}

		@Override
		public void initialize(int width, int height, int parentWidth, int parentHeight) {
			super.initialize(width, height, parentWidth, parentHeight);
			camera = new Camera();
			centerX = width / 2;
			centerY = height / 2;
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			final double radians = Math.PI * interpolatedTime;
			float degrees = (float)(180.0 * radians / Math.PI);

			if (interpolatedTime >= 0.5f) {
				degrees -= 180;

				if (!visibilitySwapped) {
					visibilitySwapped = true;
					setImageResource(newRes);
				}
			}

			final Matrix matrix = t.getMatrix();

			camera.save();
			camera.translate(0.0f, 0.0f, (float) (150.0 * Math.sin(radians)));
			camera.rotateY(degrees);
			camera.getMatrix(matrix);
			camera.restore();

			matrix.preTranslate(-centerX, -centerY);
			matrix.postTranslate(centerX, centerY);
		}
	}
}

