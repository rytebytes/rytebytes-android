package com.myrytebytes.datamodel;

import java.util.ArrayList;
import java.util.List;

public class Order {

	private List<OrderItem> mOrderItems;

	private static Order sharedOrder;

	public Order() {
		mOrderItems = new ArrayList<>();
	}

	public static Order getSharedOrder() {
		if (sharedOrder == null) {
			sharedOrder = new Order();
		}
		return sharedOrder;
	}

	public void clear() {
		mOrderItems.clear();
	}

	public int getQuantity(MenuItem menuItem) {
		for (OrderItem orderItem : mOrderItems) {
			if (orderItem.menuItem.uid.equals(menuItem.uid)) {
				return orderItem.quantity;
			}
		}

		return 0;
	}

	public int getItemTotal() {
		int total = 0;
		for (OrderItem orderItem : mOrderItems) {
			total += orderItem.quantity;
		}
		return total;
	}

	public int getUniqueItemTotal() {
		return mOrderItems.size();
	}

	public void add(MenuItem menuItem) {
		boolean added = false;
		for (OrderItem orderItem : mOrderItems) {
			if (orderItem.menuItem.uid.equals(menuItem.uid)) {
				orderItem.quantity++;
				added = true;
				break;
			}
		}

		if (!added) {
			mOrderItems.add(new OrderItem(menuItem, 1));
		}
	}

	public void remove(MenuItem menuItem) {
		for (int i = mOrderItems.size() - 1; i >= 0; i--) {
			OrderItem orderItem = mOrderItems.get(i);

			if (orderItem.menuItem.uid.equals(menuItem.uid)) {
				orderItem.quantity--;

				if (orderItem.quantity == 0) {
					mOrderItems.remove(i);
				}
				break;
			}
		}
	}
}
