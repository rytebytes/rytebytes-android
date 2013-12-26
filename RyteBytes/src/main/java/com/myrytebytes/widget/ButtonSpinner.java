package com.myrytebytes.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.myrytebytes.rytebytes.R;

public class ButtonSpinner extends CustomFontButton {

    public interface ButtonSpinnerListener {
        public String[] getDropdownContents(ButtonSpinner spinner);
        public void onItemSelected(int index, ButtonSpinner spinner);
    }

    private PopupWindow mPopupWindow;
    private ButtonSpinnerListener mListener;
    private OnClickListener mSubOnClickListener;

    private final OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            String[] contents = mListener.getDropdownContents(ButtonSpinner.this);
            if (contents != null && contents.length > 0) {
                mPopupWindow = getPopupWindow(contents);
                mPopupWindow.showAsDropDown(ButtonSpinner.this);
            }

            if (mSubOnClickListener != null) {
                mSubOnClickListener.onClick(ButtonSpinner.this);
            }
        }
    };
    private final OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            mListener.onItemSelected(i, ButtonSpinner.this);
            setTextColor(Color.BLACK);
            setText(mListener.getDropdownContents(ButtonSpinner.this)[i]);

            Animation fadeAnimation = AnimationUtils.loadAnimation(view.getContext(), android.R.anim.fade_out);
            fadeAnimation.setDuration(10);
            view.startAnimation(fadeAnimation);
            mPopupWindow.dismiss();
        }
    };

    public ButtonSpinner(Context context) {
        super(context);
        init();
    }

    public ButtonSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setOnClickListener(mOnClickListener);
        setBackgroundResource(R.drawable.spinner_background_holo_light);
        setTextColor(0xFF555555);

        float density = getContext().getResources().getDisplayMetrics().density;
        setPadding((int)(12 * density), (int)(12 * density), getPaddingRight(), getPaddingBottom());
    }

    public void setListener(ButtonSpinnerListener listener) {
        mListener = listener;
    }

    public PopupWindow getPopupWindow(String[] contents) {
        Context context = getContext();
        PopupWindow popupWindow = new PopupWindow(context);
        ListView listView = new ListView(context);

        listView.setAdapter(new ArrayAdapter<>(context, R.layout.spinner_dropdown_item, contents));
        listView.setOnItemClickListener(mOnItemClickListener);
        popupWindow.setFocusable(true);
        popupWindow.setWidth(getMeasuredWidth());
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(listView);

        return popupWindow;
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        if (l == mOnClickListener) {
            super.setOnClickListener(l);
        } else {
            mSubOnClickListener = l;
        }
    }
}
