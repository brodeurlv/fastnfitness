package com.easyfitness.fonte;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.easyfitness.MainActivity;
import com.easyfitness.R;

public class FontesViewPagerAdapter extends FragmentPagerAdapter {

    final int PAGE_COUNT = 3;
    Context context;
    FragmentManager lFm = null;
    // Tab Titles
    private String tabtitles[] = new String[3]; // { "Records", "Graphics", "History" };
    private FontesFragment mpFontesFrag = null;
    private FonteHistoryFragment mpHistoryFrag = null;
    private FonteGraphFragment mpGraphFrag = null;

    public FontesViewPagerAdapter(FragmentManager fm, Context ct) {
        super(fm);
        lFm = fm;
        tabtitles[0] = ct.getResources().getString(R.string.RecordLabel);
        tabtitles[1] = ct.getResources().getString(R.string.GraphLabel);
        tabtitles[2] = ct.getResources().getString(R.string.HistoryLabel);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {

            // Open FragmentTab1.java
            case 0:
                return this.getFontesFragment();

            // Open FragmentTab3.java
            case 1:
                return this.getGraphFragment();

            // Open FragmentTab2.java
            case 2:
                return this.getHistoricFragment();
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabtitles[position];
    }

    public FontesFragment getFontesFragment() {
        if (mpFontesFrag == null)
            mpFontesFrag = (FontesFragment) lFm.findFragmentByTag(MainActivity.FONTES);
        if (mpFontesFrag == null) mpFontesFrag = FontesFragment.newInstance(MainActivity.FONTES, 1);

        //mpFontesFrag.onHiddenChanged(false);
        return mpFontesFrag;
    }

    public FonteGraphFragment getGraphFragment() {
        if (mpGraphFrag == null)
            mpGraphFrag = (FonteGraphFragment) lFm.findFragmentByTag(MainActivity.GRAPHIC);
        if (mpGraphFrag == null)
            mpGraphFrag = FonteGraphFragment.newInstance(MainActivity.GRAPHIC, 2);

        //mpGraphFrag.onHiddenChanged(false);
        return mpGraphFrag;
    }

    public FonteHistoryFragment getHistoricFragment() {
        if (mpHistoryFrag == null)
            mpHistoryFrag = (FonteHistoryFragment) lFm.findFragmentByTag(MainActivity.HISTORY);
        if (mpHistoryFrag == null) mpHistoryFrag = FonteHistoryFragment.newInstance(-1, -1);

        //mpHistoryFrag.onHiddenChanged(false);
        return mpHistoryFrag;
    }

    public void restoreFontesFragment(FontesFragment mpFrag) {
        mpFontesFrag = mpFrag;
    }

    public void restoreGraphFragment(FonteGraphFragment mpFrag) {
        mpGraphFrag = mpFrag;
    }

    public void restoreHistoricFragment(FonteHistoryFragment mpFrag) {
        mpHistoryFrag = mpFrag;
    }
}
