package com.easyfitness.fonte;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.easyfitness.BtnClickListener;
import com.easyfitness.CountdownDialogbox;
import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.DAO.Machine;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.Weight;
import com.easyfitness.DAO.record.DAOCardio;
import com.easyfitness.DAO.record.DAOFonte;
import com.easyfitness.DAO.record.DAORecord;
import com.easyfitness.DAO.record.DAOStatic;
import com.easyfitness.DAO.record.Record;
import com.easyfitness.DatePickerDialogFragment;
import com.easyfitness.MainActivity;
import com.easyfitness.AppViMo;
import com.easyfitness.R;
import com.easyfitness.SettingsFragment;
import com.easyfitness.TimePickerDialogFragment;
import com.easyfitness.enums.DisplayType;
import com.easyfitness.enums.DistanceUnit;
import com.easyfitness.enums.ExerciseType;
import com.easyfitness.enums.WeightUnit;
import com.easyfitness.machines.ExerciseDetailsPager;
import com.easyfitness.machines.MachineArrayFullAdapter;
import com.easyfitness.machines.MachineCursorAdapter;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.ExpandedListView;
import com.easyfitness.utils.ImageUtil;
import com.easyfitness.utils.Keyboard;
import com.easyfitness.utils.UnitConverter;
import com.easyfitness.views.WorkoutValuesInputView;
import com.ikovac.timepickerwithseconds.MyTimePickerDialog;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class FontesFragment extends Fragment {

    private int lTableColor = 1;
    private DisplayType mDisplayType = DisplayType.FREE_WORKOUT_DISPLAY;
    private long mTemplateId;
    private MainActivity mActivity = null;
    private AutoCompleteTextView machineEdit = null;
    private MachineArrayFullAdapter machineEditAdapter = null;
    private CircularImageView machineImage = null;
    private ImageButton machineListButton = null;
    private boolean[] checkedFilterItems = {true, true, true};
    private ArrayList<ExerciseType> selectedTypes = new ArrayList();
    private ImageButton detailsExpandArrow = null;
    private LinearLayout detailsLayout = null;
    private CardView detailsCardView = null;
    private CheckBox autoTimeCheckBox = null;
    private TextView dateEdit = null;
    private final DatePickerDialog.OnDateSetListener dateSet = (view, year, month, day) -> {
        dateEdit.setText(DateConverter.dateToLocalDateStr(year, month, day, getContext()));
        Keyboard.hide(getContext(), dateEdit);
    };
    private TextView timeEdit = null;
    private final MyTimePickerDialog.OnTimeSetListener timeSet = (view, hourOfDay, minute, second) -> {
        // Do something with the time chosen by the user
        Date date = DateConverter.timeToDate(hourOfDay, minute, second);
        timeEdit.setText(DateConverter.dateToLocalTimeStr(date, getContext()));
        Keyboard.hide(getContext(), timeEdit);
    };
    private final CompoundButton.OnCheckedChangeListener checkedAutoTimeCheckBox = (buttonView, isChecked) -> {
        dateEdit.setEnabled(!isChecked);
        timeEdit.setEnabled(!isChecked);
        if (isChecked) {
            dateEdit.setText(DateConverter.currentDate(getContext()));
            timeEdit.setText(DateConverter.currentTime(getContext()));
        }
    };
    private Button addButton = null;
    private ExpandedListView recordList = null;
    private AlertDialog machineListDialog;
    private AlertDialog machineFilterDialog;
    private DatePickerDialogFragment mDateFrag = null;
    private TimePickerDialogFragment mTimeFrag = null;
    private final OnClickListener clickDateEdit = v -> {
        switch (v.getId()) {
            case R.id.editDate:
                showDatePickerFragment();
                break;
            case R.id.editTime:
                showTimePicker(timeEdit);
                break;
        }
    };

    private WorkoutValuesInputView workoutValuesInputView;
    private final OnClickListener collapseDetailsClick = v -> {
        detailsLayout.setVisibility(detailsLayout.isShown() ? View.GONE : View.VISIBLE);
        detailsExpandArrow.setImageResource(detailsLayout.isShown() ? R.drawable.ic_expand_less : R.drawable.ic_expand_more);
        saveSharedParams();
    };
    private DAOFonte mDbBodyBuilding = null;
    private DAOCardio mDbCardio = null;
    private DAOStatic mDbStatic = null;
    private DAORecord mDbRecord = null;
    private DAOMachine mDbMachine = null;
    private AppViMo appViMo;
    private final BtnClickListener itemClickCopyRecord = v -> {
        Record r = mDbRecord.getRecord((long) v.getTag());
        if (r != null) {
            // Copy values above
            setCurrentMachine(r.getExercise());
            if (r.getExerciseType() == ExerciseType.STRENGTH) {
                workoutValuesInputView.setReps(r.getReps());
                workoutValuesInputView.setSets(r.getSets());

                Float poids = r.getWeight();
                poids = UnitConverter.weightConverter(poids, WeightUnit.KG, r.getWeightUnit());
                workoutValuesInputView.setWeight(poids, r.getWeightUnit());
            } else if (r.getExerciseType() == ExerciseType.ISOMETRIC) {
                workoutValuesInputView.setSeconds(r.getSeconds());
                workoutValuesInputView.setSets(r.getSets());
                Float poids = r.getWeight();
                poids = UnitConverter.weightConverter(poids, WeightUnit.KG, r.getWeightUnit());
                workoutValuesInputView.setWeight(poids, r.getWeightUnit());
            } else if (r.getExerciseType() == ExerciseType.CARDIO) {
                float distance = r.getDistance();
                DistanceUnit distanceUnit = DistanceUnit.KM;
                if (r.getDistanceUnit() == DistanceUnit.MILES) {
                    distance = UnitConverter.KmToMiles(r.getDistance());
                    distanceUnit = DistanceUnit.MILES;
                }
                workoutValuesInputView.setDistance(distance, distanceUnit);
                workoutValuesInputView.setDuration(r.getDuration());
            }
            KToast.infoToast(getMainActivity(), getString(R.string.recordcopied), Gravity.BOTTOM, KToast.LENGTH_SHORT);
        }
    };
    private final OnItemLongClickListener itemlongclickDeleteRecord = (listView, view, position, id) -> {
        showRecordListMenu(id);
        return true;
    };
    private final TextWatcher exerciseTextWatcher = new TextWatcher() {
        public void afterTextChanged(Editable s) {
            String exerciseName = s.toString();
            MachineArrayFullAdapter adapter = (MachineArrayFullAdapter) machineEdit.getAdapter();
            if (adapter != null) {
                boolean exerciseExists = adapter.containsExercise(exerciseName);
                if (!exerciseExists) {
                    workoutValuesInputView.setShowExerciseTypeSelector(true);
                    updateMachineImage();
                } else {
                    setCurrentMachine(exerciseName);
                }
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    };

    private final View.OnClickListener clickFilterButton = v -> {
        if (machineFilterDialog != null && machineFilterDialog.isShowing()) {
            return;
        }

        Cursor c = mDbMachine.getAllMachines();

        if (c == null || c.getCount() == 0) {
            KToast.warningToast(getActivity(), getResources().getText(R.string.createExerciseFirst).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            builder.setTitle(R.string.filterExerciseTypeDialogLabel);
            String[] availableExerciseTypes = {getResources().getText(R.string.strength_category).toString(),
                    getResources().getText(R.string.CardioLabel).toString(),
                    getResources().getText(R.string.staticExercise).toString()};
            builder.setMultiChoiceItems(availableExerciseTypes, checkedFilterItems, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    if (isChecked) {
                        selectedTypes.add(ExerciseType.fromInteger(which));
                    } else {
                        selectedTypes.remove(ExerciseType.fromInteger(which));
                    }
                }
            });

            // Add OK and Cancel buttons
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (selectedTypes.size() == 0) {
                        KToast.warningToast(getActivity(), getResources().getText(R.string.selectExerciseTypeFirst).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                        Arrays.fill(checkedFilterItems, true);
                        selectedTypes.add(ExerciseType.CARDIO);
                        selectedTypes.add(ExerciseType.ISOMETRIC);
                        selectedTypes.add(ExerciseType.STRENGTH);
                    }
                    refreshDialogData();
                }
            });
            builder.setNegativeButton("Cancel", null);
            machineFilterDialog = builder.create();
            machineFilterDialog.show();
        }
    };

    private final OnClickListener clickAddButton = v -> {
        // Verifie que les infos sont completes
        if (machineEdit.getText().toString().isEmpty()) {
            KToast.warningToast(getActivity(), getResources().getText(R.string.missinginfo).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
            return;
        }

        Date date;

        if (autoTimeCheckBox.isChecked()) {
            date = new Date();
        } else {
            date = DateConverter.localDateTimeStrToDateTime(dateEdit.getText().toString(), timeEdit.getText().toString(), getContext());
        }

        ExerciseType exerciseType;
        Machine lMachine = mDbMachine.getMachine(machineEdit.getText().toString());
        if (lMachine == null) {
            exerciseType = workoutValuesInputView.getSelectedType();
        } else {
            exerciseType = lMachine.getType();
        }

        if (exerciseType == ExerciseType.STRENGTH) {
            if (!workoutValuesInputView.isFilled()) {
                KToast.warningToast(getActivity(), getResources().getText(R.string.missinginfo).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                return;
            }

            /* Convertion du poid */
            float tmpPoids = workoutValuesInputView.getWeightValue();
            tmpPoids = UnitConverter.weightConverter(tmpPoids, workoutValuesInputView.getWeightUnit(), WeightUnit.KG); // Always convert to KG

            if (mDisplayType == DisplayType.FREE_WORKOUT_DISPLAY) {
                mDbBodyBuilding.addBodyBuildingRecord(date,
                        machineEdit.getText().toString(),
                        workoutValuesInputView.getSets(),
                        workoutValuesInputView.getReps(),
                        tmpPoids, // Always save in KG
                        workoutValuesInputView.getWeightUnit(), // Store Unit for future display
                        "", //Notes
                        getProfile().getId(), -1);

                float iTotalWeightSession = mDbBodyBuilding.getTotalWeightSession(date, getProfile());
                float iTotalWeight = mDbBodyBuilding.getTotalWeightMachine(date, machineEdit.getText().toString(), getProfile());
                int iNbSeries = mDbBodyBuilding.getNbSeries(date, machineEdit.getText().toString(), getProfile());

                //--Launch Rest Dialog
                boolean bLaunchRest = workoutValuesInputView.isRestTimeActivated();
                int restTime = workoutValuesInputView.getRestTime();

                // Launch Countdown
                if (bLaunchRest && DateConverter.dateToLocalDateStr(date, getContext()).equals(DateConverter.dateToLocalDateStr(new Date(), getContext()))) { // Only launch Countdown if date is today.
                    CountdownDialogbox cdd = new CountdownDialogbox(getActivity(), restTime, lMachine);
                    cdd.setNbSeries(iNbSeries);
                    cdd.setTotalWeightMachine(iTotalWeight);
                    cdd.setTotalWeightSession(iTotalWeightSession);
                    cdd.show();
                }
            } else if (mDisplayType == DisplayType.PROGRAM_EDIT_DISPLAY) {
                for (int i = 0; i < workoutValuesInputView.getSets(); i++) {
                    mDbBodyBuilding.addWeightRecordToProgramTemplate(mTemplateId, -1, date,
                            machineEdit.getText().toString(),
                            1,
                            workoutValuesInputView.getReps(),
                            tmpPoids, // Always save in KG
                            workoutValuesInputView.getWeightUnit(),
                            workoutValuesInputView.getRestTime()
                    );
                }
            }
        } else if (exerciseType == ExerciseType.ISOMETRIC) {
            // Verifie que les infos sont completes
            if (!workoutValuesInputView.isFilled()) {
                KToast.warningToast(getActivity(), getResources().getText(R.string.missinginfo).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                return;
            }

            /* Convertion du poid */
            float tmpPoids = workoutValuesInputView.getWeightValue();
            tmpPoids = UnitConverter.weightConverter(tmpPoids, workoutValuesInputView.getWeightUnit(), WeightUnit.KG); // Always convert to KG

            if (mDisplayType == DisplayType.FREE_WORKOUT_DISPLAY) {
                mDbStatic.addStaticRecord(date,
                        machineEdit.getText().toString(),
                        workoutValuesInputView.getSets(),
                        workoutValuesInputView.getSeconds(),
                        tmpPoids, // Always save in KG
                        getProfile().getId(),
                        workoutValuesInputView.getWeightUnit(), // Store Unit for future display
                        "", //Notes
                        -1
                );

                float iTotalWeightSession = mDbStatic.getTotalWeightSession(date, getProfile());
                float iTotalWeight = mDbStatic.getTotalWeightMachine(date, machineEdit.getText().toString(), getProfile());
                int iNbSeries = mDbStatic.getNbSeries(date, machineEdit.getText().toString(), getProfile());

                //--Launch Rest Dialog
                boolean bLaunchRest = workoutValuesInputView.isRestTimeActivated();
                int restTime = workoutValuesInputView.getRestTime();

                // Launch Countdown
                if (bLaunchRest && DateConverter.dateToLocalDateStr(date, getContext()).equals(DateConverter.dateToLocalDateStr(new Date(), getContext()))) { // Only launch Countdown if date is today.
                    CountdownDialogbox cdd = new CountdownDialogbox(getActivity(), restTime, lMachine);
                    cdd.setNbSeries(iNbSeries);
                    cdd.setTotalWeightMachine(iTotalWeight);
                    cdd.setTotalWeightSession(iTotalWeightSession);
                    cdd.show();
                }
            } else if (mDisplayType == DisplayType.PROGRAM_EDIT_DISPLAY) {
                for (int i = 0; i < workoutValuesInputView.getSets(); i++) {
                    mDbStatic.addStaticRecordToProgramTemplate(mTemplateId, -1, date,
                            machineEdit.getText().toString(),
                            1,
                            workoutValuesInputView.getSeconds(),
                            tmpPoids, // Always save in KG
                            workoutValuesInputView.getWeightUnit(),
                            workoutValuesInputView.getRestTime()
                    );
                }
            }
        } else if (exerciseType == ExerciseType.CARDIO) {
            // Verifie que les infos sont completes
            if (!workoutValuesInputView.isFilled()) {
                KToast.warningToast(getActivity(), getResources().getText(R.string.missinginfo).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                return;
            }

            long duration = workoutValuesInputView.getDurationValue();

            float distance = workoutValuesInputView.getDistanceValue();
            if (workoutValuesInputView.getDistanceUnit() == DistanceUnit.MILES) {
                distance = UnitConverter.MilesToKm(distance); // Always convert to KG
            }

            if (mDisplayType == DisplayType.FREE_WORKOUT_DISPLAY) {
                mDbCardio.addCardioRecord(date,
                        machineEdit.getText().toString(),
                        distance,
                        duration,
                        getProfile().getId(),
                        workoutValuesInputView.getDistanceUnit(), -1);

                //--Launch Rest Dialog
                boolean bLaunchRest = workoutValuesInputView.isRestTimeActivated();
                int restTime = workoutValuesInputView.getRestTime();

                // Launch Countdown
                if (bLaunchRest && DateConverter.dateToLocalDateStr(date, getContext()).equals(DateConverter.dateToLocalDateStr(new Date(), getContext()))) { // Only launch Countdown if date is today.
                    CountdownDialogbox cdd = new CountdownDialogbox(getActivity(), restTime, lMachine);
                    cdd.show();
                }
            } else if (mDisplayType == DisplayType.PROGRAM_EDIT_DISPLAY) {
                mDbCardio.addCardioRecordToProgramTemplate(mTemplateId, -1,
                        date,
                        machineEdit.getText().toString(),
                        distance,
                        workoutValuesInputView.getDistanceUnit(),
                        duration,
                        workoutValuesInputView.getRestTime()
                );
            }
        }

        getActivity().findViewById(R.id.drawer_layout).requestFocus();
        Keyboard.hide(getContext(), v);

        lTableColor = (lTableColor + 1) % 2; // Change la couleur a chaque ajout de donnees

        refreshData();

        //Rajoute le moment du dernier ajout dans le bouton Add
        if (mDisplayType == DisplayType.FREE_WORKOUT_DISPLAY)
            addButton.setText(getView().getContext().getString(R.string.AddLabel) + "\n(" + DateConverter.currentTime(getContext()) + ")");

        mDbCardio.closeCursor();
        mDbBodyBuilding.closeCursor();
        mDbStatic.closeCursor();
        mDbRecord.closeCursor();

        saveSharedParams();
    };
    private final OnClickListener onClickMachineListWithIcons = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Cursor oldCursor;

            // In case the dialog is already open
            if (machineListDialog != null && machineListDialog.isShowing()) {
                return;
            }

            ListView machineList = new ListView(v.getContext());

            // Version avec table Machine
            Cursor c = mDbMachine.getAllMachines(selectedTypes);

            if (c == null || c.getCount() == 0) {
                if (selectedTypes.size() == 0) {
                    KToast.warningToast(getActivity(), getResources().getText(R.string.selectExerciseTypeFirst).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                } else {
                    //Toast.makeText(getActivity(), R.string.createExerciseFirst, Toast.LENGTH_SHORT).show();
                    KToast.warningToast(getActivity(), getResources().getText(R.string.createExerciseFirst).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                }
                machineList.setAdapter(null);
            } else {
                if (machineList.getAdapter() == null) {
                    MachineCursorAdapter mTableAdapter = new MachineCursorAdapter(getActivity(), c, 0, mDbMachine);
                    //MachineArrayFullAdapter lAdapter = new MachineArrayFullAdapter(v.getContext(),records);
                    machineList.setAdapter(mTableAdapter);
                } else {
                    MachineCursorAdapter mTableAdapter = (MachineCursorAdapter) machineList.getAdapter();
                    oldCursor = mTableAdapter.swapCursor(c);
                    if (oldCursor != null) oldCursor.close();
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                View customLayout = getLayoutInflater().inflate(R.layout.tab_machine, null);
                Button addButton = customLayout.findViewById(R.id.addExercise);
                addButton.setVisibility(View.GONE);

                AutoCompleteTextView textFilter = customLayout.findViewById(R.id.searchField);
                textFilter.setVisibility(View.GONE);

                TextView textViewFilterExplanation = customLayout.findViewById(R.id.textViewFilterByTypes);
                textViewFilterExplanation.setVisibility(View.VISIBLE);

                ImageButton filterButton = customLayout.findViewById(R.id.buttonFilterListMachine);
                filterButton.setOnClickListener(clickFilterButton);
                ListView listView = customLayout.findViewById(R.id.listMachine);
                listView.setAdapter(machineList.getAdapter());
                listView.setOnItemClickListener((parent, view, position, id) -> {
                    TextView textView = view.findViewById(R.id.LIST_MACHINE_ID);
                    long machineID = Long.parseLong(textView.getText().toString());
                    DAOMachine lMachineDb = new DAOMachine(getContext());
                    Machine lMachine = lMachineDb.getMachine(machineID);

                    setCurrentMachine(lMachine.getName());

                    getMainActivity().findViewById(R.id.drawer_layout).requestFocus();
                    Keyboard.hide(getContext(), getMainActivity().findViewById(R.id.drawer_layout));

                    if (machineListDialog.isShowing()) {
                        machineListDialog.dismiss();
                    }
                });
                builder.setTitle(R.string.selectMachineDialogLabel);
                builder.setView(customLayout);
                machineListDialog = builder.create();
                machineListDialog.show();
            }
        }
    };
    private final OnItemClickListener onItemClickFilterList = (parent, view, position, id) -> setCurrentMachine(machineEdit.getText().toString());
    private final OnFocusChangeListener touchRazEdit = (v, hasFocus) -> {
        if (hasFocus) {
            updateMachineImage();

            workoutValuesInputView.setWeightComment("");
            workoutValuesInputView.setShowExerciseTypeSelector(true);

            v.post(() -> {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
            });
        } else {
            // If a creation of a new machine is not ongoing.
            if (!workoutValuesInputView.isShowExerciseTypeSelector())
                setCurrentMachine(machineEdit.getText().toString());
        }
        updateMachineImage();
    };

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static FontesFragment newInstance(int displayType, long templateId) {
        FontesFragment f = new FontesFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putLong("templateId", templateId);
        args.putInt("displayType", displayType);
        f.setArguments(args);

        return f;
    }

    private void updateMachineImage() {
        switch (workoutValuesInputView.getSelectedType()) {
            case CARDIO:
                machineImage.setImageResource(R.drawable.ic_training_50dp);
                break;
            case ISOMETRIC:
                machineImage.setImageResource(R.drawable.ic_static_50dp);
                break;
            case STRENGTH:
            default:
                machineImage.setImageResource(R.drawable.ic_gym_bench_50dp);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        selectedTypes.add(ExerciseType.CARDIO);
        selectedTypes.add(ExerciseType.ISOMETRIC);
        selectedTypes.add(ExerciseType.STRENGTH);

        View view = inflater.inflate(R.layout.tab_fontes, container, false);

        mTemplateId = getArguments().getLong("templateId", -1);
        mDisplayType = DisplayType.fromInteger(getArguments().getInt("displayType", DisplayType.FREE_WORKOUT_DISPLAY.ordinal()));

        machineEdit = view.findViewById(R.id.editMachine);

        workoutValuesInputView = view.findViewById(R.id.WorkoutValuesInput);

        recordList = view.findViewById(R.id.listRecord);
        machineListButton = view.findViewById(R.id.buttonListMachine);
        addButton = view.findViewById(R.id.addperff);

        detailsCardView = view.findViewById(R.id.detailsCardView);
        detailsLayout = view.findViewById(R.id.notesLayout);
        detailsExpandArrow = view.findViewById(R.id.buttonExpandArrow);
        machineImage = view.findViewById(R.id.imageMachine);

        autoTimeCheckBox = view.findViewById(R.id.autoTimeCheckBox);
        dateEdit = view.findViewById(R.id.editDate);
        timeEdit = view.findViewById(R.id.editTime);

        /* Initialisation des boutons */
        addButton.setOnClickListener(clickAddButton);
        machineListButton.setOnClickListener(onClickMachineListWithIcons); //onClickMachineList

        dateEdit.setOnClickListener(clickDateEdit);
        timeEdit.setOnClickListener(clickDateEdit);
        autoTimeCheckBox.setOnCheckedChangeListener(checkedAutoTimeCheckBox);

        //machineEdit.setOnKeyListener(checkExerciseExists);
        machineEdit.addTextChangedListener(exerciseTextWatcher);
        //machineEdit.setOnFocusChangeListener(touchRazEdit);
        machineEdit.setOnItemClickListener(onItemClickFilterList);
        recordList.setOnItemLongClickListener(itemlongclickDeleteRecord);
        detailsExpandArrow.setOnClickListener(collapseDetailsClick);

        restoreSharedParams();

        WeightUnit weightUnit = SettingsFragment.getDefaultWeightUnit(getActivity());
        workoutValuesInputView.setWeightUnit(weightUnit);

        DistanceUnit distanceUnit = SettingsFragment.getDefaultDistanceUnit(getActivity());
        workoutValuesInputView.setDurationUnit(distanceUnit);

        // Initialisation de la base de donnee
        mDbBodyBuilding = new DAOFonte(getContext());
        mDbCardio = new DAOCardio(getContext());
        mDbStatic = new DAOStatic(getContext());
        mDbRecord = new DAORecord(getContext());
        mDbMachine = new DAOMachine(getContext());

        machineImage.setOnClickListener(v -> {
            Machine m = mDbMachine.getMachine(machineEdit.getText().toString());
            if (m != null) {
                ExerciseDetailsPager machineDetailsFragment = ExerciseDetailsPager.newInstance(m.getId(), ((MainActivity) getActivity()).getCurrentProfile().getId());
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack so the user can navigate back
                transaction.replace(R.id.fragment_container, machineDetailsFragment, MainActivity.MACHINESDETAILS);
                transaction.addToBackStack(null);
                // Commit the transaction
                transaction.commit();
            }
        });

        appViMo = new ViewModelProvider(requireActivity()).get(AppViMo.class);
        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        appViMo.getProfile().observe(getViewLifecycleOwner(), profile -> {
            // Update the UI, in this case, a TextView.
            refreshData();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        this.mActivity = (MainActivity) this.getActivity();
        dateEdit.setText(DateConverter.currentDate(getContext()));
        timeEdit.setText(DateConverter.currentTime(getContext()));
        if (mDisplayType == DisplayType.PROGRAM_EDIT_DISPLAY) {
            addButton.setText(R.string.add_to_template);
            detailsCardView.setVisibility(View.GONE);
        }
        refreshData();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle outState) {
        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }

    public String getName() {
        return getArguments().getString("name");
    }

    public MainActivity getMainActivity() {
        return (MainActivity) this.getActivity();
    }

    private void showRecordListMenu(final long id) {
        // Get the cursor, positioned to the corresponding row in the result set
        //Cursor cursor = (Cursor) listView.getItemAtPosition(position);

        String[] profilListArray = new String[3];
        profilListArray[0] = getActivity().getResources().getString(R.string.DeleteLabel);
        profilListArray[1] = getActivity().getResources().getString(R.string.EditLabel);
        profilListArray[2] = getActivity().getResources().getString(R.string.ShareLabel);

        AlertDialog.Builder itemActionbuilder = new AlertDialog.Builder(getView().getContext());
        itemActionbuilder.setTitle("").setItems(profilListArray, (dialog, which) -> {

            switch (which) {
                // Delete
                case 0:
                    showDeleteDialog(id);
                    break;
                // Edit
                case 1:
                    Toast.makeText(getActivity(), R.string.edit_soon_available, Toast.LENGTH_SHORT).show();
                    break;
                // Share
                case 2:
                    //Toast.makeText(getActivity(), "Share soon available", Toast.LENGTH_SHORT).show();
                    Record r = mDbRecord.getRecord(id);
                    String text = "";
                    if (r.getExerciseType() == ExerciseType.STRENGTH || r.getExerciseType() == ExerciseType.ISOMETRIC) {
                        // Build text
                        text = getView().getContext().getResources().getText(R.string.ShareTextDefault).toString();
                        text = text.replace(getView().getContext().getResources().getText(R.string.ShareParamWeight), String.valueOf(r.getWeight()));
                    } else {
                        // Build text
                        text = "I have done __METER__ in __TIME__ on __MACHINE__.";
                        text = text.replace("__METER__", String.valueOf(r.getDistance()));
                        text = text.replace("__TIME__", String.valueOf(r.getDuration()));
                    }
                    text = text.replace(getView().getContext().getResources().getText(R.string.ShareParamMachine), r.getExercise());
                    shareRecord(text);
                    break;
                default:
            }
        });
        itemActionbuilder.show();
    }

    private void showDeleteDialog(final long idToDelete) {

        new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.DeleteRecordDialog))
                .setContentText(getResources().getText(R.string.areyousure).toString())
                .setCancelText(getResources().getText(R.string.global_no).toString())
                .setConfirmText(getResources().getText(R.string.global_yes).toString())
                .showCancelButton(true)
                .setConfirmClickListener(sDialog -> {
                    mDbRecord.deleteRecord(idToDelete);

                    updateRecordTable(machineEdit.getText().toString());

                    // Info
                    KToast.infoToast(getActivity(), getResources().getText(R.string.removedid).toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                    sDialog.dismissWithAnimation();
                })
                .show();
    }

    private void showDatePickerFragment() {
        if (mDateFrag == null) {
            mDateFrag = DatePickerDialogFragment.newInstance(dateSet);
            mDateFrag.show(getActivity().getSupportFragmentManager().beginTransaction(), "dialog");
        } else {
            if (!mDateFrag.isVisible())
                mDateFrag.show(getActivity().getSupportFragmentManager().beginTransaction(), "dialog");
        }
    }

    private void showTimePicker(TextView timeTextView) {
        Calendar calendar = Calendar.getInstance();
        Date time = DateConverter.localTimeStrToDate(timeTextView.getText().toString(), getContext());
        calendar.setTime(time);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int sec = calendar.get(Calendar.SECOND);

        if (timeTextView.getId() == R.id.editTime) {
            if (mTimeFrag == null) {
                mTimeFrag = TimePickerDialogFragment.newInstance(timeSet, hour, min, sec);
                mTimeFrag.show(getActivity().getSupportFragmentManager().beginTransaction(), "dialog_time");
            } else {
                if (!mTimeFrag.isVisible()) {
                    Bundle bundle = new Bundle();
                    bundle.putInt("HOUR", hour);
                    bundle.putInt("MINUTE", min);
                    bundle.putInt("SECOND", sec);
                    mTimeFrag.setArguments(bundle);
                    mTimeFrag.show(getActivity().getSupportFragmentManager().beginTransaction(), "dialog_time");
                }
            }
        }
    }

    // Share your performances with friends
    public boolean shareRecord(String text) {
        AlertDialog.Builder newProfilBuilder = new AlertDialog.Builder(getView().getContext());

        newProfilBuilder.setTitle(getView().getContext().getResources().getText(R.string.ShareTitle));
        newProfilBuilder.setMessage(getView().getContext().getResources().getText(R.string.ShareInstruction));

        // Set an EditText view to get user input
        final EditText input = new EditText(getView().getContext());
        input.setText(text);
        newProfilBuilder.setView(input);

        newProfilBuilder.setPositiveButton(getView().getContext().getResources().getText(R.string.ShareText), (dialog, whichButton) -> {
            String value = input.getText().toString();

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, value);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        });

        newProfilBuilder.setNegativeButton(getView().getContext().getResources().getText(android.R.string.cancel), (dialog, whichButton) -> {

        });

        newProfilBuilder.show();

        return true;
    }

    public FontesFragment getFragment() {
        return this;
    }

    private Profile getProfile() {
        return appViMo.getProfile().getValue();
    }

    public String getMachine() {
        return machineEdit.getText().toString();
    }

    private void setCurrentMachine(String machineStr) {
        if (machineStr.isEmpty()) {
            updateMachineImage();
            machineImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            // Default image
            workoutValuesInputView.setShowExerciseTypeSelector(true);
            workoutValuesInputView.setWeightComment("");
            return;
        }

        Machine lMachine = mDbMachine.getMachine(machineStr);
        if (lMachine == null) {
            machineEdit.setText("");
            machineImage.setImageResource(R.drawable.ic_gym_bench_50dp); // Default image
            changeExerciseTypeUI(ExerciseType.STRENGTH, true);
            updateMinMax(null);
            return;
        }

        // Update EditView
        if (!machineEdit.getText().toString().equals(lMachine.getName()))
            machineEdit.setText(lMachine.getName());

        // Update exercise Image
        // Default image
        if (!ImageUtil.setPic(machineImage, ImageUtil.getThumbPath(lMachine.getPicture()))) // Overwrite image is there is one
        {
            switch (lMachine.getType()) {
                case CARDIO:
                    machineImage.setImageResource(R.drawable.ic_training_50dp);
                    break;
                case ISOMETRIC:
                    machineImage.setImageResource(R.drawable.ic_static_50dp);
                    break;
                default:
                    machineImage.setImageResource(R.drawable.ic_gym_bench_50dp);
            }
        }

        // Update Table
        updateRecordTable(lMachine.getName());
        // Update display type
        changeExerciseTypeUI(lMachine.getType(), false);
        // Update Min Max
        updateMinMax(lMachine);
        // Update last values
        updateLastRecord(lMachine);
    }

    private void updateMinMax(Machine m) {
        String comment = "";
        String unitStr = "";
        float weight = 0;
        if (getProfile() != null && m != null) {
            if (m.getType() == ExerciseType.STRENGTH || m.getType() == ExerciseType.ISOMETRIC) {
                DecimalFormat numberFormat = new DecimalFormat("#.##");
                Weight minValue = mDbBodyBuilding.getMin(getProfile(), m);
                if (minValue != null && minValue.getStoredWeight() != 0) {
                    weight = UnitConverter.weightConverter(minValue.getStoredWeight(), WeightUnit.KG, minValue.getStoredUnit());
                    unitStr = minValue.getStoredUnit().toString();

                    comment = getContext().getString(R.string.min) + ":" + numberFormat.format(weight) + unitStr + " - ";
                }

                Weight maxValue = mDbBodyBuilding.getMax(getProfile(), m);
                if (maxValue != null && maxValue.getStoredWeight() != 0) {
                    weight = UnitConverter.weightConverter(maxValue.getStoredWeight(), WeightUnit.KG, maxValue.getStoredUnit());
                    unitStr = maxValue.getStoredUnit().toString();
                    comment = comment + getContext().getString(R.string.max) + ":" + numberFormat.format(weight) + unitStr;
                } else {
                    comment = "";
                }
            } else if (m.getType() == ExerciseType.CARDIO) {
                comment = "";
            }
        } else {
            comment = "";
        }

        workoutValuesInputView.setWeightComment(comment);
    }

    private void updateLastRecord(Machine m) {
        Record lLastRecord = mDbRecord.getLastExerciseRecord(m.getId(), getProfile());

        // Getting the prefered default units.
        WeightUnit weightUnit = SettingsFragment.getDefaultWeightUnit(getActivity());
        DistanceUnit distanceUnit = SettingsFragment.getDefaultDistanceUnit(getActivity());

        // Default Values
        workoutValuesInputView.setSets(1);
        workoutValuesInputView.setReps(10);
        workoutValuesInputView.setSeconds(60);
        workoutValuesInputView.setWeight(50, weightUnit);
        workoutValuesInputView.setDistance(10, distanceUnit);
        workoutValuesInputView.setDuration(600000);
        if (lLastRecord == null) {
            // Set default values or nothing.
        } else if (lLastRecord.getExerciseType() == ExerciseType.STRENGTH) {
            workoutValuesInputView.setSets(lLastRecord.getSets());
            workoutValuesInputView.setReps(lLastRecord.getReps());
            workoutValuesInputView.setWeight(UnitConverter.weightConverter(lLastRecord.getWeight(), WeightUnit.KG, lLastRecord.getWeightUnit()), lLastRecord.getWeightUnit());
        } else if (lLastRecord.getExerciseType() == ExerciseType.CARDIO) {
            workoutValuesInputView.setDuration(lLastRecord.getDuration());
            if (lLastRecord.getDistanceUnit() == DistanceUnit.MILES)
                workoutValuesInputView.setDistance(UnitConverter.KmToMiles(lLastRecord.getDistance()), DistanceUnit.MILES);
            else
                workoutValuesInputView.setDistance(lLastRecord.getDistance(), DistanceUnit.KM);
        } else if (lLastRecord.getExerciseType() == ExerciseType.ISOMETRIC) {
            workoutValuesInputView.setSets(lLastRecord.getSets());
            workoutValuesInputView.setSeconds(lLastRecord.getSeconds());
            workoutValuesInputView.setWeight(UnitConverter.weightConverter(lLastRecord.getWeight(), WeightUnit.KG, lLastRecord.getWeightUnit()), lLastRecord.getWeightUnit());
        }
    }

    private void updateRecordTable(String pMachine) {
        // Informe l'activité de la machine courante
        this.getMainActivity().setCurrentMachine(pMachine);
        if (getView() == null) return;
        getView().post(() -> {

            Cursor c = null;
            if (mDisplayType == DisplayType.FREE_WORKOUT_DISPLAY) {
                c = mDbRecord.getTop3DatesRecords(getProfile());
            } else if (mDisplayType == DisplayType.PROGRAM_EDIT_DISPLAY) {
                c = mDbRecord.getProgramTemplateRecords(mTemplateId);
            }

            List<Record> records = mDbRecord.fromCursorToList(c);

            if (records.isEmpty()) {
                recordList.setAdapter(null);
            } else {
                //if (mDisplayType==DisplayType.FREE_WORKOUT_DISPLAY) {
                if (recordList.getAdapter() == null) {
                    RecordArrayAdapter mTableAdapter = new RecordArrayAdapter(getActivity(), getContext(), records, mDisplayType, itemClickCopyRecord);
                    //RecordArrayAdapter mTableAdapter = new RecordArrayAdapter(getActivity(), getContext(), records, DisplayType.PROGRAM_EDIT_DISPLAY, itemClickCopyRecord);
                    recordList.setAdapter(mTableAdapter);
                } else {
                    ((RecordArrayAdapter) recordList.getAdapter()).setRecords(records);
                }
                //}
            }
        });
    }

    private void refreshDialogData() {
        Cursor oldCursor;

        ListView machineList = new ListView(getContext());

        // Version avec table Machine
        Cursor c = mDbMachine.getAllMachines(selectedTypes);

        if (c == null || c.getCount() == 0) {
            if (selectedTypes.size() == 0) {
                KToast.warningToast(getActivity(), getResources().getText(R.string.selectExerciseTypeFirst).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
            } else {
                //Toast.makeText(getActivity(), R.string.createExerciseFirst, Toast.LENGTH_SHORT).show();
                KToast.warningToast(getActivity(), getResources().getText(R.string.createExerciseFirst).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
            }
            machineList.setAdapter(null);
        } else {
            if (machineList.getAdapter() == null) {
                MachineCursorAdapter mTableAdapter = new MachineCursorAdapter(getActivity(), c, 0, mDbMachine);
                //MachineArrayFullAdapter lAdapter = new MachineArrayFullAdapter(v.getContext(),records);
                machineList.setAdapter(mTableAdapter);
            } else {
                MachineCursorAdapter mTableAdapter = (MachineCursorAdapter) machineList.getAdapter();
                oldCursor = mTableAdapter.swapCursor(c);
                if (oldCursor != null) oldCursor.close();
            }

            ListView listView = machineListDialog.findViewById(R.id.listMachine);
            listView.setAdapter(machineList.getAdapter());
        }
    }

    private void refreshData() {
        View fragmentView = getView();
        if (fragmentView != null) {
            if (getProfile() != null) {
                mDbRecord.setProfile(getProfile());

                // Version avec table Machine
                ArrayList<Machine> machineListArray = mDbMachine.getAllMachinesArray(selectedTypes);

                /* Init machines list*/
                machineEditAdapter = new MachineArrayFullAdapter(getContext(), machineListArray);
                machineEdit.setAdapter(machineEditAdapter);

                // If profile has changed
                Profile profile = getProfile();

                if (machineEdit.getText().toString().isEmpty()) {
                    Record lLastRecord = mDbRecord.getLastRecord(getProfile());
                    if (lLastRecord != null) {
                        // Last recorded exercise
                        setCurrentMachine(lLastRecord.getExercise());
                    } else {
                        // Getting the prefered default units.
                        WeightUnit weightUnit = SettingsFragment.getDefaultWeightUnit(getActivity());
                        DistanceUnit distanceUnit = SettingsFragment.getDefaultDistanceUnit(getActivity());

                        // Default Values
                        machineEdit.setText("");
                        // Default Values
                        workoutValuesInputView.setSets(1);
                        workoutValuesInputView.setReps(10);
                        workoutValuesInputView.setSeconds(60);
                        workoutValuesInputView.setWeight(50, weightUnit);
                        workoutValuesInputView.setDistance(10, distanceUnit);
                        workoutValuesInputView.setDuration(600000);
                        setCurrentMachine("");
                        changeExerciseTypeUI(ExerciseType.STRENGTH, true);
                    }
                } else { // Restore on fragment restore.
                    setCurrentMachine(machineEdit.getText().toString());
                }

                // Set Initial text
                if (autoTimeCheckBox.isChecked()) {
                    dateEdit.setText(DateConverter.currentDate(getContext()));
                    timeEdit.setText(DateConverter.currentTime(getContext()));
                }

                // Set Table
                updateRecordTable(machineEdit.getText().toString());
            }
        }
    }

    private void changeExerciseTypeUI(ExerciseType pType, boolean displaySelector) {
        workoutValuesInputView.setShowExerciseTypeSelector(displaySelector);
        switch (pType) {
            case CARDIO:
                workoutValuesInputView.setSelectedType(ExerciseType.CARDIO);
                break;
            case ISOMETRIC:
                workoutValuesInputView.setSelectedType(ExerciseType.ISOMETRIC);
                break;
            case STRENGTH:
            default:
                workoutValuesInputView.setSelectedType(ExerciseType.STRENGTH);
        }
    }

    public void saveSharedParams() {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("restTime2", workoutValuesInputView.getRestTime());
        editor.putBoolean("restCheck", workoutValuesInputView.isRestTimeActivated());
        editor.putBoolean("showDetails", this.detailsLayout.isShown());
        editor.apply();
    }

    public void restoreSharedParams() {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        workoutValuesInputView.setRestTime(sharedPref.getInt("restTime2", 60));
        workoutValuesInputView.activatedRestTime(sharedPref.getBoolean("restCheck", true));

        if (sharedPref.getBoolean("showDetails", false)) {
            detailsLayout.setVisibility(View.VISIBLE);
        } else {
            detailsLayout.setVisibility(View.GONE);
        }
        detailsExpandArrow.setImageResource(sharedPref.getBoolean("showDetails", false) ? R.drawable.ic_expand_less : R.drawable.ic_expand_more);
    }

    /*@Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden)
            refreshData();
    }*/
}
