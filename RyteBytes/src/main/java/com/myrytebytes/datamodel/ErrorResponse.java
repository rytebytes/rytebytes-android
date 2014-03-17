package com.myrytebytes.datamodel;

import com.myrytebytes.datamanagement.Logr;
import com.myrytebytes.remote.JsonHandler;
import com.myrytebytes.remote.JsonHandler.JsonHandlerListenerAdapter;
import com.myrytebytes.remote.SafeJsonParser;

import java.io.IOException;

public class ErrorResponse implements JacksonParser {

    public int code;
    public String message;

    public ErrorResponse() { }

    public ErrorResponse(SafeJsonParser jsonParser, boolean closeWhenComplete) {
        try {
            fillFromJSON(jsonParser, closeWhenComplete);
        } catch (IOException e) {
            Logr.e("Error filling ErrorResponse", e);
        }
    }

    @Override
    public void fillFromJSON(SafeJsonParser jsonParser, boolean closeWhenComplete) throws IOException {
        JsonHandler.handleJson(jsonParser, new JsonHandlerListenerAdapter() {
            @Override
            public void onField(String tag, SafeJsonParser jsonParser) throws IOException {
                switch (tag) {
                    case "code":
                        code = jsonParser.getIntValue();
                        break;
                    case "error":
                        message = jsonParser.getStringValue();
                        break;
                }
            }
        }, closeWhenComplete);
    }
}
