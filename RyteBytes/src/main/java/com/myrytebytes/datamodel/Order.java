package com.myrytebytes.datamodel;

import com.fasterxml.jackson.core.JsonGenerator;
import com.myrytebytes.datamanagement.Log;

import java.util.ArrayList;
import java.util.List;

public class Order {

	private final List<OrderItem> mOrderItems;

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
		for (int i = mOrderItems.size() - 1; i >= 0; i--) {
			OrderItem orderItem = mOrderItems.get(i);
			if (orderItem.menuItem.equals(menuItem)) {
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

	public int getTotalPrice() {
		int total = 0;
		for (OrderItem orderItem : mOrderItems) {
			total += orderItem.menuItem.price * orderItem.quantity;
		}
		return total;
	}

    public void setQuantity(MenuItem menuItem, int quantity) {
        OrderItem orderItem = null;

        for (int i = mOrderItems.size() - 1; i >= 0; i--) {
            OrderItem item = mOrderItems.get(i);
            if (item.menuItem.equals(menuItem)) {
                orderItem = item;
                break;
            }
        }

        if (quantity == 0) {
            if (orderItem != null) {
                mOrderItems.remove(orderItem);
            }
        } else {
            if (orderItem == null) {
                mOrderItems.add(new OrderItem(menuItem, quantity));
            } else {
                orderItem.quantity = quantity;
            }
        }
    }

	public void decrementQuantity(MenuItem menuItem) {
		for (int i = mOrderItems.size() - 1; i >= 0; i--) {
			OrderItem orderItem = mOrderItems.get(i);
			if (orderItem.menuItem.equals(menuItem)) {
				if (orderItem.quantity <= 1) {
					mOrderItems.remove(i);
				} else {
					orderItem.quantity--;
				}
				break;
			}
		}
	}

	public void incrementQuantity(MenuItem menuItem) {
		boolean incremented = false;
		for (OrderItem orderItem : mOrderItems) {
			if (orderItem.menuItem.equals(menuItem)) {
				orderItem.quantity++;
				incremented = true;
				break;
			}
		}

		if (!incremented) {
			mOrderItems.add(new OrderItem(menuItem, 1));
		}
	}

	public MenuItem getItemAtPosition(int position) {
		return mOrderItems.get(position).menuItem;
	}

	public void writeJson(JsonGenerator generator) {
		try {
			for (OrderItem orderItem : mOrderItems) {
				generator.writeObjectFieldStart(orderItem.menuItem.objectId);
				generator.writeNumberField("quantity", orderItem.quantity);
                generator.writeEndObject();
			}
		} catch (Exception e) {
			Log.e(e);
		}
	}

	private static class OrderItem {
		public MenuItem menuItem;
		public int quantity;

		public OrderItem(MenuItem menuItem, int quantity) {
			this.menuItem = menuItem;
			this.quantity = quantity;
		}

		public OrderItem(MenuItem menuItem) {
			this.menuItem = menuItem;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) { return true; }
			if (o == null || getClass() != o.getClass()) { return false; }

			OrderItem orderItem = (OrderItem) o;

			if (!menuItem.equals(orderItem.menuItem)) { return false; }

			return true;
		}

		@Override
		public int hashCode() {
			return menuItem.hashCode();
		}
	}
}
