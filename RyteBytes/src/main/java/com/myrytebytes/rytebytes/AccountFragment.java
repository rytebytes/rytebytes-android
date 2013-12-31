package com.myrytebytes.rytebytes;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.myrytebytes.datamanagement.LoginController;
import com.myrytebytes.remote.ApiInterface;
import com.myrytebytes.remote.ApiListener.ResetPasswordListener;
import com.myrytebytes.widget.HoloDialog;

public class AccountFragment extends BaseFragment {

    /*package*/ Dialog mProgressDialog;

    private final OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_reset_password:
                    mProgressDialog = HoloDialog.showProgressDialog(getActivity(), "Resetting Password", "Please wait...");
                    ApiInterface.resetPassword(LoginController.getSessionUser().getEmail(), mResetPasswordListener);
                    break;
                case R.id.btn_change_credit_card:
                    pushFragment(ContentType.CHANGE_CREDIT_CARD, null);
                    break;
                case R.id.btn_change_pickup_location:
                    pushFragment(ContentType.PICKUP_LOCATIONS, null);
                    break;
                case R.id.btn_logout:
                    LoginController.logOut(getApplicationContext());
                    mActivityCallbacks.displayLoginFragment(true);
                    break;
            }
        }
    };

    private final ResetPasswordListener mResetPasswordListener = new ResetPasswordListener() {
        @Override
        public void onComplete(boolean success) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            if (success) {
                showOkDialog("Success!", "You should receive an email with password reset instructions shortly.");
            } else {
                showOkDialog("Error", "An error occurred while resetting your password. Please try again soon.");
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
