package com.easyfitness.fonte;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
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

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.easyfitness.BtnClickListener;
import com.easyfitness.CountdownDialogbox;
import com.easyfitness.DAO.Cardio;
import com.easyfitness.DAO.DAOCardio;
import com.easyfitness.DAO.DAOFonte;
import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.DAO.DAORecord;
import com.easyfitness.DAO.DAOStatic;
import com.easyfitness.DAO.Fonte;
import com.easyfitness.DAO.IRecord;
import com.easyfitness.DAO.Machine;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.StaticExercise;
import com.easyfitness.DAO.Weight;
import com.easyfitness.DatePickerDialogFragment;
import com.easyfitness.MainActivity;
import com.easyfitness.R;
import com.easyfitness.SettingsFragment;
import com.easyfitness.TimePickerDialogFragment;
import com.easyfitness.machines.ExerciseDetailsPager;
import com.easyfitness.machines.MachineArrayFullAdapter;
import com.easyfitness.machines.MachineCursorAdapter;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.DistanceUnit;
import com.easyfitness.utils.ExerciseType;
import com.easyfitness.utils.ExpandedListView;
import com.easyfitness.utils.ImageUtil;
import com.easyfitness.utils.UnitConverter;
import com.easyfitness.utils.WeightUnit;
import com.easyfitness.views.WorkoutValuesInputView;
import com.ikovac.timepickerwithseconds.MyTimePickerDialog;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class FontesFragment extends Fragment {
    MainActivity mActivity = null;
    Profile mProfile = null;

    AutoCompleteTextView machineEdit = null;
    MachineArrayFullAdapter machineEditAdapter = null;
    CircularImageView machineImage = null;
    ImageButton machineListButton = null;
    ImageButton detailsExpandArrow = null;
    LinearLayout detailsLayout = null;

    LinearLayout restTimeLayout = null;
    EditText restTimeEdit = null;
    CheckBox restTimeCheck = null;

    CheckBox autoTimeCheckBox = null;
    TextView dateEdit = null;
    TextView timeEdit = null;

    Button addButton = null;
    ExpandedListView recordList = null;

    AlertDialog machineListDialog;
    DatePickerDialogFragment mDateFrag = null;
    TimePickerDialogFragment mDurationFrag = null;
    TimePickerDialogFragment mTimeFrag = null;

    int lTableColor = 1;

    // Cardio Part

    private WorkoutValuesInputView workoutValuesInputView;


    public MyTimePickerDialog.OnTimeSetListener timeSet = (view, hourOfDay, minute, second) -> {
        // Do something with the time chosen by the user
        String strMinute = "00";
        String strHour = "00";
        String strSecond = "00";

        if (minute < 10) strMinute = "0" + Integer.toString(minute);
        else strMinute = Integer.toString(minute);
        if (hourOfDay < 10) strHour = "0" + Integer.toString(hourOfDay);
        else strHour = Integer.toString(hourOfDay);
        if (second < 10) strSecond = "0" + Integer.toString(second);
        else strSecond = Integer.toString(second);

        View viewT = view.getRootView();

        String date = strHour + ":" + strMinute + ":" + strSecond;
        timeEdit.setText(date);
        hideKeyboard(timeEdit);
    };

    private DAOFonte mDbBodyBuilding = null;
    private DAOCardio mDbCardio = null;
    private DAOStatic mDbStatic = null;
    private DAORecord mDb = null;
    private DAOMachine mDbMachine = null;
    private OnClickListener collapseDetailsClick = v -> {
        detailsLayout.setVisibility(detailsLayout.isShown() ? View.GONE : View.VISIBLE);
        detailsExpandArrow.setImageResource(detailsLayout.isShown() ? R.drawable.ic_expand_less_black_24dp : R.drawable.ic_expand_more_black_24dp);
        saveSharedParams();
    };
    private OnClickListener clickExerciseTypeSelector = v -> {
        switch (v.getId()) {
            case R.id.IsometricSelector:
                changeExerciseTypeUI(DAOMachine.TYPE_STATIC, true);
                break;
            case R.id.CardioSelector:
                changeExerciseTypeUI(DAOMachine.TYPE_CARDIO, true);
                break;
            case R.id.StrenghSelector:
            default:
                changeExerciseTypeUI(DAOMachine.TYPE_FONTE, true);
                break;
        }
    };
    private View.OnKeyListener checkExerciseExists = (v, keyCode, event) -> {
        Machine lMach = mDbMachine.getMachine(machineEdit.getText().toString());
        if (lMach == null) {
            workoutValuesInputView.setShowExerciseTypeSelector(true);
        } else {
            changeExerciseTypeUI(lMach.getType(), false);
        }
        return false;
    };
    private OnFocusChangeListener restTimeEditChange = (v, hasFocus) -> {
        if (!hasFocus) {
            saveSharedParams();
        }
    };
    private CompoundButton.OnCheckedChangeListener restTimeCheckChange = (buttonView, isChecked) -> saveSharedParams();
    private BtnClickListener itemClickDeleteRecord = this::showDeleteDialog;
    private BtnClickListener itemClickCopyRecord = id -> {
        IRecord r = mDb.getRecord(id);
        if (r != null) {
            // Copy values above
            setCurrentMachine(r.getExercise());
            if (r.getType() == DAOMachine.TYPE_FONTE) {
                Fonte f = (Fonte) r;
                workoutValuesInputView.setReps(f.getRepetition());
                workoutValuesInputView.setSets(f.getSerie());

                Float poids = f.getPoids();
                WeightUnit weightUnit = WeightUnit.KG;
                if (f.getUnit() == UnitConverter.UNIT_LBS) {
                    poids = UnitConverter.KgtoLbs(poids);
                    weightUnit = WeightUnit.LBS;
                }
                workoutValuesInputView.setWeight(poids, weightUnit);
            } else if (r.getType() == DAOMachine.TYPE_STATIC) {
                StaticExercise f = (StaticExercise) r;
                workoutValuesInputView.setSeconds(f.getSecond());
                workoutValuesInputView.setSets(f.getSerie());
                Float poids = f.getPoids();
                WeightUnit weightUnit = WeightUnit.KG;
                if (f.getUnit() == UnitConverter.UNIT_LBS) {
                    poids = UnitConverter.KgtoLbs(poids);
                    weightUnit = WeightUnit.LBS;
                }
                workoutValuesInputView.setWeight(poids, weightUnit);
            }else if (r.getType() == DAOMachine.TYPE_CARDIO) {
                Cardio c = (Cardio) r;
                float distance = c.getDistance();
                DistanceUnit distanceUnit = DistanceUnit.KM;
                if (c.getDistanceUnit() == UnitConverter.UNIT_MILES) {
                    distance = UnitConverter.KmToMiles((c.getDistance()));
                    distanceUnit = DistanceUnit.MILES;
                }
                workoutValuesInputView.setDistance(distance, distanceUnit);
                workoutValuesInputView.setDuration(c.getDuration());
            }
            KToast.infoToast(getMainActivity(), getString(R.string.recordcopied), Gravity.BOTTOM, KToast.LENGTH_SHORT);
        }
    };
    private OnClickListener clickAddButton = v -> {
        // Verifie que les infos sont completes
        if (machineEdit.getText().toString().isEmpty()) {
            KToast.warningToast(getActivity(), getResources().getText(R.string.missinginfo).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
            return;
        }

        String timeStr;
        Date date;

        if (autoTimeCheckBox.isChecked()) {
            date = new Date();
            timeStr = DateConverter.currentTime();
        }else {
            date = DateConverter.editToDate(dateEdit.getText().toString());
            timeStr = timeEdit.getText().toString();
        }

        ExerciseType exerciseType;
        Machine lMachine = mDbMachine.getMachine(machineEdit.getText().toString());
        if (lMachine == null) {
            exerciseType = workoutValuesInputView.getSelectedType();
        } else {
            exerciseType = ExerciseType.fromInteger(lMachine.getType());
        }

        if (exerciseType == ExerciseType.STRENGTH) {
            if (!workoutValuesInputView.isFilled()) {
                KToast.warningToast(getActivity(), getResources().getText(R.string.missinginfo).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                return;
            }

            /* Convertion du poid */
            float tmpPoids = workoutValuesInputView.getWeightValue();
            int unitPoids = UnitConverter.UNIT_KG; // Kg
            if (workoutValuesInputView.getWeightUnit()==WeightUnit.LBS) {
                tmpPoids = UnitConverter.LbstoKg(tmpPoids); // Always convert to KG
                unitPoids = UnitConverter.UNIT_LBS; // LBS
            }

            mDbBodyBuilding.addBodyBuildingRecord(date,
                machineEdit.getText().toString(),
                workoutValuesInputView.getSets(),
                workoutValuesInputView.getReps(),
                tmpPoids, // Always save in KG
                getProfil(),
                unitPoids, // Store Unit for future display
                "", //Notes
                timeStr
            );

            float iTotalWeightSession = mDbBodyBuilding.getTotalWeightSession(date);
            float iTotalWeight = mDbBodyBuilding.getTotalWeightMachine(date, machineEdit.getText().toString());
            int iNbSeries = mDbBodyBuilding.getNbSeries(date, machineEdit.getText().toString());

            //--Launch Rest Dialog
            boolean bLaunchRest = restTimeCheck.isChecked();
            int restTime = 60;
            try {
                restTime = Integer.valueOf(restTimeEdit.getText().toString());
            } catch (NumberFormatException e) {
                restTime = 60;
                restTimeEdit.setText("60");
            }

            // Launch Countdown
            if (bLaunchRest && DateConverter.dateToLocalDateStr(date, getContext()).equals(DateConverter.dateToLocalDateStr(new Date(), getContext()))) { // Only launch Countdown if date is today.
                CountdownDialogbox cdd = new CountdownDialogbox(mActivity, restTime);
                cdd.setNbSeries(iNbSeries);
                cdd.setTotalWeightMachine(iTotalWeight);
                cdd.setTotalWeightSession(iTotalWeightSession);
                cdd.show();
            }
        } else if (exerciseType == ExerciseType.ISOMETRIC) {
            // Verifie que les infos sont completes
            if (!workoutValuesInputView.isFilled()) {
                KToast.warningToast(getActivity(), getResources().getText(R.string.missinginfo).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                return;
            }

            /* Convertion du poid */
            /* Convertion du poid */
            float tmpPoids = workoutValuesInputView.getWeightValue();
            int unitPoids = UnitConverter.UNIT_KG; // Kg
            if (workoutValuesInputView.getWeightUnit()==WeightUnit.LBS) {
                tmpPoids = UnitConverter.LbstoKg(tmpPoids); // Always convert to KG
                unitPoids = UnitConverter.UNIT_LBS; // LBS
            }

            mDbStatic.addStaticRecord(date,
                machineEdit.getText().toString(),
                workoutValuesInputView.getSets(),
                workoutValuesInputView.getSeconds(),
                tmpPoids, // Always save in KG
                getProfil(),
                unitPoids, // Store Unit for future display
                "", //Notes
                timeStr
            );

            float iTotalWeightSession = mDbStatic.getTotalWeightSession(date);
            float iTotalWeight = mDbStatic.getTotalWeightMachine(date, machineEdit.getText().toString());
            int iNbSeries = mDbStatic.getNbSeries(date, machineEdit.getText().toString());

            //--Launch Rest Dialog
            boolean bLaunchRest = restTimeCheck.isChecked();
            int restTime = 60;
            try {
                restTime = Integer.valueOf(restTimeEdit.getText().toString());
            } catch (NumberFormatException e) {
                restTime = 60;
                restTimeEdit.setText("60");
            }

            // Launch Countdown
            if (bLaunchRest && DateConverter.dateToLocalDateStr(date, getContext()).equals(DateConverter.dateToLocalDateStr(new Date(), getContext()))) { // Only launch Countdown if date is today.
                CountdownDialogbox cdd = new CountdownDialogbox(mActivity, restTime);
                cdd.setNbSeries(iNbSeries);
                cdd.setTotalWeightMachine(iTotalWeight);
                cdd.setTotalWeightSession(iTotalWeightSession);
                cdd.show();
            }
        } else if (exerciseType == ExerciseType.CARDIO) {
            // Verifie que les infos sont completes
            if (!workoutValuesInputView.isFilled()) {
                KToast.warningToast(getActivity(), getResources().getText(R.string.missinginfo).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                return;
            }

            long duration = workoutValuesInputView.getDurationValue();

            float distance = workoutValuesInputView.getDistanceValue();

            int unitDistance = UnitConverter.UNIT_KM;
            if (workoutValuesInputView.getDistanceUnit()==DistanceUnit.MILES) {
                distance = UnitConverter.MilesToKm(distance); // Always convert to KG
                unitDistance = UnitConverter.UNIT_MILES;
            }

            mDbCardio.addCardioRecord(date,
                timeStr,
                machineEdit.getText().toString(),
                distance,
                duration,
                getProfil(),
                unitDistance);

            // No Countdown for Cardio
        }

        getActivity().findViewById(R.id.drawer_layout).requestFocus();
        hideKeyboard(v);

        lTableColor = (lTableColor + 1) % 2; // Change la couleur a chaque ajout de donnees

        refreshData();

        /* Reinitialisation des machines */
        // TODO Eviter de recreer a chaque fois l'adapter. On peut utiliser toujours le meme.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getView().getContext(),
            android.R.layout.simple_dropdown_item_1line, mDb.getAllMachines(getProfil()));
        machineEdit.setAdapter(adapter);

        //Rajoute le moment du dernier ajout dans le bouton Add
        addButton.setText(getView().getContext().getString(R.string.AddLabel) + "\n(" + DateConverter.currentTime() + ")");

        mDbCardio.closeCursor();
        mDbBodyBuilding.closeCursor();
        mDbStatic.closeCursor();
        mDb.closeCursor();
    };
    private OnClickListener onClickMachineListWithIcons = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Cursor c;
            Cursor oldCursor;

            // In case the dialog is already open
            if (machineListDialog != null && machineListDialog.isShowing()) {
                return;
            }

            ListView machineList = new ListView(v.getContext());

            // Version avec table Machine
            c = mDbMachine.getAllMachines();

            if (c == null || c.getCount() == 0) {
                //Toast.makeText(getActivity(), R.string.createExerciseFirst, Toast.LENGTH_SHORT).show();
                KToast.warningToast(getActivity(), getResources().getText(R.string.createExerciseFirst).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                machineList.setAdapter(null);
            } else {
                if (machineList.getAdapter() == null) {
                    MachineCursorAdapter mTableAdapter = new MachineCursorAdapter(getActivity(), c, 0, mDbMachine);
                    //MachineArrayFullAdapter lAdapter = new MachineArrayFullAdapter(v.getContext(),records);
                    machineList.setAdapter(mTableAdapter);
                } else {
                    MachineCursorAdapter mTableAdapter = ((MachineCursorAdapter) machineList.getAdapter());
                    oldCursor = mTableAdapter.swapCursor(c);
                    if (oldCursor != null) oldCursor.close();
                }

                machineList.setOnItemClickListener((parent, view, position, id) -> {
                    TextView textView = view.findViewById(R.id.LIST_MACHINE_ID);
                    long machineID = Long.parseLong(textView.getText().toString());
                    DAOMachine lMachineDb = new DAOMachine(getContext());
                    Machine lMachine = lMachineDb.getMachine(machineID);

                    setCurrentMachine(lMachine.getName());

                    getMainActivity().findViewById(R.id.drawer_layout).requestFocus();

                    hideKeyboard(getMainActivity().findViewById(R.id.drawer_layout));

                    if (machineListDialog.isShowing()) {
                        machineListDialog.dismiss();
                    }
                });

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle(R.string.selectMachineDialogLabel);
                builder.setView(machineList);
                machineListDialog = builder.create();
                machineListDialog.show();
            }
        }
    };

    private OnItemLongClickListener itemlongclickDeleteRecord = (listView, view, position, id) -> {
        showRecordListMenu(id);
        return true;
    };

    private OnItemClickListener onItemClickFilterList = (parent, view, position, id) -> setCurrentMachine(machineEdit.getText().toString());
    private DatePickerDialog.OnDateSetListener dateSet = (view, year, month, day) -> {
        dateEdit.setText(DateConverter.dateToString(year, month + 1, day));
        hideKeyboard(dateEdit);
    };
    private OnClickListener clickDateEdit = v -> {
        switch (v.getId()) {
            case R.id.editDate:
                showDatePickerFragment();
                break;
            case R.id.editTime:
                showTimePicker(timeEdit);
                break;
        }
    };
    private OnFocusChangeListener touchRazEdit = (v, hasFocus) -> {
        if (hasFocus) {
            switch (v.getId()) {
                case R.id.editMachine:
                    machineEdit.setText("");
                    switch (workoutValuesInputView.getSelectedType()) {
                        case CARDIO:
                            machineImage.setImageResource(R.drawable.ic_training_white_50dp);
                            break;
                        case ISOMETRIC:
                            machineImage.setImageResource(R.drawable.ic_static);
                            break;
                        case STRENGTH:
                        default:
                            machineImage.setImageResource(R.drawable.ic_gym_bench_50dp);
                    }

                    workoutValuesInputView.setWeightComment("");
                    workoutValuesInputView.setShowExerciseTypeSelector(true);
                    break;
            }
            v.post(() -> {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
            });
        } else if (!hasFocus) {
            switch (v.getId()) {
                case R.id.editMachine:
                    // If a creation of a new machine is not ongoing.
                    if (!workoutValuesInputView.isShowExerciseTypeSelector())
                        setCurrentMachine(machineEdit.getText().toString());
                    break;
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener checkedAutoTimeCheckBox = (buttonView, isChecked) -> {
        dateEdit.setEnabled(!isChecked);
        timeEdit.setEnabled(!isChecked);
        if (isChecked) {
            dateEdit.setText(DateConverter.currentDate());
            timeEdit.setText(DateConverter.currentTime());
        }
    };


    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static FontesFragment newInstance(String name, int id) {
        FontesFragment f = new FontesFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putInt("id", id);
        f.setArguments(args);

        return f;
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
            ListView lv = ((AlertDialog) dialog).getListView();

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
                    IRecord r = mDb.getRecord(id);
                    String text = "";
                    if (r.getType() == DAOMachine.TYPE_FONTE ||r.getType() == DAOMachine.TYPE_STATIC  ) {
                        Fonte fonte = (Fonte) r;
                        // Build text
                        text = getView().getContext().getResources().getText(R.string.ShareTextDefault).toString();
                        text = text.replace(getView().getContext().getResources().getText(R.string.ShareParamWeight), String.valueOf(fonte.getPoids()));
                        text = text.replace(getView().getContext().getResources().getText(R.string.ShareParamMachine), fonte.getExercise());
                    } else {
                        Cardio cardio = (Cardio) r;
                        // Build text
                        text = "I have done __METER__ in __TIME__ on __MACHINE__.";
                        text = text.replace("__METER__", String.valueOf(cardio.getDistance()));
                        text = text.replace("__TIME__", String.valueOf(cardio.getDuration()));
                        text = text.replace(getView().getContext().getResources().getText(R.string.ShareParamMachine), cardio.getExercise());
                    }
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
                mDb.deleteRecord(idToDelete);

                updateRecordTable(machineEdit.getText().toString());

                //Toast.makeText(getContext(), getResources().getText(R.string.removedid) + " " + idToDelete, Toast.LENGTH_SHORT).show();
                // Info
                KToast.infoToast(getActivity(), getResources().getText(R.string.removedid).toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                sDialog.dismissWithAnimation();
            })
            .show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.tab_fontes, container, false);

        machineEdit = view.findViewById(R.id.editMachine);

        workoutValuesInputView = view.findViewById(R.id.WorkoutValuesInput);

        recordList = view.findViewById(R.id.listRecord);
        machineListButton = view.findViewById(R.id.buttonListMachine);
        addButton = view.findViewById(R.id.addperff);


        detailsLayout = view.findViewById(R.id.notesLayout);
        detailsExpandArrow = view.findViewById(R.id.buttonExpandArrow);
        restTimeEdit = view.findViewById(R.id.editRestTime);
        restTimeCheck = view.findViewById(R.id.restTimecheckBox);
        machineImage = view.findViewById(R.id.imageMachine);


        restTimeLayout = view.findViewById(R.id.restTimeLayout);

        autoTimeCheckBox = view.findViewById(R.id.autoTimeCheckBox);
        dateEdit = view.findViewById(R.id.editDate);
        timeEdit = view.findViewById(R.id.editTime);

        /* Initialisation des boutons */
        addButton.setOnClickListener(clickAddButton);
        machineListButton.setOnClickListener(onClickMachineListWithIcons); //onClickMachineList

        dateEdit.setOnClickListener(clickDateEdit);
        timeEdit.setOnClickListener(clickDateEdit);
        autoTimeCheckBox.setOnCheckedChangeListener(checkedAutoTimeCheckBox);

        machineEdit.setOnKeyListener(checkExerciseExists);
        machineEdit.setOnFocusChangeListener(touchRazEdit);
        machineEdit.setOnItemClickListener(onItemClickFilterList);
        recordList.setOnItemLongClickListener(itemlongclickDeleteRecord);
        detailsExpandArrow.setOnClickListener(collapseDetailsClick);
        restTimeEdit.setOnFocusChangeListener(restTimeEditChange);
        restTimeCheck.setOnCheckedChangeListener(restTimeCheckChange); //.setOnFocusChangeListener(restTimeEditChange);

        restoreSharedParams();

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getActivity());
        WeightUnit weightUnit = WeightUnit.KG;
        try {
            weightUnit = WeightUnit.fromInteger(Integer.valueOf(SP.getString(SettingsFragment.WEIGHT_UNIT_PARAM, "0")));
        } catch (NumberFormatException e) {
            weightUnit = WeightUnit.KG;
        }
        workoutValuesInputView.setWeightUnit(weightUnit);

        DistanceUnit distanceUnit;
        try {
            distanceUnit = DistanceUnit.fromInteger(Integer.valueOf(SP.getString(SettingsFragment.DISTANCE_UNIT_PARAM, "0")));
        } catch (NumberFormatException e) {
            distanceUnit = DistanceUnit.KM;
        }
        workoutValuesInputView.setDurationUnit(distanceUnit);

        // Initialisation de la base de donnee
        mDbBodyBuilding = new DAOFonte(getContext());
        mDbCardio = new DAOCardio(getContext());
        mDbStatic = new DAOStatic(getContext());
        mDb = new DAORecord(getContext());

        mDbMachine = new DAOMachine(getContext());
        dateEdit.setText(DateConverter.currentDate());
        timeEdit.setText(DateConverter.currentTime());

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

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        this.mActivity = (MainActivity) this.getActivity();
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

    public int getFragmentId() {
        return getArguments().getInt("id", 0);
    }

    public MainActivity getMainActivity() {
        return this.mActivity;
    }

    private void showDatePickerFragment() {
        if (mDateFrag == null) {
            mDateFrag = DatePickerDialogFragment.newInstance(dateSet);
            mDateFrag.show(getActivity().getFragmentManager().beginTransaction(), "dialog");
        } else {
            if (!mDateFrag.isVisible())
                mDateFrag.show(getActivity().getFragmentManager().beginTransaction(), "dialog");
        }
    }

    private void showTimePicker(TextView timeTextView) {
        String tx =  timeTextView.getText().toString();
        int hour;
        try {
            hour = Integer.valueOf(tx.substring(0, 2));
        } catch (Exception e) {
            hour=0;
        }
        int min;
        try {
            min = Integer.valueOf(tx.substring(3, 5));
        } catch (Exception e) {
            min=0;
        }
        int sec;
        try {
        sec = Integer.valueOf(tx.substring(6));
        } catch (Exception e) {
            sec=0;
        }

        switch(timeTextView.getId()) {
            case R.id.editTime:
                if (mTimeFrag == null) {
                    mTimeFrag = TimePickerDialogFragment.newInstance(timeSet, hour, min, sec);
                    mTimeFrag.show(getActivity().getFragmentManager().beginTransaction(), "dialog_time");
                } else {
                    if (!mTimeFrag.isVisible()) {
                        Bundle bundle = new Bundle();
                        bundle.putInt("HOUR", hour);
                        bundle.putInt("MINUTE", min);
                        bundle.putInt("SECOND", sec);
                        mTimeFrag.setArguments(bundle);
                        mTimeFrag.show(getActivity().getFragmentManager().beginTransaction(), "dialog_time");
                    }
                }
                break;
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

        newProfilBuilder.setPositiveButton(getView().getContext().getResources().getText(R.string.ShareText), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, value);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });

        newProfilBuilder.setNegativeButton(getView().getContext().getResources().getText(R.string.global_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        newProfilBuilder.show();

        return true;
    }

    public FontesFragment getFragment() {
        return this;
    }

    private Profile getProfil() {
        return mActivity.getCurrentProfile();
    }

    public String getMachine() {
        /*if (machineEdit == null)
            machineEdit = this.getView().findViewById(R.id.editMachine);*/
        return machineEdit.getText().toString();
    }

    public void setCurrentMachine(String machineStr) {
        if (machineStr.isEmpty()) {
            switch (workoutValuesInputView.getSelectedType()) {
                case CARDIO:
                    machineImage.setImageResource(R.drawable.ic_training_white_50dp);
                    break;
                case ISOMETRIC:
                    machineImage.setImageResource(R.drawable.ic_static);
                    break;
                case STRENGTH:
                default:
                    machineImage.setImageResource(R.drawable.ic_gym_bench_50dp);
            }
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
            changeExerciseTypeUI(DAOMachine.TYPE_FONTE, true);
            updateMinMax(null);
            return;
        }



        // Update EditView
        machineEdit.setText(lMachine.getName());
        // Update exercise Image
        // Default image
        switch (lMachine.getType()) {
            case DAOMachine.TYPE_CARDIO:
                machineImage.setImageResource(R.drawable.ic_training_white_50dp);
                break;
            case DAOMachine.TYPE_STATIC:
                machineImage.setImageResource(R.drawable.ic_static);
                break;
            default:
                machineImage.setImageResource(R.drawable.ic_gym_bench_50dp);
        }
        ImageUtil imgUtil = new ImageUtil();
        ImageUtil.setThumb(machineImage, imgUtil.getThumbPath(lMachine.getPicture())); // Overwrite image is there is one

        // Update Table
        updateRecordTable(lMachine.getName());
        // Update display type
        changeExerciseTypeUI(lMachine.getType(), false);

        // Depending on the machine type :
        // Update Min Max
        updateMinMax(lMachine);
        // Update last values
        updateLastRecord(lMachine);
    }

    private void updateMinMax(Machine m) {
        String comment ="";
        String unitStr = "";
        float weight = 0;
        if (getProfil() != null && m != null) {
            if (m.getType() == DAOMachine.TYPE_FONTE || m.getType() == DAOMachine.TYPE_STATIC) {
                DecimalFormat numberFormat = new DecimalFormat("#.##");
                Weight minValue = mDbBodyBuilding.getMin(getProfil(), m);
                if (minValue != null && minValue.getStoredWeight()!=0) {
                    if (minValue.getStoredUnit() == UnitConverter.UNIT_LBS) {
                        weight = UnitConverter.KgtoLbs(minValue.getStoredWeight());
                        unitStr = getContext().getString(R.string.LbsUnitLabel);
                    } else {
                        weight = minValue.getStoredWeight();
                        unitStr = getContext().getString(R.string.KgUnitLabel);
                    }

                    comment = getContext().getString(R.string.min) + ":" + numberFormat.format(weight) + unitStr + " - ";
                }

                Weight maxValue = mDbBodyBuilding.getMax(getProfil(), m);
                if (maxValue != null && maxValue.getStoredWeight()!=0) {
                    if (maxValue.getStoredUnit() == UnitConverter.UNIT_LBS) {
                        weight = UnitConverter.KgtoLbs(maxValue.getStoredWeight());
                        unitStr = getContext().getString(R.string.LbsUnitLabel);
                    } else {
                        weight = maxValue.getStoredWeight();
                        unitStr = getContext().getString(R.string.KgUnitLabel);
                    }
                    comment = comment + getContext().getString(R.string.max) + ":" + numberFormat.format(weight) +  unitStr;
                } else {
                    comment = "";
                }
            } else if (m.getType() == DAOMachine.TYPE_CARDIO) {
                comment = "";
            }
        } else {
            comment ="";
        }

        workoutValuesInputView.setWeightComment(comment);
    }

    private void updateLastRecord(Machine m) {
        IRecord lLastRecord = mDb.getLastExerciseRecord(m.getId(), getProfil());
        // Default Values
        workoutValuesInputView.setSets(1);
        workoutValuesInputView.setReps(10);
        workoutValuesInputView.setSeconds(60);
        workoutValuesInputView.setWeight(50, WeightUnit.KG);
        workoutValuesInputView.setDistance(10, DistanceUnit.KM);
        workoutValuesInputView.setDuration(600000);
        if (lLastRecord == null) {
            // Set default values or nothing.
        } else if (lLastRecord.getType() == DAOMachine.TYPE_FONTE) {
            Fonte lLastBodyBuildingRecord = (Fonte) lLastRecord;
            workoutValuesInputView.setSets(lLastBodyBuildingRecord.getSerie());
            workoutValuesInputView.setReps(lLastBodyBuildingRecord.getRepetition());
            if (lLastBodyBuildingRecord.getUnit() == UnitConverter.UNIT_LBS)
                workoutValuesInputView.setWeight(UnitConverter.KgtoLbs(lLastBodyBuildingRecord.getPoids()), WeightUnit.LBS);
            else
                workoutValuesInputView.setWeight(lLastBodyBuildingRecord.getPoids(), WeightUnit.KG);
        } else if (lLastRecord.getType() == DAOMachine.TYPE_CARDIO) {
            Cardio lLastCardioRecord = (Cardio) lLastRecord;
            workoutValuesInputView.setDuration(lLastCardioRecord.getDuration());
            if (lLastCardioRecord.getDistanceUnit() == UnitConverter.UNIT_MILES)
                workoutValuesInputView.setDistance(UnitConverter.KmToMiles(lLastCardioRecord.getDistance()), DistanceUnit.MILES);
            else
                workoutValuesInputView.setDistance(lLastCardioRecord.getDistance(), DistanceUnit.KM);
        } else if (lLastRecord.getType() == DAOMachine.TYPE_STATIC) {
            StaticExercise lLastStaticRecord = (StaticExercise) lLastRecord;
            workoutValuesInputView.setSets(lLastStaticRecord.getSerie());
            workoutValuesInputView.setSeconds(lLastStaticRecord.getSecond());
            if (lLastStaticRecord.getUnit() == UnitConverter.UNIT_LBS)
                workoutValuesInputView.setWeight(UnitConverter.KgtoLbs(lLastStaticRecord.getPoids()), WeightUnit.LBS);
            else
                workoutValuesInputView.setWeight(lLastStaticRecord.getPoids(), WeightUnit.KG);
        }
    }

    /*  */
    private void updateRecordTable(String pMachine) {
        // Informe l'activité de la machine courante
        this.getMainActivity().setCurrentMachine(pMachine);
        if (getView()==null) return;
        getView().post(() -> {

            Cursor c = null;
            Cursor oldCursor = null;

            IRecord r = mDb.getLastRecord(getProfil());

            // Recupere les valeurs
            //if (pMachine == null || pMachine.isEmpty()) {
            if (r != null)
                c = mDb.getTop3DatesRecords(getProfil());
            else
                return;

            if (c == null || c.getCount() == 0) {
                recordList.setAdapter(null);
            } else {
                if (recordList.getAdapter() == null) {
                    RecordCursorAdapter mTableAdapter = new RecordCursorAdapter(mActivity, c, 0, itemClickDeleteRecord, itemClickCopyRecord);
                    mTableAdapter.setFirstColorOdd(lTableColor);
                    recordList.setAdapter(mTableAdapter);
                } else {
                    RecordCursorAdapter mTableAdapter = ((RecordCursorAdapter) recordList.getAdapter());
                    mTableAdapter.setFirstColorOdd(lTableColor);
                    oldCursor = mTableAdapter.swapCursor(c);
                    if (oldCursor != null) oldCursor.close();
                }
            }
        });
    }

    private void refreshData() {
        View fragmentView = getView();
        if (fragmentView != null) {
            if (getProfil() != null) {
                mDb.setProfile(getProfil());

                ArrayList<Machine> machineListArray;
                // Version avec table Machine
                machineListArray = mDbMachine.getAllMachinesArray();

                /* Init machines list*/
                machineEditAdapter = new MachineArrayFullAdapter(getContext(), machineListArray);
                machineEdit.setAdapter(machineEditAdapter);

                // If profile has changed
                mProfile = getProfil();

                if (machineEdit.getText().toString().isEmpty()) {
                    IRecord lLastRecord = mDb.getLastRecord(getProfil());
                    if (lLastRecord != null) {
                        // Last recorded exercise
                        setCurrentMachine(lLastRecord.getExercise());
                    } else {
                        // Default Values
                        machineEdit.setText("");
                        // Default Values
                        workoutValuesInputView.setSets(1);
                        workoutValuesInputView.setReps(10);
                        workoutValuesInputView.setSeconds(60);
                        workoutValuesInputView.setWeight(50, WeightUnit.KG);
                        workoutValuesInputView.setDistance(10, DistanceUnit.KM);
                        workoutValuesInputView.setDuration(600000);
                        setCurrentMachine("");
                        changeExerciseTypeUI(DAOMachine.TYPE_FONTE, true);
                    }
                } else { // Restore on fragment restore.
                    setCurrentMachine(machineEdit.getText().toString());
                }

                // Set Initial text
                if (autoTimeCheckBox.isChecked()) {
                    dateEdit.setText(DateConverter.currentDate());
                    timeEdit.setText(DateConverter.currentTime());
                }

                // Set Table
                updateRecordTable(machineEdit.getText().toString());
            }
        }
    }

    private void changeExerciseTypeUI(int pType, boolean displaySelector) {
        workoutValuesInputView.setShowExerciseTypeSelector(displaySelector);
        switch (pType) {
            case DAOMachine.TYPE_CARDIO:
                workoutValuesInputView.setSelectedType(ExerciseType.CARDIO);
                restTimeLayout.setVisibility(View.GONE);
                break;
            case DAOMachine.TYPE_STATIC:
                workoutValuesInputView.setSelectedType(ExerciseType.ISOMETRIC);
                restTimeLayout.setVisibility(View.VISIBLE);
                break;
            case DAOMachine.TYPE_FONTE:
            default:
                workoutValuesInputView.setSelectedType(ExerciseType.STRENGTH);
                restTimeLayout.setVisibility(View.VISIBLE);
        }
    }

    public void saveSharedParams() {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("restTime", restTimeEdit.getText().toString());
        editor.putBoolean("restCheck", restTimeCheck.isChecked());
        editor.putBoolean("showDetails", this.detailsLayout.isShown());
        editor.apply();
    }

    public void restoreSharedParams() {
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        restTimeEdit.setText(sharedPref.getString("restTime", ""));
        restTimeCheck.setChecked(sharedPref.getBoolean("restCheck", true));

        if (sharedPref.getBoolean("showDetails", false)) {
            detailsLayout.setVisibility(View.VISIBLE);
        } else {
            detailsLayout.setVisibility(View.GONE);
        }
        detailsExpandArrow.setImageResource(sharedPref.getBoolean("showDetails", false) ? R.drawable.ic_expand_less_black_24dp : R.drawable.ic_expand_more_black_24dp);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden)
            refreshData();
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
