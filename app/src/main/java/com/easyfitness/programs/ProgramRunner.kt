package com.easyfitness.programs

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.*
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class ProgramRunner : Fragment() {
    private lateinit var mainActivity: MainActivity
    private lateinit var exerciseEdit: AutoCompleteTextView
    private lateinit var seriesEdit: EditText

    //    private lateinit var repetitionEdit: EditText
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
    private lateinit var machineListDialog: AlertDialog
    private lateinit var minMaxLayout: LinearLayout

    // Selection part
    private lateinit var exerciseTypeSelectorLayout: LinearLayout
    private var programSelectorLayout: LinearLayout? = null
    private lateinit var bodybuildingSelector: TextView
    private lateinit var cardioSelector: TextView
    private lateinit var staticExerciseSelector: TextView
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
    private var currentExerciseOrder = 1
    private lateinit var exercisesFromProgram: List<ExerciseInProgram>
    private lateinit var daoRecord: DAORecord
    private lateinit var strengthRecordsDao: DAOFonte
    private lateinit var daoCardio: DAOCardio
    private lateinit var daoStatic: DAOStatic
    private lateinit var dateEdit: TextView
    private lateinit var timeEdit: TextView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.tab_program_runner, container, false)
        repsPicker = view.findViewById(R.id.numberPicker)
        // Initialization of the database
        daoProgram = DAOProgram(context)
        daoRecord = DAORecord(context)
        strengthRecordsDao = DAOFonte(context)
        daoCardio = DAOCardio(context)
        daoStatic = DAOStatic(context)
        val programs = daoProgram.allProgramsNames
        daoExerciseInProgram = DAOExerciseInProgram(context)
        if (programs == null || programs.size == 0) {
            val profileId: Long = (requireActivity() as MainActivity).currentProfil.id
            val programsFragment = ProgramsFragment.newInstance("", profileId)
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            // Replace whatever is in the fragment_container view with this fragment,
            // and add the transaction to the back stack so the user can navigate back
            Toast.makeText(context, "Add program first", Toast.LENGTH_LONG).show()
            transaction.replace(R.id.fragment_container, programsFragment)
            transaction.addToBackStack(null)
            // Commit the transaction
            transaction.commit()
        } else {
            programId = daoProgram.getRecord(programs[0]).id
            programSelect = view.findViewById(R.id.programSelect)
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, programs)
            programSelect.adapter = adapter
            programSelect.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>,
                                            view: View, position: Int, id: Long) {
                    programId = daoProgram.getRecord(programs[position]).id
                    currentExerciseOrder = 1

                    exercisesFromProgram = daoExerciseInProgram.getAllExerciseInProgram(programId)
                    refreshData()
                    Toast.makeText(context, getString(R.string.program_selection) + " " + programs[position], Toast.LENGTH_SHORT).show()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
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
        autoTimeCheckBox = view.findViewById(R.id.autoTimeCheckBox)
        dateEdit = view.findViewById<TextView>(R.id.editDate)
        timeEdit = view.findViewById<TextView>(R.id.editTime)

        // Cardio Part
        bodybuildingSelector = view.findViewById(R.id.bodyBuildingSelection)
        cardioSelector = view.findViewById(R.id.cardioSelection)
        staticExerciseSelector = view.findViewById(R.id.staticSelection)
        programSelectorLayout = view.findViewById(R.id.programSelectionLayout)
        exerciseTypeSelectorLayout = view.findViewById(R.id.exerciseTypeSelectionLayout)
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
        addButton.setOnClickListener(clickAddButton)
        machineListButton.setOnClickListener(onClickMachineListWithIcons) //onClickMachineList
        seriesEdit.onFocusChangeListener = touchRazEdit
        poidsEdit.onFocusChangeListener = touchRazEdit
        distanceEdit.onFocusChangeListener = touchRazEdit
        durationEdit.setOnClickListener(clickDateEdit)
        secondsEdit.onFocusChangeListener = touchRazEdit
        exerciseEdit.setOnKeyListener(checkExerciseExists)
        exerciseEdit.onFocusChangeListener = touchRazEdit
        exerciseEdit.onItemClickListener = onItemClickFilterList
        restTimeEdit.onFocusChangeListener = restTimeEditChange
        restTimeCheck.setOnCheckedChangeListener(restTimeCheckChange)
        bodybuildingSelector.setOnClickListener(clickExerciseTypeSelector)
        cardioSelector.setOnClickListener(clickExerciseTypeSelector)
        staticExerciseSelector.setOnClickListener(clickExerciseTypeSelector)
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

    private val durationSet = MyTimePickerDialog.OnTimeSetListener { _: TimePicker?, hourOfDay: Int, minute: Int, second: Int ->
        val strMinute: String = if (minute < 10) "0$minute" else minute.toString()
        val strHour: String = if (hourOfDay < 10) "0$hourOfDay" else hourOfDay.toString()
        val strSecond: String = if (second < 10) "0$second" else second.toString()
        val date = "$strHour:$strMinute:$strSecond"
        durationEdit.text = date
        hideKeyboard()
    }
    private lateinit var daoExerciseInProgram: DAOExerciseInProgram
    private lateinit var mDbMachine: DAOMachine
    private val clickExerciseTypeSelector = View.OnClickListener { v: View ->
        when (v.id) {
            R.id.staticSelection -> changeExerciseTypeUI(TYPE_STATIC, true)
            R.id.cardioSelection -> changeExerciseTypeUI(TYPE_CARDIO, true)
            R.id.bodyBuildingSelection -> changeExerciseTypeUI(TYPE_FONTE, true)
            else -> changeExerciseTypeUI(TYPE_FONTE, true)
        }
    }
    private val checkExerciseExists = View.OnKeyListener { _: View?, _: Int, _: KeyEvent? ->
        val lMach = mDbMachine.getMachine(exerciseEdit.text.toString())
        if (lMach == null) {
            showExerciseTypeSelector(true)
        } else {
            changeExerciseTypeUI(lMach.type, false)
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
        var timeStr: String
        var date: Date
        if (autoTimeCheckBox.isChecked) {
            date = Date()
            timeStr = DateConverter.currentTime()
        } else {
            date = DateConverter.editToDate(dateEdit.text.toString())
            timeStr = timeEdit.text.toString()
        }
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
                    seriesEdit.getText().toString().toInt(),
                    repsPicker.progress,
                    tmpPoids, // Always save in KG
                    // Always save in KG
                    getProfilFromMain(),
                    unitPoids, // Store Unit for future display
                    // Store Unit for future display
                    "", //Notes
                    //Notes
                    timeStr
                )
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
                    // Store Unit for future display
                    "", //Notes
                    //Notes
                    timeStr)
                runRestAndPrepareForNextExercise(restTime);
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
                var distance: Float
                distance = if (distanceEdit.text.toString().isEmpty()) {
                    0f
                } else {
                    distanceEdit.text.toString().replace(",".toRegex(), ".").toFloat()
                }
                var unitDistance = UnitConverter.UNIT_KM
                if (unitDistanceSpinner.selectedItem.toString()
                    == view?.context?.getString(R.string.MilesUnitLabel)) {
                    distance = UnitConverter.MilesToKm(distance) // Always convert to KG
                    unitDistance = UnitConverter.UNIT_MILES
                }
                daoCardio.addCardioRecord(date,
                    timeStr,
                    exerciseEdit.getText().toString(),
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
//        daoExerciseInProgram.closeCursor()
        // Launch Countdown


    }

    private fun runRestAndPrepareForNextExercise(restTime: Int) {
        // Launch Countdown
//        if (bLaunchRest && DateConverter.dateToLocalDateStr(date, context) == DateConverter.dateToLocalDateStr(Date(), context)) { // Only launch Countdown if date is today.
        val cdd = CountdownDialogbox(mainActivity, restTime)
//            cdd.setNbSeries(iNbSeries)
//            cdd.setTotalWeightMachine(iTotalWeight)
//            cdd.setTotalWeightSession(iTotalWeightSession)
        cdd.show()
//        }
    }

    private val onClickMachineListWithIcons = View.OnClickListener { v ->
        val oldCursor: Cursor
        if (machineListDialog.isShowing) {        // In case the dialog is already open
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
                if (machineListDialog.isShowing) {
                    machineListDialog.dismiss()
                }
            }
            val builder = AlertDialog.Builder(v.context)
            builder.setTitle(R.string.selectMachineDialogLabel)
            builder.setView(machineList)
            machineListDialog = builder.create()
            machineListDialog.show()
        }
    }
    private val onItemClickFilterList = OnItemClickListener { _: AdapterView<*>?, _: View?, _: Int, _: Long -> setCurrentExercise(exerciseEdit.text.toString()) }

    //Required for cardio/duration
    private val clickDateEdit = View.OnClickListener { v: View ->
        when (v.id) {
            R.id.editDuration -> showTimePicker(durationEdit)
            R.id.editMachine -> {
            }
        }
    }
    private val touchRazEdit = OnFocusChangeListener { v: View, hasFocus: Boolean ->
        if (hasFocus) {
            when (v.id) {
                R.id.editSerie -> seriesEdit.setText("")
//                repsPicker.progress=8
//                R.id.editRepetition -> repetitionEdit.setText("")
                R.id.editSeconds -> secondsEdit.setText("")
                R.id.editPoids -> poidsEdit.setText("")
                R.id.editDuration -> showTimePicker(durationEdit)
                R.id.editDistance -> distanceEdit.setText("")
                R.id.editMachine -> {
                    exerciseEdit.setText("")
                    exerciseImage.setImageResource(R.drawable.ic_machine)
                    minMaxLayout.visibility = View.GONE
                    showExerciseTypeSelector(true)
                }
            }
            v.post {
                val imm = Objects.requireNonNull(requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE)) as InputMethodManager
                imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT)
            }
        } else {
            if (v.id == R.id.editMachine) { // If a creation of a new machine is not ongoing.
                if (exerciseTypeSelectorLayout.visibility == View.GONE) setCurrentExercise(exerciseEdit.text.toString())
            }
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
                daoExerciseInProgram.deleteRecord(idToDelete) //Toast.makeText(getContext(), getResources().getText(R.string.removedid) + " " + idToDelete, Toast.LENGTH_SHORT).show();
                updateRecordTable(exerciseEdit.text.toString())
                KToast.infoToast(requireActivity(), resources.getText(R.string.removedid).toString(), Gravity.BOTTOM, KToast.LENGTH_LONG)
                sDialog.dismissWithAnimation()
            }
            .show()
    }

    override fun onStart() {
        super.onStart()
        mainActivity = this.activity as MainActivity
//        refreshData()
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
            showExerciseTypeSelector(true)
            minMaxLayout.visibility = View.GONE
            return
        }
        val lMachine = mDbMachine.getMachine(machineStr)
        if (lMachine == null) {
            exerciseEdit.setText("")
            exerciseImage.setImageResource(R.drawable.ic_machine) // Default image
            changeExerciseTypeUI(TYPE_FONTE, true)
            return
        }
        // Update EditView
        exerciseEdit.setText(lMachine.name)
        // Update exercise Image
        exerciseImage.setImageResource(R.drawable.ic_machine) // Default image
        val imgUtil = ImageUtil()
        ImageUtil.setThumb(exerciseImage, imgUtil.getThumbPath(lMachine.picture)) // Overwrite image is there is one
        // Update Table
//        updateRecordTable(lMachine.name)
        // Update display type
        changeExerciseTypeUI(lMachine.type, false)
        // Update last values
//        updateLastRecord()
    }

    private fun setRunningExercise(exercise: ExerciseInProgram) {
        // Update EditView
        exerciseEdit.setText(exercise.exerciseName)
        // Update exercise Image
        exerciseImage.setImageResource(R.drawable.ic_machine) // Default image
//        val imgUtil = ImageUtil() //TODO images currently not supported in program runner
//        ImageUtil.setThumb(exerciseImage, imgUtil.getThumbPath(exercise.picture)) // Overwrite image is there is one
        // Update Table
        // Update display type
        changeExerciseTypeUI(exercise.type, false)
        updateRecordTable(exercise.exerciseName)
        if (exercise.type == TYPE_FONTE) {
            repsPicker.progress = exercise.repetition
            seriesEdit.setText(exercise.serie.toString())
            restTimeEdit.setText(exercise.secRest.toString())
            poidsEdit.setText(exercise.poids.toString())
        } else if (exercise.type == TYPE_CARDIO) {
            durationEdit.text = exercise.duration
            distanceEdit.setText(exercise.distance)
            unitDistanceSpinner.setSelection(exercise.distanceUnit, false)
        } else if (exercise.type == TYPE_STATIC) {
            secondsEdit.setText(exercise.seconds)
        }
    }

//    @SuppressLint("SetTextI18n")
//    private fun updateLastRecord() {
//        val lLastRecord = daoRecord.getLastRecord(profil)
//        // Default Values
//        seriesEdit.setText("1")
//        repetitionEdit.setText("10")
//        secondsEdit.setText("60")
//        poidsEdit.setText("50")
//        distanceEdit.setText("1")
//        durationEdit.text = "00:10:00"
//        if (lLastRecord != null) {
//            if (lLastRecord.type == TYPE_FONTE) {
//                val lLastBodyBuildingRecord = lLastRecord as ARecord
//                seriesEdit.setText(lLastBodyBuildingRecord.serie.toString())
//                repetitionEdit.setText(lLastBodyBuildingRecord.repetition.toString())
//                unitSpinner.setSelection(lLastBodyBuildingRecord.unit)
//                val numberFormat = DecimalFormat("#.##")
//                if (lLastBodyBuildingRecord.unit == UnitConverter.UNIT_LBS) poidsEdit.setText(numberFormat.format(UnitConverter.KgtoLbs(lLastBodyBuildingRecord.poids).toDouble())) else poidsEdit.setText(numberFormat.format(lLastBodyBuildingRecord.poids.toDouble()))
//            } else if (lLastRecord.type == TYPE_CARDIO) {
//                val lLastCardioRecord = lLastRecord as Cardio
//                durationEdit.text = DateConverter.durationToHoursMinutesSecondsStr(lLastCardioRecord.duration)
//                unitDistanceSpinner.setSelection(lLastCardioRecord.distanceUnit)
//                val numberFormat = DecimalFormat("#.##")
//                if (lLastCardioRecord.distanceUnit == UnitConverter.UNIT_MILES) distanceEdit.setText(numberFormat.format(UnitConverter.KmToMiles(lLastCardioRecord.distance).toDouble())) else distanceEdit.setText(numberFormat.format(lLastCardioRecord.distance.toDouble()))
//            } else if (lLastRecord.type == TYPE_STATIC) {
//                val lLastStaticRecord = lLastRecord as StaticExercise
//                seriesEdit.setText(lLastStaticRecord.serie.toString())
//                secondsEdit.setText(lLastStaticRecord.second.toString())
//                unitSpinner.setSelection(lLastStaticRecord.unit)
//                val numberFormat = DecimalFormat("#.##")
//                if (lLastStaticRecord.unit == UnitConverter.UNIT_LBS) poidsEdit.setText(numberFormat.format(UnitConverter.KgtoLbs(lLastStaticRecord.poids).toDouble())) else poidsEdit.setText(numberFormat.format(lLastStaticRecord.poids.toDouble()))
//            }
//        }
//    }

    private fun updateRecordTable(exerciseName: String) { // Records from records table
        mainActivity.currentMachine = exerciseName
        requireView().post {
            val c: Cursor?
            val oldCursor: Cursor
            val r = daoRecord.getLastRecord(profil)
            //Get results
            c = if (r != null) daoRecord.getTop3DatesRecords(profil) else return@post
            if (c == null || c.count == 0) {
                recordList.adapter = null
            } else {
                if (recordList.adapter == null) {
                    val mTableAdapter = RecordCursorAdapter(mainActivity, c, 0, itemClickDeleteRecord, itemClickDeleteRecord)
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
        val fragmentView = view
        if (fragmentView != null) {
            if (profil != null) {
                daoExerciseInProgram.setProfile(profil)
                if (exercisesFromProgram.isNotEmpty()) {
                    val currentExercise = exercisesFromProgram[currentExerciseOrder]
                    setRunningExercise(currentExercise)
                    updateRecordTable(currentExercise.exerciseName)
                }
            }
        }
    }

    private fun showExerciseTypeSelector(displaySelector: Boolean) {
        if (displaySelector) exerciseTypeSelectorLayout.visibility = View.VISIBLE else exerciseTypeSelectorLayout.visibility = View.GONE
    }

    private fun changeExerciseTypeUI(pType: Int, displaySelector: Boolean) {
        showExerciseTypeSelector(displaySelector)
        when (pType) {
            TYPE_CARDIO -> {
                cardioSelector.setBackgroundColor(ContextCompat.getColor(requireActivity().baseContext, R.color.record_background_odd))
                bodybuildingSelector.setBackgroundColor(ContextCompat.getColor(requireActivity().baseContext, R.color.background))
                staticExerciseSelector.setBackgroundColor(ContextCompat.getColor(requireActivity().baseContext, R.color.background))
                serieCardView.visibility = View.GONE
                repetitionCardView.visibility = View.GONE
                weightCardView.visibility = View.GONE
                secondsCardView.visibility = View.GONE
                restTimeLayout.visibility = View.GONE
                distanceCardView.visibility = View.VISIBLE
                durationCardView.visibility = View.VISIBLE
                selectedType = TYPE_CARDIO
            }
            TYPE_STATIC -> {
                cardioSelector.setBackgroundColor(ContextCompat.getColor(requireActivity().baseContext, R.color.background))
                bodybuildingSelector.setBackgroundColor(ContextCompat.getColor(requireActivity().baseContext, R.color.background))
                staticExerciseSelector.setBackgroundColor(ContextCompat.getColor(requireActivity().baseContext, R.color.record_background_odd))
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
                cardioSelector.setBackgroundColor(ContextCompat.getColor(requireActivity().baseContext, R.color.background))
                bodybuildingSelector.setBackgroundColor(ContextCompat.getColor(requireActivity().baseContext, R.color.record_background_odd))
                staticExerciseSelector.setBackgroundColor(ContextCompat.getColor(requireActivity().baseContext, R.color.background))
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
                cardioSelector.setBackgroundColor(ContextCompat.getColor(requireActivity().baseContext, R.color.background))
                bodybuildingSelector.setBackgroundColor(ContextCompat.getColor(requireActivity().baseContext, R.color.record_background_odd))
                staticExerciseSelector.setBackgroundColor(ContextCompat.getColor(requireActivity().baseContext, R.color.background))
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
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        restTimeEdit.setText(sharedPref?.getString("restTime", ""))
        restTimeCheck.isChecked = sharedPref!!.getBoolean("restCheck", true)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) refreshData()
    }

    private fun hideKeyboard() {
        val inputMethodManager = Objects.requireNonNull(requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE)) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    companion object {
        /**
         * Create a new instance of DetailsFragment, initialized to
         * show the text at 'index'.
         */
        fun newInstance(name: String?, id: Int): ProgramRunner {
            val f = ProgramRunner()
            // Supply index input as an argument.
            val args = Bundle()
            args.putString("name", name)
            args.putInt("id", id)
            f.arguments = args
            return f
        }
    }
}
