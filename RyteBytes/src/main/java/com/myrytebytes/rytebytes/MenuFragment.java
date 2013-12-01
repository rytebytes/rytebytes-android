package com.myrytebytes.rytebytes;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.myrytebytes.datamodel.MenuItem;
import com.myrytebytes.remote.ApiInterface;
import com.myrytebytes.remote.ApiListener.GetMenuListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MenuFragment extends BaseFragment {

	private ListView mLvMenu;
	private MenuAdapter mMenuAdapter;
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			MenuItem menuItem = mMenuAdapter.getMenuItem(position);
			pushFragment(MenuItemFragment.newInstance(menuItem), ContentType.MENU_ITEM);
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_menu, container, false);
		mLvMenu = (ListView)rootView.findViewById(R.id.lv_menu);
		mMenuAdapter = new MenuAdapter(inflater.getContext());
		mLvMenu.setAdapter(mMenuAdapter);
		mLvMenu.setOnItemClickListener(mOnItemClickListener);
		return rootView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		refreshMenu();
//		createStripeCustomer();
	}

	@Override
	protected ContentType getContentType() {
		return ContentType.MENU;
	}

	@Override
	protected int getTitle() {
		return R.string.app_name;
	}

	@Override
	protected void onShown() { }

	public void refreshMenu() {
		ApiInterface.getMenu(new GetMenuListener() {
			@Override
			public void onComplete(List<MenuItem> menu, int statusCode) {
				if (menu != null) {
					mMenuAdapter.setMenu(menu);
				}
			}
		});
	}

	private static class MenuAdapter extends BaseAdapter {
		private final LayoutInflater mLayoutInflater;
		private final Context mContext;
		private final List<MenuItem> mMenu;

		public MenuAdapter(Context context) {
			mContext = context;
			mLayoutInflater = LayoutInflater.from(context);
			mMenu = new ArrayList<>();
		}

		@Override
		public int getCount() {
			return mMenu.size();
		}

		@Override
		public Object getItem(int position) {
			return getMenuItem(position);
		}

		public MenuItem getMenuItem(int position) {
			return mMenu.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public void setMenu(List<MenuItem> menu) {
			mMenu.clear();
			mMenu.addAll(menu);
			notifyDataSetChanged();
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final MenuItem item = mMenu.get(position);

			if (convertView == null) {
				ViewHolder viewHolder = new ViewHolder();

				convertView = mLayoutInflater.inflate(R.layout.row_menu_item, parent, false);
				viewHolder.imageView = (ImageView)convertView.findViewById(R.id.img_menu_item);

				convertView.setTag(viewHolder);
			}

			ViewHolder holder = (ViewHolder)convertView.getTag();

			try {
				InputStream is = mContext.getAssets().open("menuimages/" + item.image);
				Drawable d = Drawable.createFromStream(is, null);
				holder.imageView.setImageDrawable(d);
			} catch (Exception e) {
				// We don't have this in the assets folder. Fetch remotely?
			}

			return convertView;
		}

		public static class ViewHolder {
			public ImageView imageView;
		}
	}
}
