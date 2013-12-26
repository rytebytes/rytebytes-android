package com.myrytebytes.rytebytes;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.myrytebytes.datamodel.MenuItem;
import com.myrytebytes.datamodel.Order;
import com.myrytebytes.remote.ApiInterface;
import com.myrytebytes.widget.AutoResizeTextView;
import com.myrytebytes.widget.ButtonSpinner;
import com.myrytebytes.widget.ButtonSpinner.ButtonSpinnerListener;
import com.myrytebytes.widget.HoloDialog;
import com.myrytebytes.widget.MenuItemImageView;

public class CheckoutFragment extends BaseFragment {

	private TextView mTvOrderTotal;
	private TextView mTvDoRyteDonation;
    private OrderAdapter mOrderAdapter;
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
    private final OrderAdapter.OrderAdapterListener mOrderAdapterListener = new OrderAdapter.OrderAdapterListener() {
        @Override
        public void onQuantityChanged(MenuItem menuItem, int newQuantity) {
            if (newQuantity == 0) {
                displayRemoveItemDialog(menuItem);
            } else {
                mOrder.setQuantity(menuItem, newQuantity);
                mOrderAdapter.notifyDataSetChanged();
                mActivityCallbacks.updateCheckoutBadge();
                setTotals();
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

        mOrderAdapter = new OrderAdapter(mOrder, mOrderAdapterListener, inflater);
		((ListView)rootView.findViewById(R.id.lv_checkout)).setAdapter(mOrderAdapter);

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

    /*package*/ void displayRemoveItemDialog(final MenuItem menuItem) {
        new HoloDialog.Builder(getActivity())
                .setTitle("Remove Item")
                .setMessage("Are you sure you want to remove " + menuItem.name + " from your cart?")
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mOrderAdapter.notifyDataSetChanged();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mOrder.setQuantity(menuItem, 0);
                        mOrderAdapter.notifyDataSetChanged();
                        mActivityCallbacks.updateCheckoutBadge();
                        setTotals();
                    }
                })
                .show();
    }

	private static class OrderAdapter extends BaseAdapter {

        public interface OrderAdapterListener {
            public void onQuantityChanged(MenuItem menuItem, int newQuantity);
        }

		private final LayoutInflater mLayoutInflater;
		private final Order mOrder;
        private final OrderAdapterListener mListener;
        private final ButtonSpinnerListener mQuantitySpinnerListener = new ButtonSpinnerListener() {
            @Override
            public String[] getDropdownContents(ButtonSpinner spinner) {
                return new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
            }

            @Override
            public void onItemSelected(int index, ButtonSpinner spinner) {
                mListener.onQuantityChanged((MenuItem)spinner.getTag(), index);
            }
        };

		private OrderAdapter(Order order, OrderAdapterListener listener, LayoutInflater layoutInflater) {
			mOrder = order;
            mListener = listener;
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
                viewHolder.spinnerQuantity = (ButtonSpinner)convertView.findViewById(R.id.spinner_quantity);
                viewHolder.spinnerQuantity.setListener(mQuantitySpinnerListener);

				convertView.setTag(viewHolder);
			}

			MenuItem menuItem = mOrder.getItemAtPosition(position);
			final int quantity = mOrder.getQuantity(menuItem);
			ViewHolder viewHolder = (ViewHolder)convertView.getTag();
			viewHolder.imageView.setMenuItem(menuItem);
			viewHolder.tvTitle.setText(menuItem.name);
			viewHolder.tvPrice.setText("x $" + String.format("%.2f", menuItem.price / 100f) + " = $" + String.format("%.2f", menuItem.price * quantity / 100f));
            viewHolder.spinnerQuantity.setText(""+mOrder.getQuantity(menuItem));
            viewHolder.spinnerQuantity.setTag(menuItem);
			return convertView;
		}
	}

	private static class ViewHolder {
		public MenuItemImageView imageView;
		public AutoResizeTextView tvTitle;
		public AutoResizeTextView tvPrice;
        public ButtonSpinner spinnerQuantity;
	}
}
