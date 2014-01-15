package com.myrytebytes.rytebytes;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.myrytebytes.datamanagement.Log;
import com.myrytebytes.datamanagement.SQLiteCursorLoader;
import com.myrytebytes.datamanagement.UserController;
import com.myrytebytes.datamodel.Location;
import com.myrytebytes.datamodel.MenuItem;
import com.myrytebytes.remote.ApiInterface;
import com.myrytebytes.remote.ApiListener.GetMenuListener;
import com.myrytebytes.widget.MenuItemImageView;

public class MenuFragment extends BaseFragment {

	private ListView mLvMenu;
	private MenuAdapter mMenuAdapter;
	private SQLiteCursorLoader mMenuLoader;
    private Location mLocation;
	private boolean isRemoteMenuLoaded;

	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Cursor cursor = (Cursor)mMenuAdapter.getItem(position);
			if (cursor != null) {
				MenuItem menuItem = new MenuItem(cursor);
				pushFragment(MenuItemFragment.newInstance(menuItem), ContentType.MENU_ITEM);
			}
		}
	};

	private final LoaderManager.LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
		@Override
		public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
			mMenuLoader = new SQLiteCursorLoader(getApplicationContext());
			mMenuLoader.setQuery(MenuItem.Columns.TABLE_NAME, null, null, null, null, null, null);
			return mMenuLoader;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
			mMenuAdapter.swapCursor(cursor);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> cursorLoader) {
			mMenuAdapter.swapCursor(null);
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
		if (!isRemoteMenuLoaded) {
            if (UserController.getActiveUser() != null) {
                mLocation = UserController.getActiveUser().location;
            }
			refreshMenu();
		}
	}

	@Override
	protected ContentType getContentType() {
		return ContentType.MENU;
	}

	@Override
	protected int getTitle() {
		return R.string.menu;
	}

	@Override
	protected void onShown() {
		getLoaderManager().initLoader(1, null, mLoaderCallbacks);
        if (UserController.getActiveUser() != null) {
            if (!UserController.getActiveUser().location.equals(mLocation)) {
                Log.d("onContentChanged");
                mLocation = UserController.getActiveUser().location;
                mMenuLoader.onContentChanged();
            }
        }
	}

	public void refreshMenu() {
		ApiInterface.getMenu(new GetMenuListener() {
			@Override
			public void onComplete(boolean success, int statusCode) {
				if (success) {
					isRemoteMenuLoaded = true;
				}
				mMenuLoader.onContentChanged();
			}
		});
	}

	private static class MenuAdapter extends CursorAdapter {
		private final LayoutInflater mLayoutInflater;

		public MenuAdapter(Context context) {
			super(context, null, 0);
			mLayoutInflater = LayoutInflater.from(context);
		}

		@Override
		public int getViewTypeCount() {
			return 1;
		}

		@Override
		public int getItemViewType(int position) {
			return 1;
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
			ViewHolder viewHolder = new ViewHolder();
			View view = mLayoutInflater.inflate(R.layout.row_menu_item, viewGroup, false);
			viewHolder.imageView = (MenuItemImageView)view.findViewById(R.id.img_menu_item);
			view.setTag(viewHolder);
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor c) {
			ViewHolder holder = (ViewHolder)view.getTag();

			MenuItem menuItem = new MenuItem();
			menuItem.imageName = c.getString(c.getColumnIndex(MenuItem.Columns.IMAGE));
			menuItem.imageResourceId = c.getInt(c.getColumnIndex(MenuItem.Columns.IMAGE_RES_ID));
            menuItem.name = c.getString(c.getColumnIndex(MenuItem.Columns.NAME));
            holder.imageView.clearImage();
			holder.imageView.setMenuItem(menuItem);
		}

		public static class ViewHolder {
			public MenuItemImageView imageView;
		}
	}
}
