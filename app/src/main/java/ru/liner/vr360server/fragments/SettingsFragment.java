package ru.liner.vr360server.fragments;

import android.view.View;

import ru.liner.vr360server.R;
import ru.liner.vr360server.activity.IServer;
import ru.liner.vr360server.activity.MainActivity;

/**
 * @author : "Line'R"
 * @mailto : serinity320@mail.com
 * @created : 08.05.2022, воскресенье
 **/
public class SettingsFragment extends BaseFragment{
    public SettingsFragment(IServer server) {
        super(server);
    }

    public SettingsFragment() {
        this.server = MainActivity.getServer();
    }

    @Override
    public void declareViews(View view) {

    }

    @Override
    public void onFragmentCreated() {

    }

    @Override
    public int getLayoutRes() {
        return R.layout.settings_fragment;
    }


}
