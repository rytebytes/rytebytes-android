package com.myrytebytes.rytebytes;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myrytebytes.datamodel.StripeCustomer;
import com.myrytebytes.remote.ApiListener.UpdateCreditCardListener;
import com.myrytebytes.remote.StripeInterface;
import com.myrytebytes.widget.CreditCardEntryLayout;
import com.myrytebytes.widget.CreditCardEntryLayout.CreditCardEntryListener;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

public class ChangeCreditCardFragment extends BaseFragment {

    private static final int REQUEST_CODE_CARD_IO = 44;

    private CreditCardEntryLayout mCardEntryLayout;
    private Dialog mProgressDialog;

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
            updateCreditCard();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_credit_card, container, false);

        String type = "Visa";
        String last4 = "4242";
        ((TextView) view.findViewById(R.id.tv_change_credit_card)).setText("Your account is currently setup to use the " + type + " ending in " + last4 + ". Enter a new card below to update.");

        mCardEntryLayout = (CreditCardEntryLayout)view.findViewById(R.id.card_entry_layout);
        mCardEntryLayout.setListener(mCreditCardEntryListener);

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

    /*package*/ void updateCreditCard() {
        String stripeId = "testStripeId";
        StripeInterface.updateCardForUser(stripeId, mCardNumber, mCardholderName, mCardExpMonth, mCardExpYear, getApplicationContext(), new UpdateCreditCardListener() {
            @Override
            public void onComplete(StripeCustomer customer, int statusCode) {
                if (customer != null) {
                    showOkDialog("Success", "Your credit card has been updated.");
                } else {
                    if (mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                    }
                    showOkDialog("Error", "An error occurred while updating your card. Please check your card number and try again.");
                }
            }
        });
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
