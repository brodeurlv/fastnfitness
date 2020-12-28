package com.easyfitness.programs;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.easyfitness.DAO.program.DAOProgram;
import com.easyfitness.DAO.program.Program;
import com.easyfitness.MainActivity;
import com.easyfitness.R;
import com.easyfitness.views.EditableInputView;
import com.onurkaganaldemir.ktoastlib.KToast;


public class ProgramInfoFragment extends Fragment {
    EditableInputView descriptionEdit = null;
    EditableInputView nameEdit = null;

    MainActivity mActivity = null;
    private DAOProgram mDb = null;
    private Program mProgram;
    private EditableInputView.OnTextChangedListener itemOnTextChange = this::requestForSave;

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

        long workoutID = getArguments().getLong("templateId", -1);

        mDb = new DAOProgram(getContext());
        mProgram = mDb.get(workoutID);
        nameEdit.setText(mProgram.getName());
        descriptionEdit.setText((mProgram.getDescription()));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        this.getView().post(() -> {
            nameEdit.setOnTextChangeListener(itemOnTextChange);
            descriptionEdit.setOnTextChangeListener(itemOnTextChange);
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = (MainActivity) activity;
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
            mDb.update(mProgram);
            KToast.infoToast(getActivity(), mProgram.getName() + " updated", Gravity.BOTTOM, KToast.LENGTH_SHORT);
        }
    }

    public Fragment getFragment() {
        return this;
    }
}
