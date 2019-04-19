package com.easyfitness;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;

import com.easyfitness.DAO.Cardio;
import com.easyfitness.DAO.DAOCardio;
import com.easyfitness.DAO.IRecord;
import com.easyfitness.DAO.Profile;
import com.easyfitness.fonte.RecordCursorAdapter;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.ExpandedListView;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import cn.pedant.SweetAlert.SweetAlertDialog;

@SuppressLint("ValidFragment")
public class CardioFragment extends Fragment {
    DatePickerDialogFragment mDateFrag = null;
    TimePickerDialogFragment mDurationFrag = null;
    MainActivity mActivity = null;
    EditText dateEdit = null;
    AutoCompleteTextView exerciceEdit = null;
    EditText distanceEdit = null;
    EditText durationEdit = null;
    public TimePickerDialog.OnTimeSetListener timeSet = (view, hourOfDay, minute) -> {
        // Do something with the time chosen by the user
        String strMinute = "00";
        String strHour = "00";

        if (minute < 10) strMinute = "0" + Integer.toString(minute);
        else strMinute = Integer.toString(minute);
        if (hourOfDay < 10) strHour = "0" + Integer.toString(hourOfDay);
        else strHour = Integer.toString(hourOfDay);

        String date = strHour + ":" + strMinute;
        durationEdit.setText(date);
        hideKeyboard(durationEdit);
    };
    Button addButton = null;
    Button chronoButton = null;
    Button paramButton = null;
    ExpandedListView recordList = null;
    String[] exerciceListArray = null;
    ImageButton exerciceListButton = null;
    ImageButton launchChronoButton = null;
    private String name;
    private int id;
    private DAOCardio mDb = null;
    private BtnClickListener itemClickDeleteRecord = this::showDeleteDialog;
    private OnClickListener clickAddButton = v -> {
        /* Reagir au clic pour les boutons 1 et 2*/

        // Verifie que les infos sont completes
        if (dateEdit.getText().toString().isEmpty() ||
            exerciceEdit.getText().toString().isEmpty() ||
            (distanceEdit.getText().toString().isEmpty() &&
                durationEdit.getText().toString().isEmpty())) {
            return;
        }

        Date date;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            date = dateFormat.parse(dateEdit.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
            date = new Date();
        }

        long duration;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
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
            distance = Float.parseFloat(distanceEdit.getText().toString());
        }

        mDb.addCardioRecord(date, "00:00",
            exerciceEdit.getText().toString(),
            distance,
            duration,
            getProfil());

        getActivity().findViewById(R.id.drawer_layout).requestFocus();

        FillRecordTable(exerciceEdit.getText().toString());

        /* Reinitialisation des machines */
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getView().getContext(),
            android.R.layout.simple_dropdown_item_1line, mDb.getAllMachines(getProfil()));
        exerciceEdit.setAdapter(adapter);
    };
    private OnClickListener onClickMachineList = v -> {
        exerciceListArray = mDb.getAllMachines(getProfil());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select a Machine")
            .setItems(exerciceListArray, (dialog, which) -> {
                exerciceEdit.setText(exerciceListArray[which]); // Met a jour le text
                FillRecordTable(exerciceListArray[which]); // Met a jour le tableau
                //((ViewGroup) machineEdit.getParent()).requestFocus(); //Permet de reactiver le clavier lors du focus sur l'editText
            });
        //builder.create();
        builder.show();
    };
    private OnItemLongClickListener itemlongclickDeleteRecord = (listView, view, position, id) -> {

        // Get the cursor, positioned to the corresponding row in the result set
        //Cursor cursor = (Cursor) listView.getItemAtPosition(position);

        //Log.v("long clicked", "pos: " + position + " id: " + id);

        mDb.deleteRecord(id);

        //listView.removeViewInLayout(view);

        FillRecordTable(exerciceEdit.getText().toString());

        KToast.infoToast(getActivity(), getActivity().getResources().getText(R.string.removedid).toString() + " " + id, Gravity.BOTTOM, KToast.LENGTH_SHORT);

        return true;
    };
    private OnItemClickListener onItemClickFilterList = (parent, view, position, id) -> FillRecordTable(exerciceEdit.getText().toString());
    private DatePickerDialog.OnDateSetListener dateSet = (view, year, month, day) -> {
        dateEdit.setText(DateConverter.dateToString(year, month + 1, day));
        hideKeyboard(dateEdit);
    };
    private OnClickListener clickDateEdit = v -> {
        switch (v.getId()) {
            case R.id.editCardioDate:
                showDatePicker();
                break;
            case R.id.editDuration:
                showTimePicker();
                break;
        }
    };
    private OnFocusChangeListener touchRazEdit = (v, hasFocus) -> {
        if (hasFocus) {
            switch (v.getId()) {
                case R.id.editCardioDate:
                    showDatePicker();
                    break;
                case R.id.editDuration:
                    showTimePicker();
                    break;
                case R.id.editSerie:
                    distanceEdit.setText("");
                    break;
                case R.id.editPoids:
                    durationEdit.setText("");
                    break;
                case R.id.editMachine:
                    ////InputMethodManager imm = getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    //imm.showSoftInput(machineEdit, InputMethodManager.SHOW_IMPLICIT);
                    exerciceEdit.setText("");
                    //machineEdit.set.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                    break;
            }
        } else {
            switch (v.getId()) {
                case R.id.editMachine:
                    FillRecordTable(exerciceEdit.getText().toString());
                    break;
            }
        }
    };

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static CardioFragment newInstance(String name, int id) {
        CardioFragment f = new CardioFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putInt("id", id);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = (MainActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.tab_cardio, container, false);

        dateEdit = view.findViewById(R.id.editCardioDate);
        exerciceEdit = view.findViewById(R.id.editExercice);
        distanceEdit = view.findViewById(R.id.editDistance);
        durationEdit = view.findViewById(R.id.editDuration);
        recordList = view.findViewById(R.id.listCardioRecord);
        exerciceListButton = view.findViewById(R.id.buttonListExercice);
        launchChronoButton = view.findViewById(R.id.buttonLaunchChrono);
        addButton = view.findViewById(R.id.addExercice);

        /* Initialisation des boutons */
        addButton.setOnClickListener(clickAddButton);
        exerciceListButton.setOnClickListener(onClickMachineList);
        //launchChronoButton.setOnClickListener();

        dateEdit.setOnClickListener(clickDateEdit);
        dateEdit.setOnFocusChangeListener(touchRazEdit);
        distanceEdit.setOnFocusChangeListener(touchRazEdit);
        durationEdit.setOnClickListener(clickDateEdit);
        durationEdit.setOnFocusChangeListener(touchRazEdit);
        exerciceEdit.setOnFocusChangeListener(touchRazEdit);
        exerciceEdit.setOnItemClickListener(onItemClickFilterList);
        recordList.setOnItemLongClickListener(itemlongclickDeleteRecord);

        // Initialisation de la base de donnee
        mDb = new DAOCardio(view.getContext());

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        refreshData();
    }

    public String getName() {
        return getArguments().getString("name");
    }

    public int getFragmentId() {
        return getArguments().getInt("id", 0);
    }

    public DAOCardio getDB() {
        return mDb;
    }

    public Fragment getFragment() {
        return this;
    }

    private Profile getProfil() {
        return mActivity.getCurrentProfil();
    }

    /*  */
    private void FillRecordTable(String pMachines) {

        List<Cardio> records = null;

        // Recupere les valeurs
        if (pMachines == null || pMachines.isEmpty()) {
            records = mDb.getAllCardioRecordsByProfile(getProfil());
        } else {
            records = mDb.getAllCardioRecordByMachines(getProfil(), pMachines);
        }

        if (records.isEmpty()) {
            //Toast.makeText(getActivity(), "No records", Toast.LENGTH_SHORT).show();
            recordList.setAdapter(null);
        } else {
            // ...
            RecordCursorAdapter mTableAdapter = new RecordCursorAdapter(this.getView().getContext(), mDb.getCursor(), 0, itemClickDeleteRecord, null);
            mTableAdapter.setFirstColorOdd(records.size() % 2);
            recordList.setAdapter(mTableAdapter);
        }
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

                FillRecordTable(exerciceEdit.getText().toString());

                //Toast.makeText(getContext(), getResources().getText(R.string.removedid) + " " + idToDelete, Toast.LENGTH_SHORT).show();
                // Info
                KToast.infoToast(getActivity(), getResources().getText(R.string.removedid).toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                sDialog.dismissWithAnimation();
            })
            .show();
    }

    private void showDatePicker() {
        if (mDateFrag == null) {
            mDateFrag = DatePickerDialogFragment.newInstance(dateSet);
            mDateFrag.show(getActivity().getFragmentManager().beginTransaction(), "dialog_date");
        } else {
            if (!mDateFrag.isVisible())
                mDateFrag.show(getActivity().getFragmentManager().beginTransaction(), "dialog_date");
        }
    }

    private void showTimePicker() {
        if (mDurationFrag == null) {
            mDurationFrag = TimePickerDialogFragment.newInstance(timeSet);
            mDurationFrag.show(getActivity().getFragmentManager().beginTransaction(), "dialog_time");
        } else {
            if (!mDurationFrag.isVisible())
                mDurationFrag.show(getActivity().getFragmentManager().beginTransaction(), "dialog_time");
        }
    }

    private void refreshData() {
        View fragmentView = getView();
        if (fragmentView != null) {
            if (getProfil() != null) {
                mDb.setProfile(getProfil());

                exerciceListArray = mDb.getAllMachines(getProfil());

                // Set Initial text
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

                Date date = new Date();
                dateEdit.setText(dateFormat.format(date));

                /* Initialisation serie */
                IRecord lLastRecord = mDb.getLastRecord(getProfil());
                if (lLastRecord != null) {
                    exerciceEdit.setText(lLastRecord.getExercise());
                    distanceEdit.setText("");
                    durationEdit.setText("");
                } else {
                    // valeur par defaut
                    exerciceEdit.setText("");
                    distanceEdit.setText("");
                    durationEdit.setText("");
                }

                // Set Table
                FillRecordTable(exerciceEdit.getText().toString());

                /* Init machines list*/
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getView().getContext(),
                    android.R.layout.simple_dropdown_item_1line, exerciceListArray);
                exerciceEdit.setAdapter(adapter);
            }
        }
    }

/*
    private void showDeleteDialog(final long idToDelete) {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    mDb.delete(idToDelete);

                    FillRecordTable();

                    Toast.makeText(mActivity, getResources().getText(R.string.removedid) + " " + idToDelete, Toast.LENGTH_SHORT)
                        .show();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getResources().getText(R.string.DeleteRecordDialog)).setPositiveButton(getResources().getText(R.string.global_yes), dialogClickListener)
            .setNegativeButton(getResources().getText(R.string.global_no), dialogClickListener).show();
    }
*/

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) refreshData();
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

} 

