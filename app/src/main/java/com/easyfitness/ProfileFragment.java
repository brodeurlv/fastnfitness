package com.easyfitness;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.easyfitness.DAO.DAOProfil;
import com.easyfitness.DAO.Profile;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.EditableInputView.EditableInputView;
import com.easyfitness.utils.ImageUtil;
import com.easyfitness.utils.RealPathUtil;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.onurkaganaldemir.ktoastlib.KToast;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ProfileFragment extends Fragment {
    EditableInputView sizeEdit = null;
    EditableInputView birthdayEdit = null;
    EditableInputView nameEdit = null;
    CircularImageView roundProfile = null;
    FloatingActionButton photoButton = null;
    String mCurrentPhotoPath = null;

    MainActivity mActivity = null;
    private DAOProfil mDb = null;
    private Profile mProfile = null;
    private ImageUtil imgUtil = null;

    DatePickerDialogFragment mDateFrag = null;

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static ProfileFragment newInstance(String name, int id) {
        ProfileFragment f = new ProfileFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putInt("id", id);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.profile, container, false);

        sizeEdit = view.findViewById(R.id.size);
        birthdayEdit = view.findViewById(R.id.birthday);
        nameEdit = view.findViewById(R.id.name);
        roundProfile = view.findViewById(R.id.photo);
        photoButton = view.findViewById(R.id.actionCamera);


        mDb = new DAOProfil(view.getContext());
        mProfile = getProfil();

        /* Initialisation des valeurs */
        sizeEdit.setText(String.valueOf(mProfile.getSize()));
        birthdayEdit.setText(DateConverter.dateToLocalDateStr(mProfile.getBirthday(), getContext()));
        nameEdit.setText(mProfile.getName());
        imgUtil = new ImageUtil();
        // ImageView must be set in OnStart. Not in OnCreateView

        /* Initialisation des boutons */
        sizeEdit.setOnTextChangeListener(itemOnTextChange);
        birthdayEdit.setOnTextChangeListener(itemOnTextChange);
        nameEdit.setOnTextChangeListener(itemOnTextChange);
        photoButton.setOnClickListener(onClickMachinePhoto);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        roundProfile.post(new Runnable() {
            @Override
            public void run() {
                refreshData();
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = (MainActivity) activity;
    }

    public String getName() {
        return getArguments().getString("name");
    }
    public int getFragmentId() {
        return getArguments().getInt("id", 0);
    }

    private void refreshData() {
        mProfile = getProfil();

        /* Initialisation des valeurs */
        sizeEdit.setText(String.valueOf(mProfile.getSize()));
        birthdayEdit.setText(DateConverter.dateToLocalDateStr(mProfile.getBirthday(), getContext()));
        nameEdit.setText(mProfile.getName());

        if (mProfile.getPhoto() != null) {
            imgUtil.setPic(roundProfile, mProfile.getPhoto());
            roundProfile.invalidate();
        } else
            roundProfile.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_profile_black));
    }


    private void requestForSave() {
        // Save all the fields in the Profile
        mProfile.setName(nameEdit.getText());
        mProfile.setSize(Integer.parseInt(sizeEdit.getText()));
        mProfile.setBirthday(DateConverter.localDateStrToDate(birthdayEdit.getText(), getContext()));
        mProfile.setPhoto(mCurrentPhotoPath);
        mDb.updateProfile(mProfile);

        KToast.infoToast(getActivity(), mProfile.getName() + " updated", Gravity.BOTTOM, KToast.LENGTH_SHORT);
    }

    ;

    private Profile getProfil() {
        return ((MainActivity) getActivity()).getCurrentProfil();
    }

    public Fragment getFragment() {
        return this;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) refreshData();
    }

    private EditableInputView.OnTextChangedListener itemOnTextChange = new EditableInputView.OnTextChangedListener() {

        @Override
        public void onTextChanged(EditableInputView view) {
            requestForSave();
        }
    };


    private OnClickListener onClickMachinePhoto = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CreatePhotoSourceDialog();
        }
    };

    private boolean CreatePhotoSourceDialog() {
        if (imgUtil == null)
            imgUtil = new ImageUtil();

        return imgUtil.CreatePhotoSourceDialog(this);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case ImageUtil.REQUEST_TAKE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    mCurrentPhotoPath = imgUtil.getFilePath();
                    imgUtil.setPic(roundProfile, mCurrentPhotoPath);
                    imgUtil.saveThumb(mCurrentPhotoPath);
                    imgUtil.galleryAddPic(this, mCurrentPhotoPath);
                    requestForSave();
                }
                break;
            case ImageUtil.REQUEST_PICK_GALERY_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    String realPath;
                    realPath = RealPathUtil.getRealPath(this.getContext(), data.getData());

                    imgUtil.setPic(roundProfile, realPath);
                    imgUtil.saveThumb(realPath);
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
                    File DestinationFile = new File(storageDir.getPath().toString() + imageFileName);

                    try {
                        DestinationFile = imgUtil.moveFile(SourceFile, storageDir);
                        Log.v("Moving", "Moving file successful.");
                        realPath = DestinationFile.getPath();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.v("Moving", "Moving file failed.");
                    }

                    imgUtil.setPic(roundProfile, realPath);
                    imgUtil.saveThumb(realPath);
                    mCurrentPhotoPath = realPath;
                    requestForSave();
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
                break;
        }
    }
}
