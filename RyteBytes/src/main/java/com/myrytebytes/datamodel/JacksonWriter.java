package com.myrytebytes.datamodel;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;

public interface JacksonWriter {
	public void writeJSON(JsonGenerator generator) throws IOException;
}
