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

import androidx.cardview.widget.CardView;

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
    private BtnClickListener mDeleteClickListener = null;
    private BtnClickListener mCopyClickListener = null;

    public RecordCursorAdapter(Context context, Cursor c, int flags, BtnClickListener clickDelete, BtnClickListener clickCopy) {
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
            cdView.setBackgroundColor(context.getResources().getColor(R.color.background));
        } else {
            cdView.setBackgroundColor(context.getResources().getColor(R.color.background_even));
        }

        /* Commun display */
        TextView tDate = view.findViewById(R.id.DATE_CELL);
        Date date;
        String dateString = cursor.getString(cursor.getColumnIndex(DAORecord.DATE));
        date = DateConverter.DBDateStrToDate(dateString);
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

        if (mCopyClickListener == null) {
            view.findViewById(R.id.copyButton).setVisibility(View.GONE);
        }

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

        // Add separator if needed
        boolean separatorNeeded = false;
        if (position == 0) {
            separatorNeeded = true;
        } else {
            cursor.moveToPosition(position - 1);
            String datePreviousString = cursor.getString(cursor.getColumnIndex(DAORecord.DATE));
            if (datePreviousString.compareTo(dateString) != 0) {
                separatorNeeded = true;
            }
            cursor.moveToPosition(position);
        }

        TextView t = view.findViewById(R.id.SEPARATOR_CELL);
        if (separatorNeeded) {
            t.setText("- " + DateConverter.dateToLocalDateStr(date, mContext) + " -");
            t.setVisibility(View.VISIBLE);
        } else {
            t.setText("");
            t.setVisibility(View.GONE);
        }

/*
        if (separatorNeeded) {
            LinearLayout l = view.findViewById(R.id.ROWFONTELAYOUT);
            TextView t = new TextView(context);
            t.setText(DateConverter.dateToLocalDateStr(date, mContext));
            l.addView(t, 1);
        }
*/

        ImageView deletImg = view.findViewById(R.id.deleteButton);
        deletImg.setTag(cursor.getLong(cursor.getColumnIndex(DAORecord.KEY)));
        deletImg.setOnClickListener(v -> {
            if (mDeleteClickListener != null)
                mDeleteClickListener.onBtnClick((long) v.getTag());
        });

        ImageView copyImg = view.findViewById(R.id.copyButton);
        copyImg.setTag(cursor.getLong(cursor.getColumnIndex(DAORecord.KEY)));
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
    public void setFirstColorOdd(int pColor) {
        mFirstColorOdd = pColor;
    }
}
