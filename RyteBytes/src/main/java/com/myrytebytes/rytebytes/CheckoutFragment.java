package com.myrytebytes.rytebytes;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.myrytebytes.datamanagement.MenuQuantityManager;
import com.myrytebytes.datamanagement.UserController;
import com.myrytebytes.datamodel.MenuItem;
import com.myrytebytes.datamodel.Order;
import com.myrytebytes.datamodel.User;
import com.myrytebytes.remote.ApiInterface;
import com.myrytebytes.remote.ApiListener.PurchaseListener;
import com.myrytebytes.widget.AutoResizeTextView;
import com.myrytebytes.widget.ButtonSpinner;
import com.myrytebytes.widget.ButtonSpinner.ButtonSpinnerListener;
import com.myrytebytes.widget.HoloDialog;
import com.myrytebytes.widget.MenuItemImageView;

public class CheckoutFragment extends BaseFragment {

    private View mEmptyView;
	private TextView mTvOrderTotal;
    private TextView mTvPickupLocation;
    private TextView mTvDoRyte;
    private View mBtnPlaceOrder;
    private OrderAdapter mOrderAdapter;
	private Order mOrder;
    private Dialog mProgressDialog;

	private final OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.btn_place_order:
                    if (verifyQuantitiesAvailable()) {
                        User user = UserController.getActiveUser();
                        if (user == null) {
                            mActivityCallbacks.displayLoginFragment(false);
                        } else {
                            mProgressDialog = HoloDialog.showProgressDialog(getActivity(), null, "Placing order...", false);
                            ApiInterface.placeOrder(mOrder, user.location.objectId, mPurchaseListener);
                        }
                    }
					break;
                case R.id.btn_add_items:
                    mActivityCallbacks.popToRoot(true);
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
    private final PurchaseListener mPurchaseListener = new PurchaseListener() {
        @Override
        public void onComplete(boolean success, int statusCode) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }

            if (success) {
                mOrder.clear();
                mActivityCallbacks.updateCheckoutBadge();
                showOkDialog("Success!", "Enjoy your dinner - a receipt will be emailed to you shortly!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mActivityCallbacks.popToRoot(true);
                    }
                });
            } else {
                //TODO: real error messages should be shown here
                showOkDialog("Error", "An error occurred while placing your order. Please try again soon!");
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

        mEmptyView = rootView.findViewById(R.id.empty_view);
        mEmptyView.findViewById(R.id.btn_add_items).setOnClickListener(mOnClickListener);

		mTvOrderTotal = (TextView)rootView.findViewById(R.id.tv_order_total);
        mTvPickupLocation = (TextView)rootView.findViewById(R.id.tv_pickup_location);
        mTvDoRyte = (TextView)rootView.findViewById(R.id.tv_do_ryte_donation);

        mBtnPlaceOrder = rootView.findViewById(R.id.btn_place_order);
		mBtnPlaceOrder.setOnClickListener(mOnClickListener);

        mOrderAdapter = new OrderAdapter(mOrder, mOrderAdapterListener, inflater);
		((ListView)rootView.findViewById(R.id.lv_checkout)).setAdapter(mOrderAdapter);

        User user = UserController.getActiveUser();
        if (user != null) {
            mTvPickupLocation.setText("Pickup Location: " + user.location.name);
        } else {
            mTvPickupLocation.setText("");
        }

        setTotals();

		return rootView;
	}

    public boolean verifyQuantitiesAvailable() {
        boolean success = true;

        for (int i = 0; i < mOrder.getUniqueItemTotal(); i++) {
            MenuItem item = mOrder.getItemAtPosition(i);
            int quantity = mOrder.getQuantity(item);
            int available = MenuQuantityManager.getAvailableQuantity(item);

            if (available < quantity) {
                success = false;
                mOrder.setQuantity(item, available);
            }
        }

        if (!success) {
            showOkDialog("Items Unavailable", "Oh no! It looks like not all of the items you're looking for are currently available at your location. Your cart has been updated with what we have available.");
            mOrderAdapter.notifyDataSetChanged();
            mActivityCallbacks.updateCheckoutBadge();
            setTotals();
        }
        return success;
    }

	public void setTotals() {
        if (mOrder.getItemTotal() == 0) {
            mEmptyView.setVisibility(View.VISIBLE);
            mTvOrderTotal.setVisibility(View.GONE);
            mTvPickupLocation.setVisibility(View.GONE);
            mTvDoRyte.setVisibility(View.GONE);
            mBtnPlaceOrder.setVisibility(View.GONE);
        } else {
            mEmptyView.setVisibility(View.GONE);
            mTvOrderTotal.setVisibility(View.VISIBLE);
            mTvPickupLocation.setVisibility(View.VISIBLE);
            mTvDoRyte.setVisibility(View.VISIBLE);
            mBtnPlaceOrder.setVisibility(View.VISIBLE);

            final float orderTotal = mOrder.getTotalPrice() / 100f;
            mTvOrderTotal.setText("Order Total: $" + String.format("%.2f", orderTotal));
        }
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
        verifyQuantitiesAvailable();
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
                String[] contents = new String[MenuQuantityManager.getAvailableQuantity((MenuItem)spinner.getTag()) + 1];
                for (int i = 0; i < contents.length; i++) {
                    contents[i] = ""+i;
                }
                return contents;
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
