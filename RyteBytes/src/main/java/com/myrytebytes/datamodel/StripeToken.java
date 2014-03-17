package com.myrytebytes.datamodel;

import com.myrytebytes.datamanagement.Logr;
import com.myrytebytes.remote.JsonHandler;
import com.myrytebytes.remote.JsonHandler.JsonHandlerListenerAdapter;
import com.myrytebytes.remote.SafeJsonParser;

import java.io.IOException;

public class StripeToken implements JacksonParser {

	public String id;
	public StripeCard card;

	public StripeToken() { }

	public StripeToken(SafeJsonParser jsonParser, boolean closeWhenComplete) {
		try {
			fillFromJSON(jsonParser, closeWhenComplete);
		} catch (IOException e) {
			Logr.e("Error filling StripeToken", e);
		}
	}

	@Override
	public void fillFromJSON(SafeJsonParser jsonParser, boolean closeWhenComplete) throws IOException {
		JsonHandler.handleJson(jsonParser, new JsonHandlerListenerAdapter() {
			@Override
			public boolean onObject(String tag, SafeJsonParser jsonParser) throws IOException {
				if (tag.equals("card")) {
					card = new StripeCard(jsonParser, false);
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
				}
			}
        }, closeWhenComplete);
	}
}
