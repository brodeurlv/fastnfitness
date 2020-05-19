package com.easyfitness

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

class SettingsFragment : PreferenceFragmentCompat() {
    private var mActivity: MainActivity? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity = activity as MainActivity?


        //addPreferencesFromResource(R.xml.settings);
        val myPref = findPreference<Preference>("prefShowMP3")
        myPref!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any? ->
            if (newValue is Boolean) {
                mActivity!!.showMP3Toolbar(newValue)
            }
            true
        }
        val myPref2 = findPreference<Preference>("defaultUnit")
        myPref2!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any? ->
            val listPreference = preference as ListPreference?
            if (newValue is String) {
                //find the index of changed value in settings.
                updateSummary(listPreference, newValue as String?, getString(R.string.pref_preferredUnitSummary))
            }
            true
        }
        val myPref3 = findPreference<Preference>("defaultDistanceUnit")
        myPref3!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any? ->
            val listPreference = preference as ListPreference?
            if (newValue is String) {
                //find the index of changed value in settings.
                updateSummary(listPreference, newValue as String?, getString(R.string.pref_preferredUnitSummary))
            }
            true
        }
        val dayNightModePref = findPreference<Preference>("dayNightAuto")
        dayNightModePref!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference: Preference?, newValue: Any? ->
            val listPreference = preference as ListPreference?
            if (newValue is String) {
                updateSummary(listPreference, newValue as String?, "")
            }
            true
        }
        val playRestSound = findPreference<Preference>("playRestSound")
        playRestSound!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any? ->
            if (newValue is Boolean) {
                saveToPreference("playRestSound", newValue as Boolean?)
            }
            true
        }
        val playStaticExerciseFinishSound = findPreference<Preference>("playStaticExerciseFinishSound")
        playStaticExerciseFinishSound!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any? ->
            if (newValue is Boolean) {
                saveToPreference("playStaticExerciseFinishSound", newValue as Boolean?)
            }
            true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle, param: String) {
        //addPreferencesFromResource(R.xml.settings);
        setPreferencesFromResource(R.xml.settings2, param)
        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        val myPref2 = findPreference<Preference>("defaultUnit") as ListPreference?
        val boolVal = sharedPreferences.getString("defaultUnit", "0")
        updateSummary(myPref2, boolVal, getString(R.string.pref_preferredUnitSummary))
        val myPref3 = findPreference<Preference>("defaultDistanceUnit") as ListPreference?
        val boolVal3 = sharedPreferences.getString("defaultDistanceUnit", "0")
        updateSummary(myPref3, boolVal3, getString(R.string.pref_preferredUnitSummary))
        val dayNightModePref = findPreference<Preference>("dayNightAuto") as ListPreference?
        val dayNightValue = sharedPreferences.getString("dayNightAuto", "2")
        updateSummary(dayNightModePref, dayNightValue, "")
    }

    private fun updateSummary(pref: ListPreference?, `val`: String?, prefix: String) {
        val prefIndex = pref!!.findIndexOfValue(`val`)
        if (prefIndex >= 0) {
            //finally set's it value changed
            pref.summary = prefix + pref.entries[prefIndex]
        }
    }

    private fun saveToPreference(prefName: String?, prefBoolToSet: Boolean?) {
        val sharedPref = requireContext().getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean(prefName, prefBoolToSet!!)
        editor.apply()
    }

    companion object {
        const val WEIGHT_UNIT_PARAM = "defaultUnit"
        const val DISTANCE_UNIT_PARAM = "defaultDistanceUnit"

        /**
         * Create a new instance of DetailsFragment, initialized to
         * show the text at 'index'.
         */
        @JvmStatic
        fun newInstance(name: String?, id: Int): SettingsFragment {
            name+id // just to warning skip
            return SettingsFragment()
        }
    }
}
