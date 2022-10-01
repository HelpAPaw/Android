package org.helpapaw.helpapaw.mysignals;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;


public class MySignalsTabAdapter extends FragmentStatePagerAdapter {

    // tab titles
    private String[] tabTitles;

    //integer to count number of tabs
    int tabCount;

    public MySignalsTabAdapter(FragmentManager fm, int tabCount, String[] tabTitles) {
        super(fm);
        //Initializing tab count
        this.tabCount = tabCount;
        this.tabTitles = tabTitles;
    }

    @Override
    public Fragment getItem(int position) {
        //Returning the current tab
        switch (position) {
            case 0 :
                MySubmittedSignalsFragment mySignalsTab = new MySubmittedSignalsFragment();
                return mySignalsTab;
            case 1:
                MyCommentedSignalsFragment commentedSignalsTab = new MyCommentedSignalsFragment();
                return commentedSignalsTab;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return tabCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}