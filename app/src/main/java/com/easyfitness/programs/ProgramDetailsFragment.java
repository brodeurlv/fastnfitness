package com.easyfitness.programs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;

import com.easyfitness.BtnClickListener;
import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.DAO.DAOProgram;
import com.easyfitness.DAO.DAORecord;
//import com.easyfitness.DAO.Machine;
import com.easyfitness.DAO.Program;
import com.easyfitness.R;
import com.easyfitness.machines.ExerciseDetailsPager;
import com.easyfitness.utils.ImageUtil;
import com.easyfitness.utils.Keyboard;
import com.easyfitness.utils.RealPathUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.onurkaganaldemir.ktoastlib.KToast;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.Fragment;
import cn.pedant.SweetAlert.SweetAlertDialog;


public class ProgramDetailsFragment extends Fragment {
    public final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 101;
    // http://labs.makemachine.net/2010/03/android-multi-selection-dialogs/
    //protected CharSequence[] _muscles = {"Biceps", "Triceps", "Epaules", "Pectoraux", "Dorseaux", "Quadriceps", "Adducteurs"};
    protected List<String> _musclesArray = new ArrayList<>();
    protected boolean[] _selections;
//    Spinner typeList = null; /*Halteres, Machines avec Poids, Cardio*/
    TextView musclesList = null;
    EditText machineName = null;
//    EditText machineDescription = null;
    ImageView machinePhoto = null;
//    FloatingActionButton machineAction = null;
//    LinearLayout machinePhotoLayout = null;
    // Selection part
    LinearLayout exerciseTypeSelectorLayout = null;
    TextView bodybuildingSelector = null;
    TextView cardioSelector = null;
    int selectedType = DAOMachine.TYPE_FONTE;
    String machineNameArg = null;
    long machineIdArg = 0;
    long machineProfilIdArg = 0;
//    boolean isImageFitToScreen = false;
    ProgramDetailsPager pager = null;
    ArrayList selectMuscleList = new ArrayList();
    DAOProgram mDbMachine = null;
    DAORecord mDbRecord = null;
    Program mMachine;

    View fragmentView = null;

    ImageUtil imgUtil = null;
    boolean isCreateMuscleDialogActive = false;
    String mCurrentPhotoPath = null;
    private boolean toBeSaved;
    public TextWatcher watcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start,
                                  int before, int count) {
            requestForSave();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };
//    private OnClickListener onClickMusclesList = v -> CreateMuscleDialog();
//    private OnLongClickListener onLongClickMachinePhoto = v -> CreatePhotoSourceDialog();

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static ProgramDetailsFragment newInstance(long machineId, long machineProfile) {
        ProgramDetailsFragment f = new ProgramDetailsFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putLong("machineID", machineId);
        args.putLong("machineProfile", machineProfile);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.program_details, container, false);
        fragmentView = view;

        // Initialisation de l'historique
        mDbMachine = new DAOProgram(view.getContext());
        mDbRecord = new DAORecord(view.getContext());

        machineName = view.findViewById(R.id.machine_name);
        Bundle args = this.getArguments();

        machineIdArg = args.getLong("machineID");
        machineProfilIdArg = args.getLong("machineProfile");

        // set events
//        musclesList.setOnClickListener(onClickMusclesList);
//        machinePhoto.setOnLongClickListener(onLongClickMachinePhoto);

        mMachine = mDbMachine.getRecord(machineNameArg);
        machineNameArg = mMachine.getProgramName();

        if (machineNameArg.equals("")) {
            requestForSave();
        }

        machineName.setText(machineNameArg);
        exerciseTypeSelectorLayout.setVisibility(View.GONE);

        if (mMachine.getType() == DAOMachine.TYPE_CARDIO) {
            cardioSelector.setBackgroundColor(getResources().getColor(R.color.record_background_odd));
            bodybuildingSelector.setVisibility(View.GONE);
            bodybuildingSelector.setBackgroundColor(getResources().getColor(R.color.background));
            selectedType = mMachine.getType();
            view.findViewById(R.id.machine_muscles).setVisibility(View.GONE);
            view.findViewById(R.id.machine_muscles_textview).setVisibility(View.GONE);
        } else {
            cardioSelector.setBackgroundColor(getResources().getColor(R.color.background));
            cardioSelector.setVisibility(View.GONE);
            bodybuildingSelector.setBackgroundColor(getResources().getColor(R.color.record_background_odd));
            selectedType = mMachine.getType();
        }

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                // Ensure you call it only once :
                fragmentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                // Here you can get the size :)

                if (mCurrentPhotoPath != null && !mCurrentPhotoPath.isEmpty()) {
                    ImageUtil.setPic(machinePhoto, mCurrentPhotoPath);
                } else {
                    if (mMachine.getType() == DAOMachine.TYPE_FONTE || mMachine.getType() == DAOMachine.TYPE_STATIC) {
                        imgUtil.getView().setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_machine));
                    } else {
                        imgUtil.getView().setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_running));
                    }
                    machinePhoto.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                }
                machinePhoto.setMaxHeight((int) (getView().getHeight() * 0.2)); // Taille initiale
            }
        });

        machineName.addTextChangedListener(watcher);

        if (getParentFragment() instanceof ProgramDetailsPager) {
            pager = (ProgramDetailsPager) getParentFragment();
        }

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void showDeleteDialog(final long idToDelete) {

        new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
            .setTitleText(getString(R.string.DeleteRecordDialog))
            .setContentText(getResources().getText(R.string.areyousure).toString())
            .setCancelText(getResources().getText(R.string.global_no).toString())
            .setConfirmText(getResources().getText(R.string.global_yes).toString())
            .showCancelButton(true)
            .setConfirmClickListener(sDialog -> {
                mDbRecord.deleteRecord(idToDelete);
                KToast.infoToast(getActivity(), getResources().getText(R.string.removedid).toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                sDialog.dismissWithAnimation();
            })
            .show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case ImageUtil.REQUEST_TAKE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    mCurrentPhotoPath = imgUtil.getFilePath();
                    ImageUtil.setPic(machinePhoto, mCurrentPhotoPath);
                    ImageUtil.saveThumb(mCurrentPhotoPath);
                    imgUtil.galleryAddPic(this, mCurrentPhotoPath);
                    requestForSave();
                }
                break;
            case ImageUtil.REQUEST_PICK_GALERY_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    String realPath;
                    realPath = RealPathUtil.getRealPath(this.getContext(), data.getData());

                    ImageUtil.setPic(machinePhoto, realPath);
                    ImageUtil.saveThumb(realPath);
                    mCurrentPhotoPath = realPath;
                    requestForSave();
                }
                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == Activity.RESULT_OK) {
                    Uri resultUri = result.getUri();
                    String realPath;
                    realPath = RealPathUtil.getRealPath(this.getContext(), resultUri);

                    // Le fichier est crée dans le cache.
                    // Déplacer le fichier dans le repertoire de FastNFitness
                    File SourceFile = new File(realPath);

                    File storageDir = null;
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String imageFileName = "JPEG_" + timeStamp + ".jpg";
                    String state = Environment.getExternalStorageState();
                    if (!Environment.MEDIA_MOUNTED.equals(state)) {
                        return;
                    } else {
                        //We use the FastNFitness directory for saving our .csv file.
                        storageDir = Environment.getExternalStoragePublicDirectory("/FastnFitness/Camera/");
                        if (!storageDir.exists()) {
                            storageDir.mkdirs();
                        }
                    }
                    File DestinationFile = null;

                    try {
                        DestinationFile = imgUtil.moveFile(SourceFile, storageDir);
                        Log.v("Moving", "Moving file successful.");
                        realPath = DestinationFile.getPath();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.v("Moving", "Moving file failed.");
                    }

                    ImageUtil.setPic(machinePhoto, realPath);
                    ImageUtil.saveThumb(realPath);
                    mCurrentPhotoPath = realPath;
                    requestForSave();
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
                break;
        }
    }

    public void setMuscleText(String t) {
        musclesList.setText(t);
    }

    public ProgramDetailsFragment getThis() {
        return this;
    }

    private void requestForSave() {
        toBeSaved = true; // setting state
        if (pager != null) pager.requestForSave();
    }


//    public void buildMusclesTable() {
//        _musclesArray.add(getActivity().getResources().getString(R.string.biceps));
//        _musclesArray.add(getActivity().getResources().getString(R.string.triceps));
//        _musclesArray.add(getActivity().getResources().getString(R.string.pectoraux));
//        _musclesArray.add(getActivity().getResources().getString(R.string.dorseaux));
//        _musclesArray.add(getActivity().getResources().getString(R.string.abdominaux));
//        _musclesArray.add(getActivity().getResources().getString(R.string.quadriceps));
//        _musclesArray.add(getActivity().getResources().getString(R.string.ischio_jambiers));
//        _musclesArray.add(getActivity().getResources().getString(R.string.adducteurs));
//        _musclesArray.add(getActivity().getResources().getString(R.string.mollets));
//        _musclesArray.add(getActivity().getResources().getString(R.string.deltoids));
//        _musclesArray.add(getActivity().getResources().getString(R.string.trapezius));
//        _musclesArray.add(getActivity().getResources().getString(R.string.shoulders));
//        _musclesArray.add(getActivity().getResources().getString(R.string.obliques));
//
//         _selections = new boolean[_musclesArray.size()];
//    }

    /*
     * @return the name of the Muscle depending on the language
     */
    public String getMuscleNameFromId(int id) {
        String ret = "";
        try {
            ret = _musclesArray.get(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    /*
     * @return the name of the Muscle depending on the language
     */
    public int getMuscleIdFromName(String pName) {
        for (int i = 0; i < _musclesArray.size(); i++) {
            if (_musclesArray.get(i).equals(pName)) return i;
        }
        return -1;
    }

//    /*
//     * @return the name of the Muscle depending on the language
//     */
//    public String getDBStringFromInput(String pInput) {
//        String[] data = pInput.split(";");
//        StringBuilder output = new StringBuilder();
//
//        if (pInput.isEmpty()) return "";
//
//        int i = 0;
//        if (data.length > 0) {
//            output = new StringBuilder(String.valueOf(getMuscleIdFromName(data[i])));
//            for (i = 1; i < data.length; i++) {
//                output.append(";").append(getMuscleIdFromName(data[i]));
//            }
//        }
//
//        return output.toString();
//    }


//    /*
//     * @return the name of the Muscle depending on the language
//     */
//    public String getInputFromDBString(String pDBString) {
//        String[] data = pDBString.split(";");
//        StringBuilder output = new StringBuilder();
//
//        int i = 0;
//
//        try {
//            if (data.length > 0) {
//                if (data[0].isEmpty()) return "";
//
//                if (!data[i].equals("-1")) {
//                    output = new StringBuilder(getMuscleNameFromId(Integer.valueOf(data[i])));
//                    _selections[Integer.valueOf(data[i])] = true;
//                    for (i = 1; i < data.length; i++) {
//                        if (!data[i].equals("-1")) {
//                            output.append(";").append(getMuscleNameFromId(Integer.valueOf(data[i])));
//                            _selections[Integer.valueOf(data[i])] = true;
//                        }
//                    }
//                }
//            }
//        } catch (NumberFormatException e) {
//            output = new StringBuilder();
//            e.printStackTrace();
//        }
//
//        return output.toString();
//    }

//    public int getCameraPhotoOrientation(Context context, Uri imageUri, String imagePath) {
//        int rotate = 0;
//        try {
//            context.getContentResolver().notifyChange(imageUri, null);
//            File imageFile = new File(imagePath);
//
//            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
//            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
//
//            switch (orientation) {
//                case ExifInterface.ORIENTATION_ROTATE_270:
//                    rotate = 270;
//                    break;
//                case ExifInterface.ORIENTATION_ROTATE_180:
//                    rotate = 180;
//                    break;
//                case ExifInterface.ORIENTATION_ROTATE_90:
//                    rotate = 90;
//                    break;
//            }
//
//            //Log.i("RotateImage", "Exif orientation: " + orientation);
//            //Log.i("RotateImage", "Rotate value: " + rotate);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return rotate;
//    }

    public boolean toBeSaved() {
        return toBeSaved;
    }

    public void machineSaved() {
        toBeSaved = false;
    }

    public Program getMachine() {
        Program m = mMachine;
        m.setProgramName(machineName.getText().toString());
//        m.setProfil(selectedType);
        return m;
    }
}

