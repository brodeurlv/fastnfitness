package com.easyfitness.fonte;

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
import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.DAO.DAORecord;
import com.easyfitness.R;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.UnitConverter;

import java.text.DecimalFormat;
import java.util.Date;

public class RecordCursorAdapter extends CursorAdapter {

    private LayoutInflater mInflater;
    private int mFirstColorOdd = 0;
    private Context mContext = null;
    BtnClickListener mDeleteClickListener = null;

    public RecordCursorAdapter(Context context, Cursor c, int flags, BtnClickListener clickList) {
        super(context, c, flags);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDeleteClickListener = clickList;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        android.support.v7.widget.CardView cdView = view.findViewById(R.id.CARDVIEW);

        if (cursor.getPosition() % 2 == mFirstColorOdd) {
            cdView.setBackgroundColor(context.getResources().getColor(R.color.background));
        } else {
            cdView.setBackgroundColor(context.getResources().getColor(R.color.background_even));
        }

        /* Commun display */
        TextView tDate = view.findViewById(R.id.DATE_CELL);
        Date date;

        date = DateConverter.DBDateStrToDate(cursor.getString(cursor.getColumnIndex(DAORecord.DATE)));
        tDate.setText(DateConverter.dateToLocalDateStr(date, mContext));

        TextView tTime = view.findViewById(R.id.TIME_CELL);
        tTime.setText(cursor.getString(cursor.getColumnIndex(DAORecord.TIME)));

        TextView tExercise = view.findViewById(R.id.MACHINE_CELL);
        tExercise.setText(cursor.getString(cursor.getColumnIndex(DAORecord.EXERCISE)));

        TextView tSerie = view.findViewById(R.id.SERIE_CELL);
        TextView tSerieLabel = view.findViewById(R.id.SERIE_LABEL);
        TextView tReps = view.findViewById(R.id.REPETITION_CELL);
        TextView tWeight = view.findViewById(R.id.POIDS_CELL);
        TextView tWeightLabel = view.findViewById(R.id.WEIGHT_LABEL);
        //LinearLayout tSerieLayout = view.findViewById(R.id.SERIE_LAYOUT);
        LinearLayout tRepsLayout = view.findViewById(R.id.REP_LAYOUT);
        //LinearLayout tWeightLayout = view.findViewById(R.id.WEIGHT_LAYOUT);


        /* Specific display */
        int recordType = cursor.getInt(cursor.getColumnIndex(DAORecord.TYPE));
        if (recordType == DAOMachine.TYPE_FONTE) {
            // UI
            tSerieLabel.setText(mContext.getString(R.string.SerieLabel));
            tWeightLabel.setText(mContext.getString(R.string.PoidsLabel));
            tRepsLayout.setVisibility(View.VISIBLE);
            // Data
            tSerie.setText(cursor.getString(cursor.getColumnIndex(DAORecord.SERIE)));
            tReps.setText(cursor.getString(cursor.getColumnIndex(DAORecord.REPETITION)));

            String unit = mContext.getString(R.string.KgUnitLabel);
            float poids = cursor.getFloat(cursor.getColumnIndex(DAORecord.WEIGHT));
            if (cursor.getInt(cursor.getColumnIndex(DAORecord.UNIT)) == UnitConverter.UNIT_LBS) {
                poids = UnitConverter.KgtoLbs(poids);
                unit = mContext.getString(R.string.LbsUnitLabel);
            }
            DecimalFormat numberFormat = new DecimalFormat("#.##");
            tWeight.setText(numberFormat.format(poids) + unit);

        } else if (recordType == DAOMachine.TYPE_CARDIO) {
            tSerieLabel.setText(mContext.getString(R.string.DistanceLabel));
            tWeightLabel.setText(mContext.getString(R.string.DurationLabel));
            tRepsLayout.setVisibility(View.GONE);
            tSerie.setText(cursor.getString(cursor.getColumnIndex(DAORecord.DISTANCE)));
            tWeight.setText(DateConverter.durationToHoursMinutesStr(cursor.getInt(cursor.getColumnIndex(DAORecord.DURATION))));
        }

        ImageView deletImg = view.findViewById(R.id.deleteButton);
        deletImg.setTag(cursor.getLong(cursor.getColumnIndex(DAORecord.KEY)));
        deletImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDeleteClickListener != null)
                    mDeleteClickListener.onBtnClick((long) v.getTag());
            }
        });
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.row_fonte, parent, false);
    }

    /*
     * @pColor : si 1 alors affiche la couleur Odd en premier. Sinon, a couleur Even.
     */
    public void setFirstColorOdd(int pColor) {
        mFirstColorOdd = pColor;
    }
}