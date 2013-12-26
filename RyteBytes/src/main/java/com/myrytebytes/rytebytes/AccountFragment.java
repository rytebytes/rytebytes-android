package com.myrytebytes.rytebytes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.myrytebytes.datamanagement.LoginController;

public class AccountFragment extends BaseFragment {

    private final OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_reset_password:

                    break;
                case R.id.btn_change_credit_card:

                    break;
                case R.id.btn_change_pickup_location:

                    break;
                case R.id.btn_logout:
                    LoginController.logOut(getApplicationContext());
                    mActivityCallbacks.displayLoginFragment(true);
                    break;
            }
        }
    };

	public static AccountFragment newInstance() {
		return new AccountFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_account, container, false);

        rootView.findViewById(R.id.btn_reset_password).setOnClickListener(mOnClickListener);
        rootView.findViewById(R.id.btn_change_credit_card).setOnClickListener(mOnClickListener);
        rootView.findViewById(R.id.btn_change_pickup_location).setOnClickListener(mOnClickListener);
        rootView.findViewById(R.id.btn_logout).setOnClickListener(mOnClickListener);

		return rootView;
	}

	@Override
	protected ContentType getContentType() {
		return ContentType.ACCOUNT;
	}

	@Override
	protected int getTitle() {
		return R.string.account;
	}

	@Override
	protected void onShown() {

	}
}
