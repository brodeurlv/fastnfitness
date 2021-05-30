package com.easyfitness.programs;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.easyfitness.AppViMo;
import com.easyfitness.DAO.program.DAOProgram;
import com.easyfitness.DAO.program.DAOProgramHistory;
import com.easyfitness.DAO.program.Program;
import com.easyfitness.MainActivity;
import com.easyfitness.R;
import com.easyfitness.views.EditableInputView;
import com.onurkaganaldemir.ktoastlib.KToast;


public class ProgramInfoFragment extends Fragment {
    private EditableInputView descriptionEdit = null;
    private EditableInputView nameEdit = null;
    private ListView historyListView;

    private MainActivity mActivity = null;
    private DAOProgram daoProgram = null;
    private DAOProgramHistory daoProgramHistory = null;
    private Program mProgram;
    private final EditableInputView.OnTextChangedListener itemOnTextChange = this::requestForSave;
    private AppViMo appViMo;

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static ProgramInfoFragment newInstance(String name, int templateId) {
        ProgramInfoFragment f = new ProgramInfoFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putInt("templateId", templateId);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.tab_program_details, container, false);

        nameEdit = view.findViewById(R.id.workout_name);
        descriptionEdit = view.findViewById(R.id.workout_description);
        historyListView = view.findViewById(R.id.listProgramHistory);

        long workoutID = getArguments().getLong("templateId", -1);

        daoProgram = new DAOProgram(getContext());
        daoProgramHistory = new DAOProgramHistory(getContext());
        mProgram = daoProgram.get(workoutID);
        nameEdit.setText(mProgram.getName());
        descriptionEdit.setText(mProgram.getDescription());

        appViMo = new ViewModelProvider(requireActivity()).get(AppViMo.class);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        nameEdit.setOnTextChangeListener(itemOnTextChange);
        descriptionEdit.setOnTextChangeListener(itemOnTextChange);

        this.getView().post(() -> {
            Cursor cursor = daoProgramHistory.getFilteredHistoryCursor(mProgram.getId(), appViMo.getProfile().getValue().getId());
            historyListView.setAdapter(new ProgramHistoryCursorAdapter(this.getContext(), cursor, 0, daoProgramHistory));
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mActivity = (MainActivity) context;
    }

    public String getName() {
        return getArguments().getString("name");
    }

    private void requestForSave(View view) {
        boolean toUpdate = false;

        // Save all the fields in the Profile
        switch (view.getId()) {
            case R.id.workout_name:
                mProgram.setName(nameEdit.getText());
                toUpdate = true;
                break;
            case R.id.workout_description:
                mProgram.setDescription(descriptionEdit.getText());
                toUpdate = true;
                break;
        }

        if (toUpdate) {
            daoProgram.update(mProgram);
            KToast.infoToast(getActivity(), mProgram.getName() + " updated", Gravity.BOTTOM, KToast.LENGTH_SHORT);
        }
    }

    public Fragment getFragment() {
        return this;
    }
}
