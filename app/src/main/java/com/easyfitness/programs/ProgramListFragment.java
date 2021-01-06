package com.easyfitness.programs;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.program.DAOProgram;
import com.easyfitness.DAO.program.Program;
import com.easyfitness.MainActivity;
import com.easyfitness.R;
import com.easyfitness.utils.Keyboard;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ProgramListFragment extends Fragment {
    private final View.OnClickListener clickAddButton = v -> {
        final EditText editText = new EditText(getContext());
        editText.setText("");
        editText.setGravity(Gravity.CENTER);
        editText.requestFocus();

        LinearLayout linearLayout = new LinearLayout(getContext().getApplicationContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(editText);

        final SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(getString(R.string.enter_workout_name))
                .setCancelText(getContext().getString(android.R.string.cancel))
                .setHideKeyBoardOnDismiss(true)
                .setCancelClickListener(sDialog -> {
                    editText.clearFocus();
                    Keyboard.hide(getContext(), editText);
                    sDialog.dismissWithAnimation();
                })
                .setConfirmClickListener(sDialog -> {

                    editText.clearFocus();
                    Keyboard.hide(getContext(), editText);
                    DAOProgram daoProgram = new DAOProgram(getContext());
                    long temp_key = daoProgram.add(new Program(0, editText.getText().toString(), ""));

                    sDialog.dismiss();
                    ProgramPagerFragment fragment = ProgramPagerFragment.newInstance(temp_key);
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    // Replace whatever is in the fragment_container view with this fragment,
                    // and add the transaction to the back stack so the user can navigate back
                    transaction.replace(R.id.fragment_container, fragment, MainActivity.WORKOUTPAGER);
                    transaction.addToBackStack(null);
                    // Commit the transaction
                    transaction.commit();
                });
        //Keyboard.hide(context, editText);});
        dialog.setOnShowListener(sDialog -> {
            editText.requestFocus();
            Keyboard.show(getContext(), editText);
        });

        dialog.setCustomView(linearLayout);
        dialog.show();
    };

    private final OnItemClickListener onClickListItem = (parent, view, position, id) -> {

        TextView textView = view.findViewById(R.id.LIST_WORKOUT_ID);
        long ID = Long.parseLong(textView.getText().toString());

        ProgramPagerFragment fragment = ProgramPagerFragment.newInstance(ID);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, fragment, MainActivity.WORKOUTPAGER);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    };
    ArrayList<Program> dataModels;
    ListView measureList = null;
    private ProgramListAdapter mListAdapter;
    private Button addButton;
    private DAOProgram mDb;

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static ProgramListFragment newInstance(String name, int id) {
        ProgramListFragment f = new ProgramListFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putInt("id", id);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            mDb = new DAOProgram(this.getContext());
            dataModels = new ArrayList<>();
            mListAdapter = new ProgramListAdapter(dataModels, getContext());
            mListAdapter.setProfile(getProfile());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.tab_program_list, container, false);


        if (savedInstanceState == null) {
            addButton = view.findViewById(R.id.newWorkout);
            addButton.setOnClickListener(clickAddButton);

            measureList = view.findViewById(R.id.listWorkout);
            // Initialisation des evenements
            measureList.setOnItemClickListener(onClickListItem);
            measureList.setAdapter(mListAdapter);
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        mDb.deleteAllEmptyWorkout();
        refreshData();
    }

    private void refreshData() {
        if (dataModels == null) {
            dataModels = new ArrayList<>();
        }

        dataModels.clear();

        List<Program> lList = mDb.getAll();
        dataModels.addAll(lList);


        if (mListAdapter == null) {
            mListAdapter = new ProgramListAdapter(dataModels, getContext());
            mListAdapter.setProfile(getProfile());
            measureList.setAdapter(mListAdapter);
        } else {
            mListAdapter.notifyDataSetChanged();
        }
    }

    private Profile getProfile() {
        return ((MainActivity) getActivity()).getCurrentProfile();
    }

}
