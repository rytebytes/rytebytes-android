package com.myrytebytes.rytebytes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myrytebytes.datamodel.MenuItem;
import com.myrytebytes.datamodel.Order;
import com.myrytebytes.widget.MenuItemImageView;

public class MenuItemFragment extends BaseFragment {

	public static final String EXTRA_MENU_ITEM = "menu_item";

	private MenuItem mMenuItem;
	private TextView mTvItemCount;
	private Order mOrder;

	private final OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.btn_step_down:
					mOrder.decrementQuantity(mMenuItem);
					mTvItemCount.setText("" + mOrder.getQuantity(mMenuItem));
					mActivityCallbacks.updateCheckoutBadge();
					break;
				case R.id.btn_step_up:
					mOrder.incrementQuantity(mMenuItem);
					mTvItemCount.setText("" + mOrder.getQuantity(mMenuItem));
					mActivityCallbacks.updateCheckoutBadge();
					break;
			}
		}
	};

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
            //TODO: this isn't actually saved in the instance state anywhere
			mMenuItem = savedInstanceState.getParcelable(EXTRA_MENU_ITEM);
		}

		mOrder = Order.getSharedOrder();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_menu_item, container, false);
		((TextView)rootView.findViewById(R.id.tv_description)).setText(mMenuItem.description);

		String caloriesString = mMenuItem.nutritionInfo.calories != null ? ""+mMenuItem.nutritionInfo.calories : "Unknown";
		((TextView)rootView.findViewById(R.id.tv_calories)).setText(caloriesString);

		String carbsString = mMenuItem.nutritionInfo.carbs != null ? ""+mMenuItem.nutritionInfo.carbs : "Unknown";
		((TextView)rootView.findViewById(R.id.tv_carbs)).setText(carbsString);

		String proteinString = mMenuItem.nutritionInfo.protein != null ? ""+mMenuItem.nutritionInfo.protein : "Unknown";
		((TextView)rootView.findViewById(R.id.tv_protein)).setText(proteinString);

		String sodiumString = mMenuItem.nutritionInfo.sodium != null ? ""+mMenuItem.nutritionInfo.sodium : "Unknown";
		((TextView)rootView.findViewById(R.id.tv_sodium)).setText(sodiumString);

		rootView.findViewById(R.id.btn_step_down).setOnClickListener(mOnClickListener);
		rootView.findViewById(R.id.btn_step_up).setOnClickListener(mOnClickListener);

		mTvItemCount = (TextView)rootView.findViewById(R.id.tv_item_count);

        MenuItemImageView imgMenuItem = (MenuItemImageView)rootView.findViewById(R.id.img_menu_item);
        imgMenuItem.setMenuItem(mMenuItem);

		return rootView;
	}

	@Override
	protected ContentType getContentType() {
		return ContentType.MENU_ITEM;
	}

	@Override
	protected int getTitle() {
		return R.string.menu;
	}

	@Override
	protected void onShown() {
        mTvItemCount.setText("" + mOrder.getQuantity(mMenuItem));
	}
}
