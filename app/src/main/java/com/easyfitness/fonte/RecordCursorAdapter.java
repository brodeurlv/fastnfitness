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
import androidx.core.content.ContextCompat;

import com.easyfitness.BtnClickListener;
import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.DAO.record.DAORecord;
import com.easyfitness.R;
import com.easyfitness.enums.DisplayType;
import com.easyfitness.enums.ExerciseType;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.UnitConverter;

import java.text.DecimalFormat;
import java.util.Date;

public class RecordCursorAdapter extends CursorAdapter {

    private LayoutInflater mInflater;
    private int mFirstColorOdd = 0;
    private Context mContext = null;
    private BtnClickListener mAction1ClickListener = null;
    private BtnClickListener mAction2ClickListener = null;
    private BtnClickListener mAction3ClickListener = null;
    private DisplayType mDisplayType = DisplayType.FREE_WORKOUT_DISPLAY;
    private DAORecord mDbRecord;

    public RecordCursorAdapter(Context context, Cursor c, int flags, BtnClickListener clickAction1, BtnClickListener clickAction2, BtnClickListener clickAction3, DisplayType displayType) {
        super(context, c, flags);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mAction1ClickListener = clickAction1;
        mAction2ClickListener = clickAction2;
        mAction3ClickListener = clickAction3;
        mDisplayType=displayType;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.ExerciseName = view.findViewById(R.id.MACHINE_CELL);
        viewHolder.CardView = view.findViewById(R.id.CARDVIEW);
        viewHolder.Separator = view.findViewById(R.id.SEPARATOR_CELL);
        viewHolder.Date = view.findViewById(R.id.DATE_CELL);
        viewHolder.Time = view.findViewById(R.id.TIME_CELL);
        viewHolder.FirstColValue = view.findViewById(R.id.SERIE_CELL);
        viewHolder.FirstColLabel = view.findViewById(R.id.SERIE_LABEL);
        viewHolder.SecondColLayout = view.findViewById(R.id.REP_LAYOUT);
        viewHolder.SecondColValue = view.findViewById(R.id.REPETITION_CELL);
        viewHolder.SecondColLabel = view.findViewById(R.id.REP_LABEL);
        viewHolder.ThirdColValue = view.findViewById(R.id.POIDS_CELL);
        viewHolder.ThirdColLabel = view.findViewById(R.id.WEIGHT_LABEL);
        viewHolder.BtAction1 = view.findViewById(R.id.action1Button);
        viewHolder.BtAction2 = view.findViewById(R.id.action2Button);
        viewHolder.BtAction3 = view.findViewById(R.id.action3Button);

        final int position = cursor.getPosition();

        if (position % 2 == mFirstColorOdd) {
            viewHolder.CardView.setBackgroundColor(context.getResources().getColor(R.color.record_background_odd));
        } else {
            viewHolder.CardView.setBackgroundColor(context.getResources().getColor(R.color.record_background_even));
        }


        /* Commun display */
        int recordType = cursor.getInt(cursor.getColumnIndex(DAORecord.RECORD_TYPE));
        ExerciseType exerciseType = ExerciseType.fromInteger(cursor.getInt(cursor.getColumnIndex(DAORecord.EXERCISE_TYPE)));

        UpdateUI(recordType, exerciseType, viewHolder);

        if (mDisplayType==DisplayType.FREE_WORKOUT_DISPLAY) {
            viewHolder.ExerciseName.setText(cursor.getString(cursor.getColumnIndex(DAORecord.EXERCISE)));

            Date date;
            String dateString = cursor.getString(cursor.getColumnIndex(DAORecord.DATE));
            date = DateConverter.DBDateStrToDate(dateString);
            viewHolder.Date.setText(DateConverter.dateToLocalDateStr(date, mContext));
            viewHolder.Time.setText(cursor.getString(cursor.getColumnIndex(DAORecord.TIME)));

            if (isSeparatorNeeded(position, dateString, cursor)) {
                viewHolder.Separator.setText("- " + DateConverter.dateToLocalDateStr(date, mContext) + " -");
                viewHolder.Separator.setVisibility(View.VISIBLE);
            } else {
                viewHolder.Separator.setText("");
                viewHolder.Separator.setVisibility(View.GONE);
            }

            if (exerciseType == ExerciseType.STRENGTH) {
                viewHolder.FirstColValue.setText(cursor.getString(cursor.getColumnIndex(DAORecord.SETS)));
                viewHolder.SecondColValue.setText(cursor.getString(cursor.getColumnIndex(DAORecord.REPS)));
                viewHolder.ThirdColValue.setText(weigthToString( cursor.getFloat(cursor.getColumnIndex(DAORecord.WEIGHT)), cursor.getInt(cursor.getColumnIndex(DAORecord.WEIGHT_UNIT))));
            } else  if (exerciseType == ExerciseType.ISOMETRIC) {
                viewHolder.FirstColValue.setText(cursor.getString(cursor.getColumnIndex(DAORecord.SETS)));
                viewHolder.SecondColValue.setText(cursor.getString(cursor.getColumnIndex(DAORecord.SECONDS)));
                viewHolder.ThirdColValue.setText(weigthToString( cursor.getFloat(cursor.getColumnIndex(DAORecord.WEIGHT)), cursor.getInt(cursor.getColumnIndex(DAORecord.WEIGHT_UNIT))));
            } else if (exerciseType == ExerciseType.CARDIO) {
                viewHolder.FirstColValue.setText(distanceToString(cursor.getFloat(cursor.getColumnIndex(DAORecord.DISTANCE)), cursor.getInt(cursor.getColumnIndex(DAORecord.DISTANCE_UNIT))) );
                viewHolder.ThirdColValue.setText(DateConverter.durationToHoursMinutesSecondsStr(cursor.getInt(cursor.getColumnIndex(DAORecord.DURATION))));
            }

            viewHolder.BtAction1.setTag(cursor.getLong(cursor.getColumnIndex(DAORecord.KEY)));
            viewHolder.BtAction1.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_clear_black_24dp));
            viewHolder.BtAction1.setOnClickListener(v -> {
                        mDbRecord.deleteRecord(cursor.getLong(cursor.getColumnIndex(DAORecord.KEY)));

                        notifyDataSetChanged();
                //if (mAction1ClickListener != null)
                //    mAction1ClickListener.onBtnClick((long) v.getTag());
            });

            viewHolder.BtAction2.setTag(cursor.getLong(cursor.getColumnIndex(DAORecord.KEY)));
            viewHolder.BtAction2.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_back));
            viewHolder.BtAction2.setRotation(90);
            viewHolder.BtAction2.setOnClickListener(v -> {
                if (mAction2ClickListener != null)
                    mAction2ClickListener.onBtnClick((long) v.getTag());
            });

            viewHolder.BtAction3.setVisibility(View.GONE);
        } else if (mDisplayType==DisplayType.PROGRAM_EDIT_DISPLAY) {
            viewHolder.ExerciseName.setText(cursor.getString(cursor.getColumnIndex(DAORecord.TEMPLATE_ORDER))+":"+cursor.getString(cursor.getColumnIndex(DAORecord.EXERCISE)));

            viewHolder.Separator.setVisibility(View.GONE);

            if (exerciseType == ExerciseType.STRENGTH) {
                viewHolder.FirstColValue.setText(cursor.getString(cursor.getColumnIndex(DAORecord.TEMPLATE_SETS)));
                viewHolder.SecondColValue.setText(cursor.getString(cursor.getColumnIndex(DAORecord.TEMPLATE_REPS)));
                viewHolder.ThirdColValue.setText(weigthToString( cursor.getFloat(cursor.getColumnIndex(DAORecord.TEMPLATE_WEIGHT)), cursor.getInt(cursor.getColumnIndex(DAORecord.TEMPLATE_WEIGHT_UNIT))));
            } else  if (exerciseType == ExerciseType.ISOMETRIC) {
                viewHolder.FirstColValue.setText(cursor.getString(cursor.getColumnIndex(DAORecord.TEMPLATE_SETS)));
                viewHolder.SecondColValue.setText(cursor.getString(cursor.getColumnIndex(DAORecord.TEMPLATE_SECONDS)));
                viewHolder.ThirdColValue.setText(weigthToString( cursor.getFloat(cursor.getColumnIndex(DAORecord.TEMPLATE_WEIGHT)), cursor.getInt(cursor.getColumnIndex(DAORecord.TEMPLATE_WEIGHT_UNIT))));
            } else if (exerciseType == ExerciseType.CARDIO) {
                viewHolder.FirstColValue.setText(distanceToString(cursor.getFloat(cursor.getColumnIndex(DAORecord.TEMPLATE_DISTANCE)), cursor.getInt(cursor.getColumnIndex(DAORecord.TEMPLATE_DISTANCE_UNIT))) );
                viewHolder.ThirdColValue.setText(DateConverter.durationToHoursMinutesSecondsStr(cursor.getInt(cursor.getColumnIndex(DAORecord.TEMPLATE_DURATION))));
            }

            long key=cursor.getLong(cursor.getColumnIndex(DAORecord.KEY));
            viewHolder.BtAction1.setTag(key);
            viewHolder.BtAction1.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_clear_black_24dp));
            viewHolder.BtAction1.setOnClickListener(v -> {
                if (mAction1ClickListener != null)
                    mAction1ClickListener.onBtnClick((long) v.getTag());
            });

            viewHolder.BtAction2.setTag(key);
            viewHolder.BtAction2.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_back));
            viewHolder.BtAction2.setRotation(-90);
            viewHolder.BtAction2.setOnClickListener(v -> {
                if (mAction2ClickListener != null)
                    mAction2ClickListener.onBtnClick((long) v.getTag());
            });

            viewHolder.BtAction3.setTag(key);
            viewHolder.BtAction3.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_back));
            viewHolder.BtAction3.setRotation(+90);
            viewHolder.BtAction3.setOnClickListener(v -> {
                if (mAction3ClickListener != null)
                    mAction3ClickListener.onBtnClick((long) v.getTag());
            });
        } else  if (mDisplayType==DisplayType.PROGRAM_WORKOUT_DISPLAY) {
            viewHolder.Separator.setVisibility(View.GONE);

            if (exerciseType == ExerciseType.STRENGTH) {
                viewHolder.FirstColValue.setText(cursor.getString(cursor.getColumnIndex(DAORecord.SETS)));
                viewHolder.SecondColValue.setText(cursor.getString(cursor.getColumnIndex(DAORecord.REPS)));
                viewHolder.ThirdColValue.setText(weigthToString( cursor.getFloat(cursor.getColumnIndex(DAORecord.WEIGHT)), cursor.getInt(cursor.getColumnIndex(DAORecord.WEIGHT_UNIT))));
            } else  if (exerciseType == ExerciseType.ISOMETRIC) {
                viewHolder.FirstColValue.setText(cursor.getString(cursor.getColumnIndex(DAORecord.SETS)));
                viewHolder.SecondColValue.setText(cursor.getString(cursor.getColumnIndex(DAORecord.SECONDS)));
                viewHolder.ThirdColValue.setText(weigthToString( cursor.getFloat(cursor.getColumnIndex(DAORecord.WEIGHT)), cursor.getInt(cursor.getColumnIndex(DAORecord.WEIGHT_UNIT))));
            } else if (exerciseType == ExerciseType.CARDIO) {
                viewHolder.FirstColValue.setText(distanceToString(cursor.getFloat(cursor.getColumnIndex(DAORecord.DISTANCE)), cursor.getInt(cursor.getColumnIndex(DAORecord.DISTANCE_UNIT))) );
                viewHolder.ThirdColValue.setText(DateConverter.durationToHoursMinutesSecondsStr(cursor.getInt(cursor.getColumnIndex(DAORecord.DURATION))));
            }

            long key=cursor.getLong(cursor.getColumnIndex(DAORecord.KEY));

            viewHolder.BtAction1.setTag(key);
            viewHolder.BtAction1.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_check_white_24dp));
            viewHolder.BtAction1.setOnClickListener(v -> {
                if (mAction1ClickListener != null)
                    mAction1ClickListener.onBtnClick((long) v.getTag());
            });

            viewHolder.BtAction2.setTag(key);
            viewHolder.BtAction2.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_cross_white_24dp));
            viewHolder.BtAction2.setOnClickListener(v -> {
                if (mAction2ClickListener != null)
                    mAction2ClickListener.onBtnClick((long) v.getTag());
            });

            viewHolder.BtAction3.setVisibility(View.GONE);
        }

        if (mAction1ClickListener==null) viewHolder.BtAction1.setVisibility(View.GONE);
        if (mAction2ClickListener==null) viewHolder.BtAction2.setVisibility(View.GONE);
        if (mAction3ClickListener==null) viewHolder.BtAction3.setVisibility(View.GONE);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        mDbRecord = new DAORecord(context);
        return mInflater.inflate(R.layout.row_fonte, parent, false);
    }

    /*
     * @pColor : si 1 alors affiche la couleur Odd en premier. Sinon, a couleur Even.
     */
    public void setFirstColorOdd(int pColor) {
        mFirstColorOdd = pColor;
    }

    private void UpdateUI(int pRecordType, ExerciseType pExerciseType, ViewHolder viewHolder) {
        if (pRecordType == DAORecord.TEMPLATE_TYPE) {
            viewHolder.Time.setVisibility(View.GONE);
            viewHolder.Date.setVisibility(View.GONE);
        } else {
            viewHolder.Time.setVisibility(View.VISIBLE);
            viewHolder.Date.setVisibility(View.VISIBLE);
        }

        if (pExerciseType == ExerciseType.CARDIO) {
            viewHolder.SecondColLayout.setVisibility(View.GONE);
            viewHolder.FirstColLabel.setText(mContext.getString(R.string.DistanceLabel));
            viewHolder.ThirdColLabel.setText(mContext.getString(R.string.DurationLabel));
        } else if (pExerciseType == ExerciseType.STRENGTH) {
            viewHolder.SecondColLayout.setVisibility(View.VISIBLE);
            viewHolder.FirstColLabel.setText(mContext.getString(R.string.SerieLabel));
            viewHolder.SecondColLabel.setText(mContext.getString(R.string.RepetitionLabel_short));
            viewHolder.ThirdColLabel.setText(mContext.getString(R.string.PoidsLabel));
        } else if (pExerciseType == ExerciseType.ISOMETRIC) {
            viewHolder.SecondColLayout.setVisibility(View.VISIBLE);
            viewHolder.FirstColLabel.setText(mContext.getString(R.string.SerieLabel));
            viewHolder.SecondColLabel.setText(mContext.getString(R.string.SecondsLabel_short));
            viewHolder.ThirdColLabel.setText(mContext.getString(R.string.PoidsLabel));
        }
    }

    private String weigthToString(float weight, int unit) {
        String defaultUnit = mContext.getString(R.string.KgUnitLabel);
        if (unit == UnitConverter.UNIT_LBS) {
            weight = UnitConverter.KgtoLbs(weight);
            defaultUnit = mContext.getString(R.string.LbsUnitLabel);
        }
        DecimalFormat numberFormat = new DecimalFormat("#.##");
        return numberFormat.format(weight) + defaultUnit;
    }

    private String distanceToString(float distance, int unit) {
        String defaultUnit = mContext.getString(R.string.KmUnitLabel);
        if (unit == UnitConverter.UNIT_MILES) {
            distance = UnitConverter.KmToMiles(distance); // Always convert to KG
            defaultUnit = mContext.getString(R.string.MilesUnitLabel);
        }
        DecimalFormat numberFormat = new DecimalFormat("#.##");
        return numberFormat.format(distance) + defaultUnit;
    }

    private boolean isSeparatorNeeded(int position, String dateString, Cursor cursor) {
        // Add separator if needed
        if (position == 0) {
            return true;
        } else {
            cursor.moveToPosition(position - 1);
            String datePreviousString = cursor.getString(cursor.getColumnIndex(DAORecord.DATE));
            if (datePreviousString.compareTo(dateString) != 0) {
                return true;
            }
            cursor.moveToPosition(position);
        }

        return false;
    }

        // View lookup cache
    private static class ViewHolder {
        CardView CardView;
        TextView Separator;
        TextView ExerciseName;
        TextView Date;
        TextView Time;
        TextView FirstColValue;
        TextView FirstColLabel;
        LinearLayout SecondColLayout;
        TextView SecondColValue;
        TextView SecondColLabel;
        TextView ThirdColValue;
        TextView ThirdColLabel;
        ImageView BtAction3;
        ImageView BtAction2;
        ImageView BtAction1;
    }
}
