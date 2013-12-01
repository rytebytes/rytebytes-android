package com.myrytebytes.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.myrytebytes.datamanagement.FontManager;

public class CustomFontEditText extends EditText {

    public CustomFontEditText(Context context) {
        super(context);
        init(context, null);
    }

    public CustomFontEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomFontEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }
    
    private void init(Context context, AttributeSet attrs) {
    	String font = null;
        if (attrs != null) {
            font = attrs.getAttributeValue(null, "font");
        }

        if (font == null && getInputType() == (EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD)) {
        	setTypeface(FontManager.getTypeFace(context, FontManager.DEFAULT_FONT_MONO));
        } else {
        	setTypeface(FontManager.getTypeFace(context, font));
        }
    }
}
