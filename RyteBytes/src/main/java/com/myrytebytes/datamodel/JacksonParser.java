package com.myrytebytes.datamodel;

import com.myrytebytes.remote.SafeJsonParser;

import java.io.IOException;

public interface JacksonParser {
	public void fillFromJSON(SafeJsonParser jsonParser, boolean closeWhenComplete) throws IOException;
}
