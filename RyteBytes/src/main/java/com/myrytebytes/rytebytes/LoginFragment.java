package com.myrytebytes.rytebytes;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;

import com.myrytebytes.datamanagement.Log;
import com.myrytebytes.datamanagement.UserController;
import com.myrytebytes.datamodel.Location;
import com.myrytebytes.datamodel.StripeCustomer;
import com.myrytebytes.remote.ApiInterface;
import com.myrytebytes.remote.ApiListener.CreateAccountListener;
import com.myrytebytes.remote.ApiListener.CreateStripeAccountListener;
import com.myrytebytes.remote.ApiListener.GetLocationListener;
import com.myrytebytes.remote.ApiListener.GetLocationsListener;
import com.myrytebytes.remote.ApiListener.GetMenuListener;
import com.myrytebytes.remote.ApiListener.LoginListener;
import com.myrytebytes.remote.StripeInterface;
import com.myrytebytes.widget.ButtonSpinner;
import com.myrytebytes.widget.ButtonSpinner.ButtonSpinnerListener;
import com.myrytebytes.widget.CreditCardEntryLayout;
import com.myrytebytes.widget.CreditCardEntryLayout.CreditCardEntryListener;
import com.myrytebytes.widget.HoloDialog;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

public class LoginFragment extends BaseFragment {

    private static final int REQUEST_CODE_CARD_IO = 44;

    private ViewGroup mRootView;
    private View mLoginViewStep1;
    private EditText mEtEmail;
    private EditText mEtPassword;
    private CreditCardEntryLayout mCardEntryLayout;
    private Dialog mProgressDialog;

    private List<Location> mLocations;

    private String mEmailAddress;
    private String mPassword;
    private Location mLocation;
    private String mCardholderName;
    private String mCardNumber;
    private String mCardExpMonth;
    private String mCardExpYear;

    private final CreditCardEntryListener mCreditCardEntryListener = new CreditCardEntryListener() {
        @Override
        public void onCardVerified(String name, String cardNumber, String cardExpirationMonth, String cardExpirationYear) {
            mCardholderName = name;
            mCardNumber = cardNumber;
            mCardExpMonth = cardExpirationMonth;
            mCardExpYear = cardExpirationYear;

            if (mLocation != null) {
                createStripeUser();
            }
        }

        @Override
        public void onCardIOSelected() {
            Intent scanIntent = new Intent(getApplicationContext(), CardIOActivity.class);
            scanIntent.putExtra(CardIOActivity.EXTRA_APP_TOKEN, Constants.CARD_IO_TOKEN);
            scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, false);
            scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, true);
            startActivityForResult(scanIntent, REQUEST_CODE_CARD_IO);
        }
    };

    private final ButtonSpinnerListener mButtonSpinnerListener = new ButtonSpinnerListener() {
        @Override
        public String[] getDropdownContents(ButtonSpinner spinner) {
            String[] contents = new String[mLocations.size()];
            for (int i = 0; i < mLocations.size(); i++) {
                contents[i] = mLocations.get(i).name;
            }
            return contents;
        }

        @Override
        public void onItemSelected(int index, ButtonSpinner spinner) {
            mLocation = mLocations.get(index);
        }
    };

    private final GetLocationListener mGetLocationListener = new GetLocationListener() {
        @Override
        public void onComplete(Location location, int statusCode) {
            if (location != null) {
                UserController.setActiveUser(ParseUser.getCurrentUser(), location);
                ApiInterface.getMenu(mGetMenuListener);
            } else {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                //TODO: better error messages
                showOkDialog("Error", "An error occurred while logging in. Please try again.");
            }
        }
    };

    private final GetMenuListener mGetMenuListener = new GetMenuListener() {
        @Override
        public void onComplete(boolean success, int statusCode) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            mActivityCallbacks.loginWillFinish(true);
        }
    };

    private final LoginListener mLoginListener = new LoginListener() {
        @Override
        public void onComplete(ParseUser user, ParseException exception) {
            if (user == null || user.get("locationId") == null) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                //TODO: better error messages
                showOkDialog("Error", "An error occurred while logging in. Please try again.");
            } else {
                ParseObject location = (ParseObject)user.get("locationId");
                ApiInterface.getLocation(location.getObjectId(), mGetLocationListener);
            }
        }
    };

    private final OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.spinner:
                    if (mLocations.size() == 0) {
                        //TODO: display no locations dialog and wait for them to come down/retry
                    }
                case R.id.btn_login:
                    handleLogin();
                    break;
                case R.id.btn_create_account:
                    createAccountButtonClicked();
                    break;
                case R.id.btn_forgot_password:

                    break;
            }
        }
    };

    private final GetLocationsListener mGetLocationsListener = new GetLocationsListener() {
        @Override
        public void onComplete(List<Location> locations, int statusCode) {
            if (locations != null) {
                mLocations.clear();
                mLocations.addAll(locations);
            } else {
                // TODO:
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ApiInterface.getLocations(mGetLocationsListener);
        mLocations = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = (ViewGroup)inflater.inflate(R.layout.fragment_login, container, false);
        mLoginViewStep1 = mRootView.findViewById(R.id.view_signup_step_1);
        mEtEmail = (EditText)mLoginViewStep1.findViewById(R.id.et_email);
        mEtPassword = (EditText)mLoginViewStep1.findViewById(R.id.et_password);

        mLoginViewStep1.findViewById(R.id.btn_login).setOnClickListener(mOnClickListener);
        mLoginViewStep1.findViewById(R.id.btn_create_account).setOnClickListener(mOnClickListener);
        mLoginViewStep1.findViewById(R.id.btn_forgot_password).setOnClickListener(mOnClickListener);

        return mRootView;
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
    protected void onShown() { }

    public void handleLogin() {
        if (validateInput()) {
            mProgressDialog = HoloDialog.showProgressDialog(getActivity(), "Logging In", "Please wait...");
            ApiInterface.login(mEmailAddress, mPassword, mLoginListener);
        }
    }

    public void createAccountButtonClicked() {
        if (validateInput()) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.view_signup_step_2, mRootView, false);
            mCardEntryLayout = (CreditCardEntryLayout)view.findViewById(R.id.card_entry_layout);
            mCardEntryLayout.setListener(mCreditCardEntryListener);

            ((ButtonSpinner)view.findViewById(R.id.spinner)).setListener(mButtonSpinnerListener);

            int viewWidth = mRootView.getMeasuredWidth();
            ViewHelper.setTranslationX(view, viewWidth);
            mRootView.addView(view);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(
                    ObjectAnimator.ofFloat(view, "translationX", 0),
                    ObjectAnimator.ofFloat(mLoginViewStep1, "translationX", -viewWidth)
            );
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginViewStep1.setVisibility(View.GONE);
                }
            });
            animatorSet.setDuration(300).start();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_CARD_IO) {
            if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);
                mCardEntryLayout.setCardNumber(scanResult.cardNumber);
            }
        }
    }

    public void createStripeUser() {
        mProgressDialog = HoloDialog.showProgressDialog(getActivity(), "Creating Account", "Please wait...");
        StripeInterface.createCustomer(mEmailAddress, mCardholderName, mCardNumber, mCardExpMonth, mCardExpYear,getApplicationContext(), new CreateStripeAccountListener() {
            @Override
            public void onComplete(StripeCustomer customer, int statusCode) {
                if (customer != null) {
                    createParseUser(customer);
                } else {
                    //TODO: error codes
                    if (mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                    Log.e("null customer!");
                }
            }
        });
    }

    public void createParseUser(StripeCustomer customer) {
        ApiInterface.createUser(customer, mLocation, mPassword, new CreateAccountListener() {
            @Override
            public void onComplete(ParseUser user, ParseException exception) {
                if (mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }

                if (user != null) {
                    ApiInterface.getMenu(mGetMenuListener);
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
