package com.myrytebytes.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import com.myrytebytes.datamanagement.FontManager;

public class CustomFontButton extends Button {

    public CustomFontButton(Context context) {
        super(context);
        
        setTypeface(FontManager.getTypeFace(context, null));
    }

    public CustomFontButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        String font = null;
        if (attrs != null) {
            font = attrs.getAttributeValue(null, "font");
        }

        setTypeface(FontManager.getTypeFace(context, font));
    }

    public CustomFontButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        String font = null;
        if (attrs != null) {
            font = attrs.getAttributeValue(null, "font");
        }

        setTypeface(FontManager.getTypeFace(context, font));
    }
}
