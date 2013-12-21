package com.myrytebytes.rytebytes;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

public class LoginFragment extends BaseFragment {

    private static final int REQUEST_CODE_CARD_IO = 44;
    private static final String CARD_IO_TOKEN = "19f7f219ce8843979fa8c5f99e86d484";

    private ViewGroup mRootView;
    private EditText mEtEmail;
    private EditText mEtPassword;
    private Button mBtnLogin;
    private Button mBtnCreateAccount;
    private Button mBtnForgotPassword;
    private ButtonSpinner mLocationSpinner;
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
    };

    private final ButtonSpinnerListener mButtonSpinnerListener = new ButtonSpinnerListener() {
        @Override
        public String[] getDropdownContents() {
            String[] contents = new String[mLocations.size()];
            for (int i = 0; i < mLocations.size(); i++) {
                contents[i] = mLocations.get(i).name;
            }
            return contents;
        }

        @Override
        public void onItemSelected(int index) {
            mLocation = mLocations.get(index);
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
//                case R.id.btn_card_io:
//                    Intent scanIntent = new Intent(getApplicationContext(), CardIOActivity.class);
//                    scanIntent.putExtra(CardIOActivity.EXTRA_APP_TOKEN, CARD_IO_TOKEN);
//                    scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, false);
//                    startActivityForResult(scanIntent, REQUEST_CODE_CARD_IO);
//                    break;
            }
        }
    };

    private final GetLocationsListener mGetLocationsListener = new GetLocationsListener() {
        @Override
        public void onComplete(List<Location> locations, int statusCode) {
            mLocations.clear();
            mLocations.addAll(locations);
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
        mEtEmail = (EditText)mRootView.findViewById(R.id.et_email);
        mEtPassword = (EditText)mRootView.findViewById(R.id.et_password);
        mBtnLogin = (Button)mRootView.findViewById(R.id.btn_login);
        mBtnCreateAccount = (Button)mRootView.findViewById(R.id.btn_create_account);
        mBtnForgotPassword = (Button)mRootView.findViewById(R.id.btn_forgot_password);

        mBtnLogin.setOnClickListener(mOnClickListener);
        mBtnCreateAccount.setOnClickListener(mOnClickListener);
        mBtnForgotPassword.setOnClickListener(mOnClickListener);

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

    public void createAccountButtonClicked() {
        if (validateInput()) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.view_signup_step_2, mRootView, false);
            CreditCardEntryLayout cardEntryLayout = (CreditCardEntryLayout)view.findViewById(R.id.card_entry_layout);
            cardEntryLayout.setListener(mCreditCardEntryListener);

            mLocationSpinner = (ButtonSpinner)view.findViewById(R.id.spinner);
            mLocationSpinner.setListener(mButtonSpinnerListener);

            int viewWidth = mRootView.getMeasuredWidth();
            ViewHelper.setTranslationX(view, viewWidth);
            mRootView.addView(view);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(
                    ObjectAnimator.ofFloat(view, "translationX", 0),
                    ObjectAnimator.ofFloat(mEtEmail, "translationX", -viewWidth),
                    ObjectAnimator.ofFloat(mEtPassword, "translationX", -viewWidth),
                    ObjectAnimator.ofFloat(mBtnLogin, "translationX", -viewWidth),
                    ObjectAnimator.ofFloat(mBtnCreateAccount, "translationX", -viewWidth),
                    ObjectAnimator.ofFloat(mBtnForgotPassword, "translationX", -viewWidth)
            );
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mEtEmail.setVisibility(View.GONE);
                    mEtPassword.setVisibility(View.GONE);
                    mBtnLogin.setVisibility(View.GONE);
                    mBtnCreateAccount.setVisibility(View.GONE);
                    mBtnForgotPassword.setVisibility(View.GONE);
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
                String cardNumber = scanResult.cardNumber;
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
