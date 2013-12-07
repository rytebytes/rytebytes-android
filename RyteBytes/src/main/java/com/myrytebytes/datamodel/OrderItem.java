package com.myrytebytes.datamodel;

public class OrderItem {

	public MenuItem menuItem;
	public int quantity;

	public OrderItem(MenuItem menuItem, int quantity) {
		this.menuItem = menuItem;
		this.quantity = quantity;
	}
}
