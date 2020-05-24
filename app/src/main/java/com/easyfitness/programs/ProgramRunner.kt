package com.easyfitness.programs

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.database.Cursor
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.View.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.preference.PreferenceManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.easyfitness.*
import com.easyfitness.DAO.*
import com.easyfitness.DAO.DAOMachine.*
import com.easyfitness.machines.ExerciseDetailsPager
import com.easyfitness.machines.MachineCursorAdapter
import com.easyfitness.utils.DateConverter
import com.easyfitness.utils.ImageUtil
import com.easyfitness.utils.UnitConverter
import com.ikovac.timepickerwithseconds.MyTimePickerDialog
import com.ikovac.timepickerwithseconds.TimePicker
import com.onurkaganaldemir.ktoastlib.KToast
import com.pacific.timer.Rx2Timer
import kotlinx.android.synthetic.main.tab_program_runner.*
import timber.log.Timber
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class ProgramRunner : Fragment(R.layout.tab_program_runner) {
    private val progressScaleFix: Int = 3
    private lateinit var mainActivity: MainActivity
    private var lTableColor = 1
    private var machineListDialog: AlertDialog? = null
    private var selectedType = TYPE_FONTE
    private lateinit var daoProgram: DAOProgram
    private var programId: Long = 1
    private var currentExerciseOrder = 0  //start from 0
    private lateinit var exercisesFromProgram: List<ExerciseInProgram>
    private lateinit var daoRecord: DAORecord
    private lateinit var strengthRecordsDao: DAOFonte
    private lateinit var daoCardio: DAOCardio
    private lateinit var daoStatic: DAOStatic
    private lateinit var daoExerciseInProgram: DAOExerciseInProgram
    private lateinit var mDbMachine: DAOMachine
    private lateinit var swipeDetectorListener: SwipeDetectorListener
    private var restTimer: Rx2Timer? = null
    private lateinit var staticTimer: Rx2Timer
    private var staticTimerRunning: Boolean = false
    private var restTimerRunning: Boolean = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
            val profileId: Long = (requireActivity() as MainActivity).currentProfile.id
            val programsFragment = ProgramsFragment.newInstance("", profileId)
            Toast.makeText(context, R.string.add_program_first, Toast.LENGTH_LONG).show()
            requireActivity().supportFragmentManager.commit {
                addToBackStack(null)
                add(R.id.fragment_container, programsFragment)
            }
        } else {
            val programFirst = daoProgram.getRecord(programs[0])
            if (programFirst != null) {
                programId = programFirst.id
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, programs)
                programSelect.adapter = adapter
                programSelect.onItemSelectedListener = object :
                    AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>,
                                                view: View?, position: Int, id: Long) {
                        val program: Program? = daoProgram.getRecord(programs[position])
                        if (program != null) {
                            programId = program.id
                            currentExerciseOrder = 0
                            exercisesFromProgram = daoExerciseInProgram.getAllExerciseInProgram(programId)
                            exerciseIndicator.initDots(exercisesFromProgram.size)
                            exerciseInProgramNumber.text = exercisesFromProgram.size.toString()
                            refreshData()
                            Toast.makeText(context, getString(R.string.program_selection) + " " + programs[position], Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                    }
                }
            }
        }
        swipeDetectorListener = SwipeDetectorListener(this)
        nextExerciseArrow.setOnClickListener(clickArrows)
        previousExerciseArrow.setOnClickListener(clickArrows)
        addButton.setOnClickListener(clickAddButton)
        failButton.setOnClickListener(clickFailButton)
        exercisesListButton.setOnClickListener(onClickMachineListWithIcons)
        durationEdit.setOnClickListener(clickDateEdit)
        exerciseEdit.setOnKeyListener(checkExerciseExists)
        exerciseEdit.onItemClickListener = onItemClickFilterList
        notesInExercise.addTextChangedListener(object : TextWatcher {
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
        imageExerciseThumb.setOnClickListener {
            val m = mDbMachine.getMachine(exerciseEdit.text.toString())
            if (m != null) {
                val profileId: Long = (requireActivity() as MainActivity).currentProfile.id
                val machineDetailsFragment = ExerciseDetailsPager.newInstance(m.id, profileId)
                requireActivity().supportFragmentManager.commit {
                    addToBackStack(null)
                    add(R.id.fragment_container, machineDetailsFragment)
                }
            }
        }

        exerciseIndicator.onSelectListener = {
            chooseExercise(it)
        }


        recordList.setOnTouchListener(swipeDetectorListener) //this is different view so require seperate listener to work
        tabProgramRunner.setOnTouchListener(swipeDetectorListener)
    }

    private fun chooseExercise(selected: Int) {
        currentExerciseOrder = selected
        currentExerciseNumber.text = (selected + 1).toString()
        refreshData()
    }


    fun nextExercise() {
        if (exercisesFromProgram.isNotEmpty() && currentExerciseOrder < exercisesFromProgram.size - 1) {
            currentExerciseOrder++
            currentExerciseNumber.text = (currentExerciseOrder + 1).toString()
            exerciseIndicator.setDotSelection(currentExerciseOrder)
            refreshData()
        }
    }

    fun previousExercise() {
        if (exercisesFromProgram.isNotEmpty() && currentExerciseOrder > 0) {
            currentExerciseOrder--
            currentExerciseNumber.text = (currentExerciseOrder + 1).toString()
            exerciseIndicator.setDotSelection(currentExerciseOrder)
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

    private val clickArrows = OnClickListener { v: View ->
        when (v.id) {
            R.id.nextExerciseArrow -> nextExercise()
            R.id.previousExerciseArrow -> previousExercise()
        }
    }
    private val checkExerciseExists = OnKeyListener { _: View?, _: Int, _: KeyEvent? ->
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
            setCurrentMachine(r.exercise, r.type)
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


    private val restClickTimer = OnClickListener {
        restTimer?.restart()
    }


    private val clickStaticTimer = OnClickListener {
        staticTimerRunning = if (!staticTimerRunning) {
            if (staticTimer.isPause) {
                staticTimer.resume()
            } else {
                staticTimer.start()
            }
            true
        } else {
            staticTimer.pause()
            false
        }
    }

    private val clickStaticReset = OnClickListener {
        staticTimer.restart()
        staticTimerRunning = true
    }

    private val clickResetStaticTimer = OnLongClickListener {
        staticTimer.restart()
        staticTimerRunning = true
        true
    }

    @SuppressLint("SetTextI18n")
    private val clickAddButton = OnClickListener {
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
        val date = Date()
        val timeStr = DateConverter.currentTime()

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
                    duration = tmpDate!!.time
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
        // Launch Countdown
        restFillBackgroundProgress.visibility = VISIBLE
        exerciseIndicator[currentExerciseOrder].setBackgroundResource(R.drawable.green_button_background)
        runRest(restTime)
        showTotalWorkload(iTotalWeightSession, iTotalWeight, iNbSeries)
    }

    private val clickFailButton = OnClickListener {
        exerciseIndicator[currentExerciseOrder].setBackgroundResource(R.drawable.red_button_background)
    }

    private fun runRest(restTime: Int) {
        restFillBackgroundProgress.visibility = VISIBLE
        restTimerRunning = true
        if (requireContext().getSharedPreferences("nextExerciseSwitch", Context.MODE_PRIVATE).getBoolean("nextExerciseSwitch", true)) {
            nextExercise()
        }
        restFillBackgroundProgress.setDuration(restTime.toLong() * progressScaleFix)
        restTimer?.stop()
        restTimer = Rx2Timer.builder()
            .initialDelay(0)
            .take(restTime)
            .onEmit { count ->
                restFillBackgroundProgress.setProgress(count.toInt() * progressScaleFix)
                countDown.text = getString(R.string.rest_counter, count)
            }
            .onError { countDown.text = getString(R.string.error) }
            .onComplete {
                countDown.text = getString(R.string.rest_finished)
                restFillBackgroundProgress.visibility = GONE
                restTimerRunning = false
                if (requireContext().getSharedPreferences("playRestSound", Context.MODE_PRIVATE).getBoolean("playRestSound", true)) {
                    val mediaPlayer = MediaPlayer.create(context, R.raw.chime)
                    mediaPlayer.start()
                }
            }
            .build()
        restTimer?.start()
        restFillBackgroundProgress.setOnClickListener(restClickTimer)
    }

    private fun showTotalWorkload(total: Float, total2: Float, total3: Int): Float {
        return total + total2 + total3
    }

    private val onClickMachineListWithIcons = OnClickListener { v ->
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
    private val clickDateEdit = OnClickListener { v: View ->
        when (v.id) {
            R.id.durationEdit -> showTimePicker(durationEdit)
            R.id.exerciseEdit -> {
//                machineImage.setImageResource(R.drawable.ic_gym_bench_50dp)
                minMaxLayout.visibility = GONE
            }
        }
    }

    private fun updateNote() {
        val previousNote = exercisesFromProgram[currentExerciseOrder].note
        if (notesInExercise.text.toString() != previousNote) {
            daoExerciseInProgram.updateString(exercisesFromProgram[currentExerciseOrder],
                DAOExerciseInProgram.NOTES, notesInExercise.text.toString())
            exercisesFromProgram[currentExerciseOrder].note = notesInExercise.text.toString()
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
        mainActivity = requireActivity() as MainActivity
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
        if (timeTextView!!.id == R.id.durationEdit) {
            val mDurationFrag = TimePickerDialogFragment.newInstance(durationSet, hour, min, sec)
            val fm = requireActivity().supportFragmentManager
            mDurationFrag.show(fm.beginTransaction(), "dialog_time")
        }
    }

    val fragment: ProgramRunner
        get() = this

    private val profil: Profile?
        get() = mainActivity.currentProfile

    val machine: String
        get() = exerciseEdit.text.toString()

    private fun setCurrentExercise(machineStr: String) {
        if (machineStr.isEmpty()) {
            imageExerciseThumb.setImageResource(R.drawable.ic_gym_bench_50dp) // Default image
            minMaxLayout.visibility = GONE
            return
        }
        val lMachine = mDbMachine.getMachine(machineStr)
        if (lMachine == null) {
            exerciseEdit.setText("")
            imageExerciseThumb.setImageResource(R.drawable.ic_gym_bench_50dp) // Default image
            changeExerciseTypeUI(TYPE_FONTE)
            return
        }
        // Update EditView
        exerciseEdit.setText(lMachine.name)
        // Update exercise Image
        imageExerciseThumb.setImageResource(R.drawable.ic_gym_bench_50dp) // Default image
        val imgUtil = ImageUtil()
        ImageUtil.setThumb(imageExerciseThumb, imgUtil.getThumbPath(lMachine.picture)) // Overwrite image is there is one
        // Update Table
        updateRecordTable(lMachine.name)
        // Update display type
        changeExerciseTypeUI(lMachine.type)
        // Update last values
        updateLastRecord(lMachine)
    }

    @SuppressLint("SetTextI18n")
    private fun setRunningExercise(exercise: ExerciseInProgram) {
        // Update EditView
        exerciseEdit.setText(exercise.exerciseName)
        // Update exercise Image
        when (exercise.type) {
            TYPE_CARDIO -> {
                imageExerciseThumb.setImageResource(R.drawable.ic_training_white_50dp)
            }
            TYPE_STATIC -> {
                imageExerciseThumb.setImageResource(R.drawable.ic_static)
                staticFillBackgroundProgress.setDuration((exercise.seconds * progressScaleFix).toLong())
                staticTimer = Rx2Timer.builder()
                    .initialDelay(0)
                    .take(exercise.seconds)
                    .onEmit { count ->
                        staticFillBackgroundProgress.setProgress(count.toInt() * progressScaleFix)
                        countDownStatic.text = getString(R.string.count_string, count)
                    }
                    .onError { countDownStatic.text = getString(R.string.error) }
                    .onComplete {
                        val staticFinishStr = getString(R.string.exercise_time) + " " + exercise.seconds.toString() + " " + getString(R.string.seconds)
                        countDownStatic.text = staticFinishStr
                        if (requireContext().getSharedPreferences("playStaticExerciseFinishSound", Context.MODE_PRIVATE).getBoolean("playStaticExerciseFinishSound", true)) {
                            val mediaPlayer = MediaPlayer.create(context, R.raw.chime)
                            mediaPlayer.start()
                        }
                    }
                    .build()
            }
            else -> {
                imageExerciseThumb.setImageResource(R.drawable.ic_gym_bench_50dp) // Default image
            }
        }
        val lMachine = mDbMachine.getMachine(exercise.exerciseName)
        if (lMachine != null) {
            val imgUtil = ImageUtil()
            ImageUtil.setThumb(imageExerciseThumb, imgUtil.getThumbPath(lMachine.picture)) // Overwrite image is there is one
        }
        changeExerciseTypeUI(exercise.type)
        updateRecordTable(exercise.exerciseName)
        notesInExercise.setText(exercise.note)
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

    private fun setCurrentMachine(machineStr: String, exerciseType: Int) {
        if (machineStr.isEmpty()) {
            imageExerciseThumb.setImageResource(R.drawable.ic_gym_bench_50dp) // Default image
            minMaxLayout.visibility = GONE
            return
        }
        val lMachine = mDbMachine.getMachine(machineStr)
        if (lMachine == null) {
            exerciseEdit.setText("")
            imageExerciseThumb.setImageResource(R.drawable.ic_gym_bench_50dp) // Default image
            changeExerciseTypeUI(TYPE_FONTE)
            updateMinMax(null)
            return
        }

        exerciseEdit.setText(lMachine.name)
        // Update exercise Image
        when (exerciseType) {
            TYPE_CARDIO -> {
                imageExerciseThumb.setImageResource(R.drawable.ic_training_white_50dp)
            }
            TYPE_STATIC -> {
                imageExerciseThumb.setImageResource(R.drawable.ic_static)
            }
            else -> {
                imageExerciseThumb.setImageResource(R.drawable.ic_gym_bench_50dp) // Default image
            }
        }
        imageExerciseThumb.setImageResource(R.drawable.ic_gym_bench_50dp) // Default image
        val imgUtil = ImageUtil()
        ImageUtil.setThumb(imageExerciseThumb, imgUtil.getThumbPath(lMachine.picture)) // Overwrite image is there is one

        updateRecordTable(lMachine.name)
        changeExerciseTypeUI(exerciseType)

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
                    minMaxLayout.visibility = VISIBLE
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
                    minMaxLayout.visibility = GONE
                }
            } else if (m.type == TYPE_CARDIO) {
                minMaxLayout.visibility = GONE
            }
        } else {
            minText.text = "-"
            maxText.text = "-"
            minMaxLayout.visibility = GONE
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
            val limitShowedResults = 10
            c = (daoRecord.getAllRecordByMachines(profil, exerciseName, limitShowedResults)
                ?: return@post)
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
        return mainActivity.currentProfile
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
                serieCardView.visibility = GONE
                repetitionCardView.visibility = GONE
                weightCardView.visibility = GONE
                secondsCardView.visibility = GONE
                distanceCardView.visibility = VISIBLE
                durationCardView.visibility = VISIBLE
                staticFillBackgroundProgress.visibility = GONE
                selectedType = TYPE_CARDIO
            }
            TYPE_STATIC -> {
                serieCardView.visibility = VISIBLE
                repetitionCardView.visibility = GONE
                secondsCardView.visibility = VISIBLE
                weightCardView.visibility = VISIBLE
                restTimeLayout.visibility = VISIBLE
                distanceCardView.visibility = GONE
                durationCardView.visibility = GONE
                staticFillBackgroundProgress.visibility = VISIBLE
                staticFillBackgroundProgress.setOnClickListener(clickStaticTimer)
                staticFillBackgroundProgress.setOnLongClickListener(clickResetStaticTimer)
                resetStaticTimerButton.setOnClickListener(clickStaticReset)
                selectedType = TYPE_STATIC
            }
            TYPE_FONTE -> {
                serieCardView.visibility = VISIBLE
                repetitionCardView.visibility = VISIBLE
                secondsCardView.visibility = GONE
                weightCardView.visibility = VISIBLE
                restTimeLayout.visibility = VISIBLE
                distanceCardView.visibility = GONE
                durationCardView.visibility = GONE
                staticFillBackgroundProgress.visibility = GONE
                selectedType = TYPE_FONTE
            }
            else -> {
                serieCardView.visibility = VISIBLE
                repetitionCardView.visibility = VISIBLE
                secondsCardView.visibility = GONE
                weightCardView.visibility = VISIBLE
                restTimeLayout.visibility = VISIBLE
                distanceCardView.visibility = GONE
                durationCardView.visibility = GONE
                staticFillBackgroundProgress.visibility = GONE
                selectedType = TYPE_FONTE
            }
        }
    }

    private fun saveSharedParams() {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE)
        val editor = sharedPref?.edit()
        editor?.putString("restTime", restTimeEdit.text.toString())
        editor?.putBoolean("restCheck", restTimeCheck.isChecked)
        editor?.putBoolean("showDetails", restControlLayout.isShown)
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
