package com.myrytebytes.datamodel;

import com.fasterxml.jackson.core.JsonGenerator;
import com.myrytebytes.datamanagement.Log;
import com.myrytebytes.remote.JsonHandler;
import com.myrytebytes.remote.JsonHandler.JsonHandlerListenerAdapter;
import com.myrytebytes.remote.JsonRequest;
import com.myrytebytes.remote.SafeJsonParser;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class User implements JacksonParser {

    public String emailAddress;
    public Location location;
    public ParseUser parseUser;

    public User(ParseUser parseUser, Location location) {
        this.parseUser = parseUser;
        this.emailAddress = parseUser.getEmail();
        this.location = location;
    }

    public User(SafeJsonParser jsonParser, boolean closeWhenComplete) {
        try {
            fillFromJSON(jsonParser, closeWhenComplete);
            parseUser = new ParseUser();
            parseUser.setUsername(emailAddress);
            parseUser.setEmail(emailAddress);
            parseUser.put("locationId", ParseObject.createWithoutData("Location", location.objectId));
        } catch (IOException e) {
            Log.e("Error filling User", e);
        }
    }

    @Override
    public void fillFromJSON(SafeJsonParser jsonParser, boolean closeWhenComplete) throws IOException {
        JsonHandler.handleJson(jsonParser, new JsonHandlerListenerAdapter() {
            @Override
            public boolean onObject(String tag, SafeJsonParser jsonParser) throws IOException {
                if (tag.equals("location")) {
                    location = new Location(jsonParser, false);
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public void onField(String tag, SafeJsonParser jsonParser) throws IOException {
                switch (tag) {
                    case "emailAddress":
                        emailAddress = jsonParser.getStringValue();
                        break;
                }
            }
        }, closeWhenComplete);
    }

    public String toJSON() {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            JsonGenerator generator = JsonRequest.JSON_FACTORY.createGenerator(os);
            generator.writeStartObject();
            generator.writeStringField("emailAddress", emailAddress);
            generator.writeObjectFieldStart("location");
            location.writeJson(generator);
            generator.writeEndObject();
            generator.writeEndObject();
            generator.close();
            return os.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
