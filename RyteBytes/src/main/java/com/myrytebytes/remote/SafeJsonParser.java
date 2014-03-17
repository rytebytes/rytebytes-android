package com.myrytebytes.remote;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.myrytebytes.datamanagement.DateFormatters;
import com.myrytebytes.datamanagement.Logr;

import java.io.IOException;
import java.util.Date;

public class SafeJsonParser {

	private JsonParser mJsonParser;

	public SafeJsonParser(JsonParser jsonParser) {
		mJsonParser = jsonParser;
		try {
			if (mJsonParser.getCurrentToken() == null) {
				mJsonParser.nextToken();
			}
		} catch (Exception e) { }
	}

	public JsonToken nextToken() throws IOException {
		return mJsonParser.nextToken();
	}

	public JsonToken getCurrentToken() {
		return mJsonParser.getCurrentToken();
	}

	public String getCurrentName() throws IOException {
		return mJsonParser.getCurrentName();
	}

	public String getStringValue() throws IOException {
		JsonToken token = mJsonParser.getCurrentToken();
		if (token == JsonToken.VALUE_STRING) {
			return mJsonParser.getValueAsString();
		} else if (token == JsonToken.VALUE_NUMBER_FLOAT) {
			return "" + mJsonParser.getFloatValue();
		} else if (token == JsonToken.VALUE_NUMBER_INT) {
			return "" + mJsonParser.getIntValue();
		} else if (token == JsonToken.VALUE_FALSE) {
			return "false";
		} else if (token == JsonToken.VALUE_TRUE) {
			return "true";
		} else {
			return null;
		}
	}

	public Integer getIntValue() throws IOException {
		JsonToken token = mJsonParser.getCurrentToken();
		if (token == JsonToken.VALUE_NUMBER_INT) {
			return mJsonParser.getIntValue();
		} else if (token == JsonToken.VALUE_STRING) {
			try {
				return Integer.parseInt(mJsonParser.getValueAsString());
			} catch (Exception e) {
				return null;
			}
		} else if (token == JsonToken.VALUE_NUMBER_FLOAT) {
			return (int)mJsonParser.getFloatValue();
		} else if (token == JsonToken.VALUE_FALSE) {
			return 0;
		} else if (token == JsonToken.VALUE_TRUE) {
			return 1;
		} else {
			return null;
		}
	}

	public Float getFloatValue() throws IOException {
		JsonToken token = mJsonParser.getCurrentToken();
		if (token == JsonToken.VALUE_NUMBER_FLOAT) {
			return mJsonParser.getFloatValue();
		} else if (token == JsonToken.VALUE_STRING) {
			try {
				return Float.parseFloat(mJsonParser.getValueAsString());
			} catch (Exception e) {
				return null;
			}
		} else if (token == JsonToken.VALUE_NUMBER_INT) {
			return (float)mJsonParser.getIntValue();
		} else if (token == JsonToken.VALUE_FALSE) {
			return 0f;
		} else if (token == JsonToken.VALUE_TRUE) {
			return 1f;
		} else {
			return null;
		}
	}

	public Long getLongValue() throws IOException {
		JsonToken token = mJsonParser.getCurrentToken();
		if (token == JsonToken.VALUE_NUMBER_INT) {
			return mJsonParser.getLongValue();
		} else if (token == JsonToken.VALUE_STRING) {
			try {
				return Long.parseLong(mJsonParser.getValueAsString());
			} catch (Exception e) {
				return null;
			}
		} else if (token == JsonToken.VALUE_NUMBER_FLOAT) {
			return (long)mJsonParser.getDoubleValue();
		} else if (token == JsonToken.VALUE_FALSE) {
			return 0l;
		} else if (token == JsonToken.VALUE_TRUE) {
			return 1l;
		} else {
			return null;
		}
	}

	public Double getDoubleValue() throws IOException {
		JsonToken token = mJsonParser.getCurrentToken();
		if (token == JsonToken.VALUE_NUMBER_FLOAT) {
			return mJsonParser.getDoubleValue();
		} else if (token == JsonToken.VALUE_STRING) {
			try {
				return Double.parseDouble(mJsonParser.getValueAsString());
			} catch (Exception e) {
				return null;
			}
		} else if (token == JsonToken.VALUE_NUMBER_INT) {
			return (double)mJsonParser.getIntValue();
		} else if (token == JsonToken.VALUE_FALSE) {
			return 0d;
		} else if (token == JsonToken.VALUE_TRUE) {
			return 1d;
		} else {
			return null;
		}
	}

	public Boolean getBooleanValue() throws IOException {
		JsonToken token = mJsonParser.getCurrentToken();
		if (token == JsonToken.VALUE_FALSE) {
			return false;
		} else if (token == JsonToken.VALUE_TRUE) {
			return true;
		} else if (token == JsonToken.VALUE_STRING) {
			try {
				return Boolean.parseBoolean(mJsonParser.getValueAsString());
			} catch (Exception e) {
				return null;
			}
		} else if (token == JsonToken.VALUE_NUMBER_FLOAT) {
			return mJsonParser.getFloatValue() == 1;
		} else if (token == JsonToken.VALUE_NUMBER_INT) {
			return mJsonParser.getIntValue() == 1;
		} else {
			return null;
		}
	}

	public Date getDateValue(DateFormatters.DateFormatter dateFormatter) throws IOException {
		String stringValue = getStringValue();
		try {
			return DateFormatters.formatWithFormatter(dateFormatter).parse(stringValue);
		} catch (Exception e) {
			Logr.e("Unable to parse date " + stringValue + " for " + getCurrentName());
			e.printStackTrace();
			return null;
		}
	}

	public void close() throws IOException {
		mJsonParser.close();
	}
}
