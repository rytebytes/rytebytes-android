package com.myrytebytes.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import com.myrytebytes.datamanagement.FontManager;
import com.myrytebytes.rytebytes.R;

public class AutoResizeTextView extends View {

    private boolean mNeedsResize = false;
    private final Rect mRect = new Rect();
    
    private String mText;

    private TextPaint mTextPaint;
    
    private int mMaxTextSize;
	private int mMinTextSize;
    private int mLineHeight;
    private int mLineCount;
    private String[] mLines;
    private boolean mResizeHeight;
	private boolean mResizeWidth;
    private int mMaxLineCount;
    private int mWidth;
    private int mHeight;
	private int mTextHeight;
    private int mDefaultLineHeight;
	private int mGravity;

	private int mLastResizeWidth;
	private int mLastResizeHeight;

    protected final float mDensity;

    public AutoResizeTextView(Context context) {
        this(context, null, 0);
    }

    public AutoResizeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoResizeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        mDensity = getResources().getDisplayMetrics().density;

        mTextPaint = new TextPaint();
        mTextPaint.setTextAlign(Align.LEFT);
        
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AutoResizeTextView);
        mMaxTextSize = a.getDimensionPixelSize(R.styleable.AutoResizeTextView_maxTextSize, (int)(16 * mDensity));
		mMinTextSize = a.getDimensionPixelSize(R.styleable.AutoResizeTextView_minTextSize, (int)(6 * mDensity));
        mTextPaint.setTextSize(mMaxTextSize);
        mTextPaint.setColor(a.getColor(R.styleable.AutoResizeTextView_android_textColor, Color.BLACK));
        mResizeHeight = a.getBoolean(R.styleable.AutoResizeTextView_resizeHeight, false);
		mResizeWidth = a.getBoolean(R.styleable.AutoResizeTextView_resizeWidth, false);
        mMaxLineCount = a.getInt(R.styleable.AutoResizeTextView_maxLineCount, 99);
		mGravity = a.getInt(R.styleable.AutoResizeTextView_android_gravity, Gravity.LEFT|Gravity.TOP);
		mText = a.getString(R.styleable.AutoResizeTextView_android_text);
        String font = a.getString(R.styleable.AutoResizeTextView_font);
        a.recycle();

		mTextPaint.setTypeface(FontManager.getTypeFace(context, font));
    }

    public void setText(String text) {
    	mText = text;
        mNeedsResize = true;
        resetTextSize();
        requestLayout();
		invalidate();
    }
    
    public void setFont(String font) {
    	mTextPaint.setTypeface(FontManager.getTypeFace(getContext(), font));
    }
    
    public void setMaxTextSize(int maxTextSize) {
    	mMaxTextSize = maxTextSize;
    }

	public void setMaxLineCount(int maxLineCount) {
		mMaxLineCount = maxLineCount;
	}

	public void setResizeHeight(boolean resizeHeight) {
    	mResizeHeight = resizeHeight;
    }
    
    public void setGravity(int gravity) {
    	mGravity = gravity;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != oldw || h != oldh) {
            mNeedsResize = true;
        }
    }

    public void resetTextSize() {
    	mTextPaint.setTextSize(mMaxTextSize);
		mLastResizeWidth = 0;
		mLastResizeHeight = 0;
    }

    public void resizeText(final int width, final int height) {
        final String text = mText;

        if (text == null || text.length() == 0 || height <= 0 || width <= 0) {
            return;
        }

		if (width != mLastResizeWidth || height != mLastResizeHeight) {
			mLastResizeWidth = width;
			mLastResizeHeight = height;

			float targetTextSize = mTextPaint.getTextSize();

			if (mMaxLineCount == 1) {
				float length = mTextPaint.measureText(text);
				while ((length > width) && (targetTextSize > mMinTextSize)) {
					targetTextSize -= 2;
					mTextPaint.setTextSize(targetTextSize);
					length = mTextPaint.measureText(text);
				}
				mLines = new String[] { text };
				final int ascent = (int)Math.ceil(-mTextPaint.ascent());
				final int descent = (int)Math.ceil(mTextPaint.descent());
				mLineHeight = ascent + descent;
				mTextHeight = mLineHeight;
			} else {
				mTextHeight = getTextHeight(text, width);
				mLines = splitString(text, width);
				int lineCount = mLines.length;
				boolean linesSet = lineCount <= mMaxLineCount;

				while (!linesSet && (targetTextSize >= mMinTextSize)) {
					targetTextSize -= 2;
					mTextPaint.setTextSize(targetTextSize);
					mTextHeight = getTextHeight(text, width);

					if (mTextHeight <= height) {
						mLines = splitString(text, width);
						lineCount = mLines.length;

						if (lineCount <= mMaxLineCount) {
							linesSet = true;
						}
					}
				}

				if (!linesSet) {
					mLines = splitString(text, width);
				}
			}
		}

        mNeedsResize = false;
    }

    private int getTextHeight(String text, int width) {
        final int ascent = (int)Math.ceil(-mTextPaint.ascent());
        final int descent = (int)Math.ceil(mTextPaint.descent());
        mLineHeight = ascent + descent;
    	
        return mLineHeight * getLinesRequired(text, width);
    }
    
    public int getLineHeight() {
    	return mLineHeight;
    }
    
    public void setTextColor(int color) {
    	mTextPaint.setColor(color);
    	invalidate();
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	final int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        
        if (widthSpecSize > mWidth || heightSpecSize > mHeight) {
        	resetTextSize();
        }

        resizeText(widthSpecSize - getPaddingLeft() - getPaddingRight(), heightSpecSize);

		if (mResizeWidth) {
			if (mText != null) {
				mWidth = (int)(mTextPaint.measureText(mText) + getPaddingLeft() + getPaddingRight());
			} else {
				mWidth = getPaddingLeft() + getPaddingRight();
			}
		} else {
			mWidth = widthSpecSize;
		}

		if (!mResizeHeight) {
        	mHeight = heightSpecSize;
        } else {
            mHeight = mTextHeight + getPaddingBottom() + getPaddingTop();

            if (mHeight > heightSpecSize) {
            	mHeight = heightSpecSize;
            }
        }

        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed || mNeedsResize) {
            int widthLimit = (right - left) - getPaddingLeft() - getPaddingRight();
            int heightLimit = (bottom - top) - getPaddingTop() - getPaddingBottom();
            resizeText(widthLimit, heightLimit);
        }
        
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
	protected void onDraw(Canvas canvas) {
    	if (mLines != null) {
        	final int yOrigin = -(int)mTextPaint.ascent();
        	final int bottomHeight = (int)mTextPaint.descent();

			int verticalGravity = mGravity & Gravity.VERTICAL_GRAVITY_MASK;
			if (verticalGravity == 0) {
				verticalGravity = Gravity.TOP;
			}

        	final int top;
        	if (mResizeHeight) {
        		top = getPaddingTop();
			} else if (verticalGravity == Gravity.TOP) {
				top = mHeight / 2 - (yOrigin + bottomHeight) / 2;
			} else if (verticalGravity == Gravity.BOTTOM) {
				top = mHeight - mTextHeight;
			} else {
				top = (mHeight - mTextHeight) / 2;
        	}

			int horizontalGravity = mGravity & Gravity.HORIZONTAL_GRAVITY_MASK;
			if (horizontalGravity == 0) {
				horizontalGravity = Gravity.LEFT;
			}

    		for (int i = 0; i < mLines.length; i++) {
    			final int left;
				if (horizontalGravity == Gravity.LEFT) {
					left = getPaddingLeft();
				} else if (horizontalGravity == Gravity.RIGHT) {
					left = (int)(mWidth - mTextPaint.measureText(mLines[i]) - getPaddingRight());
				} else {
					left = getPaddingLeft() + (int)(mWidth - getPaddingLeft() - getPaddingRight() - mTextPaint.measureText(mLines[i])) / 2;
				}
    			canvas.drawText(mLines[i], left, (yOrigin * (i + 1) + bottomHeight * i) + top, mTextPaint);
    		}
    	}
	}
    
	public int getLinesRequired(String text, final int width) {
		mTextPaint.getTextBounds(text, 0, text.length(), mRect);
		final int roughMaxLettersPerLine = (width/(mRect.width()/text.length()));
		
		int lineCount = 1;
    	int mark = 0;

		int textLength = text.length();

    	while (mark < textLength)  {
    		if (textLength == 1) {
    			mark = 1;
    		} else {
        		mark = getNextSplitPoint(text, width, roughMaxLettersPerLine);

        		if (mark == 0) {
					mLineCount = 100;
        			return 100;
        		}
    		}
    		
    		if (mark < textLength) {
        		lineCount++;
        		text = text.substring(mark, textLength);
				textLength -= mark;
        		mark = 0;
    		} else {
    			break;
    		}
    	}

    	mLineCount = lineCount;
    	return lineCount;
    }
	
	public String[] splitString(String text, final int width) {
		mTextPaint.getTextBounds(text, 0, text.length(), mRect);
		final int roughMaxLettersPerLine = (width/(mRect.width()/text.length()));

		final String[] lines = new String[mLineCount];
    	int currentLine = 0;
    	int mark;

    	while (currentLine < lines.length)  {
    		mark = getNextSplitPoint(text, width, roughMaxLettersPerLine);
    		if (mark == 0) {
    			mark = roughMaxLettersPerLine > text.length() ? text.length() : roughMaxLettersPerLine;
    		}
    		
    		if (currentLine == (lines.length-1)) {
        		lines[currentLine++] = text;
    		} else {
        		lines[currentLine++] = text.substring(0, mark);
    			text = text.substring(mark, text.length());
    		}
    	}
    	
    	return lines;
    }
	
	private int getNextSplitPoint(final String text, final int width, final int roughMaxLettersPerLine) {
		int mark; 
    	int splitMax;

		mark = 0;
		splitMax = roughMaxLettersPerLine > text.length() ? text.length() : roughMaxLettersPerLine;

		for (int i = 0; i < splitMax; i++) {
			if (text.charAt(i) == '\n') {
				mark = i;
				break;
			} else if (text.charAt(i) == ' ' || (i == text.length()-1)) {
				mark = i;
			}
		}

		if (mark == 0) {
			return 0;
		}
		
		mTextPaint.getTextBounds(text, 0, (mark <= text.length()) ? mark+1 : mark, mRect);
		int lineWidth = mRect.width();
		
		while (lineWidth > width) {
			for (int i = mark-1; i >= 0; i--) {
				if (text.charAt(i) == '\n' || text.charAt(i) == ' ' || i == 0) {
					mark = i;
					break;
				}
    		}
			mTextPaint.getTextBounds(text, 0, (mark <= text.length()) ? mark+1 : mark, mRect);
			lineWidth = mRect.width();
		}
		
		if (mark == 0) {
			return 0;
		} else {
    		if (mark <= text.length()) {
    			mark++;    		
    		}
		}
		
		return mark;
	}
	
	public int getDefaultHeight(int lines) {
		if (mDefaultLineHeight == 0) {
			float textSize = mTextPaint.getTextSize();
			mTextPaint.setTextSize(13 * mDensity);
			mDefaultLineHeight = (int)(-mTextPaint.ascent() + mTextPaint.descent());
			mTextPaint.setTextSize(textSize);
		}
		return mDefaultLineHeight * lines;
	}
	
	public float getTextSize() {
		return mTextPaint.getTextSize();
	}
}
