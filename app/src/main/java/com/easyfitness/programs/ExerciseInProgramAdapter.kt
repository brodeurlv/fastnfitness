package com.easyfitness.programs

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.recyclerview.widget.RecyclerView
import com.easyfitness.DAO.DAOMachine.*
import com.easyfitness.DAO.ExerciseInProgram
import com.easyfitness.R
import com.easyfitness.utils.BtnOnPostiomClickListener
import com.easyfitness.utils.DateConverter
import java.text.DecimalFormat

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
        val exerciseInProgram = this.exercisesList[position]
        holder.setExercise(exerciseInProgram)
    }

    override fun getItemCount(): Int {
        return exercisesList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mTitleTextView: TextView = itemView.findViewById(R.id.MACHINE_CELL)
        @SuppressLint("SetTextI18n")
        fun setExercise(exercise: ExerciseInProgram) {
            val exerciseName = exercise.exerciseName
            mTitleTextView.text = exerciseName
            val tSerie = itemView.findViewById<TextView>(R.id.SERIE_CELL)
            val tSerieLabel = itemView.findViewById<TextView>(R.id.SERIE_LABEL)
            val tReps = itemView.findViewById<TextView>(R.id.REPETITION_CELL)
            val tRepsLabel = itemView.findViewById<TextView>(R.id.REP_LABEL)
            val tWeight = itemView.findViewById<TextView>(R.id.POIDS_CELL)
            val tWeightLabel = itemView.findViewById<TextView>(R.id.WEIGHT_LABEL)
            val tRepsLayout = itemView.findViewById<LinearLayout>(R.id.REP_LAYOUT)

            when (exercise.type) {
                TYPE_FONTE -> {
                    // UI
                    tSerieLabel.text = mContext.getString(R.string.SerieLabel)
                    tWeightLabel.text = mContext.getString(R.string.PoidsLabel)
                    tRepsLabel.text = mContext.getString(R.string.RepetitionLabel_short)
                    tRepsLayout.visibility = View.VISIBLE
                    // Data
                    tSerie.text = exercise.serie.toString()
                    tReps.text = exercise.repetition.toString()
                    val unit = mContext.getString(R.string.KgUnitLabel)
                    val poids = exercise.poids
                    val numberFormat = DecimalFormat("#.##")
                    tWeight.text = numberFormat.format(poids.toDouble()) + unit
                }
                TYPE_STATIC -> {
                    // UI
                    tSerieLabel.text = mContext.getString(R.string.SerieLabel)
                    tWeightLabel.text = mContext.getString(R.string.PoidsLabel)
                    tRepsLabel.text = mContext.getString(R.string.SecondsLabel_short)
                    tRepsLayout.visibility = View.VISIBLE
                    // Data
                    tSerie.text = exercise.serie.toString()
                    tReps.text = exercise.seconds.toString()
                    val unit = mContext.getString(R.string.KgUnitLabel)
                    val poids = exercise.poids
                    val numberFormat = DecimalFormat("#.##")
                    tWeight.text = numberFormat.format(poids.toDouble()) + unit
                }
                TYPE_CARDIO -> {
                    tSerieLabel.text = mContext.getString(R.string.DistanceLabel)
                    tWeightLabel.text = mContext.getString(R.string.DurationLabel)
                    tRepsLayout.visibility = View.GONE
                    val distance = exercise.distance
                    val unit = mContext.getString(R.string.KmUnitLabel)
                    val numberFormat = DecimalFormat("#.##")
                    tSerie.text = numberFormat.format(distance.toDouble()) + unit
                    tWeight.text = DateConverter.durationToHoursMinutesSecondsStr(exercise.duration)
                }
            }

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
