package com.myrytebytes.rytebytes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.myrytebytes.datamanagement.Log;
import com.myrytebytes.datamodel.MenuItem;
import com.myrytebytes.datamodel.Order;
import com.myrytebytes.remote.ApiInterface;
import com.myrytebytes.widget.AutoResizeTextView;
import com.myrytebytes.widget.MenuItemImageView;

public class CheckoutFragment extends BaseFragment {

	private TextView mTvOrderTotal;
	private TextView mTvDoRyteDonation;
	private Order mOrder;

	private final OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.btn_place_order:
					//TODO: add locationID
					ApiInterface.placeOrder(mOrder, "");
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

		((ListView)rootView.findViewById(R.id.lv_checkout)).setAdapter(new OrderAdapter(mOrder, inflater));

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

	private static class OrderAdapter extends BaseAdapter {

		private final LayoutInflater mLayoutInflater;
		private final Order mOrder;

		private OrderAdapter(Order order, LayoutInflater layoutInflater) {
			mOrder = order;
			mLayoutInflater = layoutInflater;
		}

		@Override
		public int getCount() {
			return mOrder.getUniqueItemTotal();
		}

		@Override
		public Object getItem(int position) {
			return mOrder.getItemAtPosition(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mLayoutInflater.inflate(R.layout.row_checkout_item, parent, false);

				ViewHolder viewHolder = new ViewHolder();
				viewHolder.imageView = (MenuItemImageView)convertView.findViewById(R.id.img_menu_item);
				viewHolder.tvTitle = (AutoResizeTextView)convertView.findViewById(R.id.tv_title);
				viewHolder.tvPrice = (AutoResizeTextView)convertView.findViewById(R.id.tv_price);

				convertView.setTag(viewHolder);
			}

			MenuItem menuItem = mOrder.getItemAtPosition(position);
			int quantity = mOrder.getQuantity(menuItem);
			ViewHolder viewHolder = (ViewHolder)convertView.getTag();
			viewHolder.imageView.setMenuItem(menuItem);
			viewHolder.tvTitle.setText(menuItem.name);
			Log.d("name = " + menuItem.name);
			viewHolder.tvPrice.setText(quantity + " x $" + String.format("%.2f", menuItem.price / 100f) + " = $" + String.format("%.2f", menuItem.price * quantity / 100f));

			return convertView;
		}
	}

	private static class ViewHolder {
		public MenuItemImageView imageView;
		public AutoResizeTextView tvTitle;
		public AutoResizeTextView tvPrice;
	}
}
