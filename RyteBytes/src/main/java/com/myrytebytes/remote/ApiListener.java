package com.myrytebytes.remote;

import com.myrytebytes.datamodel.Location;
import com.myrytebytes.datamodel.MenuItem;
import com.myrytebytes.datamodel.StripeCustomer;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.List;

public class ApiListener {

	public interface GetMenuListener {
		public void onComplete(List<MenuItem> menu, int statusCode);
	}

	public interface GetLocationsListener {
		public void onComplete(List<Location> locations, int statusCode);
	}

	public interface CreateStripeAccountListener {
		public void onComplete(StripeCustomer customer, int statusCode);
	}

	public interface CreateAccountListener {
		public void onComplete(ParseUser user, ParseException exception);
	}

	public interface LoginListener {
		public void onComplete(ParseUser user, ParseException exception);
	}

    public interface PurchaseListener {
        public void onComplete(boolean success, int statusCode);
    }

    public interface ResetPasswordListener {
        public void onComplete(boolean success);
    }
}
