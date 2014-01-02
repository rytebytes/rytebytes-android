package com.myrytebytes.datamodel;

import com.myrytebytes.datamanagement.Log;
import com.myrytebytes.remote.JsonHandler;
import com.myrytebytes.remote.JsonHandler.JsonHandlerListenerAdapter;
import com.myrytebytes.remote.SafeJsonParser;

import java.io.IOException;

public class Charity implements JacksonParser {

	public String name;
	public String objectId;
	public String streetAddress;
	public String city;
	public String state;
	public String zipcode;
    public int totalDonationsInCents;
    public String image;

	public Charity() { }

	public Charity(SafeJsonParser jsonParser, boolean closeWhenComplete) {
		try {
			fillFromJSON(jsonParser, closeWhenComplete);
		} catch (IOException e) {
			Log.e("Error filling Charity", e);
		}
	}

	@Override
	public void fillFromJSON(SafeJsonParser jsonParser, boolean closeWhenComplete) throws IOException {
		JsonHandler.handleJson(jsonParser, new JsonHandlerListenerAdapter() {
			@Override
			public void onField(String tag, SafeJsonParser jsonParser) throws IOException {
				switch (tag) {
					case "name":
						name = jsonParser.getStringValue();
						break;
					case "streetAddress":
						streetAddress = jsonParser.getStringValue();
						break;
					case "city":
						city = jsonParser.getStringValue();
						break;
					case "state":
						state = jsonParser.getStringValue();
						break;
					case "zipcode":
						zipcode = jsonParser.getStringValue();
						break;
					case "objectId":
						objectId = jsonParser.getStringValue();
						break;
                    case "totalDonationsInCents":
                        totalDonationsInCents = jsonParser.getIntValue();
                        break;
                    case "picture":
                        image = jsonParser.getStringValue();
                        break;
				}
			}
		}, closeWhenComplete);
	}
}
