package com.myrytebytes.datamanagement;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DateFormatters {

	private static final Map<DateFormatter, DateFormat> FORMATTER_MAP = new HashMap<>();

	public enum DateFormatter {
		ISO8601NoMsZ
	}

	private static void addToMap(DateFormatter formatter) {
		switch (formatter) {
			case ISO8601NoMsZ:
				FORMATTER_MAP.put(formatter, new ZDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US));
				break;
		}
	}

	public static DateFormat formatWithFormatter(DateFormatter formatter) {
		if (!FORMATTER_MAP.containsKey(formatter)) {
			addToMap(formatter);
		}
		return FORMATTER_MAP.get(formatter);
	}

	private static class ZDateFormat extends SimpleDateFormat {

		private ZDateFormat(String template, Locale locale) {
			super(template, locale);
		}

		private String getFixedInputString(String input) {
			return input.replaceAll("Z$", "+0000");
		}

		@Override
		public Date parse(String string) throws ParseException {
			return super.parse(getFixedInputString(string));
		}

		@Override
		public Object parseObject(String string, ParsePosition position) {
			return super.parseObject(getFixedInputString(string), position);
		}
	}
}
