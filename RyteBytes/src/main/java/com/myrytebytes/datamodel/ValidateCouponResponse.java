package com.myrytebytes.datamodel;

import com.myrytebytes.datamanagement.Logr;
import com.myrytebytes.remote.JsonHandler;
import com.myrytebytes.remote.JsonHandler.JsonHandlerListenerAdapter;
import com.myrytebytes.remote.SafeJsonParser;

import java.io.IOException;

public class ValidateCouponResponse implements JacksonParser {

    public boolean valid;
    public String message;
    public int amount;

    public ValidateCouponResponse() { }

    public ValidateCouponResponse(SafeJsonParser jsonParser, boolean closeWhenComplete) {
        try {
            fillFromJSON(jsonParser, closeWhenComplete);
        } catch (IOException e) {
            Logr.e("Error filling ValidateCouponResponse", e);
        }
    }

    @Override
    public void fillFromJSON(SafeJsonParser jsonParser, boolean closeWhenComplete) throws IOException {
        JsonHandler.handleJson(jsonParser, new JsonHandlerListenerAdapter() {
            @Override
            public void onField(String tag, SafeJsonParser jsonParser) throws IOException {
                switch (tag) {
                    case "valid":
                        valid = jsonParser.getBooleanValue();
                        break;
                    case "message":
                        message = jsonParser.getStringValue();
                        break;
                    case "amount":
                        amount = jsonParser.getIntValue();
                        break;
                }
            }
        }, closeWhenComplete);
    }

    @Override
    public String toString() {
        return "ValidateCouponResponse{" +
                "valid=" + valid +
                ", message='" + message + '\'' +
                ", amount=" + amount +
                '}';
    }
}
