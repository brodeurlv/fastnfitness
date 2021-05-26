package com.easyfitness.machines;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.DAO.Profile;
import com.easyfitness.MainActivity;
import com.easyfitness.R;
import com.easyfitness.enums.ExerciseType;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MachineFragment extends Fragment {

    ListView machineList = null;
    Button addButton = null;
    AutoCompleteTextView searchField = null;
    MachineCursorAdapter mTableAdapter;
    ImageButton filterButton = null;
    private CharSequence filterText;
    private boolean[] checkedFilterItems = {true, true, true};
    private ArrayList<ExerciseType> selectedTypes = new ArrayList();
    private DAOMachine mDbMachine = null;
    private AlertDialog machineFilterDialog;

    private final OnItemClickListener onClickListItem = (parent, view, position, id) -> {
        // Get Machine Name selected
        TextView textViewID = view.findViewById(R.id.LIST_MACHINE_ID);
        long machineId = Long.parseLong(textViewID.getText().toString());

        ExerciseDetailsPager machineDetailsFragment = ExerciseDetailsPager.newInstance(machineId, ((MainActivity) getActivity()).getCurrentProfile().getId());
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, machineDetailsFragment, MainActivity.MACHINESDETAILS);
        transaction.addToBackStack(null);
        // Commit the transaction
        transaction.commit();
    };
    private final View.OnClickListener clickAddButton = v -> {

        // create a temporarily exercise with name="" and open it like any other existing exercises
        long new_id = -1;


        SweetAlertDialog dlg = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(getString(R.string.what_type_of_exercise))
                .setContentText("")
                .setCancelText(getResources().getText(R.string.CardioLabel).toString())
                .setConfirmText(getResources().getText(R.string.strength_category).toString())
                .setNeutralText(getResources().getText(R.string.staticExercise).toString())
                .showCancelButton(true)
                .setConfirmClickListener(sDialog -> {
                    String pMachine = "";
                    DAOMachine lDAOMachine = new DAOMachine(getContext());
                    long temp_machine_key = lDAOMachine.addMachine(pMachine, "", ExerciseType.STRENGTH, "", false, "");
                    sDialog.dismissWithAnimation();

                    ExerciseDetailsPager machineDetailsFragment = ExerciseDetailsPager.newInstance(temp_machine_key, ((MainActivity) getActivity()).getCurrentProfile().getId());
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    // Replace whatever is in the fragment_container view with this fragment,
                    // and add the transaction to the back stack so the user can navigate back
                    transaction.replace(R.id.fragment_container, machineDetailsFragment, MainActivity.MACHINESDETAILS);
                    transaction.addToBackStack(null);
                    // Commit the transaction
                    transaction.commit();
                })
                .setNeutralClickListener(sDialog -> {
                    String pMachine = "";
                    DAOMachine lDAOMachine = new DAOMachine(getContext());
                    long temp_machine_key = lDAOMachine.addMachine(pMachine, "", ExerciseType.ISOMETRIC, "", false, "");
                    sDialog.dismissWithAnimation();

                    ExerciseDetailsPager machineDetailsFragment = ExerciseDetailsPager.newInstance(temp_machine_key, ((MainActivity) getActivity()).getCurrentProfile().getId());
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    // Replace whatever is in the fragment_container view with this fragment,
                    // and add the transaction to the back stack so the user can navigate back
                    transaction.replace(R.id.fragment_container, machineDetailsFragment, MainActivity.MACHINESDETAILS);
                    transaction.addToBackStack(null);
                    // Commit the transaction
                    transaction.commit();
                })
                .setCancelClickListener(sDialog -> {
                    String pMachine = "";
                    DAOMachine lDAOMachine = new DAOMachine(getContext());
                    long temp_machine_key = lDAOMachine.addMachine(pMachine, "", ExerciseType.CARDIO, "", false, "");
                    sDialog.dismissWithAnimation();

                    ExerciseDetailsPager machineDetailsFragment = ExerciseDetailsPager.newInstance(temp_machine_key, ((MainActivity) getActivity()).getCurrentProfile().getId());
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    // Replace whatever is in the fragment_container view with this fragment,
                    // and add the transaction to the back stack so the user can navigate back
                    transaction.replace(R.id.fragment_container, machineDetailsFragment, MainActivity.MACHINESDETAILS);
                    transaction.addToBackStack(null);
                    // Commit the transaction
                    transaction.commit();
                });

        dlg.show();

        dlg.getButton(SweetAlertDialog.BUTTON_CONFIRM).setBackgroundResource(R.color.record_background_odd);
        dlg.getButton(SweetAlertDialog.BUTTON_CONFIRM).setPadding(0, 0, 0, 0);
        dlg.getButton(SweetAlertDialog.BUTTON_CONFIRM).setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dlg.getButton(SweetAlertDialog.BUTTON_CONFIRM).setAutoSizeTextTypeUniformWithConfiguration(8, 12, 1, TypedValue.COMPLEX_UNIT_SP);
        }
        dlg.getButton(SweetAlertDialog.BUTTON_CANCEL).setBackgroundResource(R.color.record_background_odd);
        dlg.getButton(SweetAlertDialog.BUTTON_CANCEL).setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        dlg.getButton(SweetAlertDialog.BUTTON_CANCEL).setPadding(0, 0, 0, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dlg.getButton(SweetAlertDialog.BUTTON_CANCEL).setAutoSizeTextTypeUniformWithConfiguration(8, 12, 1, TypedValue.COMPLEX_UNIT_SP);
        }

        dlg.getButton(SweetAlertDialog.BUTTON_NEUTRAL).setBackgroundResource(R.color.record_background_odd);
        dlg.getButton(SweetAlertDialog.BUTTON_NEUTRAL).setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        dlg.getButton(SweetAlertDialog.BUTTON_NEUTRAL).setPadding(0, 0, 0, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dlg.getButton(SweetAlertDialog.BUTTON_NEUTRAL).setAutoSizeTextTypeUniformWithConfiguration(8, 12, 1, TypedValue.COMPLEX_UNIT_SP);
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
                    refreshData();
                }
            });
            builder.setNegativeButton("Cancel", null);
            machineFilterDialog = builder.create();
            machineFilterDialog.show();
        }
    };

    public TextWatcher onTextChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            filterText = charSequence;
            if (charSequence.length() == 0) {
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

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static MachineFragment newInstance(String name, int id) {
        MachineFragment f = new MachineFragment();

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
        filterText = "";

        selectedTypes.add(ExerciseType.CARDIO);
        selectedTypes.add(ExerciseType.ISOMETRIC);
        selectedTypes.add(ExerciseType.STRENGTH);

        // activates onCreateOptionsMenu in this fragment
        setHasOptionsMenu(true);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.tab_machine, container, false);

        addButton = view.findViewById(R.id.addExercise);
        addButton.setOnClickListener(clickAddButton);

        filterButton = view.findViewById(R.id.buttonFilterListMachine);
        filterButton.setOnClickListener(clickFilterButton);

        searchField = view.findViewById(R.id.searchField);
        searchField.addTextChangedListener(onTextChangeListener);

        machineList = view.findViewById(R.id.listMachine);
        machineList.setOnItemClickListener(onClickListItem);

        // Initialisation de l'historique
        mDbMachine = new DAOMachine(getContext());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mDbMachine.deleteAllEmptyExercises();
        refreshData();

        // for resetting the search field at the start:
        searchField.setText("");

    }

    public String getName() {
        return getArguments().getString("name");
    }

    public MachineFragment getThis() {
        return this;
    }

    private void refreshData() {
        Cursor c = null;
        Cursor oldCursor = null;

        View fragmentView = getView();
        if (fragmentView != null) {
            if (getProfil() != null) {

                // Version avec table Machine
                if (filterText.length() == 0) {
                    c = mDbMachine.getAllMachines(selectedTypes);
                } else {
                    c = mDbMachine.getFilteredMachines(filterText, selectedTypes);
                }

                if (c == null || c.getCount() == 0) {
                    //Toast.makeText(getActivity(), "No records", Toast.LENGTH_SHORT).show();
                    machineList.setAdapter(null);
                } else {
                    if (machineList.getAdapter() == null) {
                        mTableAdapter = new MachineCursorAdapter(getActivity(), c, 0, mDbMachine);
                        machineList.setAdapter(mTableAdapter);
                    } else {
                        mTableAdapter = (MachineCursorAdapter) machineList.getAdapter();
                        oldCursor = mTableAdapter.swapCursor(c);
                        if (oldCursor != null) oldCursor.close();
                    }

                    mTableAdapter.setFilterQueryProvider(constraint -> mDbMachine.getFilteredMachines(constraint, selectedTypes));
                }
            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) refreshData();
    }

    private Profile getProfil() {
        return ((MainActivity) getActivity()).getCurrentProfile();
    }

}
