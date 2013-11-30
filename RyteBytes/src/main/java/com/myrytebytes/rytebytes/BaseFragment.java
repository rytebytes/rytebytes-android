package com.myrytebytes.rytebytes;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.animation.Animation;

import com.myrytebytes.widget.HoloDialog;

public abstract class BaseFragment extends Fragment {

	public enum ContentType {
		MENU(false),
		MENU_ITEM(false);

		boolean isModal;

		private ContentType(boolean isModal) {
			this.isModal = isModal;
		}
	}

	public interface ActivityCallbacks {
		public void pushFragment(ContentType contentType, Bundle extras);
		public void pushFragment(BaseFragment fragment, ContentType fragmentType);
		public void displayModalFragment(ContentType contentType, Bundle extras);
		public void setTitle(int title);
		public void popToRoot(boolean animated, boolean resetRoot);
		public void openNavDrawer();
		public void hideSoftKeyboard();
	}

	private static final ActivityCallbacks DUMMY_CALLBACKS = new ActivityCallbacks() {
		@Override
		public void pushFragment(ContentType contentType, Bundle extras) { }

		@Override
		public void pushFragment(BaseFragment fragment, ContentType fragmentType) { }

		@Override
		public void displayModalFragment(ContentType contentType, Bundle extras) { }

		@Override
		public void setTitle(int title) { }

		@Override
		public void popToRoot(boolean animated, boolean resetRoot) { }

		@Override
		public void openNavDrawer() { }

		@Override
		public void hideSoftKeyboard() { }
	};

	public static boolean animationsDisabled;
	protected ActivityCallbacks mActivityCallbacks = DUMMY_CALLBACKS;
	private Context mContext;
	protected boolean isAttached;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		isAttached = true;
		mContext = activity.getApplicationContext();
		mActivityCallbacks = (ActivityCallbacks)activity;
	}

	@Override
	public void onResume() {
		super.onResume();

		if (getTitle() != 0) {
			mActivityCallbacks.setTitle(getTitle());
		}

		onShown();
	}

	@Override
	public void onDetach() {
		isAttached = false;
		super.onDetach();
		mActivityCallbacks = DUMMY_CALLBACKS;
	}

	@Override
	public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
		if (animationsDisabled) {
			Animation a = new Animation() {};
			a.setDuration(0);
			return a;
		} else {
			return super.onCreateAnimation(transit, enter, nextAnim);
		}
	}

	public void hideSoftKeyboard() {
		mActivityCallbacks.hideSoftKeyboard();
	}

	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return false;
	}

	protected void finish() {
		FragmentActivity activity = getActivity();
		if (activity != null) {
			FragmentManager fragmentManager = activity.getSupportFragmentManager();
			if (fragmentManager.getBackStackEntryCount() == 0) {
				activity.finish();
			} else {
				try {
					fragmentManager.popBackStack();
				} catch (Exception e) { }
			}
		}
	}

	protected void pushFragment(ContentType fragmentType, Bundle extras) {
		mActivityCallbacks.pushFragment(fragmentType, extras);
	}

	protected void pushFragment(BaseFragment fragment, ContentType fragmentType) {
		mActivityCallbacks.pushFragment(fragment, fragmentType);
	}

	protected void displayModalFragment(ContentType fragmentType, Bundle extras) {
		mActivityCallbacks.displayModalFragment(fragmentType, extras);
	}

	protected Context getApplicationContext() {
		return mContext;
	}

	/*package*/ void showOkDialog(String title, String message) {
		if (isAttached) {
			new HoloDialog.Builder(getActivity())
					.setTitle(title)
					.setMessage(message)
					.setPositiveButton(android.R.string.ok, null)
					.show();
		}
	}

	protected abstract ContentType getContentType();
	protected abstract int getTitle();
	protected abstract void onShown();
}
