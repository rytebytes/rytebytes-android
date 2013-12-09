package com.myrytebytes.rytebytes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.myrytebytes.datamanagement.Log;
import com.myrytebytes.datamodel.Order;

public class CheckoutFragment extends BaseFragment {

	private TextView mTvOrderTotal;
	private TextView mTvDoRyteDonation;
	private Order mOrder;

	private final OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.btn_place_order:
					Log.d("place order called");
					break;
			}
		}
	};

	public static CheckoutFragment newInstance() {
		return new CheckoutFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mOrder = Order.getSharedOrder();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_checkout, container, false);

		mTvOrderTotal = (TextView)rootView.findViewById(R.id.tv_order_total);
		mTvDoRyteDonation = (TextView)rootView.findViewById(R.id.tv_do_ryte_donation);
		rootView.findViewById(R.id.btn_place_order).setOnClickListener(mOnClickListener);

		setTotals();

		return rootView;
	}

	public void setTotals() {
		final float orderTotal = mOrder.getTotalPrice() / 100f;
		mTvOrderTotal.setText("Order Total: $" + String.format("%.2f", orderTotal));
		mTvDoRyteDonation.setText("Do Ryte Donation (Estimated): $" + String.format("%.2f", orderTotal * 0.05));
	}

	@Override
	protected ContentType getContentType() {
		return ContentType.CHECKOUT;
	}

	@Override
	protected int getTitle() {
		return R.string.checkout;
	}

	@Override
	protected void onShown() {

	}
}
