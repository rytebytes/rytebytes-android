package com.myrytebytes.datamodel;

import com.myrytebytes.datamanagement.Logr;
import com.myrytebytes.remote.JsonHandler;
import com.myrytebytes.remote.JsonHandler.JsonHandlerListenerAdapter;
import com.myrytebytes.remote.SafeJsonParser;

import java.io.IOException;

public class StripeCard implements JacksonParser {

	public String id;
	public Integer last4;
	public String type;
	public Integer expMonth;
	public Integer expYear;
	public String customer;

	public StripeCard() { }

	public StripeCard(SafeJsonParser jsonParser, boolean closeWhenComplete) {
		try {
			fillFromJSON(jsonParser, closeWhenComplete);
		} catch (IOException e) {
			Logr.e("Error filling StripeCard", e);
		}
	}

	@Override
	public void fillFromJSON(SafeJsonParser jsonParser, boolean closeWhenComplete) throws IOException {
		JsonHandler.handleJson(jsonParser, new JsonHandlerListenerAdapter() {
			@Override
			public void onField(String tag, SafeJsonParser jsonParser) throws IOException {
				switch (tag) {
					case "id":
						id = jsonParser.getStringValue();
						break;
					case "last4":
						last4 = jsonParser.getIntValue();
						break;
					case "type":
						type = jsonParser.getStringValue();
						break;
					case "exp_month":
						expMonth = jsonParser.getIntValue();
						break;
					case "exp_year":
						expYear = jsonParser.getIntValue();
						break;
					case "customer":
						customer = jsonParser.getStringValue();
						break;
				}
			}
		}, closeWhenComplete);
	}
}
