package com.easyfitness.programs;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.easyfitness.DAO.DAOExerciseInProgram;
import com.easyfitness.DAO.ExerciseInProgram;
import com.easyfitness.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Created by senpl based on batra android-popup-remainder
 */

public class ExerciseInProgramAdapter extends RecyclerView.Adapter<ExerciseInProgramAdapter.ViewHolder> {

    private List<ExerciseInProgram> exerciseInProgramList;
    private IOnRecyclerItemLongClick mItemLongClickHandler;
    private final Handler mRetrievalHandler = new Handler();
    Context mContext;

    public ExerciseInProgramAdapter(Context context, Long programId, @Nullable IOnRecyclerItemLongClick itemLongClick) {
        if (itemLongClick != null) mItemLongClickHandler = itemLongClick;
        mContext = context;
        reload(programId, null);
    }

    public void reload(Long programId, final ReloadDoneCallback callback) {
        mRetrievalHandler.post(() -> {
            DAOExerciseInProgram daoExerciseInProgram = new DAOExerciseInProgram(mContext);
            setExercises(daoExerciseInProgram.getAllExerciseInProgram(programId));
            notifyDataSetChanged();
            if (callback != null) callback.onReloadDone();
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowExerciseInProgramView = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.row_exercise_in_program, parent, false);
        return new ViewHolder(rowExerciseInProgramView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ExerciseInProgram notification = getExercisesList().get(position);
        holder.setNotification(notification);
//        holder.setDeleteSeen();
    }

    @Override
    public int getItemCount() {
        if (getExercisesList() == null) return 0;
        return getExercisesList().size();
    }

    public List<ExerciseInProgram> getExercisesList() {
        return exerciseInProgramList;
    }

    private void setExercises(List<ExerciseInProgram> exerciseInProgramList) {
        this.exerciseInProgramList = exerciseInProgramList;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitleTextView;

        ViewHolder(View itemView) {
            super(itemView);

            mTitleTextView = itemView.findViewById(R.id.MACHINE_CELL);
//            mDescriptionTextView = itemView.findViewById(R.id.notification_description);
//            mTimestampTextView = itemView.findViewById(R.id.notification_timestamp);
//            mMarkSeenButton = itemView.findViewById(R.id.notification_mark_seen_button);
//            mSelectionCheckbox = itemView.findViewById(R.id.notification_selection_checkbox);

            if (mItemLongClickHandler != null) {
                itemView.setOnLongClickListener(v -> {
                    if (mItemLongClickHandler == null) return false;

                    mItemLongClickHandler.onItemLongClick(getAdapterPosition());
//                        setSelected(true);
                    return true;
                });
            }

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if(mSelectionCheckbox.getVisibility() == View.VISIBLE)
//                        setSelected(!mSelectionCheckbox.isChecked());
//                }
//            });
        }

        void setNotification(ExerciseInProgram exercise) {
            String exerciseName = exercise.getExerciseName();
            mTitleTextView.setText(exerciseName);
//            mDescriptionTextView.setText(mNotification.getNote());


//            mMarkSeenButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if(mNotification.isSeen()) return;
//
//                    mService.markNotificationSeen(mContext, mNotification.id());
//                    mMarkSeenButton.setEnabled(false);
//                    mMarkSeenButton.setText(mContext.getString(R.string.checkmark));
//                }
//            });
        }

//        public void setDeleteSeen() {
//            mDeleteSeenButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Log log;
////                    mService.deleteNotification();
//                    //if(mNotification.isSeen()) return;
//
////                    mService.markNotificationSeen(mContext, mNotification.id());
////                    mMarkSeenButton.setEnabled(false);
////                    mMarkSeenButton.setText(mContext.getString(R.string.checkmark));
//                }
//            });
//        }
//
//        void setSelectionVisibility(boolean isCheckboxVisible) {
//            if(isCheckboxVisible) mSelectionCheckbox.setVisibility(View.VISIBLE);
//            else mSelectionCheckbox.setVisibility(View.GONE);
//        }


//        void setSelected(boolean isSelected) {
//            mSelectionCheckbox.setChecked(isSelected);
//        }

//        boolean isSelected() { return mSelectionCheckbox.isChecked(); }
    }
}
