package com.easyfitness;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.app.ActionBar;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;

public class SettingsFragment extends PreferenceFragmentCompat {
	
	Toolbar top_toolbar = null;
    MainActivity mActivity = null;
	
    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static SettingsFragment newInstance(String name, int id) {
    	SettingsFragment f = new SettingsFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putInt("id", id);
        f.setArguments(args);



        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = (MainActivity) getActivity();

        addPreferencesFromResource(R.xml.settings);

        Preference myPref = (Preference) findPreference("prefShowMP3");
        myPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if(newValue instanceof Boolean){
                    Boolean boolVal = (Boolean)newValue;
                    mActivity.showMP3Toolbar(boolVal);
                }

                return true;
            }
        });
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String param) {
        //addPreferencesFromResource(R.xml.settings);
    }


    @Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
	    super.onCreateOptionsMenu(menu, inflater);
	    menu.clear();
	    //fragment specific menu creation
	    //((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	    //((MainActivity)getActivity()).getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	}
}

