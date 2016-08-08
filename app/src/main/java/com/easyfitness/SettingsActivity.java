package com.easyfitness;

import java.util.List;

import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.easyfitness.utils.AppCompatPreferenceActivity;

public class SettingsActivity extends AppCompatPreferenceActivity  {
	
	  @Override
	  public void onBuildHeaders(List<Header> target) {
	    loadHeadersFromResource(R.xml.settings, target);

	    setContentView(R.layout.setting_activity);
	    Toolbar toolbar = (Toolbar) findViewById(R.id.actionToolbarSettings);
	    setSupportActionBar(toolbar);

	    ActionBar bar = getSupportActionBar();
	    bar.setHomeButtonEnabled(true);
	    bar.setDisplayHomeAsUpEnabled(true);
	    bar.setDisplayShowTitleEnabled(true);
	    bar.setHomeAsUpIndicator(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
	    bar.setTitle("Settings"); ///TODO : mettre un static
	  }

	  @Override
	  protected boolean isValidFragment(String fragmentName) {
	    return SettingsFragment.class.getName().equals(fragmentName);
	  }

	  @Override
	  public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	      case android.R.id.home:
	        onBackPressed();
	        break;
	    }
	    return super.onOptionsItemSelected(item);
	  }
}
