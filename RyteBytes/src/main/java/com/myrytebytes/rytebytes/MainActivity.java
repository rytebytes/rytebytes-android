package com.myrytebytes.rytebytes;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.myrytebytes.datamanagement.UserController;
import com.myrytebytes.rytebytes.BaseFragment.ActivityCallbacks;
import com.myrytebytes.rytebytes.BaseFragment.ContentType;
import com.myrytebytes.widget.CheckoutActionItem;
import com.myrytebytes.widget.CheckoutActionItem.CheckoutActionItemListener;

public class MainActivity extends ActionBarActivity implements ActivityCallbacks {

	private BaseFragment mContent;
	private int mBackstackEntryCount;
    private int mCurrentTitle;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawer;
	private ActionBarDrawerToggle mDrawerToggle;
	private CheckoutActionItem mCheckoutActionItem;
	private boolean isDrawerOpen;
	private InputMethodManager mInputMethodManager;
	private PostLoginContainer mPostLoginContainer;
	private boolean isLoggingIn;

	private CheckoutActionItemListener mCheckoutActionItemListener = new CheckoutActionItemListener() {
		@Override
		public void onClick() {
			pushFragment(CheckoutFragment.newInstance(), ContentType.CHECKOUT);
		}
	};

	private FragmentManager.OnBackStackChangedListener mOnBackStackChangedListener = new FragmentManager.OnBackStackChangedListener() {
		@Override
		public void onBackStackChanged() {
			FragmentManager fragmentManager = getSupportFragmentManager();
			final int newBackstackEntryCount = fragmentManager.getBackStackEntryCount();

			if (isLoggingIn) {
				if (newBackstackEntryCount < mBackstackEntryCount || newBackstackEntryCount == 0) {
					isLoggingIn = false;
					if (mPostLoginContainer != null && mPostLoginContainer.popIfUnsuccessful && UserController.getActiveUser() == null) {
						BaseFragment.animationsDisabled = true;
						fragmentManager.popBackStackImmediate();
						BaseFragment.animationsDisabled = false;
					}
					mPostLoginContainer = null;
				}
			}

			if (newBackstackEntryCount < mBackstackEntryCount) {
				BaseFragment fragment = (BaseFragment)fragmentManager.findFragmentById(R.id.container);
				if (fragment.getContentType().isModal) {
					configureUIForModalFragment(fragment);
				} else {
					setContent(fragment);
				}
				mContent.onShown();
			}

			mBackstackEntryCount = newBackstackEntryCount;
		}
	};
	private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			NavigationDrawerItem item = (NavigationDrawerItem)parent.getAdapter().getItem(position);
			switchContent(item.contentType, false);
			mDrawerLayout.closeDrawers();
		}
	};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.activity_main);

		mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.LEFT);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.open_drawer, R.string.close_drawer) {
			@Override
			public void onDrawerSlide(View drawerView, float slideOffset) {
				super.onDrawerSlide(drawerView, slideOffset);
				if (slideOffset < 0.1) {
					if (isDrawerOpen) {
						isDrawerOpen = false;
						setTitle(mCurrentTitle);
						supportInvalidateOptionsMenu();
					}
				} else if (!isDrawerOpen) {
					isDrawerOpen = true;
					getSupportActionBar().setTitle(R.string.app_name);
					supportInvalidateOptionsMenu();
					hideSoftKeyboard();
					mDrawerLayout.bringChildToFront(mDrawer);
				}
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		mDrawer = (ListView)mDrawerLayout.findViewById(R.id.drawer);
		mDrawer.setAdapter(new NavigationDrawerAdapter(this));
		mDrawer.setOnItemClickListener(mOnItemClickListener);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

		if (savedInstanceState == null) {
            if (UserController.getPickupLocation() == null) {
                switchContent(ContentType.INTRO, true);
            } else {
                switchContent(ContentType.MENU, true);
            }
		} else {
			mBackstackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
		}

		getSupportFragmentManager().addOnBackStackChangedListener(mOnBackStackChangedListener);
    }

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.checkout, menu);
		mCheckoutActionItem = ((CheckoutActionItem)MenuItemCompat.getActionView(menu.findItem(R.id.btn_checkout)));
		mCheckoutActionItem.setListener(mCheckoutActionItemListener);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return mDrawerToggle.onOptionsItemSelected(item);
	}

	@Override
	public void onConfigurationChanged(android.content.res.Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public void onAttachFragment(Fragment fragment) {
		super.onAttachFragment(fragment);

		BaseFragment baseFragment = (BaseFragment)fragment;
		if (baseFragment.getContentType().isModal) {
			configureUIForModalFragment(baseFragment);
		} else if (fragment.getId() == R.id.container) {
			setContent(baseFragment);
		}
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.setGroupVisible(0, !isDrawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void pushFragment(ContentType contentType, Bundle extras) {
		pushFragment(null, contentType, extras);
	}

	@Override
	public void pushFragment(BaseFragment fragment, ContentType contentType) {
		pushFragment(fragment, contentType, null);
	}

	private void pushFragment(BaseFragment fragment, ContentType fragmentType, Bundle extras) {
		if (fragmentType.requiresLogin && UserController.getActiveUser() == null) {
			displayLoginFragment(new PostLoginContainer(fragment, fragmentType, extras, false, false));
		} else if (mContent == null || fragmentType != mContent.getContentType()) {
			if (fragment == null) {
				fragment = getFragmentForType(fragmentType);
			}

			if (extras != null) {
				fragment.setArguments(extras);
			}

			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.setCustomAnimations(R.anim.fragment_push_enter, R.anim.fragment_push_exit, R.anim.fragment_pop_enter, R.anim.fragment_pop_exit);

			if (mContent != null) {
				transaction.hide(mContent);
			}
			transaction.add(R.id.container, fragment);

			if (mContent != null) {
				transaction.addToBackStack(null);
			}

			transaction.commitAllowingStateLoss();
		}
	}

	@Override
	public void displayModalFragment(ContentType contentType, Bundle extras) {
		BaseFragment modalFragment = getFragmentForType(contentType);

		if (extras != null) {
			modalFragment.setArguments(extras);
		}

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.setCustomAnimations(R.anim.fragment_modal_enter, 0, 0, R.anim.fragment_modal_exit);
		transaction.add(R.id.container, modalFragment);
		transaction.addToBackStack(null);
		transaction.commitAllowingStateLoss();
	}

    @Override
    public void replaceContent(ContentType contentType) {
        BaseFragment fragment = getFragmentForType(contentType);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(0, 0, 0, R.anim.fragment_pop_exit_root);

        transaction.replace(R.id.container, fragment);

        transaction.commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();
    }

	public void switchContent(ContentType contentType, boolean onLaunch) {
		if (contentType.requiresLogin && UserController.getActiveUser() == null) {
			displayLoginFragment(new PostLoginContainer(null, contentType, null, true, false));
		} else if (mContent == null || contentType != mContent.getContentType()) {
			if (contentType == ContentType.MENU && mContent != null) {
				popToRoot(false);
			} else {
				BaseFragment fragment = getFragmentForType(contentType);

				FragmentManager fragmentManager = getSupportFragmentManager();
				FragmentTransaction transaction = fragmentManager.beginTransaction();
				transaction.setCustomAnimations(0, 0, 0, R.anim.fragment_pop_exit_root);

				if (mContent != null) {
					transaction.hide(mContent);
				}
				transaction.add(R.id.container, fragment);
				if (mContent != null) {
					transaction.addToBackStack(null);
				}

				if (!onLaunch) {
					if (fragmentManager.getBackStackEntryCount() > 0) {
						popToRoot(false);
						Fragment rootFragment = fragmentManager.getFragments().get(0);
						if (rootFragment != null) {
							transaction.hide(rootFragment);
						}
					}
				}

				transaction.commitAllowingStateLoss();
				fragmentManager.executePendingTransactions();
			}
		}
	}

	@Override
	public void updateCheckoutBadge() {
		mCheckoutActionItem.updateBadge();
	}

	private void configureUIForModalFragment(BaseFragment fragment) {
		if (mDrawerLayout != null) {
			mContent = fragment;
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, mDrawer);
			getSupportActionBar().hide();
		} else {
			new Handler().post(new BaseFragmentRunnable(fragment) {
				@Override
				public void run() {
					configureUIForModalFragment(baseFragment);
				}
			});
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		boolean handled = false;
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (mDrawerLayout.getDrawerLockMode(Gravity.LEFT) == DrawerLayout.LOCK_MODE_UNLOCKED) {
				if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
					mDrawerLayout.closeDrawer(Gravity.LEFT);
				} else {
					mDrawerLayout.openDrawer(Gravity.LEFT);
				}
			}
			handled = true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK && mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
			mDrawerLayout.closeDrawer(Gravity.LEFT);
			handled = true;
		} else if (mContent != null && mContent.onKeyUp(keyCode, event)) {
			handled = true;
		}

		return handled || super.onKeyUp(keyCode, event);
	}

	@Override
	public void loginWillFinish(boolean loggedIn) {
		// First dismiss the login fragment
		((BaseFragment)getSupportFragmentManager().findFragmentById(R.id.container)).finish();

		// Now add the view if successful
		if (mPostLoginContainer != null) {
			if (loggedIn) {
				if (mPostLoginContainer.contentType != null) {
					if (mPostLoginContainer.isSwitch) {
						switchContent(mPostLoginContainer.contentType, false);
					} else {
						pushFragment(mPostLoginContainer.fragment, mPostLoginContainer.contentType, mPostLoginContainer.extras);
					}
				}
			} else if (mPostLoginContainer.popIfUnsuccessful) {
				getSupportFragmentManager().popBackStack();
			}
		}
	}

	/*package*/ void setContent(BaseFragment content) {
		if (mDrawerLayout != null) {
			if (mContent != content) {
				setTitle(content.getTitle());
				configureForContentType(content.getContentType());

				mContent = content;
				mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, mDrawer);
			}
		} else {
			new Handler().post(new BaseFragmentRunnable(content) {
				@Override
				public void run() {
					setContent(baseFragment);
				}
			});
		}
	}

	@Override
	public void setTitle(int title) {
		mCurrentTitle = title;
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			if (title != 0) {
				actionBar.setTitle(title);
			} else {
				actionBar.setTitle("");
			}
		}
	}

	public void configureForContentType(ContentType contentType) {
		if (mDrawerLayout == null) {
			return;
		}

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			if (contentType.isModal) {
				if (actionBar.isShowing()) {
					actionBar.hide();
				}
			} else {
				if (!actionBar.isShowing()) {
					actionBar.show();
				}
			}
		}
	}

	@Override
	public void popToRoot(boolean animated) {
		FragmentManager fm = getSupportFragmentManager();
		if (fm.getBackStackEntryCount() > 0) {
			if (!animated) {
				BaseFragment.animationsDisabled = true;
			}
			fm.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
			BaseFragment.animationsDisabled = false;
		}
	}

	@Override
	public boolean isLoginFragmentShowing() {
		return isLoggingIn;
	}

	private BaseFragment getFragmentForType(ContentType fragmentType) {
		switch (fragmentType) {
            case INTRO:
                return new IntroFragment();
			case MENU:
				return new MenuFragment();
			case MENU_ITEM:
				return new MenuItemFragment();
			case ACCOUNT:
				return new AccountFragment();
			case DO_RYTE:
				return new DoRyteFragment();
			case HEATING:
				return new HeatingFragment();
			case CHECKOUT:
				return new CheckoutFragment();
            case PICKUP_LOCATIONS:
                return new PickupLocationsFragment();
            case CHANGE_CREDIT_CARD:
                return new ChangeCreditCardFragment();
			default:
				return null;
		}
	}

	public void displayLoginFragment(PostLoginContainer postLoginContainer) {
		mPostLoginContainer = postLoginContainer;
		LoginFragment loginFragment = new LoginFragment();
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.setCustomAnimations(R.anim.fragment_modal_enter, 0, 0, R.anim.fragment_modal_exit);
		transaction.add(R.id.container, loginFragment);
		transaction.addToBackStack(null);
		transaction.commitAllowingStateLoss();
		isLoggingIn = true;
	}

	@Override
	public void displayLoginFragment(boolean popIfUnsuccessful) {
		displayLoginFragment(new PostLoginContainer(null, null, null, false, popIfUnsuccessful));
	}

	@Override
	public void hideSoftKeyboard() {
		if (mInputMethodManager == null) {
			mInputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		}

		final View currentFocus = getCurrentFocus();
		if (currentFocus != null) {
			mInputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
		}
	}

	private static abstract class BaseFragmentRunnable implements Runnable {
		public BaseFragment baseFragment;

		public BaseFragmentRunnable(BaseFragment fragment) {
			this.baseFragment = fragment;
		}
	}

	private static class NavigationDrawerItem {
		public int title;
		public int iconRes;
		public boolean isHeader;
		public ContentType contentType;

		public static NavigationDrawerItem menuItem(int title, int iconRes, ContentType contentType) {
			NavigationDrawerItem item = new NavigationDrawerItem();
			item.title = title;
			item.iconRes = iconRes;
			item.contentType = contentType;
			return item;
		}

		public static NavigationDrawerItem headerItem(int title) {
			NavigationDrawerItem item = new NavigationDrawerItem();
			item.title = title;
			item.isHeader = true;
			return item;
		}
	}

	private static class NavigationDrawerAdapter extends BaseAdapter {
		private static final NavigationDrawerItem[] menu = new NavigationDrawerItem[] {
				NavigationDrawerItem.headerItem(R.string.order),
				NavigationDrawerItem.menuItem(R.string.eat_ryte, R.drawable.ic_menu_eat_ryte, ContentType.MENU),
				NavigationDrawerItem.headerItem(R.string.account),
				NavigationDrawerItem.menuItem(R.string.my_account, R.drawable.ic_menu_settings, ContentType.ACCOUNT),
				NavigationDrawerItem.headerItem(R.string.about),
				NavigationDrawerItem.menuItem(R.string.heating, R.drawable.ic_menu_heating, ContentType.HEATING),
				NavigationDrawerItem.menuItem(R.string.do_ryte, R.drawable.ic_menu_do_ryte, ContentType.DO_RYTE),
		};

		private final LayoutInflater mLayoutInflater;

		public NavigationDrawerAdapter(Context context) {
			mLayoutInflater = LayoutInflater.from(context);
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public int getItemViewType(int position) {
			if (menu[position].isHeader) {
				return 0;
			} else {
				return 1;
			}
		}

		@Override
		public int getCount() {
			return menu.length;
		}

		@Override
		public Object getItem(int position) {
			return menu[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public boolean isEnabled(int position) {
			return !menu[position].isHeader;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final NavigationDrawerItem item = menu[position];

			if (convertView == null) {
				ViewHolder viewHolder = new ViewHolder();

				if (item.isHeader) {
					convertView = mLayoutInflater.inflate(R.layout.row_drawer_header, parent, false);
					viewHolder.textView = (TextView)convertView.findViewById(R.id.title);
				} else {
					convertView = mLayoutInflater.inflate(R.layout.row_drawer_menu_item, parent, false);
					viewHolder.imageView = (ImageView)convertView.findViewById(R.id.icon);
					viewHolder.textView = (TextView)convertView.findViewById(R.id.title);
				}

				convertView.setTag(viewHolder);
			}

			ViewHolder holder = (ViewHolder)convertView.getTag();
			if (item.title != 0) {
				holder.textView.setText(item.title);
			}
			if (item.iconRes != 0) {
				holder.imageView.setImageResource(item.iconRes);
			}

			return convertView;
		}

		public static class ViewHolder {
			public TextView textView;
			public ImageView imageView;
		}
	}

	private static class PostLoginContainer {
		public BaseFragment fragment;
		public ContentType contentType;
		public Bundle extras;
		public boolean isSwitch;
		public boolean popIfUnsuccessful;

		private PostLoginContainer(BaseFragment fragment, ContentType contentType, Bundle extras, boolean isSwitch, boolean popIfUnsuccessful) {
			this.fragment = fragment;
			this.contentType = contentType;
			this.extras = extras;
			this.isSwitch = isSwitch;
			this.popIfUnsuccessful = popIfUnsuccessful;
		}
	}
}
