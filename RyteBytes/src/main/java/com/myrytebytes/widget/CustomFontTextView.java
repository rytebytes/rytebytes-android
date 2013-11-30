package com.myrytebytes.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.myrytebytes.datamanagement.FontManager;

public class CustomFontTextView extends TextView {

    public CustomFontTextView(Context context) {
        super(context);
        
        setTypeface(FontManager.getTypeFace(context, null));
    }

	public CustomFontTextView(Context context, String font) {
		super(context);

		setTypeface(FontManager.getTypeFace(context, font));
	}

    public CustomFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        String font = null;
        if (attrs != null) {
            font = attrs.getAttributeValue(null, "font");
        }

        setTypeface(FontManager.getTypeFace(context, font));
    }

    public CustomFontTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        String font = null;
        if (attrs != null) {
            font = attrs.getAttributeValue(null, "font");
        }

        setTypeface(FontManager.getTypeFace(context, font));
    }
}
