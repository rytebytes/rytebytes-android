package com.myrytebytes.remote;

import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

public class JsonHandler {

	public static void handleJson(SafeJsonParser jsonParser, JsonHandlerListener listener, boolean closeAfter) throws IOException {
		try {
			while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
				if (jsonParser.getCurrentToken() == JsonToken.FIELD_NAME) {
					String tag = jsonParser.getCurrentName();
					if (tag == null) { continue; }

					jsonParser.nextToken();
					JsonToken token = jsonParser.getCurrentToken();

					if (token == JsonToken.START_OBJECT) {
						boolean handled = listener.onObject(tag, jsonParser);
						if (!handled) {
							int objectDepth = 1;
							while (objectDepth > 0) {
								jsonParser.nextToken();
								if (jsonParser.getCurrentToken() == JsonToken.END_OBJECT) {
									objectDepth--;
								} else if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
									objectDepth++;
								}
							}
						}
					} else if (token == JsonToken.START_ARRAY) {
						boolean handled = listener.onArray(tag, jsonParser);
						if (!handled) {
							int arrayDepth = 1;
							while (arrayDepth > 0) {
								jsonParser.nextToken();

								if (jsonParser.getCurrentToken() == JsonToken.END_ARRAY) {
									arrayDepth--;
								} else if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
									arrayDepth++;
								}
							}
						}
					} else if (token != JsonToken.END_ARRAY && token != JsonToken.END_OBJECT) {
						listener.onField(tag, jsonParser);
					}
				} else if (jsonParser.getCurrentToken() == null) {
					// Something went wrong - don't let there be an infinite loop
					break;
				}
			}
		} finally {
			if (closeAfter) {
				try {
					jsonParser.close();
				} catch (Exception e) { }
			}
		}
	}

	public static interface JsonHandlerListener {
		public boolean onObject(String tag, SafeJsonParser jsonParser) throws IOException;
		public boolean onArray(String tag, SafeJsonParser jsonParser) throws IOException;
		public void onField(String tag, SafeJsonParser jsonParser) throws IOException;
	}

	public static class JsonHandlerListenerAdapter implements JsonHandlerListener {
		@Override
		public boolean onObject(String tag, SafeJsonParser jsonParser) throws IOException { return false; }

		@Override
		public boolean onArray(String tag, SafeJsonParser jsonParser) throws IOException { return false; }

		@Override
		public void onField(String tag, SafeJsonParser jsonParser) throws IOException { }
	}
}
