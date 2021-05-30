package com.easyfitness.programs;

import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.program.DAOProgram;
import com.easyfitness.DAO.program.DAOProgramHistory;
import com.easyfitness.DAO.program.Program;
import com.easyfitness.DAO.program.ProgramHistory;
import com.easyfitness.DAO.record.DAORecord;
import com.easyfitness.DAO.record.Record;
import com.easyfitness.MainActivity;
import com.easyfitness.AppViMo;
import com.easyfitness.R;
import com.easyfitness.enums.DisplayType;
import com.easyfitness.enums.ProgramRecordStatus;
import com.easyfitness.enums.ProgramStatus;
import com.easyfitness.enums.RecordType;
import com.easyfitness.fonte.RecordArrayAdapter;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.Keyboard;
import com.easyfitness.utils.OnCustomEventListener;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ProgramRunnerFragment extends Fragment {
    private final View.OnClickListener clickAddProgramButton = v -> {
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
    private Button mStartStopButton;
    private Button mNewButton;
    private Button mEditButton;
    private ListView mProgramRecordsList;
    private Spinner mProgramsSpinner;
    private final View.OnClickListener onClickEditProgram = view -> {

        Program program = (Program) mProgramsSpinner.getSelectedItem();
        if (program == null) return;

        ProgramPagerFragment fragment = ProgramPagerFragment.newInstance(program.getId());
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, fragment, MainActivity.WORKOUTPAGER);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    };
    private TextView mProgramRecordsListTitle;
    private DAOProgram mDbWorkout;
    private DAOProgramHistory mDbWorkoutHistory;
    private DAORecord mDbRecord;
    private ArrayAdapter<Program> mAdapterPrograms;
    private List<Program> mProgramsArray;
    private Program mRunningProgram;
    private ProgramHistory mRunningProgramHistory;
    private boolean mIsProgramRunning = false;
    private AppViMo appViMo;
    private final OnCustomEventListener onProgramCompletedListener = eventName -> {
        // Open dialog box to finish program
        final SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText(getString(R.string.program_completed))
                .setConfirmText(getContext().getString(R.string.global_yes))
                .setCancelText(getContext().getString(R.string.global_no))
                .setHideKeyBoardOnDismiss(true)
                .setConfirmClickListener(sDialog -> {
                    stopProgram();
                    sDialog.dismiss();
                });
        dialog.show();
    };
    private final View.OnClickListener clickStartStopButton = v -> {
        if (mRunningProgram == null) {
            mRunningProgram = (Program) mProgramsSpinner.getSelectedItem();
            if (mRunningProgram != null) {
                long runningProgramId = mRunningProgram.getId();
                long profileId = getProfile().getId();
                ProgramHistory programHistory = new ProgramHistory(-1, runningProgramId, profileId, ProgramStatus.RUNNING, DateConverter.currentDate(getContext()), DateConverter.currentTime(getContext()), "", "");
                long workoutHistoryId = mDbWorkoutHistory.add(programHistory);
                mRunningProgramHistory = mDbWorkoutHistory.get(workoutHistoryId);
                mProgramsSpinner.setEnabled(false);
                mStartStopButton.setText(R.string.finish_program);

                // add all template records with status "Pending"
                Cursor cursor = mDbRecord.getProgramTemplateRecords(((Program) mProgramsSpinner.getSelectedItem()).getId());
                List<Record> recordList = mDbRecord.fromCursorToList(cursor);
                for (Record record : recordList) {
                    record.setTemplateRecordId(record.getId());
                    record.setTemplateSessionId(workoutHistoryId);
                    record.setRecordType(RecordType.PROGRAM_RECORD_TYPE);
                    record.setProgramRecordStatus(ProgramRecordStatus.PENDING);
                    record.setProfileId(getProfile().getId());
                    mDbRecord.addRecord(record);
                }
                // refresh table
                refreshData();
            }
        } else {
            stopProgram();
        }
    };
    private final AdapterView.OnItemSelectedListener onProgramSelected = new AdapterView.OnItemSelectedListener() {

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

    private void stopProgram() {
        mRunningProgramHistory.setEndDate(DateConverter.currentDate(getContext()));
        mRunningProgramHistory.setEndTime(DateConverter.currentTime(getContext()));
        mRunningProgramHistory.setStatus(ProgramStatus.CLOSED);

        // Check is there is no record
        Cursor c = mDbRecord.getProgramWorkoutRecords(mRunningProgramHistory.getId());
        List<Record> recordList = mDbRecord.fromCursorToList(c);

        int successCount = 0;
        int failedCount = 0;
        for (Record record:recordList) {
            if(record.getProgramRecordStatus() == ProgramRecordStatus.SUCCESS) {
                successCount++;
            } else if (record.getProgramRecordStatus() == ProgramRecordStatus.FAILED) {
                failedCount++;
            }
        }

        if (successCount == 0 &&  failedCount==0) {
            mDbWorkoutHistory.delete(mRunningProgramHistory);
            for (Record record:recordList) {
                mDbRecord.deleteRecord(record.getId());
            }
        } else {
            mDbWorkoutHistory.update(mRunningProgramHistory);
        }

        mRunningProgram = null;
        mRunningProgramHistory = null;
        mProgramsSpinner.setEnabled(true);
        mStartStopButton.setText(R.string.start_program);
        refreshData();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbWorkout = new DAOProgram(this.getContext());
        mDbWorkoutHistory = new DAOProgramHistory(this.getContext());
        mDbRecord = new DAORecord(this.getContext());


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.tab_program_runner, container, false);

        mStartStopButton = view.findViewById(R.id.startStopProgram);
        mProgramsSpinner = view.findViewById(R.id.programSpinner);
        mProgramRecordsList = view.findViewById(R.id.listProgramRecord);
        mProgramRecordsListTitle = view.findViewById(R.id.programListTitle);
        mNewButton = view.findViewById(R.id.newProgram);
        mEditButton = view.findViewById(R.id.editProgram);
        TextView emptyList = view.findViewById(R.id.listProgramEmpty);
        mProgramRecordsList.setEmptyView(emptyList);

        mStartStopButton.setOnClickListener(clickStartStopButton);
        mProgramsSpinner.setOnItemSelectedListener(onProgramSelected);
        mNewButton.setOnClickListener(clickAddProgramButton);
        mEditButton.setOnClickListener(onClickEditProgram);

        appViMo = new ViewModelProvider(requireActivity()).get(AppViMo.class);
        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        appViMo.getProfile().observe(getViewLifecycleOwner(), profile -> {
            // Update the UI, in this case, a TextView.
            refreshData();
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        //refreshData();
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
        mRunningProgramHistory = mDbWorkoutHistory.getRunningProgram(getProfile());
        if (mRunningProgramHistory != null) {
            int position = 0;
            for (int i = 0; i < mAdapterPrograms.getCount(); i++) {
                if (mAdapterPrograms.getItem(i).getId() == mRunningProgramHistory.getProgramId()) {
                    position = i;
                    mRunningProgram = mAdapterPrograms.getItem(i);
                    mIsProgramRunning = true;
                    mProgramsSpinner.setSelection(position);
                    mProgramsSpinner.setEnabled(false);
                    mStartStopButton.setText(R.string.finish_program);
                    mProgramRecordsListTitle.setText(R.string.program_ongoing);
                    break;
                }
            }
        }

        if (!mIsProgramRunning) {
            mRunningProgram = null;
            mProgramsSpinner.setEnabled(true);
            mStartStopButton.setText(R.string.start_program);
            mProgramRecordsListTitle.setText(R.string.program_preview);
        }

        // 3. display the ongoing records
        Cursor cursor = null;
        if (mIsProgramRunning) {
            cursor = mDbRecord.getProgramWorkoutRecords(mRunningProgramHistory.getId());
        } else {
            Program selectedProgram = (Program) mProgramsSpinner.getSelectedItem();
            if (selectedProgram != null) {
                cursor = mDbRecord.getProgramTemplateRecords(((Program) mProgramsSpinner.getSelectedItem()).getId());
            }
        }

        List<Record> recordList = mDbRecord.fromCursorToList(cursor);

        DisplayType displayType;
        if (mIsProgramRunning) {
            displayType = DisplayType.PROGRAM_RUNNING_DISPLAY;
        } else {
            displayType = DisplayType.PROGRAM_PREVIEW_DISPLAY;
        }

        if (recordList.isEmpty()) {
            mProgramRecordsList.setAdapter(null);
        } else {
            if (mProgramRecordsList.getAdapter() == null) {
                RecordArrayAdapter mTableAdapter = new RecordArrayAdapter(getActivity(), getContext(), recordList, displayType, null);
                mTableAdapter.setOnProgramCompletedListener(onProgramCompletedListener);
                mProgramRecordsList.setAdapter(mTableAdapter);
            } else {
                RecordArrayAdapter mTableAdapter = (RecordArrayAdapter) mProgramRecordsList.getAdapter();
                if (mTableAdapter.getDisplayType() != displayType) {
                    mTableAdapter = new RecordArrayAdapter(getActivity(), getContext(), recordList, displayType, null);
                    mTableAdapter.setOnProgramCompletedListener(onProgramCompletedListener);
                    mProgramRecordsList.setAdapter(mTableAdapter);
                } else {
                    mTableAdapter.setRecords(recordList);
                }
            }
        }
    }

    private Profile getProfile() {
        return appViMo.getProfile().getValue();
    }

}
