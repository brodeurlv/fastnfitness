package com.easyfitness.fonte;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.easyfitness.MainActivity;
import com.easyfitness.R;

public class FontesViewPagerAdapter extends FragmentPagerAdapter {

	final int PAGE_COUNT = 3;
	// Tab Titles
	private String tabtitles[] = new String[3]; // { "Records", "Graphics", "History" };
	Context context;
	
	private FontesFragment mpFontesFrag = null;   
	private FonteHistoryFragment mpHistoryFrag = null;  
	private FonteGraphFragment mpGraphFrag = null; 
	
	FragmentManager lFm = null;
	
	public FontesViewPagerAdapter(FragmentManager fm, Context ct) {
		super(fm);
		lFm = fm;
		tabtitles[0] =  ct.getResources().getString(R.string.RecordsLabel);
		tabtitles[1] =  ct.getResources().getString(R.string.GraphLabel);
		tabtitles[2] =  ct.getResources().getString(R.string.HistoryLabel);
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
	
	private FontesFragment getFontesFragment() {
		if (mpFontesFrag==null) 	mpFontesFrag= (FontesFragment) lFm.findFragmentByTag(MainActivity.FONTES);
		if (mpFontesFrag==null) 	mpFontesFrag= FontesFragment.newInstance(MainActivity.FONTES, 1); 
		
		return mpFontesFrag;
	}
	private FonteGraphFragment getGraphFragment() {
		if (mpGraphFrag==null) 	mpGraphFrag= (FonteGraphFragment) lFm.findFragmentByTag(MainActivity.GRAPHIC);
		if (mpGraphFrag==null) 	mpGraphFrag= FonteGraphFragment.newInstance(MainActivity.GRAPHIC, 2); 
		
		return mpGraphFrag;
	}
	private FonteHistoryFragment getHistoricFragment() {
		if (mpHistoryFrag==null) 	mpHistoryFrag= (FonteHistoryFragment) lFm.findFragmentByTag(MainActivity.HISTORY);
		if (mpHistoryFrag==null) 	mpHistoryFrag= FonteHistoryFragment.newInstance(MainActivity.HISTORY, 3); 
		
		return mpHistoryFrag;
	}
	
}