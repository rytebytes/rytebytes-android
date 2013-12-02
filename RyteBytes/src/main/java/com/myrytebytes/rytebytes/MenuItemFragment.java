package com.myrytebytes.rytebytes;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.myrytebytes.datamodel.MenuItem;

import java.io.InputStream;

public class MenuItemFragment extends BaseFragment {

	public static final String EXTRA_MENU_ITEM = "menu_item";

	private MenuItem mMenuItem;
	private ImageView mImgMenuItem;

	public static MenuItemFragment newInstance(MenuItem menuItem) {
		MenuItemFragment fragment = new MenuItemFragment();
		Bundle args = new Bundle();
		args.putParcelable(EXTRA_MENU_ITEM, menuItem);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			savedInstanceState = getArguments();
		}

		if (savedInstanceState != null) {
			mMenuItem = savedInstanceState.getParcelable(EXTRA_MENU_ITEM);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_menu_item, container, false);
		mImgMenuItem = (ImageView)rootView.findViewById(R.id.img_menu_item);
		((TextView)rootView.findViewById(R.id.tv_title)).setText(mMenuItem.name);
		((TextView)rootView.findViewById(R.id.tv_description)).setText(mMenuItem.description);

		try {
			InputStream is = getApplicationContext().getAssets().open("menuimages/" + mMenuItem.imageName);
			Drawable d = Drawable.createFromStream(is, null);
			mImgMenuItem.setImageDrawable(d);
		} catch (Exception e) {
			// We don't have this in the assets folder. Fetch remotely?
		}

		return rootView;
	}

	@Override
	protected ContentType getContentType() {
		return ContentType.MENU_ITEM;
	}

	@Override
	protected int getTitle() {
		return R.string.app_name;
	}

	@Override
	protected void onShown() {

	}
}
