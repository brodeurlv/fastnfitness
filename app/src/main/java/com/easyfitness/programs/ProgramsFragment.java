package com.easyfitness.programs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.easyfitness.BtnClickListener;
import com.easyfitness.DAO.Cardio;
import com.easyfitness.DAO.DAOCardio;
import com.easyfitness.DAO.DAOExerciseInProgram;
import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.DAO.DAOStatic;
import com.easyfitness.DAO.IRecord;
import com.easyfitness.DAO.Machine;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.ExerciseInProgram;
import com.easyfitness.DAO.StaticExercise;
import com.easyfitness.MainActivity;
import com.easyfitness.R;
import com.easyfitness.SettingsFragment;
import com.easyfitness.TimePickerDialogFragment;
import com.easyfitness.machines.ExerciseDetailsPager;
import com.easyfitness.machines.MachineCursorAdapter;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.ExpandedListView;
import com.easyfitness.utils.ImageUtil;
import com.easyfitness.utils.UnitConverter;
import com.ikovac.timepickerwithseconds.MyTimePickerDialog;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class ProgramsFragment extends Fragment {
    private MainActivity mActivity = null;
    private AutoCompleteTextView machineEdit = null;
    private EditText seriesEdit = null;
    private EditText repetitionEdit = null;
    private EditText poidsEdit = null;
    private LinearLayout detailsLayout = null;
    private Button addButton = null;
    private ExpandedListView recordList = null;
    private Spinner unitSpinner = null;
    private Spinner unitDistanceSpinner = null;
    private EditText restTimeEdit = null;
    private CheckBox restTimeCheck = null;
    private CircularImageView machineImage = null;
    private int lTableColor = 1;
    private AlertDialog machineListDialog;
    private LinearLayout minMaxLayout = null;
    // Selection part
    private LinearLayout exerciseTypeSelectorLayout = null;
    private TextView bodybuildingSelector = null;
    private TextView cardioSelector = null;
    private TextView staticExerciseSelector = null;
    private int selectedType = DAOMachine.TYPE_FONTE;
    private LinearLayout restTimeLayout = null;
    private EditText distanceEdit = null;
    private TextView durationEdit = null;
    private EditText secondsEdit = null;
    private CardView seriesCardView = null;
    private CardView repetitionCardView = null;
    private CardView secondsCardView = null;
    private CardView weightCardView = null;
    private CardView distanceCardView = null;
    private CardView durationCardView = null;

    private MyTimePickerDialog.OnTimeSetListener durationSet = (view, hourOfDay, minute, second) -> {
        // Do something with the time chosen by the user
        String strMinute;
        String strHour;
        String strSecond;

        if (minute < 10) strMinute = "0" + minute;
        else strMinute = Integer.toString(minute);
        if (hourOfDay < 10) strHour = "0" + hourOfDay;
        else strHour = Integer.toString(hourOfDay);
        if (second < 10) strSecond = "0" + second;
        else strSecond = Integer.toString(second);

        String date = strHour + ":" + strMinute + ":" + strSecond;
        durationEdit.setText(date);
        hideKeyboard(durationEdit);
    };
    private DAOExerciseInProgram mDbBodyBuilding = null;
    private DAOCardio mDbCardio = null;
    private DAOStatic mDbStatic = null;
    private DAOExerciseInProgram mDb = null;
    private DAOMachine mDbMachine = null;

    private OnClickListener clickExerciseTypeSelector = v -> {
        switch (v.getId()) {
            case R.id.staticSelection:
                changeExerciseTypeUI(DAOMachine.TYPE_STATIC, true);
                break;
            case R.id.cardioSelection:
                changeExerciseTypeUI(DAOMachine.TYPE_CARDIO, true);
                break;
            case R.id.bodyBuildingSelection:
            default:
                changeExerciseTypeUI(DAOMachine.TYPE_FONTE, true);
                break;
        }
    };
    private View.OnKeyListener checkExerciseExists = (v, keyCode, event) -> {
        Machine lMach = mDbMachine.getMachine(machineEdit.getText().toString());
        if (lMach == null) {
            showExerciseTypeSelector(true);
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
    @SuppressLint("DefaultLocale")
    private BtnClickListener itemClickCopyRecord = id -> {
        IRecord r = mDb.getRecord(id);
        if (r != null) {
            // Copy values above
            setCurrentMachine(r.getExercise());
            if (r.getType() == DAOMachine.TYPE_FONTE) {
                ExerciseInProgram f = (ExerciseInProgram) r;
                repetitionEdit.setText(String.format("%d", f.getRepetition()));
                seriesEdit.setText(String.format("%d", f.getSerie()));
                DecimalFormat numberFormat = new DecimalFormat("#.##");

                Float poids = f.getPoids();
                if (f.getUnit() == UnitConverter.UNIT_LBS) {
                    poids = UnitConverter.KgtoLbs(poids);
                }
                unitSpinner.setSelection(f.getUnit());
                poidsEdit.setText(numberFormat.format(poids));
            } else if (r.getType() == DAOMachine.TYPE_STATIC) {
                StaticExercise f = (StaticExercise) r;
                secondsEdit.setText(String.format("%d", f.getSecond()));
                seriesEdit.setText(String.format("%d", f.getSerie()));
                DecimalFormat numberFormat = new DecimalFormat("#.##");
                poidsEdit.setText(numberFormat.format(f.getPoids()));
            } else if (r.getType() == DAOMachine.TYPE_CARDIO) {
                Cardio c = (Cardio) r;
                DecimalFormat numberFormat = new DecimalFormat("#.##");

                float distance = c.getDistance();
                if (c.getDistanceUnit() == UnitConverter.UNIT_MILES) {
                    distance = UnitConverter.KmToMiles((c.getDistance()));
                }
                unitDistanceSpinner.setSelection(c.getDistanceUnit());
                distanceEdit.setText(numberFormat.format(distance));

                durationEdit.setText(DateConverter.durationToHoursMinutesSecondsStr(c.getDuration()));
            }
            KToast.infoToast(getMainActivity(), getString(R.string.recordcopied), Gravity.BOTTOM, KToast.LENGTH_SHORT);
        }
    };
    @SuppressLint("SetTextI18n")
    private OnClickListener clickAddButton = v -> {
        if (machineEdit.getText().toString().isEmpty()) {
            KToast.warningToast(Objects.requireNonNull(getActivity()), getResources().getText(R.string.missinginfo).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
            return;
        }

        int exerciseType;
        Machine lMachine = mDbMachine.getMachine(machineEdit.getText().toString());
        if (lMachine == null) {
            exerciseType = selectedType;
        } else {
            exerciseType = lMachine.getType();
        }

        int restTime = 60;
        try {
            restTime = Integer.parseInt(restTimeEdit.getText().toString());
        } catch (NumberFormatException e) {
            restTimeEdit.setText("60");
        }
        if (exerciseType == DAOMachine.TYPE_FONTE) {
            if (seriesEdit.getText().toString().isEmpty() ||
                repetitionEdit.getText().toString().isEmpty() ||
                poidsEdit.getText().toString().isEmpty()) {
                KToast.warningToast(Objects.requireNonNull(getActivity()), getResources().getText(R.string.missinginfo).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                return;
            }

            /* Weight conversion */
            float tmpPoids = Float.parseFloat(poidsEdit.getText().toString().replaceAll(",", "."));
            int unitPoids = UnitConverter.UNIT_KG; // Kg
            Context mContext = getContext();
            if(mContext!=null) {
                if (unitSpinner.getSelectedItem().toString().equals(mContext.getString(R.string.LbsUnitLabel))) {
                    tmpPoids = UnitConverter.LbstoKg(tmpPoids); // Always convert to KG
                    unitPoids = UnitConverter.UNIT_LBS; // LBS
                }
            }
            mDbBodyBuilding.addRecord(
                restTime,
                machineEdit.getText().toString(),
                exerciseType,
                Integer.parseInt(seriesEdit.getText().toString()),
                Integer.parseInt(repetitionEdit.getText().toString()),
                tmpPoids, // Always save in KG
                getProfil(),
                unitPoids, // Store Unit for future display
                "", //Notes,
                "",0,0,0,0
            );

        } else if (exerciseType == DAOMachine.TYPE_STATIC) {
            if (seriesEdit.getText().toString().isEmpty() ||
                secondsEdit.getText().toString().isEmpty() ||
                poidsEdit.getText().toString().isEmpty()) {
                KToast.warningToast(Objects.requireNonNull(getActivity()), getResources().getText(R.string.missinginfo).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                return;
            }

            /* Weight conversion */
            float tmpPoids = Float.parseFloat(poidsEdit.getText().toString().replaceAll(",", "."));
            int unitPoids = UnitConverter.UNIT_KG; // Kg
            if (unitSpinner.getSelectedItem().toString().equals(Objects.requireNonNull(getView()).getContext().getString(R.string.LbsUnitLabel))) {
                tmpPoids = UnitConverter.LbstoKg(tmpPoids); // Always convert to KG
                unitPoids = UnitConverter.UNIT_LBS; // LBS
            }
            try {
                restTime = Integer.parseInt(restTimeEdit.getText().toString());
            } catch (NumberFormatException e) {
                restTime = 0;
                restTimeEdit.setText("0");
            }
            mDbBodyBuilding.addRecord(restTime,
                machineEdit.getText().toString(),
                Integer.parseInt(seriesEdit.getText().toString()),
                Integer.parseInt(secondsEdit.getText().toString()),
                1,
                tmpPoids, // Always save in KG
                getProfil(),
                unitPoids, // Store Unit for future display
                "" //Notes
                ,"",0,0,0,0
            );
        } else if (exerciseType == DAOMachine.TYPE_CARDIO) {
            if (durationEdit.getText().toString().isEmpty() && // Only one is mandatory
                distanceEdit.getText().toString().isEmpty()) {
                KToast.warningToast(Objects.requireNonNull(getActivity()),
                    getResources().getText(R.string.missinginfo).toString()+" Distance missing",
                    Gravity.BOTTOM, KToast.LENGTH_SHORT);
                return;
            }

            long duration;
            try {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                Date tmpDate = dateFormat.parse(durationEdit.getText().toString());
                duration = tmpDate.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
                duration = 0;
            }

            float distance;
            if (distanceEdit.getText().toString().isEmpty()) {
                distance = 0;
            } else {
                distance = Float.parseFloat(distanceEdit.getText().toString().replaceAll(",", "."));
            }

            int unitDistance = UnitConverter.UNIT_KM;
            if (unitDistanceSpinner.getSelectedItem().toString()
                .equals(Objects.requireNonNull(getView()).getContext().getString(R.string.MilesUnitLabel))) {
                distance = UnitConverter.MilesToKm(distance); // Always convert to KG
                unitDistance = UnitConverter.UNIT_MILES;
            }

            mDbBodyBuilding.addRecord(restTime,
                machineEdit.getText().toString(),
                exerciseType,
                1,
                1,
                0,
                getProfil(),
                1,
                "",
                "",
                distance,
                duration,
                0,
                unitDistance);

            // No Countdown for Cardio
        }

        Objects.requireNonNull(getActivity()).findViewById(R.id.drawer_layout).requestFocus();
        hideKeyboard(v);

        lTableColor = (lTableColor + 1) % 2; // Change the color each time you add data

        refreshData();

        /* Reinitialisation des machines */
        ArrayAdapter<String> adapter = new ArrayAdapter<>(Objects.requireNonNull(getView()).getContext(),
            android.R.layout.simple_dropdown_item_1line, mDb.getAllMachines(getProfil()));
        machineEdit.setAdapter(adapter);

        addButton.setText(R.string.AddLabel);

        mDbCardio.closeCursor();
        mDbBodyBuilding.closeCursor();
        mDbStatic.closeCursor();
        mDb.closeCursor();
    };
    private OnClickListener onClickMachineListWithIcons = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Cursor c;
            Cursor oldCursor;

            // In case the dialog is already open
            if (machineListDialog != null && machineListDialog.isShowing()) {
                return;
            }

            ListView machineList = new ListView(v.getContext());

            c = mDbMachine.getAllMachines();

            if (c == null || c.getCount() == 0) {
                //Toast.makeText(getActivity(), R.string.createExerciseFirst, Toast.LENGTH_SHORT).show();
                KToast.warningToast(Objects.requireNonNull(getActivity()), getResources().getText(R.string.createExerciseFirst).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                machineList.setAdapter(null);
            } else {
                if (machineList.getAdapter() == null) {
                    MachineCursorAdapter mTableAdapter = new MachineCursorAdapter(getActivity(), c, 0, mDbMachine);
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

    private OnItemClickListener onItemClickFilterList = (parent, view, position, id) -> setCurrentMachine(machineEdit.getText().toString());

    //Required for cardio/duration
    private OnClickListener clickDateEdit = v -> {
        switch (v.getId()) {
            case R.id.editDuration:
                showTimePicker(durationEdit);
                break;
            case R.id.editMachine:
                //InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                //imm.showSoftInput(machineEdit, InputMethodManager.SHOW_IMPLICIT);
                //machineEdit.setText("");
                //machineEdit.set.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                break;
        }
    };
    private OnFocusChangeListener touchRazEdit = (v, hasFocus) -> {
        if (hasFocus) {
            switch (v.getId()) {
                case R.id.editSerie:
                    seriesEdit.setText("");
                    break;
                case R.id.editRepetition:
                    repetitionEdit.setText("");
                    break;
                case R.id.editSeconds:
                    secondsEdit.setText("");
                    break;
                case R.id.editPoids:
                    poidsEdit.setText("");
                    break;
                case R.id.editDuration:
                    showTimePicker(durationEdit);
                    break;
                case R.id.editDistance:
                    distanceEdit.setText("");
                    break;
                case R.id.editMachine:
                    machineEdit.setText("");
                    machineImage.setImageResource(R.drawable.ic_machine);
                    minMaxLayout.setVisibility(View.GONE);
                    showExerciseTypeSelector(true);
                    break;
            }
            v.post(() -> {
                    InputMethodManager imm =
                        (InputMethodManager) Objects.requireNonNull(Objects.requireNonNull(getActivity()).getSystemService(Context.INPUT_METHOD_SERVICE));
                    imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
            });
        } else {
            if (v.getId() == R.id.editMachine) {// If a creation of a new machine is not ongoing.
                if (exerciseTypeSelectorLayout.getVisibility() == View.GONE)
                    setCurrentMachine(machineEdit.getText().toString());
            }
        }
    };

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static ProgramsFragment newInstance(String name, int id) {
        ProgramsFragment f = new ProgramsFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putInt("id", id);
        f.setArguments(args);

        return f;
    }

    private void showDeleteDialog(final long idToDelete) {

        new SweetAlertDialog(Objects.requireNonNull(getContext()), SweetAlertDialog.WARNING_TYPE)
            .setTitleText(getString(R.string.DeleteRecordDialog))
            .setContentText(getResources().getText(R.string.areyousure).toString())
            .setCancelText(getResources().getText(R.string.global_no).toString())
            .setConfirmText(getResources().getText(R.string.global_yes).toString())
            .showCancelButton(true)
            .setConfirmClickListener(sDialog -> {
                mDb.deleteRecord(idToDelete);//Toast.makeText(getContext(), getResources().getText(R.string.removedid) + " " + idToDelete, Toast.LENGTH_SHORT).show();
                updateRecordTable(machineEdit.getText().toString());
                KToast.infoToast(Objects.requireNonNull(getActivity()), getResources().getText(R.string.removedid).toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                sDialog.dismissWithAnimation();
            })
            .show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.tab_program_with_exercises, container, false);
        machineEdit = view.findViewById(R.id.editMachine);
        seriesEdit = view.findViewById(R.id.editSerie);
        repetitionEdit = view.findViewById(R.id.editRepetition);
        poidsEdit = view.findViewById(R.id.editPoids);
        recordList = view.findViewById(R.id.listRecord);
        ImageButton machineListButton = view.findViewById(R.id.buttonListMachine);
        addButton = view.findViewById(R.id.addperff);
        unitSpinner = view.findViewById(R.id.spinnerUnit);
        unitDistanceSpinner = view.findViewById(R.id.spinnerDistanceUnit);
        detailsLayout = view.findViewById(R.id.notesLayout);
        detailsLayout.setVisibility(View.VISIBLE);
        restTimeEdit = view.findViewById(R.id.editRestTime);
        restTimeCheck = view.findViewById(R.id.restTimecheckBox);
        machineImage = view.findViewById(R.id.imageMachine);
        // Cardio Part
        bodybuildingSelector = view.findViewById(R.id.bodyBuildingSelection);
        cardioSelector = view.findViewById(R.id.cardioSelection);
        staticExerciseSelector = view.findViewById(R.id.staticSelection);
        exerciseTypeSelectorLayout = view.findViewById(R.id.exerciseTypeSelectionLayout);
        minMaxLayout = view.findViewById(R.id.minmaxLayout);
        restTimeLayout = view.findViewById(R.id.restTimeLayout);
        durationEdit = view.findViewById(R.id.editDuration);
        distanceEdit = view.findViewById(R.id.editDistance);
        secondsEdit = view.findViewById(R.id.editSeconds);

        seriesCardView = view.findViewById(R.id.cardviewSerie);
        repetitionCardView = view.findViewById(R.id.cardviewRepetition);
        secondsCardView = view.findViewById(R.id.cardviewSeconds);
        weightCardView = view.findViewById(R.id.cardviewWeight);
        distanceCardView = view.findViewById(R.id.cardviewDistance);
        durationCardView = view.findViewById(R.id.cardviewDuration);

        addButton.setOnClickListener(clickAddButton);
        machineListButton.setOnClickListener(onClickMachineListWithIcons); //onClickMachineList
        seriesEdit.setOnFocusChangeListener(touchRazEdit);
        repetitionEdit.setOnFocusChangeListener(touchRazEdit);
        poidsEdit.setOnFocusChangeListener(touchRazEdit);
        distanceEdit.setOnFocusChangeListener(touchRazEdit);
        durationEdit.setOnClickListener(clickDateEdit);
        secondsEdit.setOnFocusChangeListener(touchRazEdit);
        machineEdit.setOnKeyListener(checkExerciseExists);
        machineEdit.setOnFocusChangeListener(touchRazEdit);
        machineEdit.setOnItemClickListener(onItemClickFilterList);
        restTimeEdit.setOnFocusChangeListener(restTimeEditChange);
        restTimeCheck.setOnCheckedChangeListener(restTimeCheckChange);

        bodybuildingSelector.setOnClickListener(clickExerciseTypeSelector);
        cardioSelector.setOnClickListener(clickExerciseTypeSelector);
        staticExerciseSelector.setOnClickListener(clickExerciseTypeSelector);

        restoreSharedParams();

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int weightUnit=UnitConverter.UNIT_KG;
        try {
                weightUnit = Integer.parseInt(Objects.requireNonNull(SP.getString(SettingsFragment.WEIGHT_UNIT_PARAM, "0")));
        } catch (NumberFormatException e) {
            Log.d("Conversion","Not important");
        }
        unitSpinner.setSelection(weightUnit);

        int distanceUnit;
        try {
            distanceUnit = Integer.parseInt(Objects.requireNonNull(SP.getString(SettingsFragment.DISTANCE_UNIT_PARAM, "0")));
        } catch (NumberFormatException e) {
            distanceUnit = UnitConverter.UNIT_KM;
        }
        unitDistanceSpinner.setSelection(distanceUnit);

        // Initialization of the database
        mDbBodyBuilding = new DAOExerciseInProgram(getContext());
        mDbCardio = new DAOCardio(getContext());
        mDbStatic = new DAOStatic(getContext());
        mDb = new DAOExerciseInProgram(getContext());

        mDbMachine = new DAOMachine(getContext());
        selectedType = DAOMachine.TYPE_FONTE;

        machineImage.setOnClickListener(v -> {
            Machine m = mDbMachine.getMachine(machineEdit.getText().toString());
            if (m != null) {
                long profileId;
                profileId=((MainActivity) Objects.requireNonNull(getActivity())).getCurrentProfil().getId();
                ExerciseDetailsPager machineDetailsFragment = ExerciseDetailsPager.newInstance(m.getId(), profileId);
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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }

    public String getName() {
        assert getArguments() != null;
        return getArguments().getString("name");
    }

    private MainActivity getMainActivity() {
        return this.mActivity;
    }

    @SuppressLint("CommitTransaction")
    private void showTimePicker(TextView timeTextView) {
        String tx =  timeTextView.getText().toString();
        int hour;
        try {
            hour = Integer.parseInt(tx.substring(0, 2));
        } catch (Exception e) {
            hour=0;
        }
        int min;
        try {
            min = Integer.parseInt(tx.substring(3, 5));
        } catch (Exception e) {
            min=0;
        }
        int sec;
        try {
        sec = Integer.parseInt(tx.substring(6));
        } catch (Exception e) {
            sec=0;
        }

        if (timeTextView.getId() == R.id.editDuration) {
            TimePickerDialogFragment mDurationFrag = TimePickerDialogFragment.newInstance(durationSet, hour, min, sec);
                mDurationFrag.show(Objects.requireNonNull(getActivity()).getFragmentManager().beginTransaction(), "dialog_time");
        }
    }

    public ProgramsFragment getFragment() {
        return this;
    }

    private Profile getProfil() {
        return mActivity.getCurrentProfil();
    }

    public String getMachine() {
        return machineEdit.getText().toString();
    }

    private void setCurrentMachine(String machineStr) {
        if (machineStr.isEmpty()) {
            machineImage.setImageResource(R.drawable.ic_machine); // Default image
            showExerciseTypeSelector(true);
            minMaxLayout.setVisibility(View.GONE);
            return;
        }

        Machine lMachine = mDbMachine.getMachine(machineStr);
        if (lMachine == null) {
            machineEdit.setText("");
            machineImage.setImageResource(R.drawable.ic_machine); // Default image
            changeExerciseTypeUI(DAOMachine.TYPE_FONTE, true);
            return;
        }

        // Update EditView
        machineEdit.setText(lMachine.getName());
        // Update exercise Image
        machineImage.setImageResource(R.drawable.ic_machine); // Default image
        ImageUtil imgUtil = new ImageUtil();
        ImageUtil.setThumb(machineImage, imgUtil.getThumbPath(lMachine.getPicture())); // Overwrite image is there is one

        // Update Table
        updateRecordTable(lMachine.getName());
        // Update display type
        changeExerciseTypeUI(lMachine.getType(), false);

        // Update last values
        updateLastRecord();
    }

    @SuppressLint("SetTextI18n")
    private void updateLastRecord() {
        IRecord lLastRecord = mDb.getLastRecord(getProfil());
        // Default Values
        seriesEdit.setText("1");
        repetitionEdit.setText("10");
        secondsEdit.setText("60");
        poidsEdit.setText("50");
        distanceEdit.setText("1");
        durationEdit.setText("00:10:00");
        if (lLastRecord != null) {
            if (lLastRecord.getType() == DAOMachine.TYPE_FONTE) {
                ExerciseInProgram lLastBodyBuildingRecord = (ExerciseInProgram) lLastRecord;
                seriesEdit.setText(String.valueOf(lLastBodyBuildingRecord.getSerie()));
                repetitionEdit.setText(String.valueOf(lLastBodyBuildingRecord.getRepetition()));
                unitSpinner.setSelection(lLastBodyBuildingRecord.getUnit());
                DecimalFormat numberFormat = new DecimalFormat("#.##");
                if (lLastBodyBuildingRecord.getUnit() == UnitConverter.UNIT_LBS)
                    poidsEdit.setText(numberFormat.format(UnitConverter.KgtoLbs(lLastBodyBuildingRecord.getPoids())));
                else
                    poidsEdit.setText(numberFormat.format(lLastBodyBuildingRecord.getPoids()));
            } else if (lLastRecord.getType() == DAOMachine.TYPE_CARDIO) {
                Cardio lLastCardioRecord = (Cardio) lLastRecord;
                durationEdit.setText(DateConverter.durationToHoursMinutesSecondsStr(lLastCardioRecord.getDuration()));
                unitDistanceSpinner.setSelection(lLastCardioRecord.getDistanceUnit());
                DecimalFormat numberFormat = new DecimalFormat("#.##");
                if (lLastCardioRecord.getDistanceUnit() == UnitConverter.UNIT_MILES)
                    distanceEdit.setText(numberFormat.format(UnitConverter.KmToMiles(lLastCardioRecord.getDistance())));
                else
                    distanceEdit.setText(numberFormat.format(lLastCardioRecord.getDistance()));
            } else if (lLastRecord.getType() == DAOMachine.TYPE_STATIC) {
                StaticExercise lLastStaticRecord = (StaticExercise) lLastRecord;
                seriesEdit.setText(String.valueOf(lLastStaticRecord.getSerie()));
                secondsEdit.setText(String.valueOf(lLastStaticRecord.getSecond()));
                unitSpinner.setSelection(lLastStaticRecord.getUnit());
                DecimalFormat numberFormat = new DecimalFormat("#.##");
                if (lLastStaticRecord.getUnit() == UnitConverter.UNIT_LBS)
                    poidsEdit.setText(numberFormat.format(UnitConverter.KgtoLbs(lLastStaticRecord.getPoids())));
                else
                    poidsEdit.setText(numberFormat.format(lLastStaticRecord.getPoids()));
            }
        }
    }

    /*  */
    private void updateRecordTable(String pMachine) {
        // Informe l'activité de la machine courante
        this.getMainActivity().setCurrentMachine(pMachine);
        if (getView()==null) return;
        getView().post(() -> {

            Cursor c;
            Cursor oldCursor;

            IRecord r = mDb.getLastRecord(getProfil());

            //Get results
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

    @SuppressLint("SetTextI18n")
    private void refreshData() {
        View fragmentView = getView();
        if (fragmentView != null) {
            if (getProfil() != null) {
                mDb.setProfile(getProfil());
                ArrayList<ExerciseInProgram> exerciseInProgramArrayList;
                exerciseInProgramArrayList = mDb.getAllExerciseInProgramArray();
                /* Init machines list*/
                ProgramInExerciseArrayFullAdapter exerciseArrayFullAdapter = new ProgramInExerciseArrayFullAdapter(getContext(), exerciseInProgramArrayList);
                machineEdit.setAdapter(exerciseArrayFullAdapter);

                if (machineEdit.getText().toString().isEmpty()) {
                    IRecord lLastRecord = mDb.getLastRecord(getProfil());
                    if (lLastRecord != null) {
                        // Last recorded exercise
                        setCurrentMachine(lLastRecord.getExercise());
                    } else {
                        // Default Values
                        machineEdit.setText("");
                        seriesEdit.setText("1");
                        repetitionEdit.setText("10");
                        secondsEdit.setText("60");
                        poidsEdit.setText("50");
                        distanceEdit.setText("1");
                        durationEdit.setText("00:10:00");
                        setCurrentMachine("");
                        changeExerciseTypeUI(DAOMachine.TYPE_FONTE, true);
                    }
                } else { // Restore on fragment restore.
                    setCurrentMachine(machineEdit.getText().toString());
                }
                // Set Table
                updateRecordTable(machineEdit.getText().toString());
            }
        }
    }

    private void showExerciseTypeSelector(boolean displaySelector) {
        if (displaySelector) exerciseTypeSelectorLayout.setVisibility(View.VISIBLE);
        else exerciseTypeSelectorLayout.setVisibility(View.GONE);
    }

    private void changeExerciseTypeUI(int pType, boolean displaySelector) {
        showExerciseTypeSelector(displaySelector);
        switch (pType) {
            case DAOMachine.TYPE_CARDIO:
                cardioSelector.setBackgroundColor(getResources().getColor(R.color.record_background_odd));
                bodybuildingSelector.setBackgroundColor(getResources().getColor(R.color.background));
                staticExerciseSelector.setBackgroundColor(getResources().getColor(R.color.background));
                seriesCardView.setVisibility(View.GONE);
                repetitionCardView.setVisibility(View.GONE);
                weightCardView.setVisibility(View.GONE);
                secondsCardView.setVisibility(View.GONE);
                restTimeLayout.setVisibility(View.GONE);
                distanceCardView.setVisibility(View.VISIBLE);
                durationCardView.setVisibility(View.VISIBLE);
                selectedType = DAOMachine.TYPE_CARDIO;
                break;
            case DAOMachine.TYPE_STATIC:
                cardioSelector.setBackgroundColor(getResources().getColor(R.color.background));
                bodybuildingSelector.setBackgroundColor(getResources().getColor(R.color.background));
                staticExerciseSelector.setBackgroundColor(getResources().getColor(R.color.record_background_odd));
                seriesCardView.setVisibility(View.VISIBLE);
                repetitionCardView.setVisibility(View.GONE);
                secondsCardView.setVisibility(View.VISIBLE);
                weightCardView.setVisibility(View.VISIBLE);
                restTimeLayout.setVisibility(View.VISIBLE);
                distanceCardView.setVisibility(View.GONE);
                durationCardView.setVisibility(View.GONE);
                selectedType = DAOMachine.TYPE_STATIC;
                break;
            case DAOMachine.TYPE_FONTE:
            default:
                cardioSelector.setBackgroundColor(getResources().getColor(R.color.background));
                bodybuildingSelector.setBackgroundColor(getResources().getColor(R.color.record_background_odd));
                staticExerciseSelector.setBackgroundColor(getResources().getColor(R.color.background));
                seriesCardView.setVisibility(View.VISIBLE);
                repetitionCardView.setVisibility(View.VISIBLE);
                secondsCardView.setVisibility(View.GONE);
                weightCardView.setVisibility(View.VISIBLE);
                restTimeLayout.setVisibility(View.VISIBLE);
                distanceCardView.setVisibility(View.GONE);
                durationCardView.setVisibility(View.GONE);
                selectedType = DAOMachine.TYPE_FONTE;
        }
    }

    private void saveSharedParams() {

        SharedPreferences sharedPref = Objects.requireNonNull(getActivity()).getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("restTime", restTimeEdit.getText().toString());
        editor.putBoolean("restCheck", restTimeCheck.isChecked());
        editor.putBoolean("showDetails", this.detailsLayout.isShown());
        editor.apply();
    }

    private void restoreSharedParams() {
        SharedPreferences sharedPref = Objects.requireNonNull(getActivity()).getPreferences(Context.MODE_PRIVATE);
        restTimeEdit.setText(sharedPref.getString("restTime", ""));
        restTimeCheck.setChecked(sharedPref.getBoolean("restCheck", true));
   }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden)
            refreshData();
    }

    private void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =
            (InputMethodManager) Objects.requireNonNull(Objects.requireNonNull(getActivity()).getSystemService(Activity.INPUT_METHOD_SERVICE));
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
