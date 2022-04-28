package ru.liner.vr360server;

import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import ru.liner.vr360server.R;
import ru.liner.vr360server.utils.ViewUtils;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 26.04.2022, вторник
 **/
public class CoreActivity extends AppCompatActivity {
    @CallSuper
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ViewUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.accentColor));
        ViewUtils.setNavigationBarColor(this, ContextCompat.getColor(this, R.color.backgroundColor));
        super.onCreate(savedInstanceState);
    }
}
