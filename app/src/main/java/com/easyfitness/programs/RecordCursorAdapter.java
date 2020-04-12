package com.easyfitness.programs;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.easyfitness.BtnClickListener;
import com.easyfitness.DAO.DAOExerciseInProgram;
import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.R;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.UnitConverter;
import java.text.DecimalFormat;
import androidx.cardview.widget.CardView;

public class RecordCursorAdapter extends CursorAdapter {

    private LayoutInflater mInflater;
    private int mFirstColorOdd = 0;
    private Context mContext;
    private BtnClickListener mDeleteClickListener;
    private BtnClickListener mCopyClickListener;

    RecordCursorAdapter(Context context, Cursor c, int flags, BtnClickListener clickDelete, BtnClickListener clickCopy) {
        super(context, c, flags);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDeleteClickListener = clickDelete;
        mCopyClickListener = clickCopy;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        CardView cdView = view.findViewById(R.id.CARDVIEW);

        final int position = cursor.getPosition();

        if (position % 2 == mFirstColorOdd) {
            cdView.setBackgroundColor(context.getResources().getColor(R.color.record_background_odd));
        } else {
            cdView.setBackgroundColor(context.getResources().getColor(R.color.record_background_even));
        }

        /* Commun display */
//        TextView tDate = view.findViewById(R.id.DATE_CELL);
//        Date date;
//        String dateString = cursor.getString(cursor.getColumnIndex(DAOExerciseInProgram.DATE));
//        date = DateConverter.DBDateStrToDate(dateString);
//        tDate.setText(DateConverter.dateToLocalDateStr(date, mContext));

//        TextView tTime = view.findViewById(R.id.TIME_CELL);
//        tTime.setText(cursor.getString(cursor.getColumnIndex(DAOExerciseInProgram.TIME)));

        TextView tExercise = view.findViewById(R.id.MACHINE_CELL);
        tExercise.setText(cursor.getString(cursor.getColumnIndex(DAOExerciseInProgram.EXERCISE)));

        TextView tSerie = view.findViewById(R.id.SERIE_CELL);
        TextView tSerieLabel = view.findViewById(R.id.SERIE_LABEL);
        TextView tReps = view.findViewById(R.id.REPETITION_CELL);
        TextView tRepsLabel = view.findViewById(R.id.REP_LABEL);
        TextView tWeight = view.findViewById(R.id.POIDS_CELL);
        TextView tWeightLabel = view.findViewById(R.id.WEIGHT_LABEL);
        LinearLayout tRepsLayout = view.findViewById(R.id.REP_LAYOUT);

        if (mCopyClickListener == null) {
            view.findViewById(R.id.copyButton).setVisibility(View.GONE);
        }

        /* Specific display */
        int recordType = cursor.getInt(cursor.getColumnIndex(DAOExerciseInProgram.TYPE));
        if (recordType == DAOMachine.TYPE_FONTE) {
            // UI
            tSerieLabel.setText(mContext.getString(R.string.SerieLabel));
            tWeightLabel.setText(mContext.getString(R.string.PoidsLabel));
            tRepsLabel.setText(mContext.getString(R.string.RepetitionLabel_short));
            tRepsLayout.setVisibility(View.VISIBLE);
            // Data
            tSerie.setText(cursor.getString(cursor.getColumnIndex(DAOExerciseInProgram.SERIE)));
            tReps.setText(cursor.getString(cursor.getColumnIndex(DAOExerciseInProgram.REPETITION)));

            String unit = mContext.getString(R.string.KgUnitLabel);
            float poids = cursor.getFloat(cursor.getColumnIndex(DAOExerciseInProgram.WEIGHT));
            if (cursor.getInt(cursor.getColumnIndex(DAOExerciseInProgram.UNIT)) == UnitConverter.UNIT_LBS) {
                poids = UnitConverter.KgtoLbs(poids);
                unit = mContext.getString(R.string.LbsUnitLabel);
            }
            DecimalFormat numberFormat = new DecimalFormat("#.##");
            tWeight.setText(numberFormat.format(poids) + unit);

        } else  if (recordType == DAOMachine.TYPE_STATIC) {
            // UI
            tSerieLabel.setText(mContext.getString(R.string.SerieLabel));
            tWeightLabel.setText(mContext.getString(R.string.PoidsLabel));
            tRepsLabel.setText(mContext.getString(R.string.SecondsLabel_short));
            tRepsLayout.setVisibility(View.VISIBLE);
            // Data
            tSerie.setText(cursor.getString(cursor.getColumnIndex(DAOExerciseInProgram.SERIE)));
            tReps.setText(cursor.getString(cursor.getColumnIndex(DAOExerciseInProgram.SECONDS)));

            String unit = mContext.getString(R.string.KgUnitLabel);
            float poids = cursor.getFloat(cursor.getColumnIndex(DAOExerciseInProgram.WEIGHT));
            if (cursor.getInt(cursor.getColumnIndex(DAOExerciseInProgram.UNIT)) == UnitConverter.UNIT_LBS) {
                poids = UnitConverter.KgtoLbs(poids);
                unit = mContext.getString(R.string.LbsUnitLabel);
            }
            DecimalFormat numberFormat = new DecimalFormat("#.##");
            tWeight.setText(numberFormat.format(poids) + unit);

        } else if (recordType == DAOMachine.TYPE_CARDIO) {
            tSerieLabel.setText(mContext.getString(R.string.DistanceLabel));
            tWeightLabel.setText(mContext.getString(R.string.DurationLabel));
            tRepsLayout.setVisibility(View.GONE);

            float distance = cursor.getFloat(cursor.getColumnIndex(DAOExerciseInProgram.DISTANCE));
            String unit = mContext.getString(R.string.KmUnitLabel);
            if (cursor.getInt(cursor.getColumnIndex(DAOExerciseInProgram.DISTANCE_UNIT)) == UnitConverter.UNIT_MILES) {
                distance = UnitConverter.KmToMiles(distance); // Always convert to KG
                unit = mContext.getString(R.string.MilesUnitLabel);
            }
            DecimalFormat numberFormat = new DecimalFormat("#.##");
            tSerie.setText(numberFormat.format(distance) + unit);

            tWeight.setText(DateConverter.durationToHoursMinutesSecondsStr(cursor.getInt(cursor.getColumnIndex(DAOExerciseInProgram.DURATION))));
        }

        // Add separator if needed
//        boolean separatorNeeded = false;
//        if (position == 0) {
//            separatorNeeded = true;
//        } else {
//            cursor.moveToPosition(position - 1);
//            String datePreviousString = cursor.getString(cursor.getColumnIndex(DAOExerciseInProgram.DATE));
//            if (datePreviousString.compareTo(dateString) != 0) {
//                separatorNeeded = true;
//            }
//            cursor.moveToPosition(position);
//        }

//        TextView t = view.findViewById(R.id.SEPARATOR_CELL);
//        if (separatorNeeded) {
//            t.setText("- " + "TEST " + " -");
//            t.setVisibility(View.VISIBLE);
//        } else {
//            t.setText("");
//            t.setVisibility(View.GONE);
//        }

        ImageView deleteImg = view.findViewById(R.id.deleteButton);
        deleteImg.setTag(cursor.getLong(cursor.getColumnIndex(DAOExerciseInProgram.KEY)));
        deleteImg.setOnClickListener(v -> {
            if (mDeleteClickListener != null)
                mDeleteClickListener.onBtnClick((long) v.getTag());
        });

        ImageView copyImg = view.findViewById(R.id.copyButton);
        copyImg.setTag(cursor.getLong(cursor.getColumnIndex(DAOExerciseInProgram.KEY)));
        copyImg.setOnClickListener(v -> {
            if (mCopyClickListener != null)
                mCopyClickListener.onBtnClick((long) v.getTag());
        });
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.row_fonte, parent, false);
    }

    /*
     * @pColor : si 1 alors affiche la couleur Odd en premier. Sinon, a couleur Even.
     */
    void setFirstColorOdd(int pColor) {
        mFirstColorOdd = pColor;
    }
}