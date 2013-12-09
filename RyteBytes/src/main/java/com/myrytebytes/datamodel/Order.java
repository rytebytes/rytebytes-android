package com.myrytebytes.datamodel;

import java.util.HashMap;
import java.util.Map;

public class Order {

	private Map<MenuItem, Integer> mOrderItemMap;

	private static Order sharedOrder;

	public Order() {
		mOrderItemMap = new HashMap<>();
	}

	public static Order getSharedOrder() {
		if (sharedOrder == null) {
			sharedOrder = new Order();
		}
		return sharedOrder;
	}

	public void clear() {
		mOrderItemMap.clear();
	}

	public int getQuantity(MenuItem menuItem) {
		Integer quantity = mOrderItemMap.get(menuItem);
		return quantity != null ? quantity : 0;
	}

	public int getItemTotal() {
		int total = 0;
		for (Integer itemTotal : mOrderItemMap.values()) {
			total += itemTotal;
		}
		return total;
	}

	public int getUniqueItemTotal() {
		return mOrderItemMap.size();
	}

	public int getTotalPrice() {
		int total = 0;
		for (MenuItem menuItem : mOrderItemMap.keySet()) {
			total += menuItem.price * mOrderItemMap.get(menuItem);
		}
		return total;
	}

	public void add(MenuItem menuItem) {
		mOrderItemMap.put(menuItem, getQuantity(menuItem) + 1);
	}

	public void remove(MenuItem menuItem) {
		if (mOrderItemMap.containsKey(menuItem)) {
			int newQuantity = getQuantity(menuItem) - 1;
			if (newQuantity <= 0) {
				mOrderItemMap.remove(menuItem);
			} else {
				mOrderItemMap.put(menuItem, newQuantity);
			}
		}
	}

	public String toJson() {
		StringBuilder sb = new StringBuilder("[");
		sb.append("[");
		for (MenuItem menuItem : mOrderItemMap.keySet()) {
			sb.append("{menuItem:");
			sb.append(menuItem.toString());
			sb.append(",quantity:");
			sb.append(mOrderItemMap.get(menuItem));
			sb.append("}");
		}
		sb.append("]");

		return sb.toString();
	}

//	private static class OrderItem {
//		public MenuItem menuItem;
//		public int quantity;
//
//		private OrderItem(MenuItem menuItem, int quantity) {
//			this.menuItem = menuItem;
//			this.quantity = quantity;
//		}
//
//		@Override
//		public String toString() {
//			return "{menuItem:" + menuItem + ", quantity:" + quantity + '}';
//		}
//	}
}
