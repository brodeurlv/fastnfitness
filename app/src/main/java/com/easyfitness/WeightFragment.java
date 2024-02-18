package com.easyfitness;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.easyfitness.DAO.DAOProfile;
import com.easyfitness.DAO.DAOProfileWeight;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.bodymeasures.BodyMeasure;
import com.easyfitness.DAO.bodymeasures.BodyPart;
import com.easyfitness.DAO.bodymeasures.BodyPartExtensions;
import com.easyfitness.DAO.bodymeasures.DAOBodyMeasure;
import com.easyfitness.DAO.bodymeasures.DAOBodyPart;
import com.easyfitness.DAO.export.OpenScaleSync;
import com.easyfitness.bodymeasures.BodyPartDetailsFragment;
import com.easyfitness.enums.Unit;
import com.easyfitness.enums.UnitType;
import com.easyfitness.graph.MiniDateGraph;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.enums.Gender;
import com.easyfitness.utils.UnitConverter;
import com.easyfitness.utils.Value;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class WeightFragment extends Fragment {
    private final DAOProfile mDb = null;
    private final AdapterView.OnClickListener showDetailsFragment = v -> {
        int bodyPartID = BodyPartExtensions.WEIGHT;
        switch (v.getId()) {
            case R.id.weightGraph:
            case R.id.weightDetailsButton:
                bodyPartID = BodyPartExtensions.WEIGHT;
                break;
            case R.id.fatGraph:
            case R.id.fatDetailsButton:
                bodyPartID = BodyPartExtensions.FAT;
                break;
            case R.id.musclesGraph:
            case R.id.musclesDetailsButton:
                bodyPartID = BodyPartExtensions.MUSCLES;
                break;
            case R.id.waterGraph:
            case R.id.waterDetailsButton:
                bodyPartID = BodyPartExtensions.WATER;
                break;
            case R.id.sizeGraph:
            case R.id.sizeDetailsButton:
                bodyPartID = BodyPartExtensions.SIZE;
                break;
        }

        BodyPartDetailsFragment fragment = BodyPartDetailsFragment.newInstance(bodyPartID, false);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, fragment, MainActivity.BODYTRACKINGDETAILS);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    };
    private final OnClickListener showHelp = v -> {
        switch (v.getId()) {
            case R.id.imcHelp:
                new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                        .setTitleText(R.string.BMI_dialog_title)
                        .setContentText(getString(R.string.BMI_formula))
                        .setConfirmText(getResources().getText(android.R.string.ok).toString())
                        .showCancelButton(true)
                        .show();
                break;
            case R.id.ffmiHelp:
                new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                        .setTitleText(R.string.FFMI_dialog_title)
                        .setContentText(getString(R.string.FFMI_formula))
                        .setConfirmText(getResources().getText(android.R.string.ok).toString())
                        .showCancelButton(true)
                        .show();
                break;
            case R.id.bmrHelp:
                new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                        .setTitleText(R.string.bmr_dialog_title)
                        .setContentText(getString(R.string.bmr_formula))
                        .setConfirmText(getResources().getText(android.R.string.ok).toString())
                        .showCancelButton(true)
                        .show();
                break;
            case R.id.dailyCalorieHelp:
                new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                        .setTitleText(R.string.daily_calorie_alert)
                        .setContentText(getString(R.string.daily_calorie_alert_body))
                        .setConfirmText(getResources().getText(android.R.string.ok).toString())
                        .showCancelButton(true)
                        .show();
                break;
            case R.id.rfmHelp:
                new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                        .setTitleText(R.string.RFM_dialog_title)
                        .setContentText(getString(R.string.RFM_female_formula) +
                                getString(R.string.RFM_male_formula))
                        .setConfirmText(getResources().getText(android.R.string.ok).toString())
                        .showCancelButton(true)
                        .show();
                break;
        }
    };
    double calorieMultiplier; //Daily calorie multiplier
    MainActivity mActivity = null;

    private SwipeRefreshLayout pullToRefresh = null;
    private TextView weightEdit = null;
    private TextView fatEdit = null;
    private TextView musclesEdit = null;
    private TextView waterEdit = null;
    private TextView sizeEdit = null;
    private TextView imcText = null;
    private TextView imcRank = null;
    private TextView ffmiText = null;
    private TextView ffmiRank = null;
    private TextView bmrCals = null;
    private TextView bmrText = null;
    private TextView dailyCalorieValue = null;
    private TextView rfmText = null;
    private TextView rfmRank = null;
    private LineChart mWeightLineChart;
    private LineChart mFatLineChart;
    private LineChart mMusclesLineChart;
    private LineChart mWaterLineChart;
    private LineChart mSizeLineChart;
    private DAOProfileWeight mWeightDb = null;
    private DAOBodyMeasure mDbBodyMeasure = null;
    private DAOBodyPart mDbBodyPart;
    private MiniDateGraph mWeightGraph;
    private MiniDateGraph mFatGraph;
    private MiniDateGraph mMusclesGraph;
    private MiniDateGraph mWaterGraph;
    private MiniDateGraph mSizeGraph;
    private BodyPart weightBodyPart;
    private BodyPart fatBodyPart;
    private BodyPart musclesBodyPart;
    private BodyPart waterBodyPart;
    private BodyPart sizeBodyPart;
    private AppViMo appViMo;
    private final OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            ValuesEditorDialogbox editorDialogbox;
            switch (view.getId()) {
                case R.id.weightInput:
                    BodyMeasure lastWeightMeasure = mDbBodyMeasure.getLastBodyMeasures(weightBodyPart.getId(), getProfile());
                    Value lastWeighValue = getValueFromLastMeasure(lastWeightMeasure, SettingsFragment.getDefaultWeightUnit(getActivity()).toUnit(), null, R.string.weightLabel);
                    editorDialogbox = new ValuesEditorDialogbox(getActivity(), new Date(), "", new Value[]{lastWeighValue});
                    editorDialogbox.setTitle(R.string.AddLabel);
                    editorDialogbox.setPositiveButton(R.string.AddLabel);
                    editorDialogbox.setOnDismissListener(dialog -> {
                        if (!editorDialogbox.isCancelled()) {
                            Date date = DateConverter.localDateStrToDate(editorDialogbox.getDate(), getContext());
                            Value newValue = editorDialogbox.getValues()[0];
                            mDbBodyMeasure.addBodyMeasure(date, weightBodyPart.getId(), newValue, getProfile().getId());
                            refreshData();
                        }
                    });
                    editorDialogbox.show();
                    break;
                case R.id.fatInput:
                    BodyMeasure lastFatMeasure = mDbBodyMeasure.getLastBodyMeasures(fatBodyPart.getId(), getProfile());
                    final Value lastFatValue = getValueFromLastMeasure(lastFatMeasure, Unit.PERCENTAGE, null, R.string.fatLabel);
                    editorDialogbox = new ValuesEditorDialogbox(getActivity(), new Date(), "", new Value[]{lastFatValue});
                    editorDialogbox.setTitle(R.string.AddLabel);
                    editorDialogbox.setPositiveButton(R.string.AddLabel);
                    editorDialogbox.setOnDismissListener(dialog -> {
                        if (!editorDialogbox.isCancelled()) {
                            Date date = DateConverter.localDateStrToDate(editorDialogbox.getDate(), getContext());
                            Value newValue = editorDialogbox.getValues()[0];
                            mDbBodyMeasure.addBodyMeasure(date, fatBodyPart.getId(), newValue, getProfile().getId());
                            refreshData();
                        }
                    });
                    editorDialogbox.setOnCancelListener(null);
                    editorDialogbox.show();
                    break;
                case R.id.musclesInput:
                    BodyMeasure lastMusclesMeasure = mDbBodyMeasure.getLastBodyMeasures(musclesBodyPart.getId(), getProfile());
                    Value lastMusclesValue = getValueFromLastMeasure(lastMusclesMeasure, Unit.PERCENTAGE, null, R.string.musclesLabel);
                    editorDialogbox = new ValuesEditorDialogbox(getActivity(), new Date(), "", new Value[]{lastMusclesValue});
                    editorDialogbox.setTitle(R.string.AddLabel);
                    editorDialogbox.setPositiveButton(R.string.AddLabel);
                    editorDialogbox.setOnDismissListener(dialog -> {
                        if (!editorDialogbox.isCancelled()) {
                            Date date = DateConverter.localDateStrToDate(editorDialogbox.getDate(), getContext());
                            Value newValue = editorDialogbox.getValues()[0];
                            mDbBodyMeasure.addBodyMeasure(date, musclesBodyPart.getId(), newValue, getProfile().getId());
                            refreshData();
                        }
                    });
                    editorDialogbox.setOnCancelListener(null);
                    editorDialogbox.show();
                    break;
                case R.id.waterInput:
                    BodyMeasure lastWaterMeasure = mDbBodyMeasure.getLastBodyMeasures(waterBodyPart.getId(), getProfile());
                    Value lastWaterValue = getValueFromLastMeasure(lastWaterMeasure, Unit.PERCENTAGE, null, R.string.waterLabel);
                    editorDialogbox = new ValuesEditorDialogbox(getActivity(), new Date(), "", new Value[]{lastWaterValue});
                    editorDialogbox.setTitle(R.string.AddLabel);
                    editorDialogbox.setPositiveButton(R.string.AddLabel);
                    editorDialogbox.setOnDismissListener(dialog -> {
                        if (!editorDialogbox.isCancelled()) {
                            Date date = DateConverter.localDateStrToDate(editorDialogbox.getDate(), getContext());
                            Value newValue = editorDialogbox.getValues()[0];
                            mDbBodyMeasure.addBodyMeasure(date, waterBodyPart.getId(), newValue, getProfile().getId());
                            refreshData();
                        }
                    });
                    editorDialogbox.setOnCancelListener(null);
                    editorDialogbox.show();
                    break;
                case R.id.sizeInput:
                    BodyMeasure lastSizeMeasure = mDbBodyMeasure.getLastBodyMeasures(sizeBodyPart.getId(), getProfile());
                    Value lastSizeValue = getValueFromLastMeasure(lastSizeMeasure, SettingsFragment.getDefaultSizeUnit(getActivity()), null, R.string.size);
                    editorDialogbox = new ValuesEditorDialogbox(getActivity(), new Date(), "", new Value[]{lastSizeValue});
                    editorDialogbox.setTitle(R.string.AddLabel);
                    editorDialogbox.setPositiveButton(R.string.AddLabel);
                    editorDialogbox.setOnDismissListener(dialog -> {
                        if (!editorDialogbox.isCancelled()) {
                            Date date = DateConverter.localDateStrToDate(editorDialogbox.getDate(), getContext());
                            Value newValue = editorDialogbox.getValues()[0];
                            mDbBodyMeasure.addBodyMeasure(date, sizeBodyPart.getId(), newValue, getProfile().getId());
                            refreshData();
                        }
                    });
                    editorDialogbox.show();
                    break;
            }
        }
    };

    private final OnClickListener mOnAddAllEntriesClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            BodyMeasure lastWeightMeasure = mDbBodyMeasure.getLastBodyMeasures(weightBodyPart.getId(), getProfile());
            BodyMeasure lastFatMeasure = mDbBodyMeasure.getLastBodyMeasures(fatBodyPart.getId(), getProfile());
            BodyMeasure lastMusclesMeasure = mDbBodyMeasure.getLastBodyMeasures(musclesBodyPart.getId(), getProfile());
            BodyMeasure lastWaterMeasure = mDbBodyMeasure.getLastBodyMeasures(waterBodyPart.getId(), getProfile());

            Value lastWeightValue = getValueFromLastMeasure(lastWeightMeasure, SettingsFragment.getDefaultWeightUnit(getActivity()).toUnit(), String.valueOf(weightBodyPart.getId()), R.string.weightLabel);
            Value lastFatValue = getValueFromLastMeasure(lastFatMeasure, Unit.PERCENTAGE, String.valueOf(fatBodyPart.getId()), R.string.fatLabel);
            Value lastMusclesValue = getValueFromLastMeasure(lastMusclesMeasure, Unit.PERCENTAGE, String.valueOf(musclesBodyPart.getId()), R.string.musclesLabel);
            Value lastWaterValue = getValueFromLastMeasure(lastWaterMeasure, Unit.PERCENTAGE, String.valueOf(waterBodyPart.getId()), R.string.waterLabel);

            // Add other unit options in addition to percentages
            Unit fatUnit = lastFatValue.getUnit();
            fatUnit.setUnitType(UnitType.WEIGHT_OR_PERCENTAGE);
            lastFatValue.setUnit(fatUnit);

            Unit musclesUnit = lastMusclesValue.getUnit();
            musclesUnit.setUnitType(UnitType.WEIGHT_OR_PERCENTAGE);
            lastMusclesValue.setUnit(musclesUnit);

            Unit waterUnit = lastWaterValue.getUnit();
            waterUnit.setUnitType(UnitType.WEIGHT_OR_PERCENTAGE);
            lastWaterValue.setUnit(waterUnit);

            ValuesEditorDialogbox editorDialog = new ValuesEditorDialogbox(getActivity(), new Date(), "", new Value[]{lastWeightValue, lastFatValue, lastMusclesValue, lastWaterValue});
            editorDialog.setTitle(R.string.AddLabel);
            editorDialog.setPositiveButton(R.string.AddLabel);
            editorDialog.setOnDismissListener(dialog -> {
                if (editorDialog.isCancelled()) {
                    return;
                }
                Date date = DateConverter.localDateStrToDate(editorDialog.getDate(), getContext());
                long profileId = getProfile().getId();
                Value[] newValues = editorDialog.getValues();
                Value newWeightValue = null;
                // Find the weight value if we need it as the baseline
                for (Value newValue : newValues) {
                    if (newValue.getId().equals(String.valueOf(weightBodyPart.getId()))) {
                        newWeightValue = newValue;
                        break;
                    }
                }
                // Keep the dialog open if the weight was not set
                if (newWeightValue.getValue() == null) {
                    editorDialog.show();
                    KToast.errorToast(getActivity(), getActivity().getResources().getText(R.string.weightRequiredForMultipleInputs).toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                    return;
                }
                for (Value newValue : newValues) {
                    long bodyPartId = Long.parseLong(newValue.getId());
                    // Value was not entered
                    if (newValue.getValue() == null) {
                        continue;
                    }
                    // If the unit is not in percent and it is a percentage measurement convert it
                    if (newValue.getUnit() != Unit.PERCENTAGE && (bodyPartId == fatBodyPart.getId() || bodyPartId == musclesBodyPart.getId() || bodyPartId == waterBodyPart.getId())) {
                        // Convert them to the same unit if necessary
                        if (newValue.getUnit() != newWeightValue.getUnit()) {
                            newValue.setValue(UnitConverter.weightConverter(newValue.getValue(), newValue.getUnit(), newWeightValue.getUnit()));
                            newValue.setUnit(newWeightValue.getUnit());
                        }

                        // Convert absolute value to percentage
                        newValue.setValue((newValue.getValue() / newWeightValue.getValue()) * 100);
                        newValue.setUnit(Unit.PERCENTAGE);
                    }
                    mDbBodyMeasure.addBodyMeasure(date, bodyPartId, newValue, profileId);
                }
                refreshData();

            });
            editorDialog.show();
        }
    };

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static WeightFragment newInstance(String name, int id) {
        WeightFragment f = new WeightFragment();

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

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.tab_weight, container, false);

        // Disable pullToRefresh on default
        pullToRefresh = view.findViewById(R.id.pullToRefresh);
        pullToRefresh.setEnabled(false);
        try{
            // Check if openScale is installed
            PackageManager pm  = mActivity.getPackageManager();
            pm.getPackageInfo("com.health.openscale", 0);

            // If openScale is installed enable pull to refresh
            pullToRefresh.setEnabled(true);

            // Set listener for updateing
            pullToRefresh.setOnRefreshListener(() -> {

                // Check access permission to openScale
                int OPENSCALE_REQUEST_CODE = 2;
                int openHealthPermission = ContextCompat.checkSelfPermission(mActivity,"com.health.openscale.READ_WRITE_DATA");
                if (openHealthPermission != PackageManager.PERMISSION_GRANTED) {

                    // Request access permisson
                    ActivityCompat.requestPermissions((Activity) mActivity,
                            new String[]{"com.health.openscale.READ_WRITE_DATA"},
                            OPENSCALE_REQUEST_CODE);
                    pullToRefresh.setRefreshing(false);
                    return;
                }

                // Import openScale data
                OpenScaleSync openScaleSync = new OpenScaleSync(mActivity.getBaseContext(), getActivity());
                if (openScaleSync.importDatabase()) {
                    // Refresh view after loading data
                    refreshData();
                }
                pullToRefresh.setRefreshing(false);
            });
        } catch (Exception ignored){

        }

        /* Views Initialisation */
        weightEdit = view.findViewById(R.id.weightInput);
        fatEdit = view.findViewById(R.id.fatInput);
        musclesEdit = view.findViewById(R.id.musclesInput);
        waterEdit = view.findViewById(R.id.waterInput);
        sizeEdit = view.findViewById(R.id.sizeInput);
        Button weightDetailsButton = view.findViewById(R.id.weightDetailsButton);
        Button fatDetailsButton = view.findViewById(R.id.fatDetailsButton);
        Button musclesDetailsButton = view.findViewById(R.id.musclesDetailsButton);
        Button waterDetailsButton = view.findViewById(R.id.waterDetailsButton);
        Button sizeDetailsButton = view.findViewById(R.id.sizeDetailsButton);
        imcText = view.findViewById(R.id.imcValue);
        imcRank = view.findViewById(R.id.imcViewText);
        ffmiText = view.findViewById(R.id.ffmiValue);
        ffmiRank = view.findViewById(R.id.ffmiViewText);
        bmrCals = view.findViewById(R.id.bmrValue);
        bmrText = view.findViewById(R.id.bmrViewText);
        dailyCalorieValue = view.findViewById(R.id.dailyCalorieValue);
        rfmText = view.findViewById(R.id.rfmValue);
        rfmRank = view.findViewById(R.id.rfmViewText);
        Spinner spinner = view.findViewById(R.id.activityLevelSpinner);

        ImageButton ffmiHelpButton = view.findViewById(R.id.ffmiHelp);
        ImageButton imcHelpButton = view.findViewById(R.id.imcHelp);
        ImageButton bmrHelpButton = view.findViewById(R.id.bmrHelp);
        ImageButton dailyCalorieHelpButton = view.findViewById(R.id.dailyCalorieHelp);
        ImageButton rfmHelpButton = view.findViewById(R.id.rfmHelp);

        FloatingActionButton addAllButton = view.findViewById(R.id.addAllWeightEntries);

        /* Initialisation des evenements */
        weightEdit.setOnClickListener(mOnClickListener);
        fatEdit.setOnClickListener(mOnClickListener);
        musclesEdit.setOnClickListener(mOnClickListener);
        waterEdit.setOnClickListener(mOnClickListener);
        sizeEdit.setOnClickListener(mOnClickListener);
        addAllButton.setOnClickListener(mOnAddAllEntriesClickListener);
        imcHelpButton.setOnClickListener(showHelp);
        ffmiHelpButton.setOnClickListener(showHelp);
        bmrHelpButton.setOnClickListener(showHelp);
        dailyCalorieHelpButton.setOnClickListener(showHelp);
        rfmHelpButton.setOnClickListener(showHelp);
        weightDetailsButton.setOnClickListener(showDetailsFragment);
        fatDetailsButton.setOnClickListener(showDetailsFragment);
        musclesDetailsButton.setOnClickListener(showDetailsFragment);
        waterDetailsButton.setOnClickListener(showDetailsFragment);
        sizeDetailsButton.setOnClickListener(showDetailsFragment);

        mWeightDb = new DAOProfileWeight(view.getContext());
        mDbBodyPart = new DAOBodyPart(view.getContext());
        mDbBodyMeasure = new DAOBodyMeasure(view.getContext());

        mWeightLineChart = view.findViewById(R.id.weightGraph);
        mWeightGraph = new MiniDateGraph(getContext(), mWeightLineChart, "");
        mWeightGraph.getChart().setOnClickListener(showDetailsFragment);
        weightBodyPart = mDbBodyPart.getBodyPartfromBodyPartKey(BodyPartExtensions.WEIGHT);

        mFatLineChart = view.findViewById(R.id.fatGraph);
        mFatGraph = new MiniDateGraph(getContext(), mFatLineChart, "");
        mFatGraph.getChart().setOnClickListener(showDetailsFragment);
        fatBodyPart = mDbBodyPart.getBodyPartfromBodyPartKey(BodyPartExtensions.FAT);

        mMusclesLineChart = view.findViewById(R.id.musclesGraph);
        mMusclesGraph = new MiniDateGraph(getContext(), mMusclesLineChart, "");
        mMusclesGraph.getChart().setOnClickListener(showDetailsFragment);
        musclesBodyPart = mDbBodyPart.getBodyPartfromBodyPartKey(BodyPartExtensions.MUSCLES);

        mWaterLineChart = view.findViewById(R.id.waterGraph);
        mWaterGraph = new MiniDateGraph(getContext(), mWaterLineChart, "");
        mWaterGraph.getChart().setOnClickListener(showDetailsFragment);
        waterBodyPart = mDbBodyPart.getBodyPartfromBodyPartKey(BodyPartExtensions.WATER);

        mSizeLineChart = view.findViewById(R.id.sizeGraph);
        mSizeGraph = new MiniDateGraph(getContext(), mSizeLineChart, "");
        mSizeGraph.getChart().setOnClickListener(showDetailsFragment);
        sizeBodyPart = mDbBodyPart.getBodyPartfromBodyPartKey(BodyPartExtensions.SIZE);

        appViMo = new ViewModelProvider(requireActivity()).get(AppViMo.class);
        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        appViMo.getProfile().observe(getViewLifecycleOwner(), profile -> {
            // Update the UI, in this case, a TextView.
            refreshData();
        });

        //Implementation for activity level spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireActivity(),
                R.array.activity_level, R.layout.calorie_spinner_selected);
        adapter.setDropDownViewResource(R.layout.calorie_spinner_dropdown);
        spinner.setAdapter(adapter);

        //Setup shared preferences to save spinner selection
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        int spinnerValue = SP.getInt("userSpinner", -1);
        if (spinnerValue != -1) {
            spinner.setSelection(spinnerValue);
        }


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                // On selecting a spinner item
                String item = parent.getItemAtPosition(position).toString();
                if (item.equals(getResources().getString(R.string.sedentary))) {
                    calorieMultiplier = 1.2;
                    refreshData();
                } else if (item.equals(getResources().getString(R.string.moderate))) {
                    calorieMultiplier = 1.375;
                    refreshData();
                } else if (item.equals(getResources().getString(R.string.active))) {
                    calorieMultiplier = 1.465;
                    refreshData();
                } else if (item.equals(getResources().getString(R.string.daily_exercise))) {
                    calorieMultiplier = 1.55;
                    refreshData();
                } else if (item.equals(getResources().getString(R.string.intense))) {
                    calorieMultiplier = 1.725;
                    refreshData();
                } else if (item.equals(getResources().getString(R.string.very_intense))) {
                    calorieMultiplier = 1.9;
                    refreshData();
                } else {
                    calorieMultiplier = 1.2;
                    refreshData();
                }

                //Saving selected activity level to shared preferences
                int userChoice = spinner.getSelectedItemPosition();
                SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
                SharedPreferences.Editor prefEditor = SP.edit();
                prefEditor.putInt("userSpinner", userChoice);
                prefEditor.commit();


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return view;
    }


    private void DrawGraph() {
        if (getView() == null) return;
        getView().post(() -> {
            if (weightBodyPart == null) return;

            List<BodyMeasure> valueList = mDbBodyMeasure.getBodyPartMeasuresListTop4(weightBodyPart.getId(), getProfile());

            // Recupere les enregistrements
            if (valueList.size() < 1) {
                mWeightLineChart.clear();
                return;
            }

            ArrayList<Entry> yVals = new ArrayList<>();

            if (valueList.size() > 0) {
                for (int i = valueList.size() - 1; i >= 0; i--) {
                    Entry value = new Entry(
                            (float) DateConverter.nbDays(valueList.get(i).getDate()),
                            UnitConverter.weightConverter(valueList.get(i).getBodyMeasure().getValue(),
                                    valueList.get(i).getBodyMeasure().getUnit(),
                                    Unit.KG
                            )
                    );
                    yVals.add(value);
                }

                mWeightGraph.draw(yVals);
            }

        });

        getView().post(() -> {
            if (fatBodyPart == null) return;

            List<BodyMeasure> valueList = mDbBodyMeasure.getBodyPartMeasuresListTop4(fatBodyPart.getId(), getProfile());

            // Recupere les enregistrements
            if (valueList.size() < 1) {
                mFatLineChart.clear();
                return;
            }

            ArrayList<Entry> yVals = new ArrayList<>();

            if (valueList.size() > 0) {
                for (int i = valueList.size() - 1; i >= 0; i--) {
                    Entry value = new Entry((float) DateConverter.nbDays(valueList.get(i).getDate()), valueList.get(i).getBodyMeasure().getValue());
                    yVals.add(value);
                }

                mFatGraph.draw(yVals);
            }
        });
        getView().post(() -> {
            if (musclesBodyPart == null) return;
            List<BodyMeasure> valueList = mDbBodyMeasure.getBodyPartMeasuresListTop4(musclesBodyPart.getId(), getProfile());

            // Recupere les enregistrements
            if (valueList.size() < 1) {
                mMusclesLineChart.clear();
                return;
            }

            ArrayList<Entry> yVals = new ArrayList<>();

            if (valueList.size() > 0) {
                for (int i = valueList.size() - 1; i >= 0; i--) {
                    Entry value = new Entry((float) DateConverter.nbDays(valueList.get(i).getDate()), valueList.get(i).getBodyMeasure().getValue());
                    yVals.add(value);
                }

                mMusclesGraph.draw(yVals);
            }
        });

        getView().post(() -> {
            if (waterBodyPart == null) return;
            List<BodyMeasure> valueList = mDbBodyMeasure.getBodyPartMeasuresListTop4(waterBodyPart.getId(), getProfile());

            // Recupere les enregistrements
            if (valueList.size() < 1) {
                mWaterLineChart.clear();
                return;
            }

            ArrayList<Entry> yVals = new ArrayList<>();

            if (valueList.size() > 0) {
                for (int i = valueList.size() - 1; i >= 0; i--) {
                    Entry value = new Entry((float) DateConverter.nbDays(valueList.get(i).getDate()), valueList.get(i).getBodyMeasure().getValue());
                    yVals.add(value);
                }

                mWaterGraph.draw(yVals);
            }
        });

        getView().post(() -> {
            if (sizeBodyPart == null) return;
            List<BodyMeasure> valueList = mDbBodyMeasure.getBodyPartMeasuresListTop4(sizeBodyPart.getId(), getProfile());

            // Recupere les enregistrements
            if (valueList.size() < 1) {
                mSizeLineChart.clear();
                return;
            }

            ArrayList<Entry> yVals = new ArrayList<>();

            if (valueList.size() > 0) {
                for (int i = valueList.size() - 1; i >= 0; i--) {
                    Entry value = new Entry((float) DateConverter.nbDays(valueList.get(i).getDate()), valueList.get(i).getBodyMeasure().getValue());
                    yVals.add(value);
                }

                mSizeGraph.draw(yVals);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        refreshData();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mActivity = (MainActivity) context;
    }

    public String getName() {
        return getArguments().getString("name");
    }

    /**
     * @param weight in kg
     * @param size   in cm
     * @return
     */
    private float calculateImc(float weight, float size) {

        if (size == 0) return 0;

        return (float) (weight / (size / 100.0 * size / 100.0));
    }

    /**
     * @param imc
     * @return text associated with imc value
     */
    private String getImcText(float imc) {
        if (imc < 18.5) {
            return getString(R.string.underweight);
        } else if (imc < 25) {
            return getString(R.string.normal);
        } else if (imc < 30) {
            return getString(R.string.overweight);
        } else {
            return getString(R.string.obese);
        }
    }

    private float calculateRfm(float waistCirc, int sex, int size) {
        float rfm = 0;

        if (waistCirc == 0) return 0;

        return 0;
    }

    /**
     * @param rfm index
     * @return text associated with Rfm value
     */
    private String getRfmText(float rfm) {
        if (rfm < 18.5) {
            return "underweight";
        } else if (rfm < 25) {
            return "normal";
        } else if (rfm < 30) {
            return "overweight";
        } else {
            return "obese";
        }
    }

    /**
     * Fat-Free Mass (FFM): FFM [kg] = weight [kg] × (1 − (body fat [%] / 100))
     * Fat-Free Mass Index (FFMI): FFMI [kg/m2] = FFM [kg] / (height [m])2
     * Normalized Fat-Free Mass Index: Normalized FFMI [kg/m2] = FFM [kg] / (height [m])2 + 6.1 × (1.8 − height [m])
     * https://goodcalculators.com/ffmi-fat-free-mass-index-calculator/
     */
    private double calculateFfmi(float weight, float size, float bodyFat) {

        if (bodyFat == 0) return 0;

        return weight * (1 - bodyFat / 100) / (size / 100.0 * size / 100.0);
    }

    /**
     * Fat-Free Mass (FFM): FFM [kg] = weight [kg] × (1 − (body fat [%] / 100))
     * Fat-Free Mass Index (FFMI): FFMI [kg/m2] = FFM [kg] / (height [m])2
     * Normalized Fat-Free Mass Index: Normalized FFMI [kg/m2] = FFM [kg] / (height [m])2 + 6.1 × (1.8 − height [m])
     * https://goodcalculators.com/ffmi-fat-free-mass-index-calculator/
     */
    private double calculateNormalizedFfmi(float weight, float size, float bodyFat) {

        if (bodyFat == 0) return 0;

        return weight * (1 - bodyFat / 100) / (size * size) + 6.1 * (1.8 - size);
    }

    /**
     * 16 – 17: below average     *
     * 18 – 19: average           *
     * 20 - 21: above average     *
     * 22: excellent              *
     * 23 – 25: superior          *
     * 26 – 27: scores considered suspicious but still attainable naturally
     */
    private String getFfmiTextForMen(double ffmi) {
        if (ffmi < 17) {
            return "below average";
        } else if (ffmi < 19) {
            return "average";
        } else if (ffmi < 21) {
            return "above average";
        } else if (ffmi < 23) {
            return "excellent";
        } else if (ffmi < 25) {
            return "superior";
        } else if (ffmi < 27) {
            return "suspicious";
        } else {
            return "very suspicious";
        }
    }

    /**
     * 16 – 17: below average     *
     * 18 – 19: average     *
     * 20 - 21: above average     *
     * 22: excellent     *
     * 23 – 25: superior     *
     * 26 – 27: scores considered suspicious but still attainable naturally
     */
    private String getFfmiTextForWomen(double ffmi) {
        if (ffmi < 14) {
            return "below average";
        } else if (ffmi < 16) {
            return "average";
        } else if (ffmi < 18) {
            return "above average";
        } else if (ffmi < 20) {
            return "excellent";
        } else if (ffmi < 22) {
            return "superior";
        } else if (ffmi < 24) {
            return "suspicious";
        } else {
            return "very suspicious";
        }
    }

    private double calculateBmrMenMifflin(float weight, float size) {
        /**
         *Mifflin-St Jeor Equation
         * For men: BMR = 10W + 6.25H - 5A + 5
         **/
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(getProfile().getBirthday());
        int birthYear = calendar.get(Calendar.YEAR);

        int age = Calendar.getInstance().get(Calendar.YEAR) - birthYear;

        return (10 * weight) + (6.25 * size) - (5 * age) + 5;
    }

    private double calculateBmrWomenMifflin(float weight, float size) {
        /**
         *Mifflin-St Jeor Equation
         * For women: BMR = 10W + 6.25H - 5A - 161
         **/
         Calendar calendar = new GregorianCalendar();
        calendar.setTime(getProfile().getBirthday());
        int birthYear = calendar.get(Calendar.YEAR);

        int age = Calendar.getInstance().get(Calendar.YEAR) - birthYear;

        return (10 * weight) + (6.25 * size) - (5 * age) - 161;
    }

    private double calculateBmrKatch(float bodyFat, float weight) {
        /**
         * Katch-McArdle Formula: BMR = 370 + 21.6(1 - F)W
         **/

        return Math.round(370 + (21.6 * (1 - (bodyFat / 100)) * weight));
    }

    /**
     * Get a Value object which uses the data from the last measure if available
     *
     * @param lastMeasure Last measure to use data from if available
     * @param defaultUnit Default unit to use in case there is no last measure
     * @param id          ID for the returned Value
     * @param label       Label for the returned Value
     */
    private Value getValueFromLastMeasure(@Nullable BodyMeasure lastMeasure, Unit defaultUnit, @Nullable String id, int label) {
        if (lastMeasure == null) {
            return new Value(0f, defaultUnit, id, label);
        } else {
            Value lastValue = lastMeasure.getBodyMeasure();
            return new Value(lastValue.getValue(), lastValue.getUnit(), id, label);
        }
    }

    private void refreshData() {
        View fragmentView = getView();
        if (fragmentView != null) {
            if (getProfile() != null) {
                BodyMeasure lastWeightValue = mDbBodyMeasure.getLastBodyMeasures(weightBodyPart.getId(), getProfile());
                BodyMeasure lastWaterValue = mDbBodyMeasure.getLastBodyMeasures(waterBodyPart.getId(), getProfile());
                BodyMeasure lastFatValue = mDbBodyMeasure.getLastBodyMeasures(fatBodyPart.getId(), getProfile());
                BodyMeasure lastMusclesValue = mDbBodyMeasure.getLastBodyMeasures(musclesBodyPart.getId(), getProfile());
                BodyMeasure lastSizeValue = mDbBodyMeasure.getLastBodyMeasures(sizeBodyPart.getId(), getProfile());

                if (lastWeightValue != null) {
                    String editText = String.format("%.1f", lastWeightValue.getBodyMeasure().getValue()) + lastWeightValue.getBodyMeasure().getUnit().toString();

                    weightEdit.setText(editText);
                    // update IMC
                    if (lastSizeValue == null || lastSizeValue.getBodyMeasure().getValue() == 0) {
                        imcText.setText("-");
                        imcRank.setText(R.string.no_size_available);
                        ffmiText.setText("-");
                        ffmiRank.setText(R.string.no_size_available);
                    } else {
                        float imcValue = calculateImc(UnitConverter.weightConverter(lastWeightValue.getBodyMeasure().getValue(), lastWeightValue.getBodyMeasure().getUnit(), Unit.KG),
                                UnitConverter.sizeConverter(lastSizeValue.getBodyMeasure().getValue(), lastSizeValue.getBodyMeasure().getUnit(), Unit.CM));
                        imcText.setText(String.format("%.1f", imcValue));
                        imcRank.setText(getImcText(imcValue));

                        if (lastFatValue != null) {
                            double ffmiValue = calculateFfmi(UnitConverter.weightConverter(lastWeightValue.getBodyMeasure().getValue(), lastWeightValue.getBodyMeasure().getUnit(), Unit.KG),
                                    UnitConverter.sizeConverter(lastSizeValue.getBodyMeasure().getValue(), lastSizeValue.getBodyMeasure().getUnit(), Unit.CM),
                                    lastFatValue.getBodyMeasure().getValue());
                            ffmiText.setText(String.format("%.1f", ffmiValue));
                            if (getProfile().getGender() == Gender.FEMALE)
                                ffmiRank.setText(getFfmiTextForWomen(ffmiValue));
                            else if (getProfile().getGender() == Gender.MALE)
                                ffmiRank.setText(getFfmiTextForMen(ffmiValue));
                            else if (getProfile().getGender() == Gender.OTHER)
                                ffmiRank.setText(getFfmiTextForMen(ffmiValue));
                            else
                                ffmiRank.setText(R.string.no_gender_defined);
                        } else {
                            ffmiText.setText("-");
                            ffmiRank.setText(R.string.no_fat_available);
                        }

                    }

                    //update BMR
                    if (lastFatValue != null && 0 <= lastFatValue.getBodyMeasure().getValue() && lastFatValue.getBodyMeasure().getValue() <= 15) {
                        bmrCals.setText((String.format("%.0f", calculateBmrKatch(lastFatValue.getBodyMeasure().getValue(), UnitConverter.weightConverter(lastWeightValue.getBodyMeasure().getValue(), lastWeightValue.getBodyMeasure().getUnit(), Unit.KG)))));
                        bmrText.setText(R.string.bmrCalories);
                        dailyCalorieValue.setText((String.format("%.0f", calculateBmrKatch(lastFatValue.getBodyMeasure().getValue(), UnitConverter.weightConverter(lastWeightValue.getBodyMeasure().getValue(), lastWeightValue.getBodyMeasure().getUnit(), Unit.KG)) * calorieMultiplier)));
                    } else if (lastSizeValue != null) {
                        if (getProfile().getGender() == Gender.MALE) {
                            bmrCals.setText(String.format("%.0f", calculateBmrMenMifflin(UnitConverter.weightConverter(lastWeightValue.getBodyMeasure().getValue(), lastWeightValue.getBodyMeasure().getUnit(), Unit.KG),
                                    UnitConverter.sizeConverter(lastSizeValue.getBodyMeasure().getValue(), lastSizeValue.getBodyMeasure().getUnit(), Unit.CM))));
                            bmrText.setText(R.string.bmrCalories);
                            dailyCalorieValue.setText(String.format("%.0f", calculateBmrMenMifflin(UnitConverter.weightConverter(lastWeightValue.getBodyMeasure().getValue(), lastWeightValue.getBodyMeasure().getUnit(), Unit.KG),
                                    UnitConverter.sizeConverter(lastSizeValue.getBodyMeasure().getValue(), lastSizeValue.getBodyMeasure().getUnit(), Unit.CM)) * calorieMultiplier));
                        } else if (getProfile().getGender() == Gender.FEMALE) {
                            bmrCals.setText(String.format("%.0f", calculateBmrWomenMifflin(UnitConverter.weightConverter(lastWeightValue.getBodyMeasure().getValue(), lastWeightValue.getBodyMeasure().getUnit(), Unit.KG),
                                    UnitConverter.sizeConverter(lastSizeValue.getBodyMeasure().getValue(), lastSizeValue.getBodyMeasure().getUnit(), Unit.CM))));
                            bmrText.setText(R.string.bmrCalories);
                            dailyCalorieValue.setText(String.format("%.0f", calculateBmrWomenMifflin(UnitConverter.weightConverter(lastWeightValue.getBodyMeasure().getValue(), lastWeightValue.getBodyMeasure().getUnit(), Unit.KG),
                                    UnitConverter.sizeConverter(lastSizeValue.getBodyMeasure().getValue(), lastSizeValue.getBodyMeasure().getUnit(), Unit.CM)) * calorieMultiplier));
                        } else {
                            bmrCals.setText(String.format("%.0f", calculateBmrMenMifflin(UnitConverter.weightConverter(lastWeightValue.getBodyMeasure().getValue(), lastWeightValue.getBodyMeasure().getUnit(), Unit.KG),
                                    UnitConverter.sizeConverter(lastSizeValue.getBodyMeasure().getValue(), lastSizeValue.getBodyMeasure().getUnit(), Unit.CM))));
                            bmrText.setText(R.string.bmrCalories);
                            dailyCalorieValue.setText(String.format("%.0f", calculateBmrMenMifflin(UnitConverter.weightConverter(lastWeightValue.getBodyMeasure().getValue(), lastWeightValue.getBodyMeasure().getUnit(), Unit.KG),
                                    UnitConverter.sizeConverter(lastSizeValue.getBodyMeasure().getValue(), lastSizeValue.getBodyMeasure().getUnit(), Unit.CM)) * calorieMultiplier));
                        }
                    } else {
                        bmrText.setText(R.string.no_size_available);
                        dailyCalorieValue.setText("-");
                    }

                } else {
                    weightEdit.setText("-");
                    imcText.setText("-");
                    imcRank.setText(R.string.no_weight_available);
                    ffmiText.setText("-");
                    ffmiRank.setText(R.string.no_weight_available);
                    bmrCals.setText("-");
                    bmrText.setText(R.string.no_weight_available);
                    dailyCalorieValue.setText("-");
                }

                if (lastWaterValue != null) {
                    String editText = String.format("%.1f", lastWaterValue.getBodyMeasure().getValue()) + lastWaterValue.getBodyMeasure().getUnit().toString();
                    waterEdit.setText(editText);
                } else
                    waterEdit.setText("-");

                if (lastFatValue != null) {
                    String editText = String.format("%.1f", lastFatValue.getBodyMeasure().getValue()) + lastFatValue.getBodyMeasure().getUnit().toString();
                    fatEdit.setText(editText);
                } else
                    fatEdit.setText("-");

                if (lastMusclesValue != null) {
                    String editText = String.format("%.1f", lastMusclesValue.getBodyMeasure().getValue()) + lastMusclesValue.getBodyMeasure().getUnit().toString();
                    musclesEdit.setText(editText);
                } else
                    musclesEdit.setText("-");

                if (lastSizeValue != null) {
                    String editText = String.format("%.1f", lastSizeValue.getBodyMeasure().getValue()) + lastSizeValue.getBodyMeasure().getUnit().toString();
                    sizeEdit.setText(editText);
                    mSizeLineChart.setVisibility(View.VISIBLE);
                } else {
                    sizeEdit.setText("-");
                    mSizeLineChart.setVisibility(View.GONE);
                }

                DrawGraph();
            }
        }
    }


    private Profile getProfile() {
        return appViMo.getProfile().getValue();
    }

    public Fragment getFragment() {
        return this;
    }
}
