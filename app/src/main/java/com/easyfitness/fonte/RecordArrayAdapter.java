package com.easyfitness.fonte;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easyfitness.BtnClickListener;
import com.easyfitness.DAO.record.DAORecord;
import com.easyfitness.DAO.record.Record;
import com.easyfitness.DAO.workout.DAOWorkout;
import com.easyfitness.DAO.workout.Workout;
import com.easyfitness.enums.DisplayType;
import com.easyfitness.enums.ExerciseType;
import com.easyfitness.enums.RecordType;
import com.easyfitness.R;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.UnitConverter;
import com.easyfitness.enums.ProgramRecordStatus;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

public class RecordArrayAdapter extends ArrayAdapter<Record> {

    private final DAOWorkout mDbWorkout;
    private LayoutInflater mInflater;
    private int mFirstColorOdd = 0;
    private Context mContext;
    private DisplayType mDisplayType;
    private DAORecord mDbRecord;
    List<Record> mRecordList;
    private BtnClickListener mAction2ClickListener = null;

    public RecordArrayAdapter(Context context, List<Record> objects, DisplayType displayType, BtnClickListener clickAction2) {
        super(context, R.layout.row_fonte, objects);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDisplayType = displayType;
        mRecordList = objects;
        mAction2ClickListener = clickAction2;
        mDbRecord = new DAORecord(context);
        mDbWorkout = new DAOWorkout(context);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        Record record = mRecordList.get(position);
        ViewHolder viewHolder;

        if (view == null) {

            // inflate the layout
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            view = inflater.inflate(R.layout.row_fonte, parent, false);

            viewHolder = new ViewHolder();
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
            viewHolder.BtActionRight = view.findViewById(R.id.action1Button);
            viewHolder.BtActionMiddle = view.findViewById(R.id.action2Button);
            viewHolder.BtActionLeft = view.findViewById(R.id.action3Button);

            viewHolder.TemplateName = view.findViewById(R.id.TEMPLATE_NAME_CELL);
            viewHolder.TemplateFirstColLabel = view.findViewById(R.id.TEMPLATE_SERIE_CELL);
            viewHolder.TemplateSecondColLabel = view.findViewById(R.id.TEMPLATE_REPETITION_CELL);
            viewHolder.TemplateThirdColValue = view.findViewById(R.id.TEMPLATE_POIDS_CELL);

            // store the holder with the view.
            view.setTag(viewHolder);
        } else {
            // we've just avoided calling findViewById() on resource everytime
            // just use the viewHolder
            viewHolder = (ViewHolder) view.getTag();
        }

        if (position % 2 == mFirstColorOdd) {
            viewHolder.CardView.setBackgroundColor(mContext.getResources().getColor(R.color.record_background_odd));
        } else {
            viewHolder.CardView.setBackgroundColor(mContext.getResources().getColor(R.color.record_background_even));
        }

        /* Commun display */
        UpdateUI(record, viewHolder);
        UpdateValues(record, position, viewHolder);

        return view;
    }

    private void UpdateValues(Record record, int position, ViewHolder viewHolder) {
        ExerciseType exerciseType = record.getExerciseType();

        viewHolder.BtActionRight.setTag(record.getId());
        viewHolder.BtActionRight.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_clear_black_24dp));
        viewHolder.BtActionRight.setOnClickListener(v -> {
            int ret = mDbRecord.deleteRecord(record.getId());
            if (ret!=0) mRecordList.remove(record);
            notifyDataSetChanged();
        });

        if (mDisplayType == DisplayType.FREE_WORKOUT_DISPLAY || mDisplayType==DisplayType.ALL_WORKOUT_DISPLAY) {
            viewHolder.ExerciseName.setText(record.getExercise());
            viewHolder.Date.setText(DateConverter.dateToLocalDateStr(record.getDate(), mContext));
            viewHolder.Time.setText(record.getTime());

            if (isSeparatorNeeded(position, record.getDate())) {
                viewHolder.Separator.setText("- " + DateConverter.dateToLocalDateStr(record.getDate(), mContext) + " -");
                viewHolder.Separator.setVisibility(View.VISIBLE);
            } else {
                viewHolder.Separator.setText("");
                viewHolder.Separator.setVisibility(View.GONE);
            }

            if (exerciseType == ExerciseType.STRENGTH) {
                viewHolder.FirstColValue.setText(String.valueOf(record.getSets()));
                viewHolder.SecondColValue.setText(String.valueOf(record.getReps()));
                viewHolder.ThirdColValue.setText(weigthToString(record.getWeight(), record.getWeightUnit()));
            } else if (exerciseType == ExerciseType.ISOMETRIC) {
                viewHolder.FirstColValue.setText(String.valueOf(record.getSets()));
                viewHolder.SecondColValue.setText(String.valueOf(record.getSecond()));
                viewHolder.ThirdColValue.setText(weigthToString(record.getWeight(), record.getWeightUnit()));
            } else if (exerciseType == ExerciseType.CARDIO) {
                viewHolder.FirstColValue.setText(distanceToString(record.getDistance(), record.getDistanceUnit()));
                viewHolder.ThirdColValue.setText(DateConverter.durationToHoursMinutesSecondsStr(record.getDuration()));
            }

            viewHolder.BtActionMiddle.setTag(record.getId());
            viewHolder.BtActionMiddle.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_back));
            viewHolder.BtActionMiddle.setRotation(90);
            viewHolder.BtActionMiddle.setOnClickListener(v -> {
                if (mAction2ClickListener != null)
                    mAction2ClickListener.onBtnClick((long) v.getTag());
            });

            viewHolder.BtActionLeft.setVisibility(View.GONE);

            if(mDisplayType==DisplayType.ALL_WORKOUT_DISPLAY && record.getTemplateId()!=-1){
                // get program name
                Workout workout = mDbWorkout.get(record.getTemplateId());
                if (workout!=null) {
                    viewHolder.TemplateName.setVisibility(View.VISIBLE);
                    viewHolder.TemplateFirstColLabel.setVisibility(View.VISIBLE);
                    viewHolder.TemplateSecondColLabel.setVisibility(View.VISIBLE);
                    viewHolder.TemplateThirdColValue.setVisibility(View.VISIBLE);
                    viewHolder.TemplateName.setText(workout.getName());
                    if (exerciseType == ExerciseType.STRENGTH) {
                        viewHolder.TemplateFirstColLabel.setText(String.valueOf(record.getTemplateSets()));
                        viewHolder.TemplateSecondColLabel.setText(String.valueOf(record.getTemplateReps()));
                        viewHolder.TemplateThirdColValue.setText(weigthToString(record.getTemplateWeight(), record.getTemplateWeightUnit()));
                    } else if (exerciseType == ExerciseType.ISOMETRIC) {
                        viewHolder.TemplateFirstColLabel.setText(String.valueOf(record.getTemplateSets()));
                        viewHolder.TemplateSecondColLabel.setText(String.valueOf(record.getTemplateSeconds()));
                        viewHolder.TemplateThirdColValue.setText(weigthToString(record.getTemplateWeight(), record.getTemplateWeightUnit()));
                    } else if (exerciseType == ExerciseType.CARDIO) {
                        viewHolder.TemplateFirstColLabel.setText(distanceToString(record.getTemplateDistance(), record.getTemplateDistanceUnit()));
                        viewHolder.TemplateThirdColValue.setText(DateConverter.durationToHoursMinutesSecondsStr(record.getTemplateDuration()));
                    }
                }
            } else {
                viewHolder.TemplateName.setVisibility(View.GONE);
                viewHolder.TemplateFirstColLabel.setVisibility(View.GONE);
                viewHolder.TemplateSecondColLabel.setVisibility(View.GONE);
                viewHolder.TemplateThirdColValue.setVisibility(View.GONE);
            }

        } else if (mDisplayType == DisplayType.PROGRAM_EDIT_DISPLAY) {
            viewHolder.ExerciseName.setText(record.getTemplateOrder() + ":" + record.getExercise());

            viewHolder.Separator.setVisibility(View.GONE);

            if (exerciseType == ExerciseType.STRENGTH) {
                viewHolder.FirstColValue.setText(String.valueOf(record.getTemplateSets()));
                viewHolder.SecondColValue.setText(String.valueOf(record.getTemplateReps()));
                viewHolder.ThirdColValue.setText(weigthToString(record.getTemplateWeight(), record.getTemplateWeightUnit()));
            } else if (exerciseType == ExerciseType.ISOMETRIC) {
                viewHolder.FirstColValue.setText(String.valueOf(record.getTemplateSets()));
                viewHolder.SecondColValue.setText(String.valueOf(record.getTemplateSeconds()));
                viewHolder.ThirdColValue.setText(weigthToString(record.getTemplateWeight(), record.getTemplateWeightUnit()));
            } else if (exerciseType == ExerciseType.CARDIO) {
                viewHolder.FirstColValue.setText(distanceToString(record.getTemplateDistance(), record.getTemplateDistanceUnit()));
                viewHolder.ThirdColValue.setText(DateConverter.durationToHoursMinutesSecondsStr(record.getTemplateDuration()));
            }



            viewHolder.BtActionMiddle.setTag(record.getId());
            viewHolder.BtActionMiddle.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_back));
            viewHolder.BtActionMiddle.setRotation(-90);
            viewHolder.BtActionMiddle.setOnClickListener(v -> {
                // Go DOWN
                int oldIndex = mRecordList.indexOf(record);
                if (oldIndex==mRecordList.size()-1) return;

                Collections.swap(mRecordList,oldIndex+1, oldIndex);
                Record record1 = mRecordList.get(oldIndex+1);
                record1.setTemplateOrder(mRecordList.indexOf(record1));
                mDbRecord.updateRecord(record1);
                Record record2 = mRecordList.get(oldIndex);
                record2.setTemplateOrder(mRecordList.indexOf(record2));
                notifyDataSetChanged();
            });

            viewHolder.BtActionLeft.setTag(record.getId());
            viewHolder.BtActionLeft.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_back));
            viewHolder.BtActionLeft.setRotation(+90);
            viewHolder.BtActionLeft.setOnClickListener(v -> {
                // Go UP
                int oldIndex = mRecordList.indexOf(record);
                if (oldIndex==0) return;

                Collections.swap(mRecordList,oldIndex-1, oldIndex);
                Record record1 = mRecordList.get(oldIndex-1);
                record1.setTemplateOrder(mRecordList.indexOf(record1));
                mDbRecord.updateRecord(record1);
                Record record2 = mRecordList.get(oldIndex);
                record2.setTemplateOrder(mRecordList.indexOf(record2));
                notifyDataSetChanged();
            });
        } else if (mDisplayType == DisplayType.PROGRAM_WORKOUT_DISPLAY || mDisplayType == DisplayType.PROGRAM_WORKOUT_PREVIEW_DISPLAY) {
            viewHolder.ExerciseName.setText(record.getExercise());
            viewHolder.Separator.setVisibility(View.GONE);

            if (exerciseType == ExerciseType.STRENGTH) {
                viewHolder.FirstColValue.setText(String.valueOf(record.getTemplateSets()));
                viewHolder.SecondColValue.setText(String.valueOf(record.getTemplateReps()));
                viewHolder.ThirdColValue.setText(weigthToString(record.getTemplateWeight(), record.getTemplateWeightUnit()));
            } else if (exerciseType == ExerciseType.ISOMETRIC) {
                viewHolder.FirstColValue.setText(String.valueOf(record.getTemplateSets()));
                viewHolder.SecondColValue.setText(String.valueOf(record.getTemplateSeconds()));
                viewHolder.ThirdColValue.setText(weigthToString(record.getTemplateWeight(), record.getTemplateWeightUnit()));
            } else if (exerciseType == ExerciseType.CARDIO) {
                viewHolder.FirstColValue.setText(distanceToString(record.getTemplateDistance(), record.getTemplateDistanceUnit()));
                viewHolder.ThirdColValue.setText(DateConverter.durationToHoursMinutesSecondsStr(record.getTemplateDuration()));
            }

            viewHolder.BtActionMiddle.setRotation(0);
            viewHolder.BtActionLeft.setRotation(0);
            if (record.getProgramRecordStatus()!=ProgramRecordStatus.PENDING && mDisplayType != DisplayType.PROGRAM_WORKOUT_PREVIEW_DISPLAY) {
                if (record.getProgramRecordStatus()==ProgramRecordStatus.SUCCESS) {
                    viewHolder.BtActionMiddle.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_check_green_24dp));
                    viewHolder.BtActionLeft.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_cross_white_24dp));
                } else {
                    viewHolder.BtActionMiddle.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_check_white_24dp));
                    viewHolder.BtActionLeft.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_cross_red_24dp));
                }
                viewHolder.Date.setText(DateConverter.dateToLocalDateStr(record.getDate(), mContext));
                viewHolder.Time.setText(record.getTime());
            } else {
                viewHolder.BtActionMiddle.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_check_white_24dp));
                viewHolder.BtActionLeft.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_cross_white_24dp));
                viewHolder.Date.setText("");
                viewHolder.Time.setText("");
            }

            long key = record.getId();

            if (mDisplayType == DisplayType.PROGRAM_WORKOUT_DISPLAY) {
                viewHolder.BtActionMiddle.setTag(key);
                viewHolder.BtActionMiddle.setOnClickListener(v -> {
                    if (record.getProgramRecordStatus()!=ProgramRecordStatus.SUCCESS) {
                        record.setSets(record.getTemplateSets());
                        record.setReps(record.getTemplateReps());
                        record.setWeight(record.getTemplateWeight());
                        record.setWeightUnit(record.getTemplateWeightUnit());
                        record.setSecond(record.getTemplateSeconds());
                        record.setDistance(record.getTemplateDistance());
                        record.setDistanceUnit(record.getTemplateDistanceUnit());
                        record.setDuration(record.getTemplateDuration());
                        record.setProgramRecordStatus(ProgramRecordStatus.SUCCESS);
                        record.setDate(DateConverter.getNewDate());
                        record.setTime(DateConverter.currentTime());
                        mDbRecord.updateRecord(record);
                        UpdateUI(record, viewHolder);
                        UpdateValues(record, position, viewHolder);
                        notifyDataSetChanged();
                    }
                });

                viewHolder.BtActionLeft.setTag(key);
                viewHolder.BtActionLeft.setOnClickListener(v -> {
                    if (record.getProgramRecordStatus()!=ProgramRecordStatus.FAILED) {
                        record.setProgramRecordStatus(ProgramRecordStatus.FAILED);
                        mDbRecord.updateRecord(record);
                        UpdateValues(record, position, viewHolder);
                        notifyDataSetChanged();
                    }
                });
            }

            viewHolder.BtActionRight.setVisibility(View.GONE);
        }
    }

    /*
     * @pColor : si 1 alors affiche la couleur Odd en premier. Sinon, a couleur Even.
     */
    public void setFirstColorOdd(int pColor) {
        mFirstColorOdd = pColor;
    }

    private void UpdateUI(Record record, ViewHolder viewHolder) {
        RecordType pRecordType = record.getRecordType();
        ExerciseType pExerciseType = record.getExerciseType();

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

    private boolean isSeparatorNeeded(int position, Date date) {
        // Add separator if needed
        if (position == 0) {
            return true;
        } else {
            Record record = mRecordList.get(position - 1);
            Date datePrevious = record.getDate();
            if (datePrevious.compareTo(date) != 0) {
                return true;
            }
        }

        return false;
    }

    public void setRecords(List<Record> data) {
        mRecordList.clear();
        mRecordList.addAll(data);
        notifyDataSetChanged();
    }

    public DisplayType getDisplayType() {
        return mDisplayType;
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
        TextView TemplateName;
        TextView TemplateFirstColLabel;
        TextView TemplateSecondColLabel;
        TextView TemplateThirdColValue;
        ImageView BtActionLeft;
        ImageView BtActionMiddle;
        ImageView BtActionRight;
    }
}
