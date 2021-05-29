package com.easyfitness.programs;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.DAO.Machine;
import com.easyfitness.DAO.program.DAOProgram;
import com.easyfitness.DAO.program.DAOProgramHistory;
import com.easyfitness.DAO.program.Program;
import com.easyfitness.DAO.record.DAORecord;
import com.easyfitness.DAO.record.Record;
import com.easyfitness.R;
import com.easyfitness.enums.ExerciseType;
import com.easyfitness.enums.ProgramRecordStatus;
import com.easyfitness.enums.ProgramStatus;
import com.easyfitness.utils.ImageUtil;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;

import java.util.List;

public class ProgramHistoryCursorAdapter extends CursorAdapter implements Filterable {

    private final LayoutInflater mInflater;
    private final DAORecord daoRecord;
    private final DAOProgramHistory daoProgramHistory;
    private final DAOProgram daoProgram;

    public ProgramHistoryCursorAdapter(Context p_context, Cursor p_cursor, int p_flags, DAOProgramHistory p_dbProgramHistory) {
        super(p_context, p_cursor, p_flags);
        daoProgramHistory = p_dbProgramHistory;
        daoProgram = new DAOProgram(p_context);
        daoRecord = new DAORecord(p_context);
        mInflater = (LayoutInflater) p_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Get Program Name
        long historyKey = cursor.getLong(cursor.getColumnIndex(DAOProgramHistory.KEY));

        long programKey = cursor.getLong(cursor.getColumnIndex(DAOProgramHistory.PROGRAM_KEY));
        Program program = daoProgram.get(programKey);

        // Get program Status
        int programStatus = cursor.getInt(cursor.getColumnIndex(DAOProgramHistory.STATUS));

        TextView programName = view.findViewById(R.id.PROGRAM_CELL);
        programName.setText(program.getName());

        TextView startDate = view.findViewById(R.id.START_DATE_CELL);
        startDate.setText(cursor.getString(cursor.getColumnIndex(DAOProgramHistory.START_DATE)));

        TextView startTime = view.findViewById(R.id.START_TIME_CELL);
        startTime.setText(cursor.getString(cursor.getColumnIndex(DAOProgramHistory.START_TIME)));

        TextView endDate = view.findViewById(R.id.END_DATE_CELL);
        if (ProgramStatus.fromInteger(programStatus) == ProgramStatus.RUNNING){
            endDate.setText("Ongoing");
        } else {
            endDate.setText(cursor.getString(cursor.getColumnIndex(DAOProgramHistory.END_DATE)));
        }

        TextView endTime = view.findViewById(R.id.END_TIME_CELL);
        endTime.setText(cursor.getString(cursor.getColumnIndex(DAOProgramHistory.END_TIME)));

        TextView success = view.findViewById(R.id.SUCCESS_CELL);
        TextView fail = view.findViewById(R.id.FAIL_CELL);

        ImageView successButton = view.findViewById(R.id.successButton);

        // Get all records
        Cursor c = daoRecord.getProgramWorkoutRecords(historyKey);
        List<Record> recordList = daoRecord.fromCursorToList(c);

        int successCount = 0;
        int failedCount = 0;

        for (Record record:recordList) {
            if(record.getProgramRecordStatus() == ProgramRecordStatus.SUCCESS) {
                successCount++;
            } else if (record.getProgramRecordStatus() == ProgramRecordStatus.FAILED) {
                failedCount++;
            }
        }

        success.setText(String.valueOf(successCount));
        fail.setText(String.valueOf(failedCount));

        successButton.setVisibility(View.GONE);
        if (successCount!=0 && successCount==recordList.size()) {
            successButton.setVisibility(View.VISIBLE);
            successButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_check_active));
            successButton.setBackgroundColor(Color.parseColor("#00AF80"));
        } else {
            successButton.setVisibility(View.VISIBLE);
            successButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_cross_active));
            successButton.setBackgroundColor(Color.RED);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.program_history_row, parent, false);
    }

}

