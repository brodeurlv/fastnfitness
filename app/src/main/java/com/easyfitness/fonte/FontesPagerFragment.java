package com.easyfitness.fonte;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;

import com.easyfitness.MainActivity;
import com.easyfitness.R;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

public class FontesPagerFragment extends Fragment {
    FragmentPagerItemAdapter pagerAdapter = null;
    ViewPager mViewPager = null;
    private String name;
    private int id;
    private FontesFragment mpFontesFrag = null;
    private FonteHistoryFragment mpHistoryFrag = null;
    private FonteGraphFragment mpGraphFrag = null;

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static FontesPagerFragment newInstance(String name, int id) {
        FontesPagerFragment f = new FontesPagerFragment();

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

        View view = inflater.inflate(R.layout.pager, container, false);

        // Locate the viewpager in activity_main.xml
        mViewPager = view.findViewById(R.id.pager);

        if (mViewPager.getAdapter() == null) {

            Bundle args = this.getArguments();
            args.putLong("machineID", -1);
            args.putLong("machineProfile", -1);

            pagerAdapter = new FragmentPagerItemAdapter(
                getChildFragmentManager(), FragmentPagerItems.with(this.getContext())
                .add(R.string.ExerciceLabel, FontesFragment.class)
                .add(R.string.GraphLabel, FonteGraphFragment.class, args)
                .add(R.string.HistoryLabel, FonteHistoryFragment.class, args)
                .create());

            mViewPager.setAdapter(pagerAdapter);

            SmartTabLayout viewPagerTab = view.findViewById(R.id.viewpagertab);
            viewPagerTab.setViewPager(mViewPager);

            viewPagerTab.setOnPageChangeListener(new OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    //if (position != 0) {
                    //pagerAdapter.getItem(position).onHiddenChanged(false);
                    //Fragment frag1 = (Fragment) pagerAdapter.instantiateItem(mViewPager, mViewPager.getCurrentItem());

                    Fragment frag1 = pagerAdapter.getPage(position);
                    if (frag1 != null)
                        frag1.onHiddenChanged(false); // Refresh data

                    //}
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });

            // Locate the viewpager in activity_main.xml
            //ViewPager viewPager = view.findViewById(R.id.pager);

            // Set the ViewPagerAdapter into ViewPager
            //viewPager.setAdapter(new FontesViewPagerAdapter(getChildFragmentManager(), getActivity().getApplicationContext()));

            // Bind the tabs to the ViewPager
            //PagerSlidingTabStrip tabs = view.findViewById(R.id.tabs);
            //tabs.setViewPager(viewPager);

/*
            viewPager.addOnPageChangeListener(new OnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    // N'update pas le fragment principal car c'est lui qui dirige les autres.
                    //if (position != 0)
                    //    getViewPagerAdapter().getItem(position).onHiddenChanged(false);
                }

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
*/

        }

        // Inflate the layout for this fragment
        return view;
    }

    //...

    public void onPageSelected(int position) {
        //.instantiateItem() from until .destroyItem() is called it will be able to get the Fragment of page.
        //Fragment page = pagerAdapter.getPage(position);
    }

    public ViewPager getViewPager() {
        return (ViewPager) getView().findViewById(R.id.pager);
    }

    public FragmentPagerItemAdapter getViewPagerAdapter() {
        return (FragmentPagerItemAdapter) ((ViewPager) (getView().findViewById(R.id.pager))).getAdapter();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
/*
            mpFontesFrag = (FontesFragment) getChildFragmentManager().getFragment(savedInstanceState, MainActivity.FONTES);
            getViewPagerAdapter().restoreFontesFragment(mpFontesFrag);

            mpGraphFrag = (FonteGraphFragment) getChildFragmentManager().getFragment(savedInstanceState, MainActivity.GRAPHIC);
            getViewPagerAdapter().restoreGraphFragment(mpGraphFrag);

            mpHistoryFrag = (FonteHistoryFragment) getChildFragmentManager().getFragment(savedInstanceState, MainActivity.HISTORY);
            getViewPagerAdapter().restoreHistoricFragment(mpHistoryFrag);
*/
        }
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle outState) {
        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);

/*
        if (getViewPagerAdapter().getFontesFragment() != null && getViewPagerAdapter().getFontesFragment().isAdded())
            getChildFragmentManager().putFragment(outState, MainActivity.FONTES, getViewPagerAdapter().getFontesFragment());
        if (getViewPagerAdapter().getGraphFragment() != null && getViewPagerAdapter().getGraphFragment().isAdded())
            getChildFragmentManager().putFragment(outState, MainActivity.GRAPHIC, getViewPagerAdapter().getGraphFragment());
        if (getViewPagerAdapter().getHistoricFragment() != null && getViewPagerAdapter().getHistoricFragment().isAdded())
            getChildFragmentManager().putFragment(outState, MainActivity.HISTORY, getViewPagerAdapter().getHistoricFragment());
*/
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) {
            // rafraichit le fragment courant

            if (getViewPagerAdapter() != null) {
                // Moyen de rafraichir tous les fragments. Attention, les View des fragments peuvent avoir ete detruit.
                // Il faut donc que cela soit pris en compte dans le refresh des fragments.
                Fragment frag1;
                for (int i = 0; i < 3; i++) {
                    frag1 = getViewPagerAdapter().getPage(i);
                    if (frag1 != null)
                        frag1.onHiddenChanged(false); // Refresh data
                }
            }
        }
    }

    public FontesFragment getFontesFragment() {
        if (mpFontesFrag == null)
            mpFontesFrag = (FontesFragment) getChildFragmentManager().findFragmentByTag(MainActivity.FONTES);
        if (mpFontesFrag == null) mpFontesFrag = FontesFragment.newInstance(MainActivity.FONTES, 1);

        //mpFontesFrag.onHiddenChanged(false);
        return mpFontesFrag;
    }

    public FonteGraphFragment getGraphFragment() {
        if (mpGraphFrag == null)
            mpGraphFrag = (FonteGraphFragment) getChildFragmentManager().findFragmentByTag(MainActivity.GRAPHIC);
        if (mpGraphFrag == null)
            mpGraphFrag = FonteGraphFragment.newInstance(MainActivity.GRAPHIC, 2);

        //mpGraphFrag.onHiddenChanged(false);
        return mpGraphFrag;
    }

    public FonteHistoryFragment getHistoricFragment() {
        if (mpHistoryFrag == null)
            mpHistoryFrag = (FonteHistoryFragment) getChildFragmentManager().findFragmentByTag(MainActivity.HISTORY);
        if (mpHistoryFrag == null)
            mpHistoryFrag = FonteHistoryFragment.newInstance(-1, -1);

        //mpHistoryFrag.onHiddenChanged(false);
        return mpHistoryFrag;
    }
}
