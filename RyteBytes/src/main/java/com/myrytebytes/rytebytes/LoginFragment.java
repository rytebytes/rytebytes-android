package com.myrytebytes.rytebytes;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;

import com.myrytebytes.datamanagement.Log;
import com.myrytebytes.datamanagement.LoginController;
import com.myrytebytes.datamodel.Location;
import com.myrytebytes.datamodel.StripeCustomer;
import com.myrytebytes.remote.ApiInterface;
import com.myrytebytes.remote.ApiListener.CreateAccountListener;
import com.myrytebytes.remote.ApiListener.CreateStripeAccountListener;
import com.myrytebytes.remote.ApiListener.GetLocationsListener;
import com.myrytebytes.remote.ApiListener.LoginListener;
import com.myrytebytes.remote.StripeInterface;
import com.myrytebytes.widget.HoloDialog;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.List;
import java.util.regex.Pattern;

public class LoginFragment extends BaseFragment {

	private EditText mEtEmail;
	private EditText mEtPassword;
	private String mEmailAddress;
	private String mPassword;
	private Dialog mProgressDialog;

	private final OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.btn_login:
					handleLogin();
					break;
				case R.id.btn_create_account:
					handleCreateAccount();
					break;
				case R.id.btn_forgot_password:

					break;
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_login, container, false);
		mEtEmail = (EditText)rootView.findViewById(R.id.et_email);
		mEtPassword = (EditText)rootView.findViewById(R.id.et_password);
		rootView.findViewById(R.id.btn_login).setOnClickListener(mOnClickListener);
		rootView.findViewById(R.id.btn_create_account).setOnClickListener(mOnClickListener);
		rootView.findViewById(R.id.btn_forgot_password).setOnClickListener(mOnClickListener);
		return rootView;
	}

	@Override
	protected ContentType getContentType() {
		return ContentType.LOGIN;
	}

	@Override
	protected int getTitle() {
		return 0;
	}

	@Override
	protected void onShown() {
		ApiInterface.getLocations(new GetLocationsListener() {
			@Override
			public void onComplete(List<Location> locations, int statusCode) {
				for (Location location : locations) {
					Log.d("location = " + location.objectId + "; " + location.streetAddress + "; " + location.city + "; " + location.state + "; " + location.zipcode + "; " + location.name + "; " + location.charityId);
				}
			}
		});
	}

	public void handleLogin() {
		if (validateInput()) {
			mProgressDialog = HoloDialog.showProgressDialog(getActivity(), "Logging In", "Please wait...");
			ApiInterface.login(mEmailAddress, mPassword, new LoginListener() {
				@Override
				public void onComplete(ParseUser user, ParseException exception) {
					if (mProgressDialog.isShowing()) {
						mProgressDialog.dismiss();
					}

					if (user != null) {
						LoginController.setUser(user);
						mActivityCallbacks.loginWillFinish(true);
					} else {
						//TODO: better error messages
						showOkDialog("Error", "An error occurred while logging in. Please try again.");
					}
				}
			});
		}
	}

	public void handleCreateAccount() {
		if (validateInput()) {
			mProgressDialog = HoloDialog.showProgressDialog(getActivity(), "Creating Account", "Please wait...");
			StripeInterface.createCustomer(mEmailAddress, "4242424242424242", 6, 2015, 222, getApplicationContext(), new CreateStripeAccountListener() {
				@Override
				public void onComplete(StripeCustomer customer, int statusCode) {
					if (customer != null) {
						createParseUser(customer);
					} else {
						if (mProgressDialog.isShowing()) {
							mProgressDialog.dismiss();
						}
						Log.e("null customer!");
					}
				}
			});
		}
	}

	public void createParseUser(StripeCustomer customer) {
		ApiInterface.createUser(customer, mPassword, new CreateAccountListener() {
			@Override
			public void onComplete(ParseUser user, ParseException exception) {
				if (mProgressDialog.isShowing()) {
					mProgressDialog.dismiss();
				}

				if (user != null) {
					LoginController.setUser(user);
					mActivityCallbacks.loginWillFinish(true);
				} else if (exception != null) {
					switch (exception.getCode()) {
						case ParseException.INVALID_EMAIL_ADDRESS:
							showOkDialog("Error", "The email address you entered is invalid.");
							break;
						case ParseException.EMAIL_TAKEN:
						case ParseException.USERNAME_TAKEN:
							showOkDialog("Error", "The email address you entered is already in use.");
							break;
						default:
							showOkDialog("Error", "An unknown error occurred while trying to create an account. Please try again later.");
							Log.e(exception);
							break;
					}
				} else {
					showOkDialog("Error", "An unknown error occurred while trying to create an account. Please try again later.");
				}
			}
		});
	}

	public boolean validateInput() {
		hideSoftKeyboard();
		mEmailAddress = mEtEmail.getText().toString().replaceAll("\\s","");
		mPassword = mEtPassword.getText().toString();

		if (TextUtils.isEmpty(mEmailAddress)) {
			showOkDialog("Error", "Email address must not be blank.");
			return false;
		} else if (TextUtils.isEmpty(mPassword)) {
			showOkDialog("Error", "Password must not be blank.");
			return false;
		} else if (!Pattern.compile("^\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b$", Pattern.CASE_INSENSITIVE).matcher(mEmailAddress).matches()) {
			showOkDialog("Error", "Please enter a valid email address.");
			return false;
		} else {
			return true;
		}
	}
}
