package ru.liner.vr360server.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.animation.AccelerateInterpolator;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;

import ru.liner.vr360server.R;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 26.04.2022, вторник
 **/
public class LImageButton extends AppCompatImageButton {
    private Callback callback;
    @ColorInt
    private int previousBackgroundColor;

    public LImageButton(@NonNull Context context) {
        this(context, null);
    }

    public LImageButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LImageButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnClickListener(null);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(v -> {
            animate()
                    .setDuration(75)
                    .scaleY(0.9f)
                    .scaleX(0.9f)
                    .setInterpolator(new AccelerateInterpolator())
                    .withEndAction(() -> animate()
                            .setDuration(75)
                            .scaleY(1f)
                            .scaleX(1f)
                            .setInterpolator(new AccelerateInterpolator())
                            .withEndAction(() -> {
                                if (callback != null)
                                    callback.onClick(LImageButton.this);
                            }).start()).start();
        });
    }

    public void setClickCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.textDisabledColor)));
        } else {
            setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.primaryColor)));
        }
    }

    public interface Callback {
        void onClick(LImageButton button);
    }
}
