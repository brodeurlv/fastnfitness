package com.easyfitness.programs

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CursorAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat.getColor
import com.easyfitness.BtnClickListener
import com.easyfitness.DAO.DAOExerciseInProgram
import com.easyfitness.DAO.DAOMachine
import com.easyfitness.DAO.DAORecord
import com.easyfitness.R
import com.easyfitness.utils.DateConverter
import com.easyfitness.utils.UnitConverter
import java.text.DecimalFormat
import java.util.*

class RecordCursorAdapter internal constructor(private val mContext: Context, c: Cursor?, flags: Int, clickDelete: BtnClickListener?, clickCopy: BtnClickListener?) : CursorAdapter(mContext, c, flags) {
    private val mInflater: LayoutInflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var mFirstColorOdd = 0
    private val mDeleteClickListener: BtnClickListener? = clickDelete
    private val mCopyClickListener: BtnClickListener? = clickCopy
    @SuppressLint("SetTextI18n")
    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val cdView: CardView = view.findViewById(R.id.CARDVIEW)
        val position = cursor.position
        if (position % 2 == mFirstColorOdd) {
            cdView.setBackgroundColor(getColor(context.resources,R.color.record_background_odd, context.theme))
        } else {
            cdView.setBackgroundColor(getColor(context.resources, R.color.record_background_even, context.theme))
        }

        /* Commun display */
        val tDate = view.findViewById<TextView>(R.id.DATE_CELL)
        val date: Date
        val dateString = cursor.getString(cursor.getColumnIndex(DAORecord.DATE))
        date = DateConverter.DBDateStrToDate(dateString)
        tDate.text = DateConverter.dateToLocalDateStr(date, mContext)

        val tTime = view.findViewById<TextView>(R.id.TIME_CELL)
        tTime.text = cursor.getString(cursor.getColumnIndex(DAORecord.TIME))

        val tExercise = view.findViewById<TextView>(R.id.MACHINE_CELL)
        tExercise.text = cursor.getString(cursor.getColumnIndex(DAOExerciseInProgram.EXERCISE))
        val tSerie = view.findViewById<TextView>(R.id.SERIE_CELL)
        val tSerieLabel = view.findViewById<TextView>(R.id.SERIE_LABEL)
        val tReps = view.findViewById<TextView>(R.id.REPETITION_CELL)
        val tRepsLabel = view.findViewById<TextView>(R.id.REP_LABEL)
        val tWeight = view.findViewById<TextView>(R.id.POIDS_CELL)
        val tWeightLabel = view.findViewById<TextView>(R.id.WEIGHT_LABEL)
        val tRepsLayout = view.findViewById<LinearLayout>(R.id.REP_LAYOUT)
        if (mCopyClickListener == null) {
            view.findViewById<View>(R.id.copyButton).visibility = View.GONE
        }

        /* Specific display */
        when (cursor.getInt(cursor.getColumnIndex(DAOExerciseInProgram.TYPE))) {
            DAOMachine.TYPE_FONTE -> {
                // UI
                tSerieLabel.text = mContext.getString(R.string.SerieLabel)
                tWeightLabel.text = mContext.getString(R.string.PoidsLabel)
                tRepsLabel.text = mContext.getString(R.string.RepetitionLabel_short)
                tRepsLayout.visibility = View.VISIBLE
                // Data
                tSerie.text = cursor.getString(cursor.getColumnIndex(DAOExerciseInProgram.SERIE))
                tReps.text = cursor.getString(cursor.getColumnIndex(DAOExerciseInProgram.REPETITION))
                var unit = mContext.getString(R.string.KgUnitLabel)
                var poids = cursor.getFloat(cursor.getColumnIndex(DAOExerciseInProgram.WEIGHT))
                if (cursor.getInt(cursor.getColumnIndex(DAOExerciseInProgram.UNIT)) == UnitConverter.UNIT_LBS) {
                    poids = UnitConverter.KgtoLbs(poids)
                    unit = mContext.getString(R.string.LbsUnitLabel)
                }
                val numberFormat = DecimalFormat("#.##")
                tWeight.text = numberFormat.format(poids.toDouble()) + unit
            }
            DAOMachine.TYPE_STATIC -> {
                // UI
                tSerieLabel.text = mContext.getString(R.string.SerieLabel)
                tWeightLabel.text = mContext.getString(R.string.PoidsLabel)
                tRepsLabel.text = mContext.getString(R.string.SecondsLabel_short)
                tRepsLayout.visibility = View.VISIBLE
                // Data
                tSerie.text = cursor.getString(cursor.getColumnIndex(DAOExerciseInProgram.SERIE))
                tReps.text = cursor.getString(cursor.getColumnIndex(DAOExerciseInProgram.SECONDS))
                var unit = mContext.getString(R.string.KgUnitLabel)
                var poids = cursor.getFloat(cursor.getColumnIndex(DAOExerciseInProgram.WEIGHT))
                if (cursor.getInt(cursor.getColumnIndex(DAOExerciseInProgram.UNIT)) == UnitConverter.UNIT_LBS) {
                    poids = UnitConverter.KgtoLbs(poids)
                    unit = mContext.getString(R.string.LbsUnitLabel)
                }
                val numberFormat = DecimalFormat("#.##")
                tWeight.text = numberFormat.format(poids.toDouble()) + unit
            }
            DAOMachine.TYPE_CARDIO -> {
                tSerieLabel.text = mContext.getString(R.string.DistanceLabel)
                tWeightLabel.text = mContext.getString(R.string.DurationLabel)
                tRepsLayout.visibility = View.GONE
                var distance = cursor.getFloat(cursor.getColumnIndex(DAOExerciseInProgram.DISTANCE))
                var unit = mContext.getString(R.string.KmUnitLabel)
                if (cursor.getInt(cursor.getColumnIndex(DAOExerciseInProgram.DISTANCE_UNIT)) == UnitConverter.UNIT_MILES) {
                    distance = UnitConverter.KmToMiles(distance) // Always convert to KG
                    unit = mContext.getString(R.string.MilesUnitLabel)
                }
                val numberFormat = DecimalFormat("#.##")
                tSerie.text = numberFormat.format(distance.toDouble()) + unit
                tWeight.text = DateConverter.durationToHoursMinutesSecondsStr(cursor.getInt(cursor.getColumnIndex(DAOExerciseInProgram.DURATION)).toLong())
            }
        }

        val deleteImg = view.findViewById<ImageView>(R.id.deleteButton)
        deleteImg.tag = cursor.getLong(cursor.getColumnIndex(DAOExerciseInProgram.KEY))
        deleteImg.setOnClickListener { v: View -> mDeleteClickListener?.onBtnClick(v.tag as Long) }
        val copyImg = view.findViewById<ImageView>(R.id.copyButton)
        copyImg.tag = cursor.getLong(cursor.getColumnIndex(DAOExerciseInProgram.KEY))
        copyImg.setOnClickListener { v: View -> mCopyClickListener?.onBtnClick(v.tag as Long) }
    }

    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        return mInflater.inflate(R.layout.row_fonte, parent, false)
    }

    /*
     * @pColor : si 1 alors affiche la couleur Odd en premier. Sinon, a couleur Even.
     */
    fun setFirstColorOdd(pColor: Int) {
        mFirstColorOdd = pColor
    }

}
