package com.qut.sps.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qut.sps.R;

/**
 * Created by lenovo on 2017/8/4.
 */

public class DanceBarFragment extends Fragment {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dance_bar_fragment,container,false);
        mViewPager = rootView.findViewById(R.id.dance_fragment_container);
        mTabLayout = rootView.findViewById(R.id.dance_fragment_tabs);

        initViews();
        initEvents();
        return rootView;
    }

    private void initEvents() {
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab == mTabLayout.getTabAt(0)){
                    mViewPager.setCurrentItem(0);
                }else if(tab == mTabLayout.getTabAt(1)){
                    mViewPager.setCurrentItem(1);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void initViews() {
        mViewPager.setAdapter(new FragmentPagerAdapter(getFragmentManager()) {
            String[] mTitles = new String[]{"消息","我的","舞吧","活动展示"};
            @Override
            public int getCount() {
                return mTitles.length;
            }

            @Override
            public Fragment getItem(int position) {
                if (position == 1){
                    return new MyFragment();
                }else if (position == 2){
                    return new LetDanceFragment();
                }else if (position == 3){
                    return new CompetitionFragment();
                }
                return new MessageFragment();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mTitles[position];
            }
        });

        mViewPager.setOffscreenPageLimit(4);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabTextColors(Color.BLACK,Color.RED);
        mTabLayout.setSelectedTabIndicatorColor(Color.RED);
    }
}
