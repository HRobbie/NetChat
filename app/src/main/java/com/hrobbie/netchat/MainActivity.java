package com.hrobbie.netchat;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.hrobbie.netchat.adapter.TabFragmentAdapter;
import com.hrobbie.netchat.ui.BaseActivity;
import com.hrobbie.netchat.ui.fragment.ContactListFragment;
import com.hrobbie.netchat.ui.fragment.SessionListFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tabs)
    TabLayout tabs;
    @BindView(R.id.appbar)
    AppBarLayout appbar;
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.main_content)
    CoordinatorLayout mainContent;

    private ArrayList<String> mTitleList = new ArrayList<>();
    private ArrayList<Fragment> mFragments = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initActivity();
    }

    private void initActivity() {
        initToolbar();

        initTabLayout();
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("网聊");
//        toolbar.setSubtitle("CSDN");
        toolbar.setLogo(R.mipmap.ic_launcher);
//        toolbar.setNavigationIcon(R.drawable.ic_list_black_24dp);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        toolbar.setOnMenuItemClickListener(this);
    }

//    @Override
//    public boolean onMenuItemClick(MenuItem item) {
//        return false;
//    }

    private void initTabLayout() {


        mTitleList.add("会话");
        mTitleList.add("通信录");
        mFragments.add(new SessionListFragment());
        mFragments.add(new ContactListFragment());
        //此处代码设置无效，不知道为啥？？？xml属性是可以的
        TabFragmentAdapter tabFragmentAdapter = new TabFragmentAdapter(getSupportFragmentManager(), mFragments, mTitleList);
        tabs.setTabMode(TabLayout.MODE_FIXED);//设置tab模式，当前为系统默认模式
        viewPager.setOffscreenPageLimit(mFragments.size()-1);
        viewPager.setAdapter(tabFragmentAdapter);
        tabs.setupWithViewPager(viewPager);

    }
}
