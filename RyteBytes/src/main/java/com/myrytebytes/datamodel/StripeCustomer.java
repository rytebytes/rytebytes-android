package com.myrytebytes.datamodel;

import com.fasterxml.jackson.core.JsonToken;
import com.myrytebytes.datamanagement.Log;
import com.myrytebytes.remote.JsonHandler;
import com.myrytebytes.remote.JsonHandler.JsonHandlerListenerAdapter;
import com.myrytebytes.remote.SafeJsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StripeCustomer implements JacksonParser {

	public String id;
	public String email;
	public List<StripeCard> cards;
	public String defaultCard;

	public StripeCustomer() { }

	public StripeCustomer(SafeJsonParser jsonParser, boolean closeWhenComplete) {
		try {
			fillFromJSON(jsonParser, closeWhenComplete);
		} catch (IOException e) {
			Log.e("Error filling StripeCustomer", e);
		}
	}

	@Override
	public void fillFromJSON(SafeJsonParser jsonParser, boolean closeWhenComplete) throws IOException {
		JsonHandler.handleJson(jsonParser, new JsonHandlerListenerAdapter() {
			@Override
			public boolean onObject(String tag, SafeJsonParser jsonParser) throws IOException {
				if (tag.equals("cards")) {
					cards = new CardsObject(jsonParser).cards;
					return true;
				} else {
					return false;
				}
			}

			@Override
			public void onField(String tag, SafeJsonParser jsonParser) throws IOException {
				switch (tag) {
					case "id":
						id = jsonParser.getStringValue();
						break;
					case "email":
						email = jsonParser.getStringValue();
						break;
					case "default_card":
						defaultCard = jsonParser.getStringValue();
						break;
				}
			}
		}, closeWhenComplete);
	}

	private static class CardsObject implements JacksonParser {
		public List<StripeCard> cards;

		public CardsObject(SafeJsonParser jsonParser) {
			try {
				fillFromJSON(jsonParser, false);
			} catch (Exception e) {
				Log.e("Error filling StripeCustomer", e);
			}
		}

		@Override
		public void fillFromJSON(SafeJsonParser jsonParser, boolean closeWhenComplete) throws IOException {
			JsonHandler.handleJson(jsonParser, new JsonHandlerListenerAdapter() {
				@Override
				public boolean onArray(String tag, SafeJsonParser jsonParser) throws IOException {
					if (tag.equals("data")) {
						cards = new ArrayList<>();
						while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
							cards.add(new StripeCard(jsonParser, false));
						}
						return true;
					} else {
						return false;
					}
				}
			}, closeWhenComplete);
		}
	}
}
