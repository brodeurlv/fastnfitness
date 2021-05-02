package com.easyfitness;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.easyfitness.enums.DistanceUnit;
import com.easyfitness.enums.Unit;
import com.easyfitness.enums.WeightUnit;
import com.easyfitness.utils.DateConverter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SettingsFragment extends PreferenceFragmentCompat {

    public final static String WEIGHT_UNIT_PARAM = "defaultUnit";
    public final static String DISTANCE_UNIT_PARAM = "defaultDistanceUnit";
    public final static String SIZE_UNIT_PARAM = "defaultSizeUnit";
    public final static String FREQUENCY_BACKUP_PARAM = "defaultBackupSetting";
    Toolbar top_toolbar = null;
    MainActivity mActivity = null;

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static SettingsFragment newInstance(String name, int id) {
        return new SettingsFragment();
    }

    public static WeightUnit getDefaultWeightUnit(Activity activity) {
        // Getting the prefered default units.
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(activity);
        WeightUnit weightUnit = WeightUnit.KG;
        try {
            weightUnit = WeightUnit.fromInteger(Integer.parseInt(SP.getString(SettingsFragment.WEIGHT_UNIT_PARAM, String.valueOf(WeightUnit.KG))));
        } catch (NumberFormatException e) {
            weightUnit = WeightUnit.KG;
        }
        return weightUnit;
    }

    public static DistanceUnit getDefaultDistanceUnit(Activity activity) {
        // Getting the prefered default units.
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(activity);
        DistanceUnit distanceUnit = DistanceUnit.KM;
        try {
            distanceUnit = DistanceUnit.fromInteger(Integer.parseInt(SP.getString(SettingsFragment.DISTANCE_UNIT_PARAM, String.valueOf(DistanceUnit.KM))));
        } catch (NumberFormatException e) {
            distanceUnit = DistanceUnit.KM;
        }
        return distanceUnit;
    }

    public static Unit getDefaultSizeUnit(Activity activity) {
        // Getting the prefered default units.
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(activity);
        Unit unit = Unit.CM;
        try {
            unit = Unit.fromInteger(Integer.parseInt(SP.getString(SettingsFragment.SIZE_UNIT_PARAM, String.valueOf(Unit.CM))));
        } catch (NumberFormatException e) {
            unit = Unit.CM;
        }
        return unit;
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

        Preference myPref2 = findPreference(WEIGHT_UNIT_PARAM);
        myPref2.setOnPreferenceChangeListener((preference, newValue) -> {
            ListPreference listPreference = (ListPreference) preference;
            if (newValue instanceof String) {
                //find the index of changed value in settings.
                updateSummary(listPreference, (String) newValue, getString(R.string.pref_preferredUnitSummary));
            }

            return true;
        });

        Preference myPref3 = findPreference(DISTANCE_UNIT_PARAM);
        myPref3.setOnPreferenceChangeListener((preference, newValue) -> {
            ListPreference listPreference = (ListPreference) preference;
            if (newValue instanceof String) {
                //find the index of changed value in settings.
                updateSummary(listPreference, (String) newValue, getString(R.string.pref_preferredUnitSummary));
            }

            return true;
        });

        Preference myPref4 = findPreference(SIZE_UNIT_PARAM);
        myPref4.setOnPreferenceChangeListener((preference, newValue) -> {
            ListPreference listPreference = (ListPreference) preference;
            if (newValue instanceof String) {
                //find the index of changed value in settings.
                updateSummary(listPreference, (String) newValue, getString(R.string.pref_preferredUnitSummary));
            }

            return true;
        });

        Preference myPref5 = findPreference(FREQUENCY_BACKUP_PARAM);
        myPref5.setOnPreferenceChangeListener((preference, newValue) -> {
            ListPreference listPreference = (ListPreference) preference;
            if (newValue instanceof String) {
                //find the index of changed value in settings.
                SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(mActivity);
                long lastBackupUTCTime = SP.getLong("prefLastTimeBackupUTCTime", -1);
                String dateText;
                if (lastBackupUTCTime == -1) {
                    dateText = getString(R.string.backup_notStarted);
                } else {
                    dateText = getDate(lastBackupUTCTime);
                }
                int prefIndex = listPreference.findIndexOfValue((String) newValue);
                myPref5.setSummary(listPreference.getEntries()[prefIndex] + "\n"
                        + getString(R.string.pref_lastBackupSettingSummary) + dateText);
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

    private String getDate(long milliSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        Date date = calendar.getTime();
        return DateConverter.dateToLocalDateStr(date, getContext()) + " " + formatter.format(date);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String param) {
        //addPreferencesFromResource(R.xml.settings);
        setPreferencesFromResource(R.xml.settings2, param);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        ListPreference myPref2 = (ListPreference) findPreference(SettingsFragment.WEIGHT_UNIT_PARAM);
        String boolVal = sharedPreferences.getString(SettingsFragment.WEIGHT_UNIT_PARAM, String.valueOf(WeightUnit.KG));
        updateSummary(myPref2, boolVal, getString(R.string.pref_preferredUnitSummary));

        ListPreference myPref3 = (ListPreference) findPreference(SettingsFragment.DISTANCE_UNIT_PARAM);
        String boolVal3 = sharedPreferences.getString(SettingsFragment.DISTANCE_UNIT_PARAM, String.valueOf(DistanceUnit.KM));
        updateSummary(myPref3, boolVal3, getString(R.string.pref_preferredUnitSummary));

        ListPreference myPref4 = (ListPreference) findPreference(SettingsFragment.SIZE_UNIT_PARAM);
        String boolVal4 = sharedPreferences.getString(SettingsFragment.SIZE_UNIT_PARAM, String.valueOf(Unit.CM));
        updateSummary(myPref4, boolVal4, getString(R.string.pref_preferredUnitSummary));

        long lastBackupUTCTime = sharedPreferences.getLong("prefLastTimeBackupUTCTime", -1);
        updateLastBackupSummary(sharedPreferences, lastBackupUTCTime);

        ListPreference dayNightModePref = (ListPreference) findPreference("dayNightAuto");
        String dayNightValue = sharedPreferences.getString("dayNightAuto", "2");
        updateSummary(dayNightModePref, dayNightValue, "");
    }

    public void updateLastBackupSummary(SharedPreferences sharedPreferences, long lastBackupUTCTime) {
        ListPreference myPref5 = (ListPreference) findPreference(SettingsFragment.FREQUENCY_BACKUP_PARAM);
        String boolVal5 = sharedPreferences.getString(SettingsFragment.FREQUENCY_BACKUP_PARAM, "0");
        int prefIndex = myPref5.findIndexOfValue(boolVal5);
        String dateText;
        if (lastBackupUTCTime == -1) {
            dateText = getString(R.string.backup_notStarted);
        } else {
            dateText = getDate(lastBackupUTCTime);
        }
        myPref5.setSummary(myPref5.getEntries()[prefIndex] + "\n"
                + getString(R.string.pref_lastBackupSettingSummary) + dateText);
    }

    private void updateSummary(ListPreference pref, String val, String prefix) {
        int prefIndex = pref.findIndexOfValue(val);
        if (prefIndex >= 0) {
            //finally set's it value changed
            pref.setSummary(prefix + pref.getEntries()[prefIndex]);
        }
    }
}
