package com.easyfitness.programs

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.easyfitness.DAO.DAOMachine
import com.easyfitness.DAO.DAOProgram
import com.easyfitness.DAO.DAORecord
import com.easyfitness.DAO.Program
import com.easyfitness.R
import com.easyfitness.utils.ImageUtil
import com.easyfitness.utils.RealPathUtil
import com.theartofdev.edmodo.cropper.CropImage
import timber.log.Timber
import java.io.File
import java.io.IOException

class ProgramDetailsFragment : Fragment() {
    private lateinit var machineName: EditText

    var machinePhoto: ImageView? = null

    // Selection part
    var exerciseTypeSelectorLayout: LinearLayout? = null
    var bodybuildingSelector: TextView? = null
    var cardioSelector: TextView? = null
    var selectedType = DAOMachine.TYPE_FONTE
    var machineNameArg: String? = null
    var machineIdArg: Long = 0
    var machineProfilIdArg: Long = 0

    var pager: ProgramDetailsPager? = null
    var mDbMachine: DAOProgram? = null
    var mDbRecord: DAORecord? = null
    lateinit var mMachine: Program
    var fragmentView: View? = null
    var imgUtil: ImageUtil? = null
    var mCurrentPhotoPath: String? = null
    private var toBeSaved = false
    private var watcher: TextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence, start: Int,
                                   before: Int, count: Int) {
            requestForSave()
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,
                                       after: Int) {
        }

        override fun afterTextChanged(s: Editable) {}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.program_details, container, false)
        fragmentView = view

        // Initialisation de l'historique
        mDbMachine = DAOProgram(view.context)
        mDbRecord = DAORecord(view.context)
        machineName = view.findViewById(R.id.machine_name)
        val args = this.arguments
        machineIdArg = args!!.getLong("machineID")
        machineProfilIdArg = args.getLong("machineProfile")
        mMachine = mDbMachine!!.getRecord(machineNameArg)!!
        machineNameArg = mMachine.programName
        if (machineNameArg == "") {
            requestForSave()
        }
        machineName.setText(machineNameArg)
        exerciseTypeSelectorLayout!!.visibility = View.GONE
        if (mMachine.type == DAOMachine.TYPE_CARDIO) {
            cardioSelector!!.setBackgroundColor(resources.getColor(R.color.record_background_odd))
            bodybuildingSelector!!.visibility = View.GONE
            bodybuildingSelector!!.setBackgroundColor(resources.getColor(R.color.background))
            selectedType = mMachine.type
            view.findViewById<View>(R.id.machine_muscles).visibility = View.GONE
            view.findViewById<View>(R.id.machine_muscles_textview).visibility = View.GONE
        } else {
            cardioSelector!!.setBackgroundColor(resources.getColor(R.color.background))
            cardioSelector!!.visibility = View.GONE
            bodybuildingSelector!!.setBackgroundColor(resources.getColor(R.color.record_background_odd))
            selectedType = mMachine.getType()
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // Ensure you call it only once :
                fragmentView!!.viewTreeObserver.removeOnGlobalLayoutListener(this)
                // Here you can get the size :)
                if (mCurrentPhotoPath != null && mCurrentPhotoPath!!.isNotEmpty()) {
                    ImageUtil.setPic(machinePhoto, mCurrentPhotoPath)
                } else {
                    if (mMachine.type == DAOMachine.TYPE_FONTE || mMachine.type == DAOMachine.TYPE_STATIC) {
                        imgUtil!!.view.setImageDrawable(activity!!.resources.getDrawable(R.drawable.ic_machine))
                    } else {
                        imgUtil!!.view.setImageDrawable(activity!!.resources.getDrawable(R.drawable.ic_running))
                    }
                    machinePhoto!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
                }
                machinePhoto!!.maxHeight = (requireView().height * 0.2).toInt() // Taille initiale
            }
        })
        machineName.addTextChangedListener(watcher)
        if (parentFragment is ProgramDetailsPager) {
            pager = parentFragment as ProgramDetailsPager?
        }
        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ImageUtil.REQUEST_TAKE_PHOTO -> if (resultCode == Activity.RESULT_OK) {
                mCurrentPhotoPath = imgUtil!!.filePath
                ImageUtil.setPic(machinePhoto, mCurrentPhotoPath)
                ImageUtil.saveThumb(mCurrentPhotoPath)
                imgUtil!!.galleryAddPic(this, mCurrentPhotoPath)
                requestForSave()
            }
            ImageUtil.REQUEST_PICK_GALERY_PHOTO -> if (resultCode == Activity.RESULT_OK) {
                val realPath: String = RealPathUtil.getRealPath(this.context, data!!.data)
                ImageUtil.setPic(machinePhoto, realPath)
                ImageUtil.saveThumb(realPath)
                mCurrentPhotoPath = realPath
                requestForSave()
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    val resultUri = result.uri
                    var realPath: String?
                    realPath = RealPathUtil.getRealPath(this.context, resultUri)
                    val sourceFile = File(realPath)
                    var storageDir: File? = null
                    val state = Environment.getExternalStorageState()
                    if (Environment.MEDIA_MOUNTED != state) {
                        return
                    } else {
                        //We use the FastNFitness directory for saving our .csv file.
                        storageDir = Environment.getExternalStoragePublicDirectory("/FastnFitness/Camera/")
                        if (!storageDir.exists()) {
                            storageDir.mkdirs()
                        }
                    }
                    var destinationFile: File? = null
                    try {
                        destinationFile = imgUtil!!.moveFile(sourceFile, storageDir)
                        Timber.tag("Moving").v("Moving file successful.")
                        realPath = destinationFile.path
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Timber.tag("Moving").v("Moving file failed.")
                    }
                    ImageUtil.setPic(machinePhoto, realPath)
                    ImageUtil.saveThumb(realPath)
                    mCurrentPhotoPath = realPath
                    requestForSave()
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    result.error
                }
            }
        }
    }

    val `this`: ProgramDetailsFragment
        get() = this

    private fun requestForSave() {
        toBeSaved = true // setting state
        if (pager != null) pager!!.requestForSave()
    }

    val machine: Program?
        get() {
            val m = mMachine
            m.programName = machineName.text.toString()
            //        m.setProfil(selectedType);
            return m
        }

    companion object {
        /**
         * Create a new instance of DetailsFragment, initialized to
         * show the text at 'index'.
         */
        fun newInstance(machineId: Long, machineProfile: Long): ProgramDetailsFragment {
            val f = ProgramDetailsFragment()
            val args = Bundle()
            args.putLong("machineID", machineId)
            args.putLong("machineProfile", machineProfile)
            f.arguments = args
            return f
        }
    }
}
