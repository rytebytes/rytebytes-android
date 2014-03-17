package com.myrytebytes.datamodel;

import com.fasterxml.jackson.core.JsonGenerator;
import com.myrytebytes.datamanagement.Logr;
import com.myrytebytes.remote.JsonHandler;
import com.myrytebytes.remote.JsonHandler.JsonHandlerListenerAdapter;
import com.myrytebytes.remote.JsonRequest;
import com.myrytebytes.remote.SafeJsonParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Location implements JacksonParser {

	public String name;
	public String objectId;
	public Charity charity;
	public String streetAddress;
	public String city;
	public String state;
	public String zipcode;

	public Location() { }

	public Location(SafeJsonParser jsonParser, boolean closeWhenComplete) {
		try {
			fillFromJSON(jsonParser, closeWhenComplete);
		} catch (IOException e) {
			Logr.e("Error filling Location", e);
		}
	}

	@Override
	public void fillFromJSON(SafeJsonParser jsonParser, boolean closeWhenComplete) throws IOException {
		JsonHandler.handleJson(jsonParser, new JsonHandlerListenerAdapter() {
			@Override
			public boolean onObject(String tag, SafeJsonParser jsonParser) throws IOException {
				if (tag.equals("charityId")) {
					charity = new Charity(jsonParser, false);
					return true;
				} else {
					return false;
				}
			}

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
				}
			}
		}, closeWhenComplete);
	}

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Location)) {
            return false;
        } else {
            return objectId.equals(((Location) o).objectId);
        }
    }

    public String toJSON() {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            JsonGenerator generator = JsonRequest.JSON_FACTORY.createGenerator(os);
            generator.writeStartObject();
            writeJson(generator);
            generator.writeEndObject();
            generator.close();
            return os.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public void writeJson(JsonGenerator generator) {
        try {
            generator.writeStringField("name", name);
            generator.writeStringField("streetAddress", streetAddress);
            generator.writeStringField("city", city);
            generator.writeStringField("state", state);
            generator.writeStringField("zipcode", zipcode);
            generator.writeStringField("objectId", objectId);
            generator.writeObjectFieldStart("charityId");
            charity.writeJson(generator);
            generator.writeEndObject();
        } catch (Exception e) {
            Logr.e(e);
        }
    }
}
