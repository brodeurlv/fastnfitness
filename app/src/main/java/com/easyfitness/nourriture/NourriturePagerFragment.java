package com.easyfitness.nourriture;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

import com.easyfitness.R;
import com.easyfitness.enums.DisplayType;
import com.easyfitness.fonte.FonteGraphFragment;
import com.easyfitness.fonte.FonteHistoryFragment;
import com.easyfitness.programs.ProgramRunnerFragment;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class NourriturePagerFragment extends Fragment {
    FragmentPagerItemAdapter pagerAdapter = null;
    ViewPager mViewPager = null;

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static NourriturePagerFragment newInstance(String name, int id) {
        NourriturePagerFragment f = new NourriturePagerFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putInt("id", id);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.nourriture_pager, container, false);

        // Locate the viewpager in activity_main.xml
        mViewPager = view.findViewById(R.id.nourriture_viewpager);

        if (mViewPager.getAdapter() != null) {
            return view;
        }

        Bundle args = this.getArguments();
//            args.putLong("machineID", -1);
//            args.putLong("machineProfile", -1);

        pagerAdapter = new FragmentPagerItemAdapter(
                getChildFragmentManager(), FragmentPagerItems.with(this.getContext())
                .add(R.string.macros_entry, NourritureFragment.class, args)
                //.add(R.string.program, ProgramRunnerFragment.class, guidedWorkoutArgs)
                //.add(R.string.GraphLabel, FonteGraphFragment.class, args)
                //.add(R.string.HistoryLabel, NourritureHistoryFragment.class, args)
                .add("Totals", NourritureTotalsFragment.class, args)
                .create());

        mViewPager.setAdapter(pagerAdapter);

        SmartTabLayout viewPagerTab = view.findViewById(R.id.nourriture_pagertab);
        viewPagerTab.setViewPager(mViewPager);

        viewPagerTab.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Fragment frag1 = pagerAdapter.getPage(position);
                if (frag1 != null)
                    frag1.onHiddenChanged(false); // Refresh data
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });


        // Inflate the layout for this fragment
        return view;
    }

    public FragmentPagerItemAdapter getViewPagerAdapter() {
        return (FragmentPagerItemAdapter) ((ViewPager) getView().findViewById(R.id.nourriture_viewpager)).getAdapter();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            // rafraichit le fragment courant

            if (getViewPagerAdapter() != null) {
                // Moyen de rafraichir tous les fragments. Attention, les View des fragments peuvent avoir ete detruit.
                // Il faut donc que cela soit pris en compte dans le refresh des fragments.
                Fragment frag1;
                for (int i = 0; i < 4; i++) {
                    frag1 = getViewPagerAdapter().getPage(i);
                    if (frag1 != null)
                        frag1.onHiddenChanged(false); // Refresh data
                }
            }
        }
    }
}
