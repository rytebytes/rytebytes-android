package com.myrytebytes.remote;

import com.myrytebytes.datamodel.Location;
import com.myrytebytes.datamodel.StripeCustomer;
import com.myrytebytes.datamodel.StripeToken;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.List;

public class ApiListener {

	public interface GetMenuListener {
		public void onComplete(boolean success, int statusCode);
	}

	public interface GetLocationsListener {
		public void onComplete(List<Location> locations, int statusCode);
	}

    public interface CreateStripeTokenListener {
        public void onComplete(StripeToken token, int statusCode);
    }

	public interface CreateAccountListener {
		public void onComplete(ParseUser user, ParseException exception);
	}

    public interface UpdateUserListener {
        public void onComplete(boolean success);
    }

	public interface LoginListener {
		public void onComplete(ParseUser user, ParseException exception);
	}

    public interface PurchaseListener {
        public void onComplete(boolean success, String errorMessage, int statusCode);
    }

    public interface ResetPasswordListener {
        public void onComplete(boolean success);
    }

    public interface GetLocationListener {
        public void onComplete(Location location, int statusCode);
    }

    public interface GetUserInfoListener {
        public void onComplete(StripeCustomer customer, int statusCode);
    }

    public interface UpdateUserInfoListener {
        public void onComplete(boolean success, int statusCode);
    }

    public interface UpdateHeatingInstructionsListener {
        public void onComplete(boolean updated);
    }
}
