package ru.liner.vr360server.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;

import ru.liner.vr360server.utils.ViewUtils;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 09.05.2022, понедельник
 **/
public class ExtraPaddingLinearLayoutManager extends LinearLayoutManager {
    private final int paddingTop;
    private final int paddingBottom;

    public ExtraPaddingLinearLayoutManager(Context context) {
        super(context);
        this.paddingTop = ViewUtils.dpToPx(48);
        this.paddingBottom = ViewUtils.dpToPx(48);
    }

    public ExtraPaddingLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        this.paddingTop = ViewUtils.dpToPx(48);
        this.paddingBottom = ViewUtils.dpToPx(48);
    }

    public ExtraPaddingLinearLayoutManager(Context context, int paddingTop, int paddingBottom) {
        super(context);
        this.paddingTop = paddingTop;
        this.paddingBottom = paddingBottom;
    }

    public ExtraPaddingLinearLayoutManager(Context context, int orientation, boolean reverseLayout, int paddingTop, int paddingBottom) {
        super(context, orientation, reverseLayout);
        this.paddingTop = paddingTop;
        this.paddingBottom = paddingBottom;
    }

    public ExtraPaddingLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.paddingTop = ViewUtils.dpToPx(48);
        this.paddingBottom = ViewUtils.dpToPx(48);
    }

    @Override
    public int getPaddingBottom() {
        return paddingBottom;
    }

    @Override
    public int getPaddingTop() {
        return paddingTop;
    }

}
