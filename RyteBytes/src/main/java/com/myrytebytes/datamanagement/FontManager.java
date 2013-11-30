package com.myrytebytes.datamanagement;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;

import java.io.IOException;
import java.util.HashMap;

public class FontManager {

	public static final String DEFAULT_FONT = "Roboto-Regular.ttf";
	public static final String DEFAULT_FONT_BOLD = "Roboto-Bold.ttf";
	public static final String DEFAULT_FONT_MONO = "DroidSansMono.ttf";

    private static HashMap<String, Typeface> sFontMap;

    public static Typeface getTypeFace(Context context, String font) {
        if (sFontMap == null) {
            initializeFontMap(context);
        }
        
        Typeface tf;
        if (font == null) {
        	tf = sFontMap.get(DEFAULT_FONT);
        } else if (font.equals("bold")) {
        	tf = sFontMap.get(DEFAULT_FONT_BOLD);
        } else {
        	tf = sFontMap.get(font);
        }
        
        if (tf == null) {
        	tf = sFontMap.get(DEFAULT_FONT);
        }
        
        return tf;
    }

    private static void initializeFontMap(Context context) {
    	sFontMap = new HashMap<>();
    	
        AssetManager assetManager = context.getAssets();
        try {
            String[] fontFileNames = assetManager.list("fonts");
            for (String fontFileName : fontFileNames) {
                Typeface typeface = Typeface.createFromAsset(assetManager, "fonts/" + fontFileName);
                sFontMap.put(fontFileName, typeface);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
