package com.juankyapps.igenda.adapters;



import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class TabViewPagerAdapter extends FragmentPagerAdapter {

    private final List<Fragment> nFragmentList = new ArrayList<>();
    private final List<String> nFragrmentTitleList = new ArrayList<>();



    public TabViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return nFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return nFragmentList.size();
    }

    public void addFragment(Fragment fragment, String title) {
        nFragmentList.add(fragment);
        nFragrmentTitleList.add(title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return nFragrmentTitleList.get(position);
    }
}
