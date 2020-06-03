package com.easyfitness.programs

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.recyclerview.widget.RecyclerView
import com.easyfitness.BtnClickListener
import com.easyfitness.DAO.DAOExerciseInProgram
import com.easyfitness.DAO.ExerciseInProgram
import com.easyfitness.R
import com.easyfitness.utils.BtnOnPostiomClickListener

/**
 * Created by senpl based on batra android-popup-remainder
 */
class ExerciseInProgramAdapter(context: Context, private val exercisesList: MutableList<ExerciseInProgram>, clickDelete: BtnOnPostiomClickListener?, itemLongClick: IOnRecyclerItemLongClick?) : RecyclerView.Adapter<ExerciseInProgramAdapter.ViewHolder>() {
    private val mDeleteClickListener: BtnOnPostiomClickListener? = clickDelete
    private var mItemLongClickHandler: IOnRecyclerItemLongClick? = null
    private val mRetrievalHandler = Handler()
    var mContext: Context
    private fun reload(@Nullable callback: ReloadDoneCallback?) {
        mRetrievalHandler.post {
            notifyDataSetChanged()
            callback?.onReloadDone()
        }
    }

    fun add(exercise: ExerciseInProgram) {
        exercisesList.add(exercise)
        notifyItemInserted(exercisesList.size)
    }

    fun removeAt(position: Int) {
        exercisesList.removeAt(position)
        notifyItemRemoved(position)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rowExerciseInProgramView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_exercise_in_program, parent, false)
        return ViewHolder(rowExerciseInProgramView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = this.exercisesList!![position]
        holder.setExercise(notification)
    }

    override fun getItemCount(): Int {
        return if (this.exercisesList == null) 0 else exercisesList!!.size
    }

//    private fun setExercises(exerciseInProgramList: List<ExerciseInProgram>) {
//        exercisesList = exerciseInProgramList
//    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mTitleTextView: TextView = itemView.findViewById(R.id.MACHINE_CELL)
        fun setExercise(exercise: ExerciseInProgram) {
            val exerciseName = exercise.exerciseName
            mTitleTextView.text = exerciseName
            //            mDescriptionTextView.setText(mNotification.getNote());
            val deleteImg = itemView.findViewById<ImageView>(R.id.deleteButton)
            deleteImg.tag = exercise.id
            deleteImg.setOnClickListener { v: View -> mDeleteClickListener?.onBtnClick(v.tag as Long, this.adapterPosition)
            }
        }
//        init {
            //            mDescriptionTextView = itemView.findViewById(R.id.notification_description);
//            mTimestampTextView = itemView.findViewById(R.id.notification_timestamp);
//            mMarkSeenButton = itemView.findViewById(R.id.deleteButton)
            //            mSelectionCheckbox = itemView.findViewById(R.id.notification_selection_checkbox);

//            if (mItemLongClickHandler != null) {
//                itemView.setOnLongClickListener(v -> {
//                    if (mItemLongClickHandler == null) return false;
//
//                    mItemLongClickHandler.onItemLongClick(getAdapterPosition());
////                        setSelected(true);
//                    return true;
//                });
//            }

//            itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if(mSelectionCheckbox.getVisibility() == View.VISIBLE)
//                        setSelected(!mSelectionCheckbox.isChecked());
//                }
//            });
//        }
    }

    init {
        if (itemLongClick != null) mItemLongClickHandler = itemLongClick
        mContext = context
        reload(null)
    }
}
