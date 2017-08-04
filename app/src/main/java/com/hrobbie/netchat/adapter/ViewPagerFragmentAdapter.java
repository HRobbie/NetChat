package com.hrobbie.netchat.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


import com.hrobbie.netchat.ui.fragment.BaseFragment;

import java.util.List;

/**
 * Created by HRobbie on 2017/6/7.
 */

public class ViewPagerFragmentAdapter extends FragmentPagerAdapter {
    private List<BaseFragment> fragments;

    public ViewPagerFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    public ViewPagerFragmentAdapter(FragmentManager fm, List<BaseFragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
