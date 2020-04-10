package com.easyfitness.machines;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.easyfitness.DAO.DAOFonte;
import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.DAO.Profile;
import com.easyfitness.MainActivity;
import com.easyfitness.R;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MachineFragment extends Fragment {
    ListView machineList = null;
    Button addButton = null;
    AutoCompleteTextView searchField = null;
    MachineCursorAdapter mTableAdapter;

    private DAOMachine mDbMachine = null;
    public TextWatcher onTextChangeListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            if (charSequence.length() == 0) {
//                mTableAdapter.notifyDataSetChanged();
//                mTableAdapter = ((MachineCursorAdapter) machineList.getAdapter());
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
        TextView textViewID = view.findViewById(R.id.LIST_MACHINE_ID);
        long machineId = Long.valueOf(textViewID.getText().toString());

        ExerciseDetailsPager machineDetailsFragment = ExerciseDetailsPager.newInstance(machineId, ((MainActivity) getActivity()).getCurrentProfile().getId());
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


        SweetAlertDialog dlg = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
            .setTitleText(getString(R.string.what_type_of_exercise))
            .setContentText("")
            .setCancelText(getResources().getText(R.string.CardioLabel).toString())
            .setConfirmText(getResources().getText(R.string.strength_category).toString())
            .setNeutralText(getResources().getText(R.string.staticExercise).toString())
            .showCancelButton(true)
            .setConfirmClickListener(sDialog -> {
                long temp_machine_key = -1;
                String pMachine = "";
                DAOMachine lDAOMachine = new DAOMachine(getContext());
                temp_machine_key = lDAOMachine.addMachine(pMachine, "", DAOMachine.TYPE_FONTE, "", false,"");
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
                long temp_machine_key = -1;
                String pMachine = "";
                DAOMachine lDAOMachine = new DAOMachine(getContext());
                temp_machine_key = lDAOMachine.addMachine(pMachine, "", DAOMachine.TYPE_STATIC, "", false, "");
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
                long temp_machine_key = -1;
                String pMachine = "";
                DAOMachine lDAOMachine = new DAOMachine(getContext());
                temp_machine_key = lDAOMachine.addMachine(pMachine, "", DAOMachine.TYPE_CARDIO, "", false, "");
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

        // activates onCreateOptionsMenu in this fragment
        setHasOptionsMenu(true);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.tab_machine, container, false);

        addButton = view.findViewById(R.id.addExercise);
        addButton.setOnClickListener(clickAddButton);

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
                c = mDbMachine.getAllMachines();
                if (c == null || c.getCount() == 0) {
                    //Toast.makeText(getActivity(), "No records", Toast.LENGTH_SHORT).show();
                    machineList.setAdapter(null);
                } else {
                    if (machineList.getAdapter() == null) {
                        mTableAdapter = new MachineCursorAdapter(getActivity(), c, 0, mDbMachine);
                        machineList.setAdapter(mTableAdapter);
                    } else {
                        mTableAdapter = ((MachineCursorAdapter) machineList.getAdapter());
                        oldCursor = mTableAdapter.swapCursor(c);
                        if (oldCursor != null) oldCursor.close();
                    }

                    mTableAdapter.setFilterQueryProvider(constraint -> mDbMachine.getFilteredMachines(constraint));
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
