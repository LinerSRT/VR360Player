package ru.liner.vr360server;

import static ru.liner.vr360server.utils.Constant.PERMISSION_REQUEST_CORE;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import ru.liner.vr360server.utils.Utils;
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
        ViewUtils.setStatusBarColor(this, ContextCompat.getColor(this, R.color.backgroundColor));
        ViewUtils.setNavigationBarColor(this, ContextCompat.getColor(this, R.color.backgroundSecondaryColor));
        Utils.requestPermissions(this);
        super.onCreate(savedInstanceState);
    }

    @CallSuper
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_REQUEST_CORE) {
            if (!Utils.isPermissionGranted(this)) {
                finish();
            } else {
                recreate();
            }
        }

    }
}
