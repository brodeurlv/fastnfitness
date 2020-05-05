package com.easyfitness.programs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import cn.pedant.SweetAlert.SweetAlertDialog
import com.easyfitness.*
import com.easyfitness.DAO.*
import com.easyfitness.DAO.DAOMachine.*
import com.easyfitness.machines.ExerciseDetailsPager
import com.easyfitness.machines.MachineCursorAdapter
import com.easyfitness.utils.DateConverter
import com.easyfitness.utils.ExpandedListView
import com.easyfitness.utils.ImageUtil
import com.easyfitness.utils.UnitConverter
import com.ikovac.timepickerwithseconds.MyTimePickerDialog
import com.ikovac.timepickerwithseconds.TimePicker
import com.mikhaellopez.circularimageview.CircularImageView
import com.onurkaganaldemir.ktoastlib.KToast
import it.sephiroth.android.library.numberpicker.NumberPicker
import timber.log.Timber
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class ProgramRunner : Fragment() {
    private lateinit var mainActivity: MainActivity
    private lateinit var exerciseEdit: AutoCompleteTextView
    private lateinit var seriesEdit: EditText
    private lateinit var poidsEdit: EditText
    private lateinit var detailsLayout: LinearLayout
    private lateinit var addButton: Button
    private lateinit var recordList: ExpandedListView
    private lateinit var unitSpinner: Spinner
    private lateinit var unitDistanceSpinner: Spinner
    private lateinit var restTimeEdit: EditText
    private lateinit var restTimeCheck: CheckBox
    private lateinit var exerciseImage: CircularImageView
    private var lTableColor = 1
    private var machineListDialog: AlertDialog? = null
    private lateinit var minMaxLayout: LinearLayout

    // Selection part
    private var programSelectorLayout: LinearLayout? = null
    private var selectedType = TYPE_FONTE
    private lateinit var restTimeLayout: LinearLayout
    private lateinit var distanceEdit: EditText
    private lateinit var durationEdit: TextView
    private lateinit var secondsEdit: EditText
    private lateinit var serieCardView: CardView
    private lateinit var repetitionCardView: CardView
    private lateinit var secondsCardView: CardView
    private lateinit var weightCardView: CardView
    private lateinit var distanceCardView: CardView
    private lateinit var durationCardView: CardView
    private lateinit var autoTimeCheckBox: CheckBox

    private lateinit var programSelect: Spinner
    private lateinit var daoProgram: DAOProgram
    private var programId: Long = 1
    private lateinit var repsPicker: NumberPicker
    private var currentExerciseOrder = 0  //start from 0
    private lateinit var exercisesFromProgram: List<ExerciseInProgram>
    private lateinit var daoRecord: DAORecord
    private lateinit var strengthRecordsDao: DAOFonte
    private lateinit var daoCardio: DAOCardio
    private lateinit var daoStatic: DAOStatic
    private lateinit var dateEdit: TextView
    private lateinit var timeEdit: TextView
    private lateinit var daoExerciseInProgram: DAOExerciseInProgram
    private lateinit var mDbMachine: DAOMachine
    private lateinit var minText: TextView
    private lateinit var maxText: TextView
    private lateinit var machineImage: CircularImageView
    private lateinit var nextExerciseArrow: Button
    private lateinit var previousExerciseArrow: Button
    private lateinit var notes: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.tab_program_runner, container, false)
        repsPicker = view.findViewById(R.id.numberPicker)
        notes = view.findViewById(R.id.notesInExercise)
        // Initialization of the database
        daoProgram = DAOProgram(context)
        daoRecord = DAORecord(context)
        strengthRecordsDao = DAOFonte(context)
        daoCardio = DAOCardio(context)
        daoStatic = DAOStatic(context)
        mDbMachine = DAOMachine(context)
        val programs = daoProgram.allProgramsNames
        daoExerciseInProgram = DAOExerciseInProgram(requireContext())
        if (programs == null || programs.isEmpty()) {
            val profileId: Long = (requireActivity() as MainActivity).currentProfil.id
            val programsFragment = ProgramsFragment.newInstance("", profileId)
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            Toast.makeText(context, R.string.add_program_first, Toast.LENGTH_LONG).show()
            transaction.replace(R.id.fragment_container, programsFragment)
            transaction.addToBackStack(null)
            // Commit the transaction
            transaction.commit()
        } else {
            val programFirst = daoProgram.getRecord(programs[0])
            if (programFirst != null) {
                programId = programFirst.id
                programSelect = view.findViewById(R.id.programSelect)
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, programs)
                programSelect.adapter = adapter
                programSelect.onItemSelectedListener = object :
                    AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>,
                                                view: View, position: Int, id: Long) {
                        val program: Program? = daoProgram.getRecord(programs[position])
                        if (program != null) {
                            programId = program.id
                            currentExerciseOrder = 0
                            exercisesFromProgram = daoExerciseInProgram.getAllExerciseInProgram(programId)
                            refreshData()
                            Toast.makeText(context, getString(R.string.program_selection) + " " + programs[position], Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                    }
                }
            }
        }
        exerciseEdit = view.findViewById(R.id.editMachine)
        seriesEdit = view.findViewById(R.id.editSerie)
        poidsEdit = view.findViewById(R.id.editPoids)
        recordList = view.findViewById(R.id.listRecord)
        val machineListButton = view.findViewById<ImageButton>(R.id.buttonListMachine)
        addButton = view.findViewById(R.id.addperff)
        unitSpinner = view.findViewById(R.id.spinnerUnit)
        unitDistanceSpinner = view.findViewById(R.id.spinnerDistanceUnit)
        detailsLayout = view.findViewById(R.id.notesLayout)
        detailsLayout.visibility = View.VISIBLE
        restTimeEdit = view.findViewById(R.id.editRestTime)
        restTimeCheck = view.findViewById(R.id.restTimecheckBox)
        exerciseImage = view.findViewById(R.id.imageMachine)
        minText = view.findViewById(R.id.minText)
        maxText = view.findViewById(R.id.maxText)
        autoTimeCheckBox = view.findViewById(R.id.autoTimeCheckBox)
        machineImage = view.findViewById(R.id.imageMachine)
        dateEdit = view.findViewById(R.id.editDate)
        timeEdit = view.findViewById(R.id.editTime)

        // Cardio Part
        programSelectorLayout = view.findViewById(R.id.programSelectionLayout)
        minMaxLayout = view.findViewById(R.id.minmaxLayout)
        restTimeLayout = view.findViewById(R.id.restTimeLayout)
        durationEdit = view.findViewById(R.id.editDuration)
        distanceEdit = view.findViewById(R.id.editDistance)
        secondsEdit = view.findViewById(R.id.editSeconds)
        serieCardView = view.findViewById(R.id.cardviewSerie)
        repetitionCardView = view.findViewById(R.id.cardviewRepetition)
        secondsCardView = view.findViewById(R.id.cardviewSeconds)
        weightCardView = view.findViewById(R.id.cardviewWeight)
        distanceCardView = view.findViewById(R.id.cardviewDistance)
        durationCardView = view.findViewById(R.id.cardviewDuration)
        nextExerciseArrow = view.findViewById(R.id.nextExerciseArrow)
        nextExerciseArrow.setOnClickListener(clickArrows)
        previousExerciseArrow = view.findViewById(R.id.previousExerciseArrow)
        previousExerciseArrow.setOnClickListener(clickArrows)
        durationCardView = view.findViewById(R.id.cardviewDuration)
        addButton.setOnClickListener(clickAddButton)
        machineListButton.setOnClickListener(onClickMachineListWithIcons)
        durationEdit.setOnClickListener(clickDateEdit)
        exerciseEdit.setOnKeyListener(checkExerciseExists)
        exerciseEdit.onItemClickListener = onItemClickFilterList
        notes.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateNote()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        restTimeEdit.onFocusChangeListener = restTimeEditChange
        restTimeCheck.setOnCheckedChangeListener(restTimeCheckChange)
        restoreSharedParams()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
        var weightUnit = UnitConverter.UNIT_KG
        try {
            weightUnit = sharedPreferences.getString(SettingsFragment.WEIGHT_UNIT_PARAM, "0")?.toInt()!!
        } catch (e: NumberFormatException) {
            Timber.d("Not important")
        }
        unitSpinner.setSelection(weightUnit)
        val distanceUnit: Int
        distanceUnit = try {
            sharedPreferences.getString(SettingsFragment.DISTANCE_UNIT_PARAM, "0")?.toInt()!!
        } catch (e: NumberFormatException) {
            UnitConverter.UNIT_KM
        }
        unitDistanceSpinner.setSelection(distanceUnit)

        mDbMachine = DAOMachine(context)
        selectedType = TYPE_FONTE
        exerciseImage.setOnClickListener {
            val m = mDbMachine.getMachine(exerciseEdit.text.toString())
            if (m != null) {
                val profileId: Long = (requireActivity() as MainActivity).currentProfil.id
                val machineDetailsFragment = ExerciseDetailsPager.newInstance(m.id, profileId)
                val transaction = requireActivity().supportFragmentManager.beginTransaction()
                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack so the user can navigate back
                transaction.replace(R.id.fragment_container, machineDetailsFragment, MainActivity.MACHINESDETAILS)
                transaction.addToBackStack(null)
                // Commit the transaction
                transaction.commit()
            }
        }
        return view
    }

    private fun nextExercise() {
        if (exercisesFromProgram.isNotEmpty() && currentExerciseOrder < exercisesFromProgram.size - 1) {
            currentExerciseOrder++
            refreshData()
        }
    }

    private fun previousExercise() {
        if (exercisesFromProgram.isNotEmpty() && currentExerciseOrder > 0) {
            currentExerciseOrder--
            refreshData()
        }
    }

    private val durationSet = MyTimePickerDialog.OnTimeSetListener { _: TimePicker?, hourOfDay: Int, minute: Int, second: Int ->
        val strMinute: String = if (minute < 10) "0$minute" else minute.toString()
        val strHour: String = if (hourOfDay < 10) "0$hourOfDay" else hourOfDay.toString()
        val strSecond: String = if (second < 10) "0$second" else second.toString()
        val date = "$strHour:$strMinute:$strSecond"
        durationEdit.text = date
        hideKeyboard()
    }

    private val clickArrows = View.OnClickListener { v: View ->
        when (v.id) {
            R.id.nextExerciseArrow -> nextExercise()
            R.id.previousExerciseArrow -> previousExercise()
        }
    }
    private val checkExerciseExists = View.OnKeyListener { _: View?, _: Int, _: KeyEvent? ->
        val lMach = mDbMachine.getMachine(exerciseEdit.text.toString())
        if (lMach != null) {
            changeExerciseTypeUI(lMach.type)
        }
        false
    }
    private val restTimeEditChange = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
        if (!hasFocus) {
            saveSharedParams()
        }
    }
    private val restTimeCheckChange = CompoundButton.OnCheckedChangeListener { _: CompoundButton?, _: Boolean -> saveSharedParams() }
    private val itemClickDeleteRecord = BtnClickListener { idToDelete: Long -> showDeleteDialog(idToDelete) }
    private val itemClickCopyRecord = BtnClickListener { id: Long ->
        val r: IRecord? = daoRecord.getRecord(id)
        if (r != null) {
            setCurrentMachine(r.exercise)
            when (r.type) {
                TYPE_FONTE -> {
                    val f = r as Fonte
                    repsPicker.progress = f.repetition
                    seriesEdit.setText(String.format("%d", f.serie))
                    val numberFormat = DecimalFormat("#.##")
                    var poids = f.poids
                    if (f.unit == UnitConverter.UNIT_LBS) {
                        poids = UnitConverter.KgtoLbs(poids)
                    }
                    unitSpinner.setSelection(f.unit)
                    poidsEdit.setText(numberFormat.format(poids))
                }
                TYPE_STATIC -> {
                    val f = r as StaticExercise
                    secondsEdit.setText(String.format("%d", f.second))
                    seriesEdit.setText(String.format("%d", f.serie))
                    val numberFormat = DecimalFormat("#.##")
                    poidsEdit.setText(numberFormat.format(f.poids.toDouble()))
                }
                TYPE_CARDIO -> {
                    val c = r as Cardio
                    val numberFormat = DecimalFormat("#.##")
                    var distance = c.distance
                    if (c.distanceUnit == UnitConverter.UNIT_MILES) {
                        distance = UnitConverter.KmToMiles(c.distance)
                    }
                    unitDistanceSpinner.setSelection(c.distanceUnit)
                    distanceEdit.setText(numberFormat.format(distance.toDouble()))
                    durationEdit.text = DateConverter.durationToHoursMinutesSecondsStr(c.duration)
                }
            }
            KToast.infoToast(mainActivity, getString(R.string.recordcopied), Gravity.BOTTOM, KToast.LENGTH_SHORT)
        }
    }

    @SuppressLint("SetTextI18n")
    private val clickAddButton = View.OnClickListener {
        if (exerciseEdit.text.toString().isEmpty()) {
            KToast.warningToast(requireActivity(), resources.getText(R.string.missinginfo).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT)
            return@OnClickListener
        }
        val exerciseType: Int
        val lMachine = mDbMachine.getMachine(exerciseEdit.text.toString())
        exerciseType = lMachine?.type ?: selectedType
        var restTime = 60
        try {
            restTime = restTimeEdit.text.toString().toInt()
        } catch (e: NumberFormatException) {
            restTimeEdit.setText("60")
        }
        val timeStr: String
        val date: Date
        if (autoTimeCheckBox.isChecked) {
            date = Date()
            timeStr = DateConverter.currentTime()
        } else {
            date = DateConverter.editToDate(dateEdit.text.toString())
            timeStr = timeEdit.text.toString()
        }
        var iTotalWeightSession = 0F
        var iTotalWeight = 0F
        var iNbSeries = 1
        when (exerciseType) {
            TYPE_FONTE -> {
                if (seriesEdit.text.toString().isEmpty() ||
                    poidsEdit.text.toString().isEmpty()) {
                    KToast.warningToast(requireActivity(), resources.getText(R.string.missinginfo).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT)
                    return@OnClickListener
                }
                var tmpPoids = poidsEdit.text.toString().replace(",".toRegex(), ".").toFloat()  /* Weight conversion */
                var unitPoids = UnitConverter.UNIT_KG // Kg
                val mContext = requireContext()
                if (unitSpinner.selectedItem.toString() == mContext.getString(R.string.LbsUnitLabel)) {
                    tmpPoids = UnitConverter.LbstoKg(tmpPoids) // Always convert to KG
                    unitPoids = UnitConverter.UNIT_LBS // LBS
                }
                strengthRecordsDao.addBodyBuildingRecord(date,
                    exerciseEdit.text.toString(),
                    seriesEdit.text.toString().toInt(),
                    repsPicker.progress,
                    tmpPoids, // Always save in KG
                    getProfilFromMain(),
                    unitPoids, // Store Unit for future display
                    "", //Notes
                    timeStr
                )
                iTotalWeightSession = strengthRecordsDao.getTotalWeightSession(date)
                iTotalWeight = strengthRecordsDao.getTotalWeightMachine(date, exerciseEdit.text.toString())
                iNbSeries = strengthRecordsDao.getNbSeries(date, exerciseEdit.text.toString())
            }
            TYPE_STATIC -> {
                if (seriesEdit.text.toString().isEmpty() ||
                    secondsEdit.text.toString().isEmpty() ||
                    poidsEdit.text.toString().isEmpty()) {
                    KToast.warningToast(requireActivity(), resources.getText(R.string.missinginfo).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT)
                    return@OnClickListener
                }
                /* Weight conversion */
                var tmpPoids = poidsEdit.text.toString().replace(",".toRegex(), ".").toFloat()
                var unitPoids = UnitConverter.UNIT_KG // Kg
                if (unitSpinner.selectedItem.toString() == requireContext().getString(R.string.LbsUnitLabel)) {
                    tmpPoids = UnitConverter.LbstoKg(tmpPoids) // Always convert to KG
                    unitPoids = UnitConverter.UNIT_LBS // LBS
                }
                try {
                    restTime = restTimeEdit.text.toString().toInt()
                } catch (e: NumberFormatException) {
                    restTime = 0
                    restTimeEdit.setText("0")
                }
                daoStatic.addStaticRecord(date,
                    exerciseEdit.text.toString(),
                    seriesEdit.text.toString().toInt(),
                    secondsEdit.text.toString().toInt(),
                    tmpPoids,
                    getProfilFromMain(),
                    unitPoids, // Store Unit for future display
                    "", //Notes
                    timeStr)
                iTotalWeightSession = daoStatic.getTotalWeightSession(date)
                iTotalWeight = daoStatic.getTotalWeightMachine(date, exerciseEdit.text.toString())
                iNbSeries = daoStatic.getNbSeries(date, exerciseEdit.text.toString())
            }
            TYPE_CARDIO -> {
                if (durationEdit.text.toString().isEmpty() &&  // Only one is mandatory
                    distanceEdit.text.toString().isEmpty()) {
                    KToast.warningToast(requireActivity(),
                        resources.getText(R.string.missinginfo).toString() + " Distance missing",
                        Gravity.BOTTOM, KToast.LENGTH_SHORT)
                    return@OnClickListener
                }
                var duration: Long
                try {
                    @SuppressLint("SimpleDateFormat") val dateFormat = SimpleDateFormat("HH:mm:ss")
                    dateFormat.timeZone = TimeZone.getTimeZone("GMT")
                    val tmpDate = dateFormat.parse(durationEdit.text.toString())
                    duration = tmpDate.time
                } catch (e: ParseException) {
                    e.printStackTrace()
                    duration = 0
                }
                var distance: Float = if (distanceEdit.text.toString().isEmpty()) {
                    0f
                } else {
                    distanceEdit.text.toString().replace(",".toRegex(), ".").toFloat()
                }
                var unitDistance = UnitConverter.UNIT_KM
                if (unitDistanceSpinner.selectedItem.toString()
                    == context?.getString(R.string.MilesUnitLabel)) {
                    distance = UnitConverter.MilesToKm(distance) // Always convert to KG
                    unitDistance = UnitConverter.UNIT_MILES
                }
                daoCardio.addCardioRecord(date,
                    timeStr,
                    exerciseEdit.text.toString(),
                    distance,
                    duration,
                    getProfilFromMain(),
                    unitDistance)
                // No Countdown for Cardio
            }
        }
        requireActivity().findViewById<View>(R.id.drawer_layout)?.requestFocus()
        hideKeyboard()
        lTableColor = (lTableColor + 1) % 2 // Change the color each time you add data
        refreshData()
        /* Reinitialisation des machines */
        val adapter = ArrayAdapter(requireView().context,
            android.R.layout.simple_dropdown_item_1line, daoRecord.getAllMachines(profil))
        exerciseEdit.setAdapter(adapter)
        addButton.setText(R.string.AddLabel)
        // Launch Countdown
        runRestAndPrepareForNextExercise(restTime, iNbSeries, iTotalWeight, iTotalWeightSession)
    }

    private fun runRestAndPrepareForNextExercise(restTime: Int, iNbSeries: Int, iTotalWeight: Float, iTotalWeightSession: Float) {
        val cdd = CountdownDialogbox(mainActivity, restTime) // Launch Countdown
        cdd.setNbSeries(iNbSeries)
        cdd.setTotalWeightMachine(iTotalWeight)
        cdd.setTotalWeightSession(iTotalWeightSession)
        cdd.show()
    }

    private val onClickMachineListWithIcons = View.OnClickListener { v ->
        val oldCursor: Cursor
        if (machineListDialog != null && machineListDialog!!.isShowing) {        // In case the dialog is already open
            return@OnClickListener
        }
        val machineList = ListView(v.context)
        val c: Cursor? = mDbMachine.allMachines
        if (c == null || c.count == 0) {
            KToast.warningToast(requireActivity(), resources.getText(R.string.createExerciseFirst).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT)
            machineList.adapter = null
        } else {
            if (machineList.adapter == null) {
                val mTableAdapter = MachineCursorAdapter(activity, c, 0, mDbMachine)
                machineList.adapter = mTableAdapter
            } else {
                val mTableAdapter = machineList.adapter as MachineCursorAdapter
                oldCursor = mTableAdapter.swapCursor(c)
                oldCursor?.close()
            }
            machineList.onItemClickListener = OnItemClickListener { _: AdapterView<*>?, view: View, _: Int, _: Long ->
                val textView = view.findViewById<TextView>(R.id.LIST_MACHINE_ID)
                val machineID = textView.text.toString().toLong()
                val lMachineDb = DAOMachine(context)
                val lMachine = lMachineDb.getMachine(machineID)
                setCurrentExercise(lMachine.name)
                mainActivity.findViewById<View>(R.id.drawer_layout).requestFocus()
                hideKeyboard()
                if (machineListDialog!!.isShowing) {
                    machineListDialog!!.dismiss()
                }
            }
            val builder = AlertDialog.Builder(v.context)
            builder.setTitle(R.string.selectMachineDialogLabel)
            builder.setView(machineList)
            machineListDialog = builder.create()
            machineListDialog!!.show()
        }
    }
    private val onItemClickFilterList = OnItemClickListener { _: AdapterView<*>?, _: View?, _: Int, _: Long -> setCurrentExercise(exerciseEdit.text.toString()) }

    //Required for cardio/duration
    private val clickDateEdit = View.OnClickListener { v: View ->
        when (v.id) {
            R.id.editDuration -> showTimePicker(durationEdit)
            R.id.editMachine -> {
                machineImage.setImageResource(R.drawable.ic_machine)
                minMaxLayout.visibility = View.GONE
            }
        }
    }

    private fun updateNote() {
        val previousNote = exercisesFromProgram[currentExerciseOrder].note
        if (notes.text.toString() != previousNote) {
            daoExerciseInProgram.updateString(exercisesFromProgram[currentExerciseOrder],
                DAOExerciseInProgram.NOTES, notes.text.toString())
        }
    }

    private fun showDeleteDialog(idToDelete: Long) {
        SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
            .setTitleText(getString(R.string.DeleteRecordDialog))
            .setContentText(resources.getText(R.string.areyousure).toString())
            .setCancelText(resources.getText(R.string.global_no).toString())
            .setConfirmText(resources.getText(R.string.global_yes).toString())
            .showCancelButton(true)
            .setConfirmClickListener { sDialog: SweetAlertDialog ->
                daoRecord.deleteRecord(idToDelete)
                updateRecordTable(exerciseEdit.text.toString())
                KToast.infoToast(requireActivity(), resources.getText(R.string.removedid).toString(), Gravity.BOTTOM, KToast.LENGTH_LONG)
                sDialog.dismissWithAnimation()
            }
            .show()
    }

    override fun onStart() {
        super.onStart()
        mainActivity = this.activity as MainActivity
    }

    val name: String?
        get() {
            return requireArguments().getString("name")
        }

    @SuppressLint("CommitTransaction")
    private fun showTimePicker(timeTextView: TextView?) {
        val tx = timeTextView?.text.toString()
        val hour: Int
        hour = try {
            tx.substring(0, 2).toInt()
        } catch (e: Exception) {
            0
        }
        val min: Int
        min = try {
            tx.substring(3, 5).toInt()
        } catch (e: Exception) {
            0
        }
        val sec: Int
        sec = try {
            tx.substring(6).toInt()
        } catch (e: Exception) {
            0
        }
        if (timeTextView!!.id == R.id.editDuration) {
            val mDurationFrag = TimePickerDialogFragment.newInstance(durationSet, hour, min, sec)
            val fm = requireActivity().supportFragmentManager
            mDurationFrag.show(fm.beginTransaction(), "dialog_time")
        }
    }

    val fragment: ProgramRunner
        get() = this

    private val profil: Profile?
        get() = mainActivity.currentProfil

    val machine: String
        get() = exerciseEdit.text.toString()

    private fun setCurrentExercise(machineStr: String) {
        if (machineStr.isEmpty()) {
            exerciseImage.setImageResource(R.drawable.ic_machine) // Default image
            minMaxLayout.visibility = View.GONE
            return
        }
        val lMachine = mDbMachine.getMachine(machineStr)
        if (lMachine == null) {
            exerciseEdit.setText("")
            exerciseImage.setImageResource(R.drawable.ic_machine) // Default image
            changeExerciseTypeUI(TYPE_FONTE)
            return
        }
        // Update EditView
        exerciseEdit.setText(lMachine.name)
        // Update exercise Image
        exerciseImage.setImageResource(R.drawable.ic_machine) // Default image
        val imgUtil = ImageUtil()
        ImageUtil.setThumb(exerciseImage, imgUtil.getThumbPath(lMachine.picture)) // Overwrite image is there is one
        // Update Table
        updateRecordTable(lMachine.name)
        // Update display type
        changeExerciseTypeUI(lMachine.type)
        // Update last values
        updateLastRecord(lMachine)
    }

    private fun setRunningExercise(exercise: ExerciseInProgram) {
        // Update EditView
        exerciseEdit.setText(exercise.exerciseName)
        // Update exercise Image
        exerciseImage.setImageResource(R.drawable.ic_machine) // Default image
        val lMachine = mDbMachine.getMachine(exercise.exerciseName)
        if (lMachine != null) {
            val imgUtil = ImageUtil()
            ImageUtil.setThumb(exerciseImage, imgUtil.getThumbPath(lMachine.picture)) // Overwrite image is there is one
        }
        changeExerciseTypeUI(exercise.type)
        updateRecordTable(exercise.exerciseName)
        notes.setText(exercise.note)
        when (exercise.type) {
            TYPE_FONTE -> {
                repsPicker.progress = exercise.repetition
                seriesEdit.setText(exercise.serie.toString())
                restTimeEdit.setText(exercise.secRest.toString())
                poidsEdit.setText(exercise.poids.toString())
            }
            TYPE_CARDIO -> {
                durationEdit.text = DateConverter.durationToHoursMinutesSecondsStr(exercise.duration)
                distanceEdit.setText(exercise.distance.toString())
                unitDistanceSpinner.setSelection(exercise.distanceUnit, false)
            }
            TYPE_STATIC -> {
                seriesEdit.setText(exercise.serie.toString())
                secondsEdit.setText(exercise.seconds.toString())
                poidsEdit.setText(exercise.poids.toString())
                restTimeEdit.setText(exercise.secRest.toString())
            }
        }
    }

    private fun setCurrentMachine(machineStr: String) {
        if (machineStr.isEmpty()) {
            machineImage.setImageResource(R.drawable.ic_machine) // Default image
            minMaxLayout.visibility = View.GONE
            return
        }
        val lMachine = mDbMachine.getMachine(machineStr)
        if (lMachine == null) {
            exerciseEdit.setText("")
            machineImage.setImageResource(R.drawable.ic_machine) // Default image
            changeExerciseTypeUI(TYPE_FONTE)
            updateMinMax(null)
            return
        }

        exerciseEdit.setText(lMachine.name)
        // Update exercise Image
        machineImage.setImageResource(R.drawable.ic_machine) // Default image
        val imgUtil = ImageUtil()
        ImageUtil.setThumb(machineImage, imgUtil.getThumbPath(lMachine.picture)) // Overwrite image is there is one

        updateRecordTable(lMachine.name)
        changeExerciseTypeUI(lMachine.type)

        updateMinMax(lMachine)
        updateLastRecord(lMachine)
    }

    @SuppressLint("SetTextI18n")
    private fun updateMinMax(m: Machine?) {
        var unitStr: String
        var weight: Float
        if (getProfilFromMain() != null && m != null) {
            if (m.type == TYPE_FONTE || m.type == TYPE_STATIC) {
                val minValue: Weight? = strengthRecordsDao.getMin(getProfilFromMain(), m)
                if (minValue != null) {
                    minMaxLayout.visibility = View.VISIBLE
                    if (minValue.storedUnit == UnitConverter.UNIT_LBS) {
                        weight = UnitConverter.KgtoLbs(minValue.storedWeight)
                        unitStr = requireContext().getString(R.string.LbsUnitLabel)
                    } else {
                        weight = minValue.storedWeight
                        unitStr = requireContext().getString(R.string.KgUnitLabel)
                    }
                    val numberFormat = DecimalFormat("#.##")
                    minText.text = numberFormat.format(weight.toDouble()) + " " + unitStr
                    val maxValue: Weight = strengthRecordsDao.getMax(getProfilFromMain(), m)
                    if (maxValue.storedUnit == UnitConverter.UNIT_LBS) {
                        weight = UnitConverter.KgtoLbs(maxValue.storedWeight)
                        unitStr = requireContext().getString(R.string.LbsUnitLabel)
                    } else {
                        weight = maxValue.storedWeight
                        unitStr = requireContext().getString(R.string.KgUnitLabel)
                    }
                    maxText.text = numberFormat.format(weight.toDouble()) + " " + unitStr
                } else {
                    minText.text = "-"
                    maxText.text = "-"
                    minMaxLayout.visibility = View.GONE
                }
            } else if (m.type == TYPE_CARDIO) {
                minMaxLayout.visibility = View.GONE
            }
        } else {
            minText.text = "-"
            maxText.text = "-"
            minMaxLayout.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateLastRecord(m: Machine) {
        val lLastRecord = daoRecord.getLastExerciseRecord(m.id, profil)
        // Default Values
        seriesEdit.setText("1")
        repsPicker.progress = 10
        secondsEdit.setText("60")
        poidsEdit.setText("50")
        distanceEdit.setText("1")
        durationEdit.text = "00:10:00"
        if (lLastRecord != null) {
            if (lLastRecord.type == TYPE_FONTE) {
                val lLastBodyBuildingRecord = lLastRecord as Fonte
                seriesEdit.setText(lLastBodyBuildingRecord.serie.toString())
                repsPicker.progress = lLastBodyBuildingRecord.repetition
                unitSpinner.setSelection(lLastBodyBuildingRecord.unit)
                val numberFormat = DecimalFormat("#.##")
                if (lLastBodyBuildingRecord.unit == UnitConverter.UNIT_LBS) poidsEdit.setText(numberFormat.format(UnitConverter.KgtoLbs(lLastBodyBuildingRecord.poids).toDouble())) else poidsEdit.setText(numberFormat.format(lLastBodyBuildingRecord.poids.toDouble()))
            } else if (lLastRecord.type == TYPE_CARDIO) {
                val lLastCardioRecord = lLastRecord as Cardio
                durationEdit.text = DateConverter.durationToHoursMinutesSecondsStr(lLastCardioRecord.duration)
                unitDistanceSpinner.setSelection(lLastCardioRecord.distanceUnit)
                val numberFormat = DecimalFormat("#.##")
                if (lLastCardioRecord.distanceUnit == UnitConverter.UNIT_MILES) distanceEdit.setText(numberFormat.format(UnitConverter.KmToMiles(lLastCardioRecord.distance).toDouble())) else distanceEdit.setText(numberFormat.format(lLastCardioRecord.distance.toDouble()))
            } else if (lLastRecord.type == TYPE_STATIC) {
                val lLastStaticRecord = lLastRecord as StaticExercise
                seriesEdit.setText(lLastStaticRecord.serie.toString())
                secondsEdit.setText(lLastStaticRecord.second.toString())
                unitSpinner.setSelection(lLastStaticRecord.unit)
                val numberFormat = DecimalFormat("#.##")
                if (lLastStaticRecord.unit == UnitConverter.UNIT_LBS) poidsEdit.setText(numberFormat.format(UnitConverter.KgtoLbs(lLastStaticRecord.poids).toDouble())) else poidsEdit.setText(numberFormat.format(lLastStaticRecord.poids.toDouble()))
            }
        }
    }

    private fun updateRecordTable(exerciseName: String) { // Records from records table
        mainActivity.currentMachine = exerciseName
        requireView().post {
            val c: Cursor?
            val oldCursor: Cursor
            //Get results
            c = (daoRecord.getAllRecordByMachines(profil, exerciseName) ?: return@post)
            if (c.count == 0) {
                recordList.adapter = null
            } else {
                if (recordList.adapter == null) {
                    val mTableAdapter = RecordCursorAdapter(mainActivity, c, 0, itemClickDeleteRecord, itemClickCopyRecord)
                    mTableAdapter.setFirstColorOdd(lTableColor)
                    recordList.adapter = mTableAdapter
                } else {
                    val mTableAdapter = recordList.adapter as RecordCursorAdapter
                    mTableAdapter.setFirstColorOdd(lTableColor)
                    oldCursor = mTableAdapter.swapCursor(c)
                    oldCursor?.close()
                }
            }
        }
    }

    private fun getProfilFromMain(): Profile? {
        return mainActivity.currentProfil
    }

    @SuppressLint("SetTextI18n")
    private fun refreshData() {
        if (profil != null) {
            daoExerciseInProgram.setProfile(profil)
            if (exercisesFromProgram.isNotEmpty()) {
                val currentExercise = exercisesFromProgram[currentExerciseOrder]
                setRunningExercise(currentExercise)
                updateRecordTable(currentExercise.exerciseName)
            }
        }
    }

    private fun changeExerciseTypeUI(pType: Int) {
        when (pType) {
            TYPE_CARDIO -> {
                serieCardView.visibility = View.GONE
                repetitionCardView.visibility = View.GONE
                weightCardView.visibility = View.GONE
                secondsCardView.visibility = View.GONE
                distanceCardView.visibility = View.VISIBLE
                durationCardView.visibility = View.VISIBLE
                selectedType = TYPE_CARDIO
            }
            TYPE_STATIC -> {
                serieCardView.visibility = View.VISIBLE
                repetitionCardView.visibility = View.GONE
                secondsCardView.visibility = View.VISIBLE
                weightCardView.visibility = View.VISIBLE
                restTimeLayout.visibility = View.VISIBLE
                distanceCardView.visibility = View.GONE
                durationCardView.visibility = View.GONE
                selectedType = TYPE_STATIC
            }
            TYPE_FONTE -> {
                serieCardView.visibility = View.VISIBLE
                repetitionCardView.visibility = View.VISIBLE
                secondsCardView.visibility = View.GONE
                weightCardView.visibility = View.VISIBLE
                restTimeLayout.visibility = View.VISIBLE
                distanceCardView.visibility = View.GONE
                durationCardView.visibility = View.GONE
                selectedType = TYPE_FONTE
            }
            else -> {
                serieCardView.visibility = View.VISIBLE
                repetitionCardView.visibility = View.VISIBLE
                secondsCardView.visibility = View.GONE
                weightCardView.visibility = View.VISIBLE
                restTimeLayout.visibility = View.VISIBLE
                distanceCardView.visibility = View.GONE
                durationCardView.visibility = View.GONE
                selectedType = TYPE_FONTE
            }
        }
    }

    private fun saveSharedParams() {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPref?.edit()
        editor?.putString("restTime", restTimeEdit.text.toString())
        editor?.putBoolean("restCheck", restTimeCheck.isChecked)
        editor?.putBoolean("showDetails", detailsLayout.isShown)
        editor?.apply()
    }

    private fun restoreSharedParams() {
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        restTimeEdit.setText(sharedPref?.getString("restTime", ""))
        restTimeCheck.isChecked = sharedPref!!.getBoolean("restCheck", true)
    }

    private fun hideKeyboard() {
        try {
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)
        } catch (ex: Exception) {
            Timber.d(ex, "EX %s", ex.message)
        }
    }

    companion object {
        /**
         * Create a new instance of DetailsFragment, initialized to
         * show the text at 'index'.
         */
        fun newInstance(name: String?, id: Int): ProgramRunner {
            val f = ProgramRunner()
            val args = Bundle()
            args.putString("name", name)
            args.putInt("id", id)
            f.arguments = args
            return f
        }
    }
}
