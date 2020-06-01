package com.easyfitness.workout;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easyfitness.DAO.workout.DAOWorkout;
import com.easyfitness.DAO.workout.Workout;
import com.easyfitness.MainActivity;
import com.easyfitness.R;
import com.easyfitness.views.EditableInputView;
import com.onurkaganaldemir.ktoastlib.KToast;

import androidx.fragment.app.Fragment;


public class WorkoutInfoFragment extends Fragment {
    EditableInputView descriptionEdit = null;
    EditableInputView nameEdit = null;

    MainActivity mActivity = null;
    private DAOWorkout mDb = null;

    private EditableInputView.OnTextChangedListener itemOnTextChange = this::requestForSave;
    private Workout mWorkout;

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static WorkoutInfoFragment newInstance(String name, int templateId) {
        WorkoutInfoFragment f = new WorkoutInfoFragment();

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

        mDb = new DAOWorkout(getContext());
        mWorkout = mDb.get(workoutID);
        nameEdit.setText(mWorkout.getName());
        descriptionEdit.setText((mWorkout.getDescription()));

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
                mWorkout.setName(nameEdit.getText());
                toUpdate = true;
                break;
            case R.id.workout_description:
                mWorkout.setDescription(descriptionEdit.getText());
                toUpdate = true;
                break;
        }

        if (toUpdate) {
            mDb.update(mWorkout);
            KToast.infoToast(getActivity(), mWorkout.getName() + " updated", Gravity.BOTTOM, KToast.LENGTH_SHORT);
        }
    }

    public Fragment getFragment() {
        return this;
    }
}
