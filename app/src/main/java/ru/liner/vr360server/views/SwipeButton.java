package ru.liner.vr360server.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

import ru.liner.vr360server.R;
import ru.liner.vr360server.utils.ViewUtils;


/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 08.05.2022, воскресенье
 **/
public class SwipeButton extends RelativeLayout {


    private ImageView swipeButtonInner;
    private float initialX;
    private boolean active;
    private TextView centerText;
    private ViewGroup background;

    private Drawable disabledDrawable;
    private Drawable enabledDrawable;
    private StateCallback stateCallback;

    private static final int ENABLED = 0;
    private static final int DISABLED = 1;

    private int collapsedWidth;
    private int collapsedHeight;

    private LinearLayout layer;
    private boolean trailEnabled = false;
    private boolean hasActivationState;

    @ColorInt
    private int textColor = Color.WHITE;

    public SwipeButton(Context context) {
        super(context);

        init(context, null, -1, -1);
    }

    public SwipeButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs, -1, -1);
    }

    public SwipeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs, defStyleAttr, -1);
    }

    @TargetApi(21)
    public SwipeButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public boolean isActive() {
        return active;
    }

    public void setText(String text) {
        centerText.setText(text);
    }

    public void setBackground(Drawable drawable) {
        background.setBackground(drawable);
    }

    public void setSlidingButtonBackground(Drawable drawable) {
        background.setBackground(drawable);
    }

    public void setDisabledDrawable(Drawable drawable) {
        disabledDrawable = drawable;

        if (!active) {
            swipeButtonInner.setImageDrawable(drawable);
        }
    }

    public void setButtonBackground(Drawable buttonBackground) {
        if (buttonBackground != null) {
            swipeButtonInner.setBackground(buttonBackground);
        }
    }

    public void setEnabledDrawable(Drawable drawable) {
        enabledDrawable = drawable;

        if (active) {
            swipeButtonInner.setImageDrawable(drawable);
        }
    }

    public void setStateCallback(StateCallback stateCallback) {
        this.stateCallback = stateCallback;
    }

    public void setInnerTextPadding(int left, int top, int right, int bottom) {
        centerText.setPadding(left, top, right, bottom);
    }

    public void setSwipeButtonPadding(int left, int top, int right, int bottom) {
        swipeButtonInner.setPadding(left, top, right, bottom);
    }

    public void setHasActivationState(boolean hasActivationState) {
        this.hasActivationState = hasActivationState;
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        hasActivationState = true;

        background = new RelativeLayout(context);

        LayoutParams layoutParamsView = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        layoutParamsView.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        addView(background, layoutParamsView);

        final TextView centerText = new TextView(context);
        this.centerText = centerText;
        centerText.setGravity(Gravity.CENTER);

        LayoutParams layoutParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        background.addView(centerText, layoutParams);

        final ImageView swipeButton = new ImageView(context);
        this.swipeButtonInner = swipeButton;

        if (attrs != null && defStyleAttr == -1 && defStyleRes == -1) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwipeButton,
                    defStyleAttr, defStyleRes);

            collapsedWidth = (int) typedArray.getDimension(R.styleable.SwipeButton_button_image_width,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            collapsedHeight = (int) typedArray.getDimension(R.styleable.SwipeButton_button_image_height,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            trailEnabled = typedArray.getBoolean(R.styleable.SwipeButton_button_trail_enabled,
                    false);
            Drawable trailingDrawable = typedArray.getDrawable(R.styleable.SwipeButton_button_trail_drawable);

            Drawable backgroundDrawable = typedArray.getDrawable(R.styleable.SwipeButton_inner_text_background);

            if (backgroundDrawable != null) {
                background.setBackground(backgroundDrawable);
            } else {
                background.setBackground(ContextCompat.getDrawable(context, R.drawable.shape_rounded_background));
            }

            if (trailEnabled) {
                layer = new LinearLayout(context);

                if (trailingDrawable != null) {
                    layer.setBackground(trailingDrawable);
                } else {
                    layer.setBackground(typedArray.getDrawable(R.styleable.SwipeButton_button_background));
                }

                layer.setGravity(Gravity.START);
                layer.setVisibility(View.GONE);
                background.addView(layer, layoutParamsView);
            }

            centerText.setText(typedArray.getText(R.styleable.SwipeButton_inner_text));
            textColor = typedArray.getColor(R.styleable.SwipeButton_inner_text_color,                    Color.WHITE);
            centerText.setTextColor(textColor);

            float textSize = ViewUtils.pxToSp(typedArray.getDimension(R.styleable.SwipeButton_inner_text_size, 0));

            if (textSize != 0) {
                centerText.setTextSize(textSize);
            } else {
                centerText.setTextSize(12);
            }

            disabledDrawable = typedArray.getDrawable(R.styleable.SwipeButton_button_image_disabled);
            enabledDrawable = typedArray.getDrawable(R.styleable.SwipeButton_button_image_enabled);
            float innerTextLeftPadding = typedArray.getDimension(
                    R.styleable.SwipeButton_inner_text_left_padding, 0);
            float innerTextTopPadding = typedArray.getDimension(
                    R.styleable.SwipeButton_inner_text_top_padding, 0);
            float innerTextRightPadding = typedArray.getDimension(
                    R.styleable.SwipeButton_inner_text_right_padding, 0);
            float innerTextBottomPadding = typedArray.getDimension(
                    R.styleable.SwipeButton_inner_text_bottom_padding, 0);

            int initialState = typedArray.getInt(R.styleable.SwipeButton_initial_state, DISABLED);

            if (initialState == ENABLED) {
                LayoutParams layoutParamsButton = new LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);

                layoutParamsButton.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                layoutParamsButton.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

                swipeButton.setImageDrawable(enabledDrawable);

                addView(swipeButton, layoutParamsButton);

                active = true;
            } else {
                LayoutParams layoutParamsButton = new LayoutParams(collapsedWidth, collapsedHeight);

                layoutParamsButton.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                layoutParamsButton.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

                swipeButton.setImageDrawable(disabledDrawable);

                addView(swipeButton, layoutParamsButton);

                active = false;
            }

            centerText.setPadding((int) innerTextLeftPadding,
                    (int) innerTextTopPadding,
                    (int) innerTextRightPadding,
                    (int) innerTextBottomPadding);

            Drawable buttonBackground = typedArray.getDrawable(R.styleable.SwipeButton_button_background);

            if (buttonBackground != null) {
                swipeButton.setBackground(buttonBackground);
            } else {
                swipeButton.setBackground(ContextCompat.getDrawable(context, R.drawable.shape_rounded_primary));
            }

            float buttonLeftPadding = typedArray.getDimension(
                    R.styleable.SwipeButton_button_left_padding, 0);
            float buttonTopPadding = typedArray.getDimension(
                    R.styleable.SwipeButton_button_top_padding, 0);
            float buttonRightPadding = typedArray.getDimension(
                    R.styleable.SwipeButton_button_right_padding, 0);
            float buttonBottomPadding = typedArray.getDimension(
                    R.styleable.SwipeButton_button_bottom_padding, 0);

            swipeButton.setPadding((int) buttonLeftPadding,
                    (int) buttonTopPadding,
                    (int) buttonRightPadding,
                    (int) buttonBottomPadding);

            hasActivationState = typedArray.getBoolean(R.styleable.SwipeButton_has_activate_state, true);

            typedArray.recycle();
        }

        setOnTouchListener(getButtonTouchListener());
    }

    private OnTouchListener getButtonTouchListener() {
        return new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        return !(event.getX() > swipeButtonInner.getX() + swipeButtonInner.getWidth());
                    case MotionEvent.ACTION_MOVE:
                        if (initialX == 0) {
                            initialX = swipeButtonInner.getX();
                        }

                        if (event.getX() > swipeButtonInner.getWidth() / 2 &&
                                event.getX() + swipeButtonInner.getWidth() / 2 < getWidth()) {
                            swipeButtonInner.setX(event.getX() - swipeButtonInner.getWidth() / 2);
                            centerText.setAlpha(1 - 1.3f * (swipeButtonInner.getX() + swipeButtonInner.getWidth()) / getWidth());
                            setTrailingEffect();
                        }

                        if (event.getX() + swipeButtonInner.getWidth() / 2 > getWidth() &&
                                swipeButtonInner.getX() + swipeButtonInner.getWidth() / 2 < getWidth()) {
                            swipeButtonInner.setX(getWidth() - swipeButtonInner.getWidth());
                        }

                        if (event.getX() < swipeButtonInner.getWidth() / 2) {
                            swipeButtonInner.setX(0);
                        }

                        return true;
                    case MotionEvent.ACTION_UP:
                        if (active) {
                            disableButton(true);
                        } else {
                            if (swipeButtonInner.getX() + swipeButtonInner.getWidth() > getWidth() * 0.9) {
                                if (hasActivationState) {
                                    enableButton(true);
                                } else if (stateCallback != null) {
                                    stateCallback.onStateChanged(SwipeButton.this, true, true);
                                    moveButtonBack();
                                }
                            } else {
                                moveButtonBack();
                            }
                        }
                        return true;
                }

                return false;
            }
        };
    }

    private void moveButtonBack() {
        final ValueAnimator positionAnimator =
                ValueAnimator.ofFloat(swipeButtonInner.getX(), 0);
        positionAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        positionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float x = (Float) positionAnimator.getAnimatedValue();
                swipeButtonInner.setX(x);
                setTrailingEffect();
            }
        });

        positionAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (layer!=null) {
                    layer.setVisibility(View.GONE);
                }
            }
        });

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(
                centerText, "alpha", 1);

        positionAnimator.setDuration(200);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator, positionAnimator);
        animatorSet.start();
    }


    private void setTrailingEffect() {
        if (trailEnabled) {
            layer.setVisibility(View.VISIBLE);
            layer.setLayoutParams(new RelativeLayout.LayoutParams(
                    (int) (swipeButtonInner.getX() + swipeButtonInner.getWidth() / 3), centerText.getHeight()));
        }
    }

    public void disableButton(boolean fromUser){
        int finalWidth;

        if (collapsedWidth == ViewGroup.LayoutParams.WRAP_CONTENT) {
            finalWidth = swipeButtonInner.getHeight();
        } else {
            finalWidth = collapsedWidth;
        }

        final ValueAnimator widthAnimator = ValueAnimator.ofInt(swipeButtonInner.getWidth(), finalWidth);

        widthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ViewGroup.LayoutParams params = swipeButtonInner.getLayoutParams();
                params.width = (Integer) widthAnimator.getAnimatedValue();
                swipeButtonInner.setLayoutParams(params);
                setTrailingEffect();
            }
        });

        widthAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                active = false;
                swipeButtonInner.setImageDrawable(disabledDrawable);
                if (stateCallback != null) {
                    stateCallback.onStateChanged(SwipeButton.this, active, fromUser);
                }
                if (layer!=null) {
                    layer.setVisibility(View.GONE);
                }
            }
        });

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(
                centerText, "alpha", 1);

        AnimatorSet animatorSet = new AnimatorSet();

        animatorSet.playTogether(objectAnimator, widthAnimator);
        animatorSet.start();
    }

    public void enableButton(boolean fromUser){
        final ValueAnimator positionAnimator =
                ValueAnimator.ofFloat(swipeButtonInner.getX(), 0);
        positionAnimator.addUpdateListener(animation -> {
            float x = (Float) positionAnimator.getAnimatedValue();
            swipeButtonInner.setX(x);
        });

        final ValueAnimator widthAnimator = ValueAnimator.ofInt(
                swipeButtonInner.getWidth(),
                getWidth());
        widthAnimator.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = swipeButtonInner.getLayoutParams();
            params.width = (Integer) widthAnimator.getAnimatedValue();
            swipeButtonInner.setLayoutParams(params);
        });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                active = true;
                swipeButtonInner.setImageDrawable(enabledDrawable);
                if (stateCallback != null) {
                    stateCallback.onStateChanged(SwipeButton.this, active, fromUser);
                }
            }
        });

        animatorSet.playTogether(positionAnimator, widthAnimator);
        animatorSet.start();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if(enabled){
            background.setBackgroundTintList(null);
            swipeButtonInner.setBackgroundTintList(null);
            centerText.setTextColor(textColor);
        } else {
            background.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.disabledColor)));
            swipeButtonInner.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.textDisabledColor)));
            centerText.setTextColor(ContextCompat.getColor(getContext(), R.color.textDisabledColor));
        }
    }

    public interface StateCallback{
        void onStateChanged(SwipeButton swipeButton, boolean enabled, boolean fromUser);
    }
}