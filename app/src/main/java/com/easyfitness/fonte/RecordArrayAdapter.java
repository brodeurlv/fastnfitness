package com.easyfitness.fonte;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.devzone.fillprogresslayout.FillProgressLayout;
import com.easyfitness.BtnClickListener;
import com.easyfitness.CountdownDialogbox;
import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.DAO.Machine;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.program.DAOProgram;
import com.easyfitness.DAO.program.Program;
import com.easyfitness.DAO.record.DAOFonte;
import com.easyfitness.DAO.record.DAORecord;
import com.easyfitness.DAO.record.DAOStatic;
import com.easyfitness.DAO.record.Record;
import com.easyfitness.MainActivity;
import com.easyfitness.R;
import com.easyfitness.RecordEditorDialogbox;
import com.easyfitness.enums.DisplayType;
import com.easyfitness.enums.DistanceUnit;
import com.easyfitness.enums.ExerciseType;
import com.easyfitness.enums.ProgramRecordStatus;
import com.easyfitness.enums.RecordType;
import com.easyfitness.enums.WeightUnit;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.Keyboard;
import com.easyfitness.utils.OnCustomEventListener;
import com.easyfitness.utils.UnitConverter;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class RecordArrayAdapter extends ArrayAdapter {

    private final DAOProgram mDbWorkout;
    private final Activity mActivity;
    private final int mFirstColorOdd = 0;
    private final Context mContext;
    private final DisplayType mDisplayType;
    private final DAORecord mDbRecord;
    List<Record> mRecordList;
    private BtnClickListener mAction2ClickListener = null;
    private OnCustomEventListener mProgramCompletedListener;

    public RecordArrayAdapter(Activity activity, Context context, List<Record> objects, DisplayType displayType, BtnClickListener clickAction2) {
        super(context, R.layout.row_fonte, objects);
        mActivity = activity;
        mContext = context;
        //mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDisplayType = displayType;
        mRecordList = objects;
        mAction2ClickListener = clickAction2;
        mDbRecord = new DAORecord(context);
        mDbWorkout = new DAOProgram(context);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        Record record = mRecordList.get(position);
        ViewHolder viewHolder;

        if (view == null) {

            // inflate the layout
            LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = mInflater.inflate(R.layout.row_fonte, null);

            viewHolder = new ViewHolder();
            viewHolder.ExerciseName = view.findViewById(R.id.MACHINE_CELL);
            viewHolder.CardView = view.findViewById(R.id.CARDVIEW);

            viewHolder.Separator = view.findViewById(R.id.SEPARATOR_CELL);
            viewHolder.Date = view.findViewById(R.id.DATE_CELL);
            viewHolder.Time = view.findViewById(R.id.TIME_CELL);
            viewHolder.FirstColValue = view.findViewById(R.id.SERIE_CELL);
            viewHolder.FirstColLabel = view.findViewById(R.id.SERIE_LABEL);
            viewHolder.SecondColValue = view.findViewById(R.id.REPETITION_CELL);
            viewHolder.SecondColLabel = view.findViewById(R.id.REP_LABEL);
            viewHolder.ThirdColValue = view.findViewById(R.id.POIDS_CELL);
            viewHolder.ThirdColLabel = view.findViewById(R.id.WEIGHT_LABEL);
            viewHolder.BtActionDelete = view.findViewById(R.id.deleteButton);
            viewHolder.BtActionMoveUp = view.findViewById(R.id.moveUpButton);
            viewHolder.BtActionMoveDown = view.findViewById(R.id.moveDownButton);
            viewHolder.BtActionSuccess = view.findViewById(R.id.successButton);
            viewHolder.BtActionFailed = view.findViewById(R.id.failedButton);
            viewHolder.BtActionEdit = view.findViewById(R.id.editButton);
            viewHolder.BtActionCopy = view.findViewById(R.id.copyButton);

            viewHolder.SecondColumn = view.findViewById(R.id.second_column);
            viewHolder.ProgramName = view.findViewById(R.id.TEMPLATE_NAME_CELL);
            viewHolder.TemplateFirstColLabel = view.findViewById(R.id.TEMPLATE_SERIE_CELL);
            viewHolder.TemplateSecondColLabel = view.findViewById(R.id.TEMPLATE_REPETITION_CELL);
            viewHolder.TemplateThirdColValue = view.findViewById(R.id.TEMPLATE_POIDS_CELL);

            viewHolder.RestTimeCardView = view.findViewById(R.id.restTimeCardView);
            viewHolder.RestTimeProgressLayout = view.findViewById(R.id.restTimeProgressLayout);
            viewHolder.RestTimeTextView = view.findViewById(R.id.restTimeTextView);

            UpdateDisplayTypeUI(viewHolder);

            // store the holder with the view.
            view.setTag(viewHolder);
        } else {
            // we've just avoided calling findViewById() on resource everytime
            // just use the viewHolder
            viewHolder = (ViewHolder) view.getTag();
        }

        if (position % 2 == mFirstColorOdd) {
            viewHolder.CardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.record_background_odd));
            viewHolder.RestTimeCardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.record_background_odd));
            viewHolder.RestTimeProgressLayout.setBackgroundResource(R.color.record_background_odd);
        } else {
            viewHolder.CardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.record_background_even));
            viewHolder.RestTimeCardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.record_background_even));
            viewHolder.RestTimeProgressLayout.setBackgroundResource(R.color.record_background_even);
        }

        // Common display
        UpdateRecordTypeUI(record, viewHolder);
        UpdateValues(record, position, viewHolder);

        return view;
    }

    private void UpdateValues(Record record, int position, ViewHolder viewHolder) {
        ExerciseType exerciseType = record.getExerciseType();

        viewHolder.BtActionDelete.setTag(record.getId());
        viewHolder.BtActionDelete.setOnClickListener(v -> showDeleteDialog(record));

        viewHolder.BtActionEdit.setTag(record.getId());
        viewHolder.BtActionEdit.setOnClickListener(v -> showEditorDialog(record, position, viewHolder));

        if (record.getProgramRecordStatus() == ProgramRecordStatus.PENDING) {
            viewHolder.FirstColValue.setText("-");
            viewHolder.SecondColValue.setText("-");
            viewHolder.ThirdColValue.setText("-");
        } else {
            if (exerciseType == ExerciseType.STRENGTH) {
                viewHolder.FirstColValue.setText(String.valueOf(record.getSets()));
                viewHolder.SecondColValue.setText(String.valueOf(record.getReps()));
                viewHolder.ThirdColValue.setText(weigthToString(record.getWeightInKg(), record.getWeightUnit()));
            } else if (exerciseType == ExerciseType.ISOMETRIC) {
                viewHolder.FirstColValue.setText(String.valueOf(record.getSets()));
                viewHolder.SecondColValue.setText(String.valueOf(record.getSeconds()));
                viewHolder.ThirdColValue.setText(weigthToString(record.getWeightInKg(), record.getWeightUnit()));
            } else if (exerciseType == ExerciseType.CARDIO) {
                viewHolder.FirstColValue.setText(distanceToString(record.getDistanceInKm(), record.getDistanceUnit()));
                viewHolder.ThirdColValue.setText(DateConverter.durationToHoursMinutesSecondsStr(record.getDuration()));
            }
        }

        if (record.getProgramId() != -1 && record.getRecordType()== RecordType.PROGRAM_RECORD) {
            // get program name
            Program program = mDbWorkout.get(record.getProgramId());
            //Record templateRecord = mDbRecord.getRecord(record.getTemplateRecordId());
            if (program != null) {
                showTemplateRow(View.VISIBLE, viewHolder);
                viewHolder.ProgramName.setText(program.getName());
                //if (templateRecord != null) {
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
                //}
            }
        } else {
            showTemplateRow(View.GONE, viewHolder);
        }

        if (mDisplayType == DisplayType.FREE_WORKOUT_DISPLAY || mDisplayType == DisplayType.HISTORY_DISPLAY) {
            viewHolder.ExerciseName.setText(record.getExercise());
            viewHolder.Date.setText(DateConverter.dateToLocalDateStr(record.getDate(), mContext));
            viewHolder.Time.setText(DateConverter.dateToLocalTimeStr(record.getDate(), mContext));

            if (isSeparatorNeeded(position, record.getDate())) {
                viewHolder.Separator.setText(String.format("- %s -", DateConverter.dateToLocalDateStr(record.getDate(), mContext)));
                viewHolder.Separator.setVisibility(View.VISIBLE);
            } else {
                viewHolder.Separator.setText("");
                viewHolder.Separator.setVisibility(View.GONE);
            }

            if (mDisplayType == DisplayType.HISTORY_DISPLAY) {
                viewHolder.BtActionCopy.setVisibility(View.GONE);
            } else {
                viewHolder.BtActionCopy.setTag(record.getId());
                viewHolder.BtActionCopy.setOnClickListener(v -> {
                    if (mAction2ClickListener != null)
                        mAction2ClickListener.onBtnClick(v);
                });
            }
        } else if (mDisplayType == DisplayType.PROGRAM_EDIT_DISPLAY) {
            viewHolder.ExerciseName.setText(record.getExercise());

            viewHolder.Separator.setVisibility(View.GONE);

            if (record.getTemplateRestTime() != 0) {
                viewHolder.RestTimeCardView.setVisibility(View.VISIBLE);
                viewHolder.RestTimeTextView.setText(getContext().getString(R.string.rest_time_row) + record.getTemplateRestTime() + getContext().getString(R.string.sec));
            } else {
                viewHolder.RestTimeCardView.setVisibility(View.GONE);
                viewHolder.RestTimeTextView.setText("No Rest");
            }
            viewHolder.RestTimeProgressLayout.setProgress(0, false);


            viewHolder.BtActionMoveDown.setTag(record.getId());
            viewHolder.BtActionMoveDown.setOnClickListener(v -> {
                // Go DOWN
                int oldIndex = mRecordList.indexOf(record);
                if (oldIndex == mRecordList.size() - 1) return;

                Collections.swap(mRecordList, oldIndex + 1, oldIndex);
                Record record1 = mRecordList.get(oldIndex + 1);
                record1.setTemplateOrder(mRecordList.indexOf(record1));
                mDbRecord.updateRecord(record1);
                Record record2 = mRecordList.get(oldIndex);
                record2.setTemplateOrder(mRecordList.indexOf(record2));
                mDbRecord.updateRecord(record2);
                notifyDataSetChanged();
            });


            viewHolder.BtActionMoveUp.setTag(record.getId());
            viewHolder.BtActionMoveUp.setOnClickListener(v -> {
                // Go UP
                int oldIndex = mRecordList.indexOf(record);
                if (oldIndex == 0) return;

                Collections.swap(mRecordList, oldIndex - 1, oldIndex);
                Record record1 = mRecordList.get(oldIndex - 1);
                record1.setTemplateOrder(mRecordList.indexOf(record1));
                mDbRecord.updateRecord(record1);
                Record record2 = mRecordList.get(oldIndex);
                record2.setTemplateOrder(mRecordList.indexOf(record2));
                mDbRecord.updateRecord(record2);
                notifyDataSetChanged();
            });
        } else if (mDisplayType == DisplayType.PROGRAM_RUNNING_DISPLAY || mDisplayType == DisplayType.PROGRAM_PREVIEW_DISPLAY) {
            viewHolder.ExerciseName.setText(record.getExercise());
            viewHolder.Separator.setVisibility(View.GONE);

            if (record.getTemplateRestTime() != 0) {
                viewHolder.RestTimeCardView.setVisibility(View.VISIBLE);
                viewHolder.RestTimeTextView.setText(getContext().getString(R.string.rest_time_row) + record.getTemplateRestTime() + getContext().getString(R.string.sec));
            } else {
                viewHolder.RestTimeCardView.setVisibility(View.GONE);
                viewHolder.RestTimeTextView.setText("No Rest");
            }

            if (mDisplayType == DisplayType.PROGRAM_PREVIEW_DISPLAY) {
                viewHolder.Date.setText("");
                viewHolder.Time.setText("");
                viewHolder.RestTimeProgressLayout.setProgress(0, false);
            } else {
                if (record.getProgramRecordStatus() == ProgramRecordStatus.SUCCESS || record.getProgramRecordStatus() == ProgramRecordStatus.FAILED) {
                    viewHolder.Date.setText(DateConverter.dateToLocalDateStr(record.getDate(), mContext));
                    viewHolder.Time.setText(DateConverter.dateToLocalTimeStr(record.getDate(), mContext));
                }

                if (record.getProgramRecordStatus() == ProgramRecordStatus.SUCCESS) {
                    viewHolder.BtActionSuccess.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_check_active));
                    viewHolder.BtActionSuccess.setBackgroundColor(Color.parseColor("#00AF80"));
                    viewHolder.BtActionFailed.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_cross_inactive));
                    viewHolder.BtActionFailed.setBackgroundColor(Color.TRANSPARENT);
                    if (!chronoOnGoing) viewHolder.RestTimeProgressLayout.setProgress(100, false);
                } else if (record.getProgramRecordStatus() == ProgramRecordStatus.FAILED) {
                    viewHolder.BtActionSuccess.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_check_inactive));
                    viewHolder.BtActionSuccess.setBackgroundColor(Color.TRANSPARENT);
                    viewHolder.BtActionFailed.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_cross_active));
                    viewHolder.BtActionFailed.setBackgroundColor(Color.RED);
                    if (!chronoOnGoing) viewHolder.RestTimeProgressLayout.setProgress(100, false);
                } else { // PENDING and NONE
                    viewHolder.BtActionSuccess.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_check_inactive));
                    viewHolder.BtActionSuccess.setBackgroundColor(Color.TRANSPARENT);
                    viewHolder.BtActionFailed.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_cross_inactive));
                    viewHolder.BtActionFailed.setBackgroundColor(Color.TRANSPARENT);
                    viewHolder.RestTimeProgressLayout.setProgress(0, false);
                }
            }
            long key = record.getId();

            Record templateRecord = mDbRecord.getRecord(record.getTemplateRecordId());

            viewHolder.BtActionSuccess.setTag(key);
            viewHolder.BtActionSuccess.setOnClickListener(v -> {
                if (mDisplayType == DisplayType.PROGRAM_RUNNING_DISPLAY) {
                    if (record.getProgramRecordStatus() != ProgramRecordStatus.SUCCESS) {
                        record.setSets(templateRecord.getSets());
                        record.setReps(templateRecord.getReps());
                        record.setWeightInKg(templateRecord.getWeightInKg());
                        record.setWeightUnit(templateRecord.getWeightUnit());
                        record.setSeconds(templateRecord.getSeconds());
                        record.setDistanceInKm(templateRecord.getDistanceInKm());
                        record.setDistanceUnit(templateRecord.getDistanceUnit());
                        record.setDuration(templateRecord.getDuration());
                        record.setProgramRecordStatus(ProgramRecordStatus.SUCCESS);
                        record.setDate(DateConverter.getNewDate());
                        mDbRecord.updateRecord(record);
                        UpdateRecordTypeUI(record, viewHolder);
                        UpdateValues(record, position, viewHolder);
                        resetCountdown(record, viewHolder);
                        launchCountdown(record, viewHolder);
                        notifyDataSetChanged();
                        boolean programComplete = true;
                        for (Record rec : mRecordList) {
                            if (rec.getProgramRecordStatus() != ProgramRecordStatus.FAILED && rec.getProgramRecordStatus() != ProgramRecordStatus.SUCCESS) {
                                programComplete = false;
                                break;
                            }
                        }
                        if (programComplete) {
                            if (mProgramCompletedListener != null)
                                mProgramCompletedListener.onEvent("");
                        }
                    } else {
                        record.setProgramRecordStatus(ProgramRecordStatus.PENDING);
                        resetCountdown(record, viewHolder);
                        mDbRecord.updateRecord(record);
                        UpdateRecordTypeUI(record, viewHolder);
                        UpdateValues(record, position, viewHolder);
                        notifyDataSetChanged();
                    }
                } else {
                    KToast.errorToast(mActivity, mActivity.getString(R.string.please_start_program_first), Gravity.BOTTOM, KToast.LENGTH_AUTO);
                }
            });

            viewHolder.BtActionFailed.setTag(key);
            viewHolder.BtActionFailed.setOnClickListener(v -> {
                if (mDisplayType == DisplayType.PROGRAM_RUNNING_DISPLAY) {
                    if (record.getProgramRecordStatus() != ProgramRecordStatus.FAILED) {
                        //Display Editor
                        record.setDate(DateConverter.getNewDate());
                        record.setProgramRecordStatus(ProgramRecordStatus.FAILED);
                        mDbRecord.updateRecord(record);
                        resetCountdown(record, viewHolder);
                        launchCountdown(record, viewHolder);
                        UpdateValues(record, position, viewHolder);
                        showEditorDialog(record, position, viewHolder);
                        boolean programComplete = true;
                        for (Record rec : mRecordList) {
                            if (rec.getProgramRecordStatus() != ProgramRecordStatus.FAILED && rec.getProgramRecordStatus() != ProgramRecordStatus.SUCCESS) {
                                programComplete = false;
                                break;
                            }
                        }
                        if (programComplete) {
                            if (mProgramCompletedListener != null)
                                mProgramCompletedListener.onEvent("");
                        }
                    } else {
                        record.setProgramRecordStatus(ProgramRecordStatus.PENDING);
                        resetCountdown(record, viewHolder);
                        mDbRecord.updateRecord(record);
                        UpdateRecordTypeUI(record, viewHolder);
                        UpdateValues(record, position, viewHolder);
                        notifyDataSetChanged();
                    }
                } else {
                    KToast.errorToast(mActivity, mActivity.getString(R.string.please_start_program_first), Gravity.BOTTOM, KToast.LENGTH_AUTO);
                }
            });
        }
    }

    private void showTemplateRow(int visibility, ViewHolder viewHolder) {
        viewHolder.ProgramName.setVisibility(visibility);
        viewHolder.TemplateFirstColLabel.setVisibility(visibility);
        viewHolder.TemplateSecondColLabel.setVisibility(visibility);
        viewHolder.TemplateThirdColValue.setVisibility(visibility);
    }

    private void showEditorDialog(Record record, int position, ViewHolder viewHolder) {
        RecordEditorDialogbox recordEditorDialogbox = new RecordEditorDialogbox(mActivity, record, mDisplayType == DisplayType.PROGRAM_EDIT_DISPLAY);
        recordEditorDialogbox.setOnCancelListener(dialog -> {
            if (mDisplayType == DisplayType.PROGRAM_RUNNING_DISPLAY) {
                //record.setProgramRecordStatus(ProgramRecordStatus.PENDING);
                //mDbRecord.updateRecord(record);
                //UpdateRecordTypeUI(record, viewHolder);
                //UpdateValues(record, position, viewHolder);
                notifyDataSetChanged();
            }
            Keyboard.hide(getContext(), viewHolder.CardView);
        });
        recordEditorDialogbox.setOnDismissListener(dialog -> {
            if (!recordEditorDialogbox.isCancelled()) {
                notifyDataSetChanged();
                Keyboard.hide(getContext(), viewHolder.CardView);
            }
        });
        recordEditorDialogbox.show();
    }

    private void UpdateRecordTypeUI(Record record, ViewHolder viewHolder) {
        ExerciseType pExerciseType = record.getExerciseType();

        switch (pExerciseType) {
            case CARDIO:
                viewHolder.SecondColumn.setVisibility(View.GONE);
                viewHolder.FirstColLabel.setText(mContext.getString(R.string.DistanceLabel));
                viewHolder.ThirdColLabel.setText(mContext.getString(R.string.DurationLabel));
                break;
            case STRENGTH:
                viewHolder.SecondColumn.setVisibility(View.VISIBLE);
                viewHolder.FirstColLabel.setText(mContext.getString(R.string.SerieLabel));
                viewHolder.SecondColLabel.setText(mContext.getString(R.string.RepetitionLabel_short));
                viewHolder.ThirdColLabel.setText(mContext.getString(R.string.PoidsLabel));
                break;
            case ISOMETRIC:
                viewHolder.SecondColumn.setVisibility(View.VISIBLE);
                viewHolder.FirstColLabel.setText(mContext.getString(R.string.SerieLabel));
                viewHolder.SecondColLabel.setText(mContext.getString(R.string.SecondsLabel_short));
                viewHolder.ThirdColLabel.setText(mContext.getString(R.string.PoidsLabel));
                break;
        }
    }

    private void UpdateDisplayTypeUI(ViewHolder viewHolder) {
        switch (mDisplayType) {
            case FREE_WORKOUT_DISPLAY:
                viewHolder.BtActionSuccess.setVisibility(View.GONE);
                viewHolder.BtActionFailed.setVisibility(View.GONE);
                viewHolder.BtActionMoveDown.setVisibility(View.GONE);
                viewHolder.BtActionMoveUp.setVisibility(View.GONE);
                viewHolder.BtActionCopy.setVisibility(View.VISIBLE);
                viewHolder.BtActionEdit.setVisibility(View.VISIBLE);
                viewHolder.BtActionDelete.setVisibility(View.VISIBLE);
                viewHolder.RestTimeCardView.setVisibility(View.GONE);
                break;
            case HISTORY_DISPLAY:
                viewHolder.BtActionSuccess.setVisibility(View.GONE);
                viewHolder.BtActionFailed.setVisibility(View.GONE);
                viewHolder.BtActionMoveDown.setVisibility(View.GONE);
                viewHolder.BtActionMoveUp.setVisibility(View.GONE);
                viewHolder.BtActionCopy.setVisibility(View.GONE);
                viewHolder.BtActionEdit.setVisibility(View.VISIBLE);
                viewHolder.BtActionDelete.setVisibility(View.VISIBLE);
                viewHolder.RestTimeCardView.setVisibility(View.GONE);
                break;
            case PROGRAM_EDIT_DISPLAY:
                viewHolder.BtActionSuccess.setVisibility(View.GONE);
                viewHolder.BtActionFailed.setVisibility(View.GONE);
                viewHolder.BtActionMoveDown.setVisibility(View.VISIBLE);
                viewHolder.BtActionMoveUp.setVisibility(View.VISIBLE);
                viewHolder.BtActionCopy.setVisibility(View.GONE);
                viewHolder.BtActionEdit.setVisibility(View.VISIBLE);
                viewHolder.BtActionDelete.setVisibility(View.VISIBLE);
                viewHolder.RestTimeCardView.setVisibility(View.VISIBLE);
                break;
            case PROGRAM_PREVIEW_DISPLAY:
                viewHolder.BtActionSuccess.setVisibility(View.GONE);
                viewHolder.BtActionFailed.setVisibility(View.GONE);
                viewHolder.BtActionMoveDown.setVisibility(View.GONE);
                viewHolder.BtActionMoveUp.setVisibility(View.GONE);
                viewHolder.BtActionCopy.setVisibility(View.GONE);
                viewHolder.BtActionEdit.setVisibility(View.GONE);
                viewHolder.BtActionDelete.setVisibility(View.GONE);
                viewHolder.RestTimeCardView.setVisibility(View.VISIBLE);
                break;
            case PROGRAM_RUNNING_DISPLAY:
                viewHolder.BtActionSuccess.setVisibility(View.VISIBLE);
                viewHolder.BtActionFailed.setVisibility(View.VISIBLE);
                viewHolder.BtActionMoveDown.setVisibility(View.GONE);
                viewHolder.BtActionMoveUp.setVisibility(View.GONE);
                viewHolder.BtActionCopy.setVisibility(View.GONE);
                viewHolder.BtActionEdit.setVisibility(View.VISIBLE);
                viewHolder.BtActionDelete.setVisibility(View.VISIBLE);
                viewHolder.RestTimeCardView.setVisibility(View.VISIBLE);
                break;
        }
    }

    private boolean chronoOnGoing = false;

    private void resetCountdown(Record record, ViewHolder viewHolder) {
        if (viewHolder.countDownTimer!=null) viewHolder.countDownTimer.cancel();
        viewHolder.RestTimeProgressLayout.setProgress(0, false);
    }

    private void launchCountdown(Record record, ViewHolder viewHolder) {
        if (record.getTemplateRestTime() > 0) {
            DAOMachine mDbMachine = new DAOMachine(getContext());
            Machine lMachine = mDbMachine.getMachine(record.getExerciseId());
            if (lMachine == null) return;

            viewHolder.RestTimeProgressLayout.setProgress(0, false);
            viewHolder.RestTimeProgressLayout.setDuration(record.getTemplateRestTime() * 1000);

            chronoOnGoing = true;
            final int[] progress = {1};

            if (viewHolder.countDownTimer!=null) {
                viewHolder.countDownTimer.cancel();
                viewHolder.countDownTimer = null;
            }

            viewHolder.countDownTimer = new CountDownTimer(record.getTemplateRestTime()*1000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        viewHolder.RestTimeProgressLayout.setProgress((progress[0] * 100 / record.getTemplateRestTime()), false);
                        progress[0]++;
                    }

                    public void onFinish() {
                        chronoOnGoing = false;
                    }
                };

            viewHolder.countDownTimer.start();

            viewHolder.RestTimeProgressLayout.setOnClickListener(v -> {
                viewHolder.RestTimeProgressLayout.setProgress(100, false);
                progress[0] = record.getTemplateRestTime();
                viewHolder.countDownTimer.cancel();
                chronoOnGoing = false;
            });

            CountdownDialogbox cdd = new CountdownDialogbox(mActivity, record.getTemplateRestTime(), lMachine);
            // Launch Countdown
            if (record.getExerciseType() == ExerciseType.STRENGTH) {
                DAOFonte mDbBodyBuilding = new DAOFonte(getContext());
                float iTotalWeightSession = mDbBodyBuilding.getTotalWeightSession(record.getDate(), getProfile());
                float iTotalWeight = mDbBodyBuilding.getTotalWeightMachine(record.getDate(), record.getExercise(), getProfile());
                int iNbSeries = mDbBodyBuilding.getSets(record.getDate(), record.getExercise(), getProfile());
                cdd.setNbSets(iNbSeries);
                cdd.setTotalWeightMachine(iTotalWeight);
                cdd.setTotalWeightSession(iTotalWeightSession);
            } else if (record.getExerciseType() == ExerciseType.ISOMETRIC) {
                DAOStatic mDbIsometric = new DAOStatic(getContext());
                float iTotalWeightSession = mDbIsometric.getTotalWeightSession(record.getDate(), getProfile());
                float iTotalWeight = mDbIsometric.getTotalWeightMachine(record.getDate(), record.getExercise(), getProfile());
                int iNbSeries = mDbIsometric.getSets(record.getDate(), record.getExercise(), getProfile());
                cdd.setNbSets(iNbSeries);
                cdd.setTotalWeightMachine(iTotalWeight);
                cdd.setTotalWeightSession(iTotalWeightSession);
            }
            cdd.show();
        }
    }

    private String weigthToString(float weight, WeightUnit unit) {
        weight = UnitConverter.weightConverter(weight, WeightUnit.KG, unit);
        DecimalFormat numberFormat = new DecimalFormat("#.##");
        return numberFormat.format(weight) + unit.toString();
    }

    private String distanceToString(float distance, DistanceUnit unit) {
        String defaultUnit = mContext.getString(R.string.KmUnitLabel);
        if (unit == DistanceUnit.MILES) {
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
            String dateString = DateConverter.dateTimeToDBDateStr(date);
            String datePreviousString = DateConverter.dateTimeToDBDateStr(datePrevious);
            return !datePreviousString.equals(dateString);
        }
    }

    public void setRecords(List<Record> data) {
        mRecordList.clear();
        mRecordList.addAll(data);
        notifyDataSetChanged();
    }

    public DisplayType getDisplayType() {
        return mDisplayType;
    }

    private void showDeleteDialog(final Record record) {

        new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getContext().getString(R.string.DeleteRecordDialog))
                .setContentText(getContext().getString(R.string.areyousure))
                .setCancelText(getContext().getString(R.string.global_no))
                .setConfirmText(getContext().getString(R.string.global_yes))
                .showCancelButton(true)
                .setConfirmClickListener(sDialog -> {
                    int ret = mDbRecord.deleteRecord(record.getId());
                    if (ret != 0) mRecordList.remove(record);
                    notifyDataSetChanged();

                    KToast.infoToast(mActivity, getContext().getString(R.string.removedid), Gravity.BOTTOM, KToast.LENGTH_LONG);
                    sDialog.dismissWithAnimation();
                })
                .show();
    }

    public void setOnProgramCompletedListener(OnCustomEventListener eventListener) {
        mProgramCompletedListener = eventListener;
    }

    private Profile getProfile() {
        return ((MainActivity) mActivity).getCurrentProfile();
    }

    // View lookup cache
    private static class ViewHolder {
        CountDownTimer countDownTimer;

        CardView CardView;
        TextView Separator;
        TextView ExerciseName;
        TextView Date;
        TextView Time;
        TextView FirstColValue;
        TextView FirstColLabel;
        LinearLayout SecondColumn;
        TextView SecondColValue;
        TextView SecondColLabel;
        TextView ThirdColValue;
        TextView ThirdColLabel;

        TextView ProgramName;
        TextView TemplateFirstColLabel;
        TextView TemplateSecondColLabel;
        TextView TemplateThirdColValue;

        ImageView BtActionDelete;
        ImageView BtActionEdit;
        ImageView BtActionCopy;
        ImageView BtActionMoveUp;
        ImageView BtActionMoveDown;
        ImageView BtActionFailed;
        ImageView BtActionSuccess;

        CardView RestTimeCardView;
        FillProgressLayout RestTimeProgressLayout;
        TextView RestTimeTextView;
    }
}
