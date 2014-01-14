package com.myrytebytes.datamodel;

import com.myrytebytes.datamanagement.Log;
import com.myrytebytes.remote.JsonHandler;
import com.myrytebytes.remote.JsonHandler.JsonHandlerListenerAdapter;
import com.myrytebytes.remote.SafeJsonParser;

import java.io.IOException;

public class LocationItem implements JacksonParser {

    public String objectId;
    public Location location;
    public MenuItem menuItem;
    public int quantity;

	public LocationItem() { }

	public LocationItem(SafeJsonParser jsonParser, boolean closeWhenComplete) {
		try {
			fillFromJSON(jsonParser, closeWhenComplete);
		} catch (IOException e) {
			Log.e("Error filling LocationItem", e);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof LocationItem)) {
			return false;
		} else {
			return objectId.equals(((LocationItem)o).objectId);
		}
	}

	@Override
	public int hashCode() {
		return objectId.hashCode();
	}

	@Override
	public void fillFromJSON(SafeJsonParser jsonParser, boolean closeWhenComplete) throws IOException {
		JsonHandler.handleJson(jsonParser, new JsonHandlerListenerAdapter() {
			@Override
			public boolean onObject(String tag, SafeJsonParser jsonParser) throws IOException {
                switch (tag) {
                    case "menuItemId":
                        menuItem = new MenuItem(jsonParser, false);
                        return true;
                    case "locationId":
                        location = new Location(jsonParser, false);
                        return true;
                    default:
                        return false;
                }
			}

			@Override
			public void onField(String tag, SafeJsonParser jsonParser) throws IOException {
				switch (tag) {
					case "objectId":
						objectId = jsonParser.getStringValue();
						break;
                    case "quantity":
                        quantity = jsonParser.getIntValue();
                        break;
				}
			}
		}, closeWhenComplete);
	}
}
