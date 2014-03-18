package com.myrytebytes.datamodel;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.myrytebytes.datamanagement.Logr;
import com.myrytebytes.remote.JsonHandler;
import com.myrytebytes.remote.JsonHandler.JsonHandlerListenerAdapter;
import com.myrytebytes.remote.SafeJsonParser;

import java.io.IOException;

public class HeatingInstructions implements JacksonParser {

    private static final String KEY = "heatingInstructions";
    private static final String DEFAULT = "Remember - keep your items frozen until you cook them!  They should not be left out or in the fridge before cooking.  Always go from freezer to pot when heating.\n" +
            "\n" +
            "Heating your RyteBytes meal is as simple as heating water! \n" +
            "\n" +
            "1.) Place the bags in a pot (no more than 2 pasta meals or 6 individual components).\n" +
            "\n" +
            "2.) Fill with hot water until bags are covered.\n" +
            "\n" +
            "3.) Put the pot on your biggest burner, turn on high and cover with the lid.\n" +
            "\n" +
            "4.) Bring the water to a boil and allow to boil for the following time:\n" +
            "\n" +
            "1 - 3 bags : 5 minutes\n" +
            "\n" +
            "4 - 6 bags : 15 minutes";
    public static String persistedText;

    public String text;

    public HeatingInstructions() { }

    public HeatingInstructions(SafeJsonParser jsonParser, boolean closeWhenComplete) {
        try {
            fillFromJSON(jsonParser, closeWhenComplete);
        } catch (IOException e) {
            Logr.e("Error filling HeatingInstructions", e);
        }
    }

    @Override
    public void fillFromJSON(SafeJsonParser jsonParser, boolean closeWhenComplete) throws IOException {
        JsonHandler.handleJson(jsonParser, new JsonHandlerListenerAdapter() {
            @Override
            public void onField(String tag, SafeJsonParser jsonParser) throws IOException {
                if (tag.equals("text")) {
                    text = jsonParser.getStringValue();
                }
            }
        }, closeWhenComplete);
    }

    public static String getPersistedText(Context context) {
        if (persistedText == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            persistedText = prefs.getString(KEY, DEFAULT);
        }
        return persistedText;
    }

    public boolean persistIfNeeded(Context context) {
        if (!getPersistedText(context).equals(text) && !TextUtils.isEmpty(text)) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(KEY, text).commit();
            return true;
        } else {
            return false;
        }
    }
}
