package com.easyfitness.workout;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.easyfitness.BtnClickListener;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.record.DAORecord;
import com.easyfitness.DAO.record.Record;
import com.easyfitness.DAO.workout.DAOWorkout;
import com.easyfitness.DAO.workout.DAOWorkoutHistory;
import com.easyfitness.DAO.workout.Workout;
import com.easyfitness.DAO.workout.WorkoutHistory;
import com.easyfitness.MainActivity;
import com.easyfitness.R;
import com.easyfitness.enums.DisplayType;
import com.easyfitness.enums.ProgramRecordStatus;
import com.easyfitness.enums.ProgramStatus;
import com.easyfitness.fonte.RecordArrayAdapter;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.ExpandedListView;

import java.util.List;

import androidx.fragment.app.Fragment;

public class ProgramRunnerFragment extends Fragment {
    private Button mStartStopButton;
    private ExpandedListView mProgramRecordsList;
    private Spinner mProgramsSpinner;

    private DAOWorkout mDbWorkout;
    private DAOWorkoutHistory mDbWorkoutHistory;
    private DAORecord mDbRecord;
    private ArrayAdapter<Workout> mAdapterPrograms;
    private List<Workout> mProgramsArray;
    private Workout mRunningProgram;
    private WorkoutHistory mRunningProgramHistory;
    private boolean mIsProgramRunning = false;

    private View.OnClickListener clickStartStopButton = v -> {
        if (mRunningProgram==null) {
            mRunningProgram=(Workout)mProgramsSpinner.getSelectedItem();
            WorkoutHistory workoutHistory = new WorkoutHistory(-1, mRunningProgram.getId(), getProfile().getId(), ProgramStatus.RUNNING, DateConverter.currentDate(), DateConverter.currentTime(), "", "");
            long workoutHistoryId = mDbWorkoutHistory.add(workoutHistory);
            mRunningProgramHistory = mDbWorkoutHistory.get(workoutHistoryId);
            mProgramsSpinner.setEnabled(false);
            mStartStopButton.setText(R.string.stop_program);

            // add all template records with status "Pending"
            Cursor cursor = mDbRecord.getProgramTemplateRecords(((Workout)mProgramsSpinner.getSelectedItem()).getId());
            List<Record> recordList = mDbRecord.fromCursorToList(cursor);
            for (Record record:recordList)
            {
                record.setProgramRecordStatus(ProgramRecordStatus.PENDING);
                record.setTemplateSessionId(workoutHistoryId);
                record.setProfileId(getProfile().getId());
                mDbRecord.updateRecord(record);
            }
            // refresh table
            refreshData();
        } else {
            mRunningProgramHistory.setEndDate(DateConverter.currentDate());
            mRunningProgramHistory.setEndTime(DateConverter.currentTime());
            mRunningProgramHistory.setStatus(ProgramStatus.CLOSED);
            mDbWorkoutHistory.update(mRunningProgramHistory);
            mRunningProgram=null;
            mRunningProgramHistory=null;
            mProgramsSpinner.setEnabled(true);
            mStartStopButton.setText(R.string.start_program);
            refreshData();
        }
    };

    private BtnClickListener onClickFailed = id -> {
        Record r = mDbRecord.getRecord(id);
    };

    private BtnClickListener onClickSuccess = id -> {
        Record r = mDbRecord.getRecord(id);
    };

    private AdapterView.OnItemSelectedListener onProgramSelected = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            refreshData();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static ProgramRunnerFragment newInstance(String name, int id) {
        ProgramRunnerFragment f = new ProgramRunnerFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putInt("id", id);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState==null) {
            mDbWorkout = new DAOWorkout(this.getContext());
            mDbWorkoutHistory = new DAOWorkoutHistory(this.getContext());
            mDbRecord = new DAORecord(this.getContext());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.tab_program_runner, container, false);


        if (savedInstanceState==null) {
            mStartStopButton = view.findViewById(R.id.startStopProgram);
            mProgramsSpinner = view.findViewById(R.id.programSpinner);
            mProgramRecordsList = view.findViewById(R.id.listRecord);

            mStartStopButton.setOnClickListener(clickStartStopButton);
            mProgramsSpinner.setOnItemSelectedListener(onProgramSelected);
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshData();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }

    private void refreshData() {
        // 1. Get all existing Program and Fill Spinner
        if (mProgramsSpinner.getAdapter() == null) {
            mProgramsArray = mDbWorkout.getAll();
            //Data are refreshed on show
            mAdapterPrograms = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item,
                mProgramsArray);
            mAdapterPrograms.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mProgramsSpinner.setAdapter(mAdapterPrograms);
        } else {
            /* Program list initialisation */
            if (mProgramsArray == null)
                mProgramsArray = mDbWorkout.getAll();
            else {
                mProgramsArray.clear();
                mProgramsArray.addAll(mDbWorkout.getAll());
                mAdapterPrograms.notifyDataSetChanged();
            }
        }

        // 2. If a program is running, then put it in the spinner, update the button text to Stop,
        mIsProgramRunning = false;
        mRunningProgramHistory = mDbWorkoutHistory.getRunningProgram();
        if (mRunningProgramHistory!=null) {
            int position = 0;
            for (int i = 0; i <= mAdapterPrograms.getCount(); i++) {
                if (mAdapterPrograms.getItem(i).getId() == mRunningProgramHistory.getProgramId()) {
                    position = i;
                    mRunningProgram = mAdapterPrograms.getItem(i);
                    mIsProgramRunning = true;
                    mProgramsSpinner.setSelection(position);
                    mProgramsSpinner.setEnabled(false);
                    mStartStopButton.setText(R.string.stop_program);
                    break;
                }
            }
        }

        if (!mIsProgramRunning) {
            mRunningProgram=null;
            mProgramsSpinner.setEnabled(true);
            mStartStopButton.setText(R.string.start_program);
        }

        // 3. display the ongoing records
        Cursor cursor = null;
        if (mIsProgramRunning) {
            cursor = mDbRecord.getProgramWorkoutRecords(mRunningProgramHistory.getId());
        } else {
            Workout selectedWorkout = (Workout) mProgramsSpinner.getSelectedItem();
            if (selectedWorkout!=null) {
                cursor = mDbRecord.getProgramTemplateRecords(((Workout)mProgramsSpinner.getSelectedItem()).getId());
            }
        }

        List<Record> recordList = mDbRecord.fromCursorToList(cursor);

        DisplayType displayType;
        if (mIsProgramRunning) {
            displayType = DisplayType.PROGRAM_WORKOUT_DISPLAY;
        } else {
            displayType = DisplayType.PROGRAM_WORKOUT_PREVIEW_DISPLAY;
        }

        if (recordList.size()==0) {
            mProgramRecordsList.setAdapter(null);
        } else {
            if (mProgramRecordsList.getAdapter() == null) {
                RecordArrayAdapter mTableAdapter = new RecordArrayAdapter(getContext(), recordList, displayType, null);
                mProgramRecordsList.setAdapter(mTableAdapter);
            } else {
                RecordArrayAdapter mTableAdapter = (RecordArrayAdapter)mProgramRecordsList.getAdapter();
                if (mTableAdapter.getDisplayType()!=displayType) {
                    mTableAdapter = new RecordArrayAdapter(getContext(), recordList, displayType, null);
                    mProgramRecordsList.setAdapter(mTableAdapter);
                } else {
                    mTableAdapter.setRecords(recordList);
                }
            }
        }
    }

    private void updateTable() {

    }

    private Profile getProfile() {
        return ((MainActivity) getActivity()).getCurrentProfile();
    }

}
