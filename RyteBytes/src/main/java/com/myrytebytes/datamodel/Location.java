package com.myrytebytes.datamodel;

import com.myrytebytes.datamanagement.Log;
import com.myrytebytes.remote.JsonHandler;
import com.myrytebytes.remote.JsonHandler.JsonHandlerListenerAdapter;
import com.myrytebytes.remote.SafeJsonParser;

import java.io.IOException;

public class Location implements JacksonParser {

	public String name;
	public String objectId;
	public String charityId;
	public String streetAddress;
	public String city;
	public String state;
	public String zipcode;

	public Location() { }

	public Location(SafeJsonParser jsonParser, boolean closeWhenComplete) {
		try {
			fillFromJSON(jsonParser, closeWhenComplete);
		} catch (IOException e) {
			Log.e("Error filling Location", e);
		}
	}

	@Override
	public void fillFromJSON(SafeJsonParser jsonParser, boolean closeWhenComplete) throws IOException {
		JsonHandler.handleJson(jsonParser, new JsonHandlerListenerAdapter() {
			@Override
			public boolean onObject(String tag, SafeJsonParser jsonParser) throws IOException {
				if (tag.equals("charityId")) {
					charityId = new CharityPointer(jsonParser, false).objectId;
					return true;
				} else {
					return false;
				}
			}

			@Override
			public void onField(String tag, SafeJsonParser jsonParser) throws IOException {
				switch (tag) {
					case "Name":
						name = jsonParser.getStringValue();
						break;
					case "StreetAddress":
						streetAddress = jsonParser.getStringValue();
						break;
					case "City":
						city = jsonParser.getStringValue();
						break;
					case "State":
						state = jsonParser.getStringValue();
						break;
					case "Zipcode":
						zipcode = jsonParser.getStringValue();
						break;
					case "objectId":
						objectId = jsonParser.getStringValue();
						break;
				}
			}
		}, closeWhenComplete);
	}

	private static class CharityPointer implements JacksonParser {

		public String objectId;

		public CharityPointer() { }

		public CharityPointer(SafeJsonParser jsonParser, boolean closeWhenComplete) {
			try {
				fillFromJSON(jsonParser, closeWhenComplete);
			} catch (IOException e) {
				Log.e("Error filling CharityPointer", e);
			}
		}

		@Override
		public void fillFromJSON(SafeJsonParser jsonParser, boolean closeWhenComplete) throws IOException {
			JsonHandler.handleJson(jsonParser, new JsonHandlerListenerAdapter() {
				@Override
				public void onField(String tag, SafeJsonParser jsonParser) throws IOException {
					if (tag.equals("objectId")) {
						objectId = jsonParser.getStringValue();
					}
				}
			}, closeWhenComplete);
		}
	}
}
