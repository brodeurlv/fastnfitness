package com.easyfitness.programs;

import android.content.Context;

import com.easyfitness.MainActivity;
import com.easyfitness.R;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class ProgramsAdapter extends FragmentPagerAdapter {

    final int PAGE_COUNT = 3;
    FragmentManager lFm = null;
    // Tab Titles
    private String tabtitles[] = new String[3]; // { "Records", "Graphics", "History" };
    private ProgramsFragment mpProgramsFrag = null;

    public ProgramsAdapter(FragmentManager fm, Context ct) {
        super(fm);
        lFm = fm;
        tabtitles[0] = ct.getResources().getString(R.string.RecordLabel);
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
                return this.getProgramsFragment();
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabtitles[position];
    }

    public ProgramsFragment getProgramsFragment() {
        if (mpProgramsFrag == null)
            mpProgramsFrag = (ProgramsFragment) lFm.findFragmentByTag(MainActivity.PROGRAMS);
        if (mpProgramsFrag == null) mpProgramsFrag = ProgramsFragment.newInstance(MainActivity.PROGRAMS, 1);

        return mpProgramsFrag;
    }
}
