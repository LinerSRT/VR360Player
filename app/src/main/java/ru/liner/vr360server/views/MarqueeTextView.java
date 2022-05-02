package ru.liner.vr360server.views;

import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;


public class MarqueeTextView extends androidx.appcompat.widget.AppCompatTextView {

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MarqueeTextView(Context context) {
        super(context);
        init();
    }

    private void init(){
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        setMarqueeRepeatLimit(-1);
        setHorizontallyScrolling(true);
        setSingleLine(true);
    }

    public boolean isFocused() {
        return true;
    }

    protected void onFocusChanged(boolean paramBoolean, int paramInt, Rect paramRect) {
        super.onFocusChanged(paramBoolean,paramInt,paramRect);
    }
}
