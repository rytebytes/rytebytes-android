/*
 * CreditCardEntryLayout.java
 * SpotHero
 *
 * Created by erickuck on 7/8/2013.
 */

package com.myrytebytes.widget;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;

import com.myrytebytes.rytebytes.R;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.Calendar;

public class CreditCardEntryLayout extends ViewGroup {

	public interface CreditCardEntryListener {
		public void onCardVerified(String name, String cardNumber, String cardExpirationMonth, String cardExpirationYear);
	}

	private int mWidth;
	private CardType mCardType;
	private boolean mNeedsLayout;
	private boolean isSecondRowShown;
	private CreditCardEntryListener mListener;
	
	private final FlipImageView mImgCard;
	private final CustomFontEditText mEtCardNumber;
	private final CustomFontEditText mEtMonth;
	private final CustomFontEditText mEtYear;
	private final CustomFontEditText mEtName;
	private final CustomFontTextView mTvSlash;
	private final CustomFontButton mBtnSubmit;

	private final int mPadding;

	private final OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			verifyCard();
		}
	};

	public CreditCardEntryLayout(Context context) {
		this(context, null, 0);
	}

	public CreditCardEntryLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CreditCardEntryLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		Context themedContext = new ContextThemeWrapper(context, R.style.Theme_RyteBytes);

		mCardType = CardType.UNDEF;
		CardEntryListener cardEntryListener = new CardEntryListener();

		final float density = getResources().getDisplayMetrics().density;
		mPadding = (int)(density * 8);

		mImgCard = new FlipImageView(themedContext);
		mEtCardNumber = new CustomFontEditText(themedContext);
		mEtMonth = new CustomFontEditText(themedContext);
		mEtYear = new CustomFontEditText(themedContext);
		mEtName = new CustomFontEditText(themedContext);
		mTvSlash = new CustomFontTextView(themedContext);
		mBtnSubmit = new CustomFontButton(themedContext);

		mImgCard.setImageResource(getCardRes(mCardType));

		mEtCardNumber.setInputType(InputType.TYPE_CLASS_NUMBER);
		mEtCardNumber.setHint("Card Number");
		mEtCardNumber.addTextChangedListener(new CreditCardNumberTextWatcher(cardEntryListener));

		mEtMonth.setInputType(InputType.TYPE_CLASS_NUMBER);
		mEtMonth.setHint("MM");
		mEtMonth.setFilters(new InputFilter[] { new InputFilter.LengthFilter(2)} );
		mEtMonth.addTextChangedListener(new CreditCardDetailTextWatcher(cardEntryListener, CreditCardDetailTextWatcher.TYPE_EXP_MONTH));

		mEtYear.setInputType(InputType.TYPE_CLASS_NUMBER);
		mEtYear.setHint("YY");
		mEtYear.setFilters(new InputFilter[] { new InputFilter.LengthFilter(2)} );
		mEtYear.addTextChangedListener(new CreditCardDetailTextWatcher(cardEntryListener, CreditCardDetailTextWatcher.TYPE_EXP_YEAR));

		mEtName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
		mEtName.setHint("Cardholder Name");
		mEtName.addTextChangedListener(new CreditCardDetailTextWatcher(cardEntryListener, CreditCardDetailTextWatcher.TYPE_CARDHOLDER_NAME));

		mBtnSubmit.setText("Submit");
		mBtnSubmit.setEnabled(false);
		mBtnSubmit.setOnClickListener(mOnClickListener);
		mTvSlash.setText("/");

		addView(mEtCardNumber);
		addView(mEtMonth);
		addView(mEtYear);
		addView(mEtName);
		addView(mTvSlash);
		addView(mImgCard);
		addView(mBtnSubmit);
	}

	public void setListener(CreditCardEntryListener listener) {
		mListener = listener;
	}

	/*package*/ void verifyCard() {
		final String name = mEtName.getText().toString();
		if (TextUtils.isEmpty(name)) {
			mEtName.setError("The cardholder name cannot be left blank.");
			return;
		}

		final String cardNumber = mEtCardNumber.getText().toString();
		if (TextUtils.isEmpty(cardNumber)) {
			mEtCardNumber.setError("The card number cannot be left blank.");
			return;
		}

		final String cardExpirationMonth = mEtMonth.getText().toString();
		if (TextUtils.isEmpty(cardExpirationMonth)) {
			mEtMonth.setError("The expiration month cannot be left blank.");
			return;
		}

		final String cardExpirationYear = mEtYear.getText().toString();
		if (cardExpirationYear.length() != 2) {
			mEtMonth.setError("The expiration year must be 2 digits.");
			return;
		}

		mListener.onCardVerified(name, cardNumber, cardExpirationMonth, cardExpirationYear);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int width = mWidth = MeasureSpec.getSize(widthMeasureSpec);

		mImgCard.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		mEtCardNumber.measure(MeasureSpec.makeMeasureSpec(width - mPadding * 3 - mImgCard.getMeasuredWidth(), MeasureSpec.EXACTLY), MeasureSpec.UNSPECIFIED);
		mEtMonth.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		mTvSlash.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		mEtYear.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		mEtName.measure(MeasureSpec.makeMeasureSpec(width - mPadding * 3 - mEtMonth.getMeasuredWidth() - mTvSlash.getMeasuredWidth() - mEtYear.getMeasuredWidth(), MeasureSpec.EXACTLY), MeasureSpec.UNSPECIFIED);
		mBtnSubmit.measure(MeasureSpec.makeMeasureSpec(width - mPadding * 2, MeasureSpec.EXACTLY), MeasureSpec.UNSPECIFIED);

		int height;
		if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY) {
			height = MeasureSpec.getSize(heightMeasureSpec);
		} else {
			height = mEtCardNumber.getMeasuredHeight() * 2 + mBtnSubmit.getMeasuredHeight() + mPadding * 2;
		}

		setMeasuredDimension(width, height);
	}
	
	@Override
	protected void onLayout(boolean changed, final int l, final int t, final int r, final int b) {
		if (changed || mNeedsLayout) {
			int imageTop = (mEtCardNumber.getMeasuredHeight() - mImgCard.getMeasuredHeight()) / 2;
			mImgCard.layout(mPadding, imageTop, mPadding + mImgCard.getMeasuredWidth(), imageTop + mImgCard.getMeasuredHeight());

			mEtCardNumber.layout(mPadding * 2 + mImgCard.getMeasuredWidth(), 0, mWidth - mPadding, mEtCardNumber.getMeasuredHeight());

			if (isSecondRowShown) {
				final int top = mEtCardNumber.getMeasuredHeight();
				int left = mPadding;
				mEtMonth.layout(left, top, left + mEtMonth.getMeasuredWidth(), top + mEtMonth.getMeasuredHeight());

				final int slashTop = top + (mEtMonth.getMeasuredHeight() - mTvSlash.getMeasuredHeight()) / 2;
				left += mEtMonth.getMeasuredWidth();
				mTvSlash.layout(left, slashTop, left + mTvSlash.getMeasuredWidth(), slashTop + mTvSlash.getMeasuredHeight());

				left += mTvSlash.getMeasuredWidth();
				mEtYear.layout(left, top, left + mEtYear.getMeasuredWidth(), top + mEtYear.getMeasuredHeight());

				left += mEtYear.getMeasuredWidth() + mPadding;
				mEtName.layout(left, top, left + mEtName.getMeasuredWidth(), top + mEtName.getMeasuredHeight());
			}

			mBtnSubmit.layout(mPadding, mEtCardNumber.getMeasuredHeight() * 2 + mPadding, mWidth - mPadding, mEtCardNumber.getMeasuredHeight() * 2 + mPadding + mBtnSubmit.getMeasuredHeight());
		}
	}

	/*package*/ boolean validateMonth() {
		String month = mEtMonth.getText().toString();
		try {
			int monthInt = Integer.parseInt(month);
			if (monthInt > 0 && monthInt <= 12) {
				mEtMonth.setError(null);
				return true;
			} else {
				mEtMonth.setError("Please enter a month between 1 and 12");
				return false;
			}
		} catch (Exception e) {
			mEtMonth.setError("Please enter a valid month");
			return false;
		}
	}

	/*package*/ boolean validateYear() {
		String year = mEtYear.getText().toString();
		try {
			int yearInt = Integer.parseInt(year);
			int currentYear = Calendar.getInstance().get(Calendar.YEAR) % 1000;

			if (yearInt >= currentYear) {
				mEtYear.setError(null);
				return true;
			} else {
				mEtYear.setError("Please enter a future expiration date");
				return false;
			}
		} catch (Exception e) {
			mEtYear.setError("Please enter a valid year");
			return false;
		}
	}

	/*package*/ boolean validateCardNumber(String cardNumber) {
		try {
			int sum = 0;
			int digit;
			int addend;
			boolean doubled = false;
			for (int i = cardNumber.length () - 1; i >= 0; i--) {
				digit = Integer.parseInt(cardNumber.substring (i, i + 1));
				if (doubled) {
					addend = digit * 2;
					if (addend > 9) {
						addend -= 9;
					}
				} else {
					addend = digit;
				}
				sum += addend;
				doubled = !doubled;
			}
			return (sum % 10) == 0;
		} catch (Exception e) {
			return false;
		}
	}

	private int getCardRes(CardType cardType) {
		switch (cardType) {
			case AMEX:
				return R.drawable.img_amex;
			case DINERS:
				return R.drawable.img_diners;
			case DISCOVER:
				return R.drawable.img_discover;
			case JCB_15:
			case JCB_16:
				return R.drawable.img_jcb;
			case MASTERCARD:
				return R.drawable.img_mastercard;
			case VISA:
				return R.drawable.img_visa;
			default:
				return R.drawable.img_generic_credit;
		}
	}

	public enum CardType {
		UNDEF(16),
		VISA(16),
		MASTERCARD(16),
		AMEX(15),
		DISCOVER(16),
		DINERS(14),
		JCB_15(15),
		JCB_16(16);
		
		public int numberLength;

		private CardType(int length) {
			this.numberLength = length;
		}
	}

	private class CardEntryListener {
		public void onNumberDigitEntered(CardType cardType, boolean newType, boolean complete) {
			if (complete) {
				boolean valid = validateCardNumber(mEtCardNumber.getText().toString().replaceAll(" ", ""));
				if (!valid) {
					mEtCardNumber.setError("The credit card number entered is invalid");
				} else {
					mEtCardNumber.setError(null);

					if (!isSecondRowShown) {
						isSecondRowShown = true;
						mNeedsLayout = true;
						AnimatorSet set = new AnimatorSet();
						set.playTogether(
								ObjectAnimator.ofFloat(mEtMonth, "translationY", -mEtMonth.getMeasuredHeight(), 0),
								ObjectAnimator.ofFloat(mTvSlash, "translationY", -mTvSlash.getMeasuredHeight(), 0),
								ObjectAnimator.ofFloat(mEtYear, "translationY", -mEtYear.getMeasuredHeight(), 0),
								ObjectAnimator.ofFloat(mEtName, "translationY", -mEtName.getMeasuredHeight(), 0)
						);
						set.setDuration(300).start();
						requestLayout();
					}

					mEtMonth.requestFocus();
				}
			} else {
				mEtCardNumber.setError(null);
			}

			if (newType) {
				mImgCard.toggleFlip(getCardRes(mCardType));
			}
		}

		public void onMonthDigitEntered(boolean complete) {
			mEtMonth.setError(null);
			if (complete && validateMonth()) {
				mEtYear.requestFocus();

				if (mEtYear.getText().length() == 2 && mEtName.getText().length() > 0) {
					mBtnSubmit.setEnabled(true);
				}
			}
		}

		public void onYearDigitEntered(boolean complete) {
			mEtYear.setError(null);
			if (complete && validateYear()) {
				mEtName.requestFocus();

				if (mEtMonth.getText().length() == 2 && mEtName.getText().length() > 0) {
					mBtnSubmit.setEnabled(true);
				}
			}
		}

		public void onCardholderNameLetterEntered() {
			mEtName.setError(null);

			if (mEtMonth.getText().length() == 2 && mEtYear.getText().length() == 2) {
				mBtnSubmit.setEnabled(true);
			}
		}
	}

	private class CreditCardNumberTextWatcher implements TextWatcher {
		private boolean mSpaceDeleted = false;
		private CardEntryListener mCallback;
		private String mPreEditString;
		private int mEditCursorPosition;

		public CreditCardNumberTextWatcher(CardEntryListener listener) {
			mCallback = listener;
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) { }

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			mPreEditString = s.toString();
			mEditCursorPosition = start;
			CharSequence charDeleted = s.subSequence(start, start + count);
			mSpaceDeleted = " ".equals(charDeleted.toString());
		}

		@Override
		public void afterTextChanged(Editable s) {
			mEtCardNumber.removeTextChangedListener(this);

			CardType newCardType = getCardType(s);
			boolean isNewCardType;
			if (newCardType != mCardType) {
				mCardType = newCardType;
				isNewCardType = true;
			} else {
				isNewCardType = false;
			}

			if (s.toString().replaceAll("\\s+", "").length() > newCardType.numberLength) {
				mEtCardNumber.setText(mPreEditString);
				if (mEditCursorPosition > 0 && mEditCursorPosition <= mPreEditString.length()) {
					mEtCardNumber.setSelection(mEditCursorPosition);
				}
				mEtCardNumber.addTextChangedListener(this);
				return;
			}

			int cursorPosition = mEtCardNumber.getSelectionStart();
			StringBuilder formatted = new StringBuilder();
			int digitCount = 0;
			for (int i = 0; i < s.length(); i++) {
				if (digitCount < newCardType.numberLength) {
					switch (newCardType) {
						case UNDEF:
						case VISA:
						case MASTERCARD:
						case DISCOVER:
						case JCB_16:
							if (Character.isDigit(s.charAt(i))) {
								if (digitCount % 4 == 0 && digitCount > 0) {
									formatted.append(" ");
								}
								formatted.append(s.charAt(i));
								digitCount++;
							}
							break;
						case AMEX:
						case JCB_15:
						case DINERS:
							if (Character.isDigit(s.charAt(i))) {
								if (digitCount == 4 || digitCount == 9) {
									formatted.append(" ");
								}
								formatted.append(s.charAt(i));
								digitCount++;
							}
							break;
					}
				}
			}

			final String text = formatted.toString();
			mEtCardNumber.setText(text);

			for (int i = cursorPosition - 1; i >= 0; i--) {
				if (s.charAt(i) == ' ') {
					cursorPosition--;
				}
			}
			for (int i = 0; i < cursorPosition; i++) {
				if (text.charAt(i) == ' ') {
					cursorPosition++;
				}
			}

			mEtCardNumber.setSelection(cursorPosition);

			if (mSpaceDeleted) {
				mEtCardNumber.setSelection(mEtCardNumber.getSelectionStart() - 1);
				mSpaceDeleted = false;
			}

			mEtCardNumber.addTextChangedListener(this);

			mCallback.onNumberDigitEntered(mCardType, isNewCardType, digitCount == mCardType.numberLength);
		}

		private CardType getCardType(Editable s) {
			CardType cardType = CardType.UNDEF;
			if (s.length() >= 1) {
				if (s.charAt(0) == '4') {
					cardType = CardType.VISA;
				}

				if (s.length() >= 2) {
					final char first = s.charAt(0);
					final char second = s.charAt(1);

					if (first == '5') {
						if (second >= '1' && second <= '5') {
							cardType = CardType.MASTERCARD;
						}
					} else if (first == '3') {
						if (second == '4' || second == '7') {
							cardType = CardType.AMEX;
						} else if (second == '6' || second == '8') {
							cardType = CardType.DINERS;
						} else if (second == '5') {
							cardType = CardType.JCB_16;
						}
					} else if (first == '6' && second == '5') {
						cardType = CardType.DISCOVER;
					}

					if (cardType == CardType.UNDEF && s.length() >= 3) {
						final char third = s.charAt(2);

						if (first == '3' && second == '0' && third >= '0' && third <= '5') {
							cardType = CardType.DINERS;
						} else if (s.length() >= 4) {
							final char fourth = s.charAt(3);

							if (first == '6' && second == '0' && third == '1' && fourth == '1') {
								cardType = CardType.DISCOVER;
							} else if (first == '2' && second == '1' && third == '3' && fourth == '1') {
								return CardType.JCB_15;
							} else if (first == '1' && second == '8' && third == '0' && fourth == '0') {
								return CardType.JCB_15;
							}
						}
					}
				}
			}
			return cardType;
		}
	}

	private static class CreditCardDetailTextWatcher implements TextWatcher {
		public static final int TYPE_EXP_MONTH = 0;
		public static final int TYPE_EXP_YEAR = 1;
		public static final int TYPE_CARDHOLDER_NAME = 3;

		private CardEntryListener mCallback;
		private int mType;
		private int mMaxLength;

		public CreditCardDetailTextWatcher(CardEntryListener listener, int type) {
			mCallback = listener;
			mType = type;

			if (type == TYPE_EXP_MONTH || type == TYPE_EXP_YEAR) {
				mMaxLength = 2;
			} else {
				mMaxLength = Integer.MAX_VALUE;
			}
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) { }

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

		@Override
		public void afterTextChanged(Editable s) {
			switch (mType) {
				case TYPE_EXP_MONTH:
					if (s.length() == 1 && s.charAt(0) >= '2' && s.charAt(0) <= '9') {
						s.insert(0, "0");
					}
					mCallback.onMonthDigitEntered(s.length() == mMaxLength);
					break;
				case TYPE_EXP_YEAR:
					mCallback.onYearDigitEntered(s.length() == mMaxLength);
					break;
				case TYPE_CARDHOLDER_NAME:
					mCallback.onCardholderNameLetterEntered();
					break;
			}
		}
	}
}
