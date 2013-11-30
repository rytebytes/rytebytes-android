package com.myrytebytes.remote;

import com.myrytebytes.datamodel.MenuItem;
import com.myrytebytes.datamodel.StripeCustomer;

import java.util.List;

public class ApiListener {

	public interface GetMenuListener {
		public void onComplete(List<MenuItem> menu, int statusCode);
	}

	public interface CreateStripeAccountListener {
		public void onComplete(StripeCustomer customer, int statusCode);
	}
}
