package ru.liner.vr360server.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 09.05.2022, понедельник
 **/
public class RoundedImageView extends AppCompatImageView {
    public RoundedImageView(@NonNull Context context) {
        super(context);
    }

    public RoundedImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundedImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void draw(Canvas canvas) {
        clip(canvas);
        super.draw(canvas);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        clip(canvas);
        super.dispatchDraw(canvas);
    }

    private void clip(Canvas canvas) {
        Path path = new Path();
        path.reset();
        path.addRoundRect(new RectF(0, 0, getWidth(), getHeight()), 16, 16, Path.Direction.CW);
        path.close();
        canvas.clipPath(path);
    }
}
