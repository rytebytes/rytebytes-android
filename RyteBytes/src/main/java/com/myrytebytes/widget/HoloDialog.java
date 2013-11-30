package com.myrytebytes.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.myrytebytes.rytebytes.R;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HoloDialog extends Dialog {

	public enum ListStyle {
		LISTVIEW,
		SINGLE_CHOICE,
		MULTI_CHOICE
	}

	private Builder mBuilder;
	private ListAdapter mAdapter;

	// Title View
	private RelativeLayout mTitleLayout;
	private AutoResizeTextView mTvTitle;
	private ImageView mImgTitleIcon;

	// Main Content Views
	private RelativeLayout mDialogLayout;

	// Dialog Buttons
	private View mDialogButtonDivider;
	private LinearLayout mDialogButtonLayout;
	private Button mBtnNegative;
	private Button mBtnNeutral;
	private Button mBtnPositive;
	private View mNegativeButtonDivider;
	private View mPositiveButtonDivider;

	private InstanceStateListener mInstanceStateListener;

	private boolean mManualDismissOnPositiveButton;
	private boolean mManualDismissOnNegativeButton;
	private boolean mManualDismissOnNeutralButton;

	public HoloDialog(Builder builder) {
		super(builder.mContext, R.style.Theme_Dialog);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		build(builder);
	}

	public static Dialog showProgressDialog(Context context, String title, String message) {
		return showProgressDialog(context, title, message, true, null);
	}

	public static Dialog showProgressDialog(Context context, String title, String message, boolean cancelable) {
		return showProgressDialog(context, title, message, cancelable, null);
	}

	public static Dialog showProgressDialog(Context context, String title, String message, boolean cancelable, OnCancelListener cancelListener) {
		return new Builder(context)
				.setTitle(title)
				.setCancelable(cancelable)
				.setOnCancelListener(cancelListener)
				.setIndeterminateProgress(message)
				.show();
	}

	public void build(Builder builder) {
		mBuilder = builder;
		setContentView(R.layout.dialog_base);

		mTitleLayout = (RelativeLayout)findViewById(R.id.layout_dialog_title);
		mTvTitle = (AutoResizeTextView)findViewById(R.id.tv_title);
		mImgTitleIcon = (ImageView)findViewById(R.id.img_title_icon);
		mDialogLayout = (RelativeLayout)findViewById(R.id.layout_dialog_view);
		mDialogButtonDivider = findViewById(R.id.dialog_button_divider);
		mDialogButtonLayout = (LinearLayout)findViewById(R.id.dialog_buttons);
		mBtnNegative = (Button)findViewById(R.id.btn_negative);
		mBtnNeutral = (Button)findViewById(R.id.btn_neutral);
		mBtnPositive = (Button)findViewById(R.id.btn_positive);
		mNegativeButtonDivider = findViewById(R.id.negative_button_divider);
		mPositiveButtonDivider = findViewById(R.id.positive_button_divider);

		setCancelable(builder.mCancelable);
		setCanceledOnTouchOutside(builder.mCancelableOutsideTouch);

		if (mBuilder.mOnKeyListener != null) {
			setOnKeyListener(mBuilder.mOnKeyListener);
		}

		if (mBuilder.mOnCancelListener != null) {
			setOnCancelListener(mBuilder.mOnCancelListener);
		}

		if (mBuilder.mOnShowListener != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			setOnShowListener(mBuilder.mOnShowListener);
		}

		if (mBuilder.mOnDismissListener != null) {
			setOnDismissListener(mBuilder.mOnDismissListener);
		}

		mInstanceStateListener = mBuilder.mInstanceStateListener;

		setTitleView();
		setDialogView();
		setButtonView();
	}

	private void setTitleView() {
		if (mBuilder.mCustomTitleView != null) {
			setCustomTitle(mBuilder.mCustomTitleView);
		} else if (mBuilder.mTitleIcon == null && mBuilder.mTitleText == null) {
			removeTitle();
			hideTitle();
		} else {
			if (mBuilder.mTitleIcon != null) {
				setIcon(mBuilder.mTitleIcon);
			} else {
				mImgTitleIcon.setVisibility(View.GONE);
			}

			setTitle(mBuilder.mTitleText);
		}
	}

	private void setDialogView() {
		if (mBuilder.mMainDialogView != null) {
			setDialogView(mBuilder.mMainDialogView);
		} else if (mBuilder.mMainDialogLayoutRes != 0) {
			setDialogView(mBuilder.mMainDialogLayoutRes);
		} else {
			if (mBuilder.mDialogMessage != null) {
				LayoutInflater.from(new ContextThemeWrapper(getContext(), R.style.Theme_RyteBytes)).inflate(R.layout.dialog_message_layout, mDialogLayout, true);
				((TextView)mDialogLayout.findViewById(R.id.tv_message)).setText(mBuilder.mDialogMessage);
			} else if (mBuilder.mShowIndeterminateProgress) {
				LayoutInflater.from(new ContextThemeWrapper(getContext(), R.style.Theme_RyteBytes)).inflate(R.layout.dialog_indeterminate_progress_layout, mDialogLayout, true);
				((TextView)mDialogLayout.findViewById(R.id.tv_message)).setText(mBuilder.mProgressMessage);
			} else if (mBuilder.mListItems != null) {
				mAdapter = new ListAdapter(mBuilder);
				LayoutInflater.from(new ContextThemeWrapper(getContext(), R.style.Theme_RyteBytes)).inflate(R.layout.dialog_list_layout, mDialogLayout, true);
				ListView listView = (ListView)mDialogLayout.findViewById(R.id.list_view);
				listView.setFastScrollEnabled(mBuilder.mSetFastScrollEnabled);
				listView.setAdapter(mAdapter);
				listView.setOnItemClickListener(mOnItemClickListener);
			}
		}
	}

	private void setButtonView() {
		setButtonEnabledState(BUTTON_NEGATIVE, mBuilder.mNegativeButtonEnabledState);
		setButtonEnabledState(BUTTON_NEUTRAL, mBuilder.mNeutralButtonEnabledState);
		setButtonEnabledState(BUTTON_POSITIVE, mBuilder.mPositiveButtonEnabledState);

		if (mBuilder.mNegativeButtonText != null) {
			setNegativeButton(mBuilder.mNegativeButtonText, mBuilder.mNegativeButtonClickListener);
		}

		if (mBuilder.mNeutralButtonText != null) {
			setNeutralButton(mBuilder.mNeutralButtonText, mBuilder.mNeutralButtonClickListener);
		}

		if (mBuilder.mPositiveButtonText != null) {
			setPositiveButton(mBuilder.mPositiveButtonText, mBuilder.mPositiveButtonClickListener);
		}

		setButtonDividers();
	}

	public void setBackground(Drawable background) {
		View rootView = findViewById(android.R.id.content).getRootView();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			rootView.setBackground(background);
		} else {
			//noinspection deprecation
			rootView.setBackgroundDrawable(background);
		}
	}

	public void setBackground(int resid) {
		findViewById(android.R.id.content).getRootView().setBackgroundResource(resid);
	}

	public void removeTitle() {
		mTitleLayout.removeAllViews();
	}

	public void hideTitle() {
		mTitleLayout.setVisibility(View.GONE);
	}

	public void showTitle() {
		mTitleLayout.setVisibility(View.VISIBLE);
	}

	public void setCustomTitle(View view) {
		mTitleLayout.removeAllViews();
		mTitleLayout.addView(view);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTvTitle.setText(title.toString());
	}

	@Override
	public void setTitle(int stringId) {
		setTitle(getContext().getString(stringId));
	}

	public void setIcon(Drawable icon) {
		mImgTitleIcon.setImageDrawable(icon);
	}

	public void setIcon(int resId) {
		mImgTitleIcon.setImageResource(resId);
	}

	public void setDialogView(View view) {
		mDialogLayout.addView(view);
	}

	public void setDialogView(int layoutRes) {
		LayoutInflater.from(new ContextThemeWrapper(getContext(), R.style.Theme_RyteBytes)).inflate(layoutRes, mDialogLayout, true);
	}

	public void addListItems(List<ListItem> listItems) {
		if (mAdapter != null) {
			mAdapter.getListItems().addAll(listItems);
			mAdapter.notifyDataSetChanged();
		}
	}

	public void addListItems(String[] labels) {
		if (mAdapter != null) {
			List<ListItem> items = new ArrayList<>();
			for (String label : labels) {
				items.add(new ListItem(label));
			}
			mAdapter.getListItems().addAll(items);
			mAdapter.notifyDataSetChanged();
		}
	}

	public void setManualDismissOnNeutralButton(boolean manualDismissOnNeutralButton) {
		mManualDismissOnNeutralButton = manualDismissOnNeutralButton;
	}

	public void setManualDismissOnPositiveButton(boolean manualDismissOnPositiveButton) {
		mManualDismissOnPositiveButton = manualDismissOnPositiveButton;
	}

	public void setManualDismissOnNegativeButton(boolean manualDismissOnNegativeButton) {
		mManualDismissOnNegativeButton = manualDismissOnNegativeButton;
	}

	public View getCustomView() {
		return mDialogLayout.getChildAt(0);
	}

	public List<ListItem> getListItems() {
		if (mAdapter == null) return null;
		return mAdapter.getListItems();
	}

	public ListItem getListItem(int position) {
		if (mAdapter != null) {
			return mAdapter.getItem(position);
		}
		return null;
	}

	public List<ListItem> getCheckedItems() {
		List<ListItem> items = new ArrayList<>();
		if (mAdapter != null) {
			for (ListItem item : mAdapter.getListItems()) {
				if (item.checked) {
					items.add(item);
				}
			}
		}
		return items;
	}

	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (mBuilder.mListStyle == ListStyle.SINGLE_CHOICE) {
				for (ListItem listItem : getListItems()) {
					listItem.checked = false;
				}

				ListItem listItem = mAdapter.getItem(position);
				listItem.checked = true;
				mAdapter.notifyDataSetChanged();

				if (mBuilder.mOnItemClickListener != null) {
					mBuilder.mOnItemClickListener.onClick(HoloDialog.this, position);
				}

				dismiss();
			} else {
				ListItem listItem = mAdapter.getItem(position);
				if (listItem.checked != null) {
					listItem.checked = !listItem.checked;
					mAdapter.notifyDataSetChanged();
				}

				if (mBuilder.mListStyle == ListStyle.MULTI_CHOICE) {
					if (mBuilder.mOnMultiChoiceClickListener != null) {
						mBuilder.mOnMultiChoiceClickListener.onClick(HoloDialog.this, position, listItem.checked);
					}
				} else {
					if (mBuilder.mOnItemClickListener != null) {
						mBuilder.mOnItemClickListener.onClick(HoloDialog.this, position);
					}
					dismiss();
				}
			}
		}

	};

	private void setButtonDividers() {
		int numButtons = 0;
		Button[] buttons = {mBtnNegative, mBtnNeutral, mBtnPositive
		};

		for (Button button : buttons) {
			if (button.getVisibility() == View.VISIBLE) {
				numButtons++;
			}
		}

		if (numButtons > 0 && mDialogButtonLayout.getVisibility() != View.VISIBLE) {
			mDialogButtonLayout.setVisibility(View.VISIBLE);
		}

		switch (numButtons) {
		case 0:
			mDialogButtonDivider.setVisibility(View.GONE);
			mNegativeButtonDivider.setVisibility(View.GONE);
			mPositiveButtonDivider.setVisibility(View.GONE);
			break;
		case 1:
			mDialogButtonDivider.setVisibility(View.VISIBLE);
			mNegativeButtonDivider.setVisibility(View.GONE);
			mPositiveButtonDivider.setVisibility(View.GONE);
			break;
		case 2:
			mDialogButtonDivider.setVisibility(View.VISIBLE);
			mNegativeButtonDivider.setVisibility(View.VISIBLE);
			mPositiveButtonDivider.setVisibility(View.GONE);
			break;
		case 3:
			mDialogButtonDivider.setVisibility(View.VISIBLE);
			mNegativeButtonDivider.setVisibility(View.VISIBLE);
			mPositiveButtonDivider.setVisibility(View.VISIBLE);
			break;
		}
	}

	public void setButtonEnabledState(int whichButton, boolean enabled) {
		if (whichButton == BUTTON_NEGATIVE) {
			mBtnNegative.setEnabled(enabled);
		} else if (whichButton == BUTTON_NEUTRAL) {
			mBtnNeutral.setEnabled(enabled);
		} else if (whichButton == BUTTON_POSITIVE) {
			mBtnPositive.setEnabled(enabled);
		}
	}

	public void setNegativeButton(String text, final OnClickListener listener) {
		mBtnNegative.setVisibility(View.VISIBLE);
		mBtnNegative.setText(text);
		mBtnNegative.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.onClick(HoloDialog.this, BUTTON_NEGATIVE);
				}
				if (!mManualDismissOnNegativeButton) {
					dismiss();
				}
			}
		});
	}

	public void setNeutralButton(String text, final OnClickListener listener) {
		mBtnNeutral.setVisibility(View.VISIBLE);
		mBtnNeutral.setText(text);
		mBtnNeutral.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.onClick(HoloDialog.this, BUTTON_NEUTRAL);
				}
				if (!mManualDismissOnNeutralButton) {
					dismiss();
				}
			}
		});
	}

	public void setPositiveButton(String text, final OnClickListener listener) {
		mBtnPositive.setVisibility(View.VISIBLE);
		mBtnPositive.setText(text);
		mBtnPositive.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (listener != null) {
					listener.onClick(HoloDialog.this, BUTTON_POSITIVE);
				}
				if (!mManualDismissOnPositiveButton) {
					dismiss();
				}
			}
		});
	}

	@Override
	public Bundle onSaveInstanceState() {
		Bundle state = super.onSaveInstanceState();
		if (mInstanceStateListener != null) {
			mInstanceStateListener.onSaveInstanceState(state);
		}
		return state;
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (mInstanceStateListener != null) {
			mInstanceStateListener.onRestoreInstanceState(savedInstanceState);
		}
	}

	public interface InstanceStateListener {
		public Bundle onSaveInstanceState(Bundle state);
		public void onRestoreInstanceState(Bundle savedInstanceState);
	}

	public static class Builder {

		protected Context mContext;

		protected boolean mCancelable = true;
		protected boolean mCancelableOutsideTouch = false;

		protected View mCustomTitleView;
		protected View mMainDialogView;
		protected int mMainDialogLayoutRes;

		protected Drawable mTitleIcon;
		protected String mTitleText;

		protected String mDialogMessage;
		protected boolean mShowIndeterminateProgress;
		protected String mProgressMessage;

		protected boolean mSetFastScrollEnabled;
		protected int mListItemTextColor = -1;
		protected ListStyle mListStyle;
		protected List<ListItem> mListItems;

		protected OnClickListener mOnItemClickListener;
		protected OnMultiChoiceClickListener mOnMultiChoiceClickListener;

		protected String mNegativeButtonText;
		protected String mNeutralButtonText;
		protected String mPositiveButtonText;

		protected boolean mNegativeButtonEnabledState = true;
		protected boolean mNeutralButtonEnabledState = true;
		protected boolean mPositiveButtonEnabledState = true;

		protected OnClickListener mNegativeButtonClickListener;
		protected OnClickListener mNeutralButtonClickListener;
		protected OnClickListener mPositiveButtonClickListener;

		protected OnCancelListener mOnCancelListener;
		protected OnKeyListener mOnKeyListener;
		protected OnShowListener mOnShowListener;
		protected OnDismissListener mOnDismissListener;
		protected InstanceStateListener mInstanceStateListener;

		public Builder(Context context) {
			mContext = context;
		}

		public Builder setCancelable(boolean cancelable) {
			this.mCancelable = cancelable;
			return this;
		}

		public Builder setCanceledOnTouchOutside(boolean cancelableOutsideTouch) {
			this.mCancelableOutsideTouch = cancelableOutsideTouch;
			return this;
		}

		public Builder setCustomTitle(View view) {
			this.mCustomTitleView = view;
			return this;
		}

		public Builder setView(View view) {
			this.mMainDialogView = view;
			return this;
		}

		public Builder setView(int layoutRes) {
			this.mMainDialogLayoutRes = layoutRes;
			return this;
		}

		public Builder setIcon(Drawable icon) {
			this.mTitleIcon = icon;
			return this;
		}

		public Builder setIcon(int drawableId) {
			this.mTitleIcon = mContext.getResources().getDrawable(drawableId);
			return this;
		}

		public Builder setTitle(String title) {
			this.mTitleText = title;
			return this;
		}

		public Builder setTitle(int stringId) {
			this.mTitleText = mContext.getResources().getString(stringId);
			return this;
		}

		public Builder setMessage(String message) {
			this.mDialogMessage = message;
			return this;
		}

		public Builder setMessage(int stringId) {
			this.mDialogMessage = mContext.getString(stringId);
			return this;
		}

		public Builder setNegativeButton(String text, OnClickListener listener) {
			this.mNegativeButtonText = text;
			this.mNegativeButtonClickListener = listener;
			return this;
		}

		public Builder setNegativeButton(int stringId, OnClickListener listener) {
			this.mNegativeButtonText = mContext.getString(stringId);
			this.mNegativeButtonClickListener = listener;
			return this;
		}

		public Builder setNeutralButton(String text, OnClickListener listener) {
			this.mNeutralButtonText = text;
			this.mNeutralButtonClickListener = listener;
			return this;
		}

		public Builder setNeutralButton(int stringId, OnClickListener listener) {
			this.mNeutralButtonText = mContext.getString(stringId);
			this.mNeutralButtonClickListener = listener;
			return this;
		}

		public Builder setPositiveButton(String text, OnClickListener listener) {
			this.mPositiveButtonText = text;
			this.mPositiveButtonClickListener = listener;
			return this;
		}

		public Builder setPositiveButton(int stringId, OnClickListener listener) {
			this.mPositiveButtonText = mContext.getString(stringId);
			this.mPositiveButtonClickListener = listener;
			return this;
		}

		public Builder setNegativeButtonEnabledState(boolean state) {
			this.mNegativeButtonEnabledState = state;
			return this;
		}

		public Builder setNeutralButtonEnabledState(boolean state) {
			this.mNeutralButtonEnabledState = state;
			return this;
		}

		public Builder setPositiveButtonEnabledState(boolean state) {
			this.mPositiveButtonEnabledState = state;
			return this;
		}

		public Builder setOnCancelListener(OnCancelListener listener) {
			this.mOnCancelListener = listener;
			return this;
		}

		public Builder setOnDismissListener(OnDismissListener listener) {
			this.mOnDismissListener = listener;
			return this;
		}

		public Builder setInstanceStateListener(InstanceStateListener instanceStateListener) {
			mInstanceStateListener = instanceStateListener;
			return this;
		}

		public Builder setOnKeyListener(OnKeyListener listener) {
			this.mOnKeyListener = listener;
			return this;
		}

		public Builder setOnShowListener(OnShowListener listener) {
			this.mOnShowListener = listener;
			return this;
		}

		public Builder setIndeterminateProgressVisibility(boolean visible) {
			this.mShowIndeterminateProgress = visible;
			return this;
		}

		public Builder setIndeterminateProgress(String message) {
			setIndeterminateProgressVisibility(true);
			this.mProgressMessage = message;
			return this;
		}

		public Builder setIndeterminateProgress(int stringId) {
			setIndeterminateProgressVisibility(true);
			this.mProgressMessage = mContext.getString(stringId);
			return this;
		}

		public Builder setFastScrollEnabled(boolean fastScroll) {
			this.mSetFastScrollEnabled = fastScroll;
			return this;
		}

		public Builder setListItemTextColor(int color) {
			this.mListItemTextColor = color;
			return this;
		}

		public Builder setItems(List<ListItem> items, OnClickListener listener) {
			this.mListStyle = ListStyle.LISTVIEW;
			this.mOnItemClickListener = listener;
			this.mListItems = items;
			return this;
		}

		public Builder setItems(String[] items, OnClickListener listener) {
			this.mListStyle = ListStyle.LISTVIEW;
			this.mOnItemClickListener = listener;
			this.mListItems = new ArrayList<>();
			for (String label : items) {
				this.mListItems.add(new ListItem(label));
			}
			return this;
		}

		public Builder setItems(Drawable[] icons, String[] items, OnClickListener listener) {
			this.mListStyle = ListStyle.LISTVIEW;
			this.mOnItemClickListener = listener;
			this.mListItems = new ArrayList<>();
			for (int i = 0; i < icons.length; i++) {
				this.mListItems.add(new ListItem(icons[i], items[i]));
			}
			return this;
		}

		public Builder setItems(Drawable[] icons, int arrayId, OnClickListener listener) {
			this.mListStyle = ListStyle.LISTVIEW;
			this.mOnItemClickListener = listener;
			this.mListItems = new ArrayList<>();
			String[] items = mContext.getResources().getStringArray(arrayId);
			for (int i = 0; i < icons.length; i++) {
				this.mListItems.add(new ListItem(icons[i], items[i]));
			}
			return this;
		}

		public Builder setItems(int arrayId, OnClickListener listener) {
			this.mListStyle = ListStyle.LISTVIEW;
			this.mOnItemClickListener = listener;
			this.mListItems = new ArrayList<>();
			String[] items = mContext.getResources().getStringArray(arrayId);
			for (String label : items) {
				this.mListItems.add(new ListItem(label));
			}
			return this;
		}

		public Builder setMultiChoiceItems(List<ListItem> items, OnMultiChoiceClickListener listener) {
			this.mListStyle = ListStyle.MULTI_CHOICE;
			this.mOnMultiChoiceClickListener = listener;
			this.mListItems = items;
			return this;
		}

		public Builder setMultiChoiceItems(String[] items, boolean[] checkedItems, OnMultiChoiceClickListener listener) {
			this.mListStyle = ListStyle.MULTI_CHOICE;
			this.mOnMultiChoiceClickListener = listener;
			this.mListItems = new ArrayList<>();
			for (int i = 0; i < checkedItems.length; i++) {
				this.mListItems.add(new ListItem(items[i], checkedItems[i]));
			}
			return this;
		}

		public Builder setMultiChoiceItems(int arrayId, boolean[] checkedItems, OnMultiChoiceClickListener listener) {
			this.mListStyle = ListStyle.MULTI_CHOICE;
			this.mOnMultiChoiceClickListener = listener;
			this.mListItems = new ArrayList<>();
			String[] items = mContext.getResources().getStringArray(arrayId);
			for (int i = 0; i < checkedItems.length; i++) {
				this.mListItems.add(new ListItem(items[i], checkedItems[i]));
			}
			return this;
		}

		public Builder setSingleChoiceItems(List<ListItem> items, OnClickListener listener) {
			this.mListStyle = ListStyle.SINGLE_CHOICE;
			this.mListItems = items;
			this.mOnItemClickListener = listener;
			return this;
		}

		public Builder setSingleChoiceItems(String[] items, int checkedItem, OnClickListener listener) {
			this.mListStyle = ListStyle.SINGLE_CHOICE;
			this.mOnItemClickListener = listener;
			this.mListItems = new ArrayList<>();
			for (int i = 0; i < items.length; i++) {
				this.mListItems.add(new ListItem(items[i], (i == checkedItem)));
			}
			return this;
		}

		public Builder setSingleChoiceItems(int arrayId, int checkedItem, OnClickListener listener) {
			this.mListStyle = ListStyle.SINGLE_CHOICE;
			this.mOnItemClickListener = listener;
			this.mListItems = new ArrayList<>();
			String[] items = mContext.getResources().getStringArray(arrayId);
			for (int i = 0; i < items.length; i++) {
				this.mListItems.add(new ListItem(items[i], (i == checkedItem)));
			}
			return this;
		}

		public HoloDialog create() {
			return new HoloDialog(this);
		}

		public HoloDialog show() {
			try {
				HoloDialog dialog = new HoloDialog(this);
				dialog.show();
				return dialog;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public void show(Handler handler) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					HoloDialog dialog = new HoloDialog(Builder.this);
					dialog.show();
				}
			});
		}
	}

	public static class ListItem {

		public String label;
		public Drawable icon;
		public String subLabel;
		public Boolean checked;
		public Object data;
		public int labelColor = -1;
		public int subLabelColor = -1;

		public ListItem() {
		}

		public ListItem(String label) {
			this(null, label, null, null);
		}

		public ListItem(String label, Boolean checked) {
			this(null, label, null, checked);
		}

		public ListItem(Drawable icon, String label) {
			this(icon, label, null, null);
		}

		public ListItem(Drawable icon, String label, Boolean checked) {
			this(icon, label, null, checked);
		}

		public ListItem(Drawable icon, String label, String subLabel) {
			this(icon, label, subLabel, null);
		}

		public ListItem(Drawable icon, String label, String subLabel, Boolean checked) {
			this.icon = icon;
			this.label = label;
			this.subLabel = subLabel;
			this.checked = checked;
		}
	}

	public static final Comparator<ListItem> LIST_ITEM_COMPARATOR = new Comparator<ListItem>() {

		@Override
		public int compare(ListItem item1, ListItem item2) {
			return item1.label.compareToIgnoreCase(item2.label);
		}

	};

	public static class ListAdapter extends BaseAdapter {

		private List<ListItem> mListItems;
		private LayoutInflater mInflater;

		private ListStyle mListStyle;
		private int mTextColor = -1;

		public ListAdapter(HoloDialog.Builder builder) {
			mInflater = LayoutInflater.from(builder.mContext);
			mListItems = builder.mListItems;
			mListStyle = builder.mListStyle;
			mTextColor = builder.mListItemTextColor;
		}

		public ListAdapter(Context context, List<ListItem> listItems, ListStyle listStyle) {
			mInflater = LayoutInflater.from(context);
			mListItems = listItems;
			mListStyle = listStyle;
		}

		public List<ListItem> getListItems() {
			return mListItems;
		}

		public void setListItems(List<ListItem> listItems) {
			mListItems = listItems;
		}

		public ListStyle getListStyle() {
			return mListStyle;
		}

		public void setListStyle(ListStyle listStyle) {
			mListStyle = listStyle;
		}

		public void setTextColor(int color) {
			mTextColor = color;
		}

		@Override
		public int getCount() {
			return mListItems.size();
		}

		@Override
		public HoloDialog.ListItem getItem(int position) {
			try {
				return mListItems.get(position);
			} catch (IndexOutOfBoundsException e) {
				return null;
			}
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder viewHolder;

			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = viewHolder.mConvertView;
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			viewHolder.setItem(getItem(position));

			return convertView;
		}

		private class ViewHolder {
			private View mConvertView;
			private TextView mTvLabel;
			private TextView mTvSubLabel;
			private CheckBox mCheckBox;
			private RadioButton mRadioButton;

			public ViewHolder() {
				mConvertView = mInflater.inflate(R.layout.dialog_list_item, null, false);
				mTvLabel = (TextView)mConvertView.findViewById(R.id.label);
				mTvSubLabel = (TextView)mConvertView.findViewById(R.id.sublabel);
				mCheckBox = (CheckBox)mConvertView.findViewById(R.id.checkbox);
				mRadioButton = (RadioButton)mConvertView.findViewById(R.id.radiobutton);
			}

			private void setBackground() {
				mConvertView.setBackgroundColor(Color.TRANSPARENT);
			}

			private void setLabel(String label) {
				if (label != null) {
					mTvLabel.setVisibility(View.VISIBLE);
					mTvLabel.setText(label);
				} else {
					mTvLabel.setVisibility(View.GONE);
				}
			}

			private void setSubLabel(String label) {
				if (label != null) {
					mTvSubLabel.setVisibility(View.VISIBLE);
					mTvSubLabel.setText(label);
				} else {
					mTvSubLabel.setVisibility(View.GONE);
				}
			}

			private void setCheckableButtons(Boolean checked) {
				switch (mListStyle) {
					case LISTVIEW:
						if (checked == null) {
							mCheckBox.setVisibility(View.GONE);
							mRadioButton.setVisibility(View.GONE);
						} else {
							mCheckBox.setVisibility(View.VISIBLE);
							mRadioButton.setVisibility(View.GONE);
							mCheckBox.setChecked(checked);
						}
						break;
					case SINGLE_CHOICE:
						mCheckBox.setVisibility(View.GONE);
						mRadioButton.setVisibility(View.VISIBLE);

						if (checked == null) {
							checked = false;
						}

						mRadioButton.setChecked(checked);
						break;
					case MULTI_CHOICE:
						mCheckBox.setVisibility(View.VISIBLE);
						mRadioButton.setVisibility(View.GONE);

						if (checked == null) {
							checked = false;
						}

						mCheckBox.setChecked(checked);
						break;
				}
			}

			private void setTheme(HoloDialog.ListItem item) {
				int textColor;

				if (mTextColor != -1) {
					textColor = mTextColor;
				} else {
					textColor = 0xFF040404;
				}

				if (item.labelColor == -1) {
					mTvLabel.setTextColor(textColor);
				} else {
					mTvLabel.setTextColor(item.labelColor);
				}

				if (item.labelColor == -1) {
					mTvSubLabel.setTextColor(textColor);
				} else {
					mTvSubLabel.setTextColor(item.subLabelColor);
				}
			}

			public void setItem(final HoloDialog.ListItem item) {
				setBackground();
				setTheme(item);
				setLabel(item.label);
				setSubLabel(item.subLabel);
				setCheckableButtons(item.checked);
			}
		}

	}

}
