package com.hrobbie.netchat.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.hrobbie.netchat.view.viewpager.SlidingTabPagerAdapter;

import java.util.List;

/**
 * Description:
 * User: xjp
 * Date: 2015/6/15
 * Time: 15:12
 */

public class MainFragmentAdapter extends SlidingTabPagerAdapter {

    private List<Fragment> mFragments;
    private List<String> mTitles;

    public MainFragmentAdapter(FragmentManager fm, int count, Context context, ViewPager pager) {
        super(fm, count, context, pager);
    }


    @Override
    public int getCacheCount() {
        return 0;
    }


    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }


}
