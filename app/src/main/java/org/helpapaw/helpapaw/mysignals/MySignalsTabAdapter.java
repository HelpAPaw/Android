package org.helpapaw.helpapaw.mysignals;

import static org.helpapaw.helpapaw.base.PawApplication.getContext;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import org.helpapaw.helpapaw.R;

public class MySignalsTabAdapter extends FragmentStatePagerAdapter {

    // tab titles
    private String[] tabTitles = getContext().getResources().getStringArray(R.array.my_signal_items);

    //integer to count number of tabs
    int tabCount;

    public MySignalsTabAdapter(FragmentManager fm, int tabCount) {
        super(fm);
        //Initializing tab count
        this.tabCount= tabCount;
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