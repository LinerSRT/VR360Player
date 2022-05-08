package ru.liner.vr360server.utils;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class FragmentAdapter extends FragmentPagerAdapter {
    private final List<Fragment> pageList;
    public FragmentAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
        pageList = new ArrayList<>();
    }
    @Override
    public Fragment getItem(int position) {
        return pageList.get(position);
    }

    @Override
    public int getCount() {
        return pageList.size();
    }

    public void add(Fragment fragment){
        pageList.add(fragment);
    }
}
