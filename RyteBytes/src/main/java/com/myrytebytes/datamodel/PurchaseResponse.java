package com.myrytebytes.datamodel;

import com.myrytebytes.datamanagement.Logr;
import com.myrytebytes.remote.JsonHandler;
import com.myrytebytes.remote.JsonHandler.JsonHandlerListenerAdapter;
import com.myrytebytes.remote.SafeJsonParser;

import java.io.IOException;

public class PurchaseResponse implements JacksonParser {

    public String result;

    public PurchaseResponse() { }

    public PurchaseResponse(SafeJsonParser jsonParser, boolean closeWhenComplete) {
        try {
            fillFromJSON(jsonParser, closeWhenComplete);
        } catch (IOException e) {
            Logr.e("Error filling PurchaseResponse", e);
        }
    }

    @Override
    public void fillFromJSON(SafeJsonParser jsonParser, boolean closeWhenComplete) throws IOException {
        JsonHandler.handleJson(jsonParser, new JsonHandlerListenerAdapter() {
            @Override
            public void onField(String tag, SafeJsonParser jsonParser) throws IOException {
                if (tag.equals("result")) {
                    result = jsonParser.getStringValue();
                }
            }
        }, closeWhenComplete);
    }
}
