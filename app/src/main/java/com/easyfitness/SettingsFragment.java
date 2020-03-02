package com.easyfitness;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragmentCompat {

    Toolbar top_toolbar = null;
    MainActivity mActivity = null;

    public final static String WEIGHT_UNIT_PARAM =  "defaultUnit";
    public final static String DISTANCE_UNIT_PARAM =  "defaultDistanceUnit";

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static SettingsFragment newInstance(String name, int id) {
        SettingsFragment f = new SettingsFragment();
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = (MainActivity) getActivity();

        //addPreferencesFromResource(R.xml.settings);

        Preference myPref = findPreference("prefShowMP3");
        myPref.setOnPreferenceChangeListener((preference, newValue) -> {
            if (newValue instanceof Boolean) {
                Boolean boolVal = (Boolean) newValue;
                mActivity.showMP3Toolbar(boolVal);
            }

            return true;
        });

        Preference myPref2 = findPreference("defaultUnit");
        myPref2.setOnPreferenceChangeListener((preference, newValue) -> {
            ListPreference listPreference = (ListPreference) preference;
            if (newValue instanceof String) {
                //find the index of changed value in settings.
                updateSummary(listPreference, (String) newValue, getString(R.string.pref_preferredUnitSummary));
            }

            return true;
        });

        Preference myPref3 = findPreference("defaultDistanceUnit");
        myPref3.setOnPreferenceChangeListener((preference, newValue) -> {
            ListPreference listPreference = (ListPreference) preference;
            if (newValue instanceof String) {
                //find the index of changed value in settings.
                updateSummary(listPreference, (String) newValue, getString(R.string.pref_preferredUnitSummary));
            }

            return true;
        });

        Preference dayNightModePref = findPreference("dayNightAuto");
        dayNightModePref.setOnPreferenceChangeListener((preference, newValue) -> {
            ListPreference listPreference = (ListPreference) preference;
            if (newValue instanceof String) {
                updateSummary(listPreference, (String) newValue, "");
            }

            return true;
        });
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String param) {
        //addPreferencesFromResource(R.xml.settings);
        setPreferencesFromResource(R.xml.settings2, param);

        SharedPreferences sharedPreferences;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        ListPreference myPref2 = (ListPreference) findPreference("defaultUnit");
        String boolVal = sharedPreferences.getString("defaultUnit", "0");
        updateSummary(myPref2, boolVal, getString(R.string.pref_preferredUnitSummary));

        ListPreference myPref3 = (ListPreference) findPreference("defaultDistanceUnit");
        String boolVal3 = sharedPreferences.getString("defaultDistanceUnit", "0");
        updateSummary(myPref3, boolVal3, getString(R.string.pref_preferredUnitSummary));

        ListPreference dayNightModePref = (ListPreference) findPreference("dayNightAuto");
        String dayNightValue = sharedPreferences.getString("dayNightAuto", "2");
        updateSummary(dayNightModePref, dayNightValue, "");

    }

    private void updateSummary(ListPreference pref, String val, String prefix) {
        int prefIndex = pref.findIndexOfValue(val);
        if (prefIndex >= 0) {
            //finally set's it value changed
            pref.setSummary(prefix + pref.getEntries()[prefIndex]);
        }
    }
}
