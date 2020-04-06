package com.easyfitness.programs;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.easyfitness.DAO.DAOProgram;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.Program;
import com.easyfitness.MainActivity;
import com.easyfitness.R;
import com.easyfitness.machines.MachineCursorAdapter;
//import com.easyfitness.machines.ExerciseDetailsPager;
import java.util.ArrayList;
import androidx.fragment.app.Fragment;
//import androidx.fragment.app.FragmentTransaction;

public class ProgramsFragment extends Fragment {
    final int addId = 555;  //for add Programs menu
    private ListView machineList = null;
    private Button addButton = null;
    private AutoCompleteTextView searchField = null;
    private ProgramCursorAdapter mTableAdapter;

    private DAOProgram mDbMachine = null;
    public TextWatcher onTextChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            if (charSequence.length() == 0) {
                mTableAdapter.notifyDataSetChanged();
                mTableAdapter = ((ProgramCursorAdapter) machineList.getAdapter());
                refreshData();
            } else {
                if (mTableAdapter != null) {
                        mTableAdapter.getFilter().filter(charSequence);
                        mTableAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };
    private OnItemClickListener onClickListItem = (parent, view, position, id) -> {
        // Get Machine Name selected
        TextView textViewID = view.findViewById(R.id.LIST_Program_ID);
        long machineId = Long.parseLong(textViewID.getText().toString());

        ProgramDetailsPager machineDetailsFragment = ProgramDetailsPager.newInstance(machineId, ((MainActivity) Objects.requireNonNull(getActivity())).getCurrentProfil().getId());
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, machineDetailsFragment, MainActivity.MACHINESDETAILS);
        transaction.addToBackStack(null);
        // Commit the transaction
        transaction.commit();
    };
    private View.OnClickListener clickAddButton = v -> {

        // create a temporarily exercise with name="" and open it like any other existing exercises
        long new_id = -1;
        String programName = "Prog2";

//        DAOMachine lDAOProgram = new DAOMachine(getContext());
//                long temp_machine_key = lDAOProgram.addMachine(pMachine,"",);
                DAOProgram lDAOProgram = new DAOProgram(getContext());
                long temp_machine_key = lDAOProgram.addRecord(programName);

//        ProgramDetailsPager machineDetailsFragment = ProgramDetailsPager.newInstance(temp_machine_key, ((MainActivity) getActivity()).getCurrentProfil().getId());
//                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
//                // Replace whatever is in the fragment_container view with this fragment,
//                // and add the transaction to the back stack so the user can navigate back
//                transaction.replace(R.id.fragment_container, machineDetailsFragment, MainActivity.MACHINESDETAILS);
//                transaction.addToBackStack(null);
//                // Commit the transaction
//                transaction.commit();

//        SweetAlertDialog dlg = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
//            .setTitleText(getString(R.string.what_type_of_exercise))
//            .setContentText("")
//            .setCancelText(getResources().getText(R.string.CardioLabel).toString())
//            .setConfirmText(getResources().getText(R.string.FonteLabel).toString())
//            .setNeutralText("Static")
//            .showCancelButton(true)
//            .setConfirmClickListener(sDialog -> {
//                long temp_machine_key = -1;
//                String pMachine = "";
//                DAOProgram lDAOMachine = new DAOProgram(getContext());
//                temp_machine_key = lDAOMachine.addRecord(pMachine);
//                sDialog.dismissWithAnimation();
//
//                ExerciseDetailsPager machineDetailsFragment = ExerciseDetailsPager.newInstance(temp_machine_key, ((MainActivity) getActivity()).getCurrentProfil().getId());
//                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
//                // Replace whatever is in the fragment_container view with this fragment,
//                // and add the transaction to the back stack so the user can navigate back
//                transaction.replace(R.id.fragment_container, machineDetailsFragment, MainActivity.MACHINESDETAILS);
//                transaction.addToBackStack(null);
//                // Commit the transaction
//                transaction.commit();
//            })
//            .setNeutralClickListener(sDialog -> {
//                long temp_machine_key = -1;
//                String pMachine = "";
//                DAOProgram lDAOMachine = new DAOProgram(getContext());
//                temp_machine_key = lDAOMachine.addRecord(pMachine);
//                sDialog.dismissWithAnimation();
//
//                ExerciseDetailsPager machineDetailsFragment = ExerciseDetailsPager.newInstance(temp_machine_key, ((MainActivity) getActivity()).getCurrentProfil().getId());
//                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
//                // Replace whatever is in the fragment_container view with this fragment,
//                // and add the transaction to the back stack so the user can navigate back
//                transaction.replace(R.id.fragment_container, machineDetailsFragment, MainActivity.MACHINESDETAILS);
//                transaction.addToBackStack(null);
//                // Commit the transaction
//                transaction.commit();
//            })
//            .setCancelClickListener(sDialog -> {
//                long temp_machine_key = -1;
//                String pMachine = "";
//                DAOProgram lDAOMachine = new DAOProgram(getContext());
//                temp_machine_key = lDAOMachine.addRecord(pMachine);
//                sDialog.dismissWithAnimation();
//
//                ExerciseDetailsPager machineDetailsFragment = ExerciseDetailsPager.newInstance(temp_machine_key, ((MainActivity) getActivity()).getCurrentProfil().getId());
//                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
//                // Replace whatever is in the fragment_container view with this fragment,
//                // and add the transaction to the back stack so the user can navigate back
//                transaction.replace(R.id.fragment_container, machineDetailsFragment, MainActivity.MACHINESDETAILS);
//                transaction.addToBackStack(null);
//                // Commit the transaction
//                transaction.commit();
//            });
//
//        dlg.show();
//
//        dlg.getButton(SweetAlertDialog.BUTTON_CONFIRM).setBackgroundResource(R.color.record_background_odd);
//        dlg.getButton(SweetAlertDialog.BUTTON_CONFIRM).setPadding(0, 0, 0, 0);
//        dlg.getButton(SweetAlertDialog.BUTTON_CONFIRM).setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            dlg.getButton(SweetAlertDialog.BUTTON_CONFIRM).setAutoSizeTextTypeUniformWithConfiguration(8, 12, 1, TypedValue.COMPLEX_UNIT_SP);
//        }
//        dlg.getButton(SweetAlertDialog.BUTTON_CANCEL).setBackgroundResource(R.color.record_background_odd);
//        dlg.getButton(SweetAlertDialog.BUTTON_CANCEL).setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
//        dlg.getButton(SweetAlertDialog.BUTTON_CANCEL).setPadding(0, 0, 0, 0);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            dlg.getButton(SweetAlertDialog.BUTTON_CANCEL).setAutoSizeTextTypeUniformWithConfiguration(8, 12, 1, TypedValue.COMPLEX_UNIT_SP);
//        }
//
//        dlg.getButton(SweetAlertDialog.BUTTON_NEUTRAL).setBackgroundResource(R.color.record_background_odd);
//        dlg.getButton(SweetAlertDialog.BUTTON_NEUTRAL).setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
//        dlg.getButton(SweetAlertDialog.BUTTON_NEUTRAL).setPadding(0, 0, 0, 0);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            dlg.getButton(SweetAlertDialog.BUTTON_NEUTRAL).setAutoSizeTextTypeUniformWithConfiguration(8, 12, 1, TypedValue.COMPLEX_UNIT_SP);
//        }
    };
    private OnItemSelectedListener onItemSelectedList = new OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            //refreshData();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // activates onCreateOptionsMenu in this fragment
        setHasOptionsMenu(true);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.tab_machine, container, false);

        addButton = view.findViewById(R.id.addExercise);
        addButton.setOnClickListener(clickAddButton);

        searchField = view.findViewById(R.id.searchField);
        searchField.addTextChangedListener(onTextChangeListener);

        //typeList = view.findViewById(R.id.filterDate);
        //machineList = (Spinner) view.findViewById(R.id.filterMachine);
        //renameMachineButton = view.findViewById(R.id.imageMachineRename);
        machineList = view.findViewById(R.id.listMachine);
        //musclesList = view.findViewById(R.id.listFilterRecord);

        machineList.setOnItemClickListener(onClickListItem);
        mDbMachine = new DAOProgram(view.getContext());
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items

        switch (item.getItemId()) {
            case addId:
                clickAddButton.onClick(getView());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

//        mDbMachine.deleteAllEmptyExercises();//TODO not sure this is need
        refreshData();

        // for resetting the search field at the start:
        searchField.setText("");

        machineList.setOnItemSelectedListener(onItemSelectedList);
    }

    public String getName() {
        return getArguments().getString("name");
    }

    public ProgramsFragment getThis() {
        return this;
    }

    private void refreshData() {
        Cursor c;
        Cursor oldCursor;

        View fragmentView = getView();
        if (fragmentView != null) {
            if (getProfil() != null) {

                c = mDbMachine.getAllPrograms();
                if (c == null || c.getCount() <= 0) {
                    //Toast.makeText(getActivity(), "No records", Toast.LENGTH_SHORT).show();
                    machineList.setAdapter(null);
                } else {
                    if (machineList.getAdapter() == null) {
                        mTableAdapter = new ProgramCursorAdapter(getActivity(), c, 0, mDbMachine);
                        machineList.setAdapter(mTableAdapter);
                    } else {
                        mTableAdapter = ((ProgramCursorAdapter)machineList.getAdapter());
                        oldCursor = mTableAdapter.swapCursor(c);
                        if (oldCursor != null) oldCursor.close();
                    }

                    mTableAdapter.setFilterQueryProvider(constraint -> mDbMachine.getFilteredPrograms(constraint));
                }
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) refreshData();
    }

    private Profile getProfil() {
        return ((MainActivity) getActivity()).getCurrentProfil();
    }

}
