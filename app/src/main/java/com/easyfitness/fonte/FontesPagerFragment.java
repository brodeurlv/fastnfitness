package com.easyfitness.fonte;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.astuetz.PagerSlidingTabStrip;

import com.easyfitness.R;

public class FontesPagerFragment extends Fragment {
	private String name; 
	private int id;


	
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
		
		View view =  inflater.inflate(R.layout.pager, container, false); 
		
		// Locate the viewpager in activity_main.xml
		ViewPager viewPager = (ViewPager) view.findViewById(R.id.pager);	
		
		if (viewPager.getAdapter()==null) {		
			// Locate the viewpager in activity_main.xml
			//ViewPager viewPager = (ViewPager) view.findViewById(R.id.pager);
		
			// Set the ViewPagerAdapter into ViewPager
			viewPager.setAdapter(new FontesViewPagerAdapter(getActivity().getSupportFragmentManager(), getActivity().getApplicationContext()));

			// Bind the tabs to the ViewPager
			PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);
			tabs.setViewPager(viewPager);

			tabs.setOnPageChangeListener(new OnPageChangeListener() {
				@Override
				public void onPageSelected(int position) {
					// N'update pas le fragment principal car c'est lui qui dirige les autres.
					if (position != 0) getViewPagerAdapter().getItem(position).onHiddenChanged(false);
				}

				@Override
				public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				}

				@Override
				public void onPageScrollStateChanged(int state) {
				}
			});

		}

		// Inflate the layout for this fragment 
		return view;
	}
	
	public ViewPager getViewPager()
	{
		return (ViewPager) getView().findViewById(R.id.pager);
	}
	
	public FontesViewPagerAdapter getViewPagerAdapter()
	{
		return (FontesViewPagerAdapter)((ViewPager)(getView().findViewById(R.id.pager))).getAdapter();
	}
	
	@Override
	public void onHiddenChanged (boolean hidden) {
		if (!hidden) {
			// rafraichit le fragment courant
			
			if ( getViewPagerAdapter() != null ) {
				// Moyen de rafraichir tous les fragments. Attention, les View des fragments peuvent avoir ete detruit. 
				// Il faut donc que cela soit pris en compte dans le refresh des fragments. 
				for (int i = 0; i < 3; i++) {
					getViewPagerAdapter().getItem(i).onHiddenChanged(false);
				}
			}
		}			
	}
}

