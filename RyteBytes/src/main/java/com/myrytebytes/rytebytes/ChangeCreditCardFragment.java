package com.myrytebytes.rytebytes;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myrytebytes.datamodel.StripeCustomer;
import com.myrytebytes.datamodel.StripeToken;
import com.myrytebytes.remote.ApiInterface;
import com.myrytebytes.remote.ApiListener.CreateStripeTokenListener;
import com.myrytebytes.remote.ApiListener.GetUserInfoListener;
import com.myrytebytes.remote.ApiListener.UpdateUserInfoListener;
import com.myrytebytes.remote.StripeInterface;
import com.myrytebytes.widget.CreditCardEntryLayout;
import com.myrytebytes.widget.CreditCardEntryLayout.CreditCardEntryListener;
import com.myrytebytes.widget.HoloDialog;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

public class ChangeCreditCardFragment extends BaseFragment {

    private static final int REQUEST_CODE_CARD_IO = 44;

    private CreditCardEntryLayout mCardEntryLayout;
    private Dialog mProgressDialog;

    private final CreditCardEntryListener mCreditCardEntryListener = new CreditCardEntryListener() {
        @Override
        public void onCardVerified(String cvc, String cardNumber, String cardExpirationMonth, String cardExpirationYear) {
            mProgressDialog = HoloDialog.showProgressDialog(getActivity(), "Updating Credit Card", "Please wait...");
            StripeInterface.createToken(cvc, cardNumber, cardExpirationMonth, cardExpirationYear, getApplicationContext(), mCreateStripeTokenListener);
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

    private final CreateStripeTokenListener mCreateStripeTokenListener = new CreateStripeTokenListener() {
        @Override
        public void onComplete(StripeToken token, int statusCode) {
            if (token != null) {
                updateCreditCard(token);
            } else {
                if (mProgressDialog != null && mProgressDialog.isShowing()) {
                    mProgressDialog.dismiss();
                }
                showOkDialog("Error", "An error occurred while updating your card. Please check your card number and try again.");
            }
        }
    };

    private final UpdateUserInfoListener mUpdateUserInfoListener = new UpdateUserInfoListener() {
        @Override
        public void onComplete(boolean success, int statusCode) {
            if (mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            if (success) {
                showOkDialog("Success", "Your credit card has been updated.", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
            } else {
                showOkDialog("Error", "An error occurred while updating your card. Please check your card number and try again.");
            }
        }
    };

    private final GetUserInfoListener mGetUserInfoListener = new GetUserInfoListener() {
        @Override
        public void onComplete(StripeCustomer customer, int statusCode) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            if (customer != null && customer.cards != null && customer.cards.size() > 0) {
                String type = customer.cards.get(0).type;
                int last4 = customer.cards.get(0).last4;
                ((TextView)getView().findViewById(R.id.tv_change_credit_card)).setText("Your account is currently setup to use the " + type + " ending in " + last4 + ". Enter a new card below to update.");
            } else {
                ((TextView)getView().findViewById(R.id.tv_change_credit_card)).setText("Enter a new card below to update the billing method of your account.");

            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_credit_card, container, false);

        mCardEntryLayout = (CreditCardEntryLayout)view.findViewById(R.id.card_entry_layout);
        mCardEntryLayout.setListener(mCreditCardEntryListener);

        mProgressDialog = HoloDialog.showProgressDialog(getActivity(), "Fetching Credit Card", "Please wait...");
        ApiInterface.getUserInfo(mGetUserInfoListener);

        return view;
    }

    @Override
    protected void onShown() { }

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

    /*package*/ void updateCreditCard(StripeToken token) {
        ApiInterface.updateUserStripeToken(token, mUpdateUserInfoListener);
    }

    @Override
    protected ContentType getContentType() {
        return ContentType.CHANGE_CREDIT_CARD;
    }

    @Override
    protected int getTitle() {
        return R.string.credit_card;
    }
}
