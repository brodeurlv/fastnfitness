package com.easyfitness;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.easyfitness.DAO.DAOProfil;
import com.easyfitness.DAO.Profile;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.EditableInputView.EditableInputView;
import com.easyfitness.utils.Gender;
import com.easyfitness.utils.ImageUtil;
import com.easyfitness.utils.RealPathUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.onurkaganaldemir.ktoastlib.KToast;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class ProfileFragment extends Fragment {
    EditableInputView sizeEdit = null;
    EditableInputView birthdayEdit = null;
    EditableInputView nameEdit = null;
    EditableInputView genderEdit = null;
    CircularImageView roundProfile = null;
    FloatingActionButton photoButton = null;
    String mCurrentPhotoPath = null;

    MainActivity mActivity = null;
    private DAOProfil mDb = null;
    private Profile mProfile = null;
    private ImageUtil imgUtil = null;
    private EditableInputView.OnTextChangedListener itemOnTextChange = this::requestForSave;
    private OnClickListener onClickMachinePhoto = v -> CreatePhotoSourceDialog();

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
        genderEdit = view.findViewById(R.id.gender);
        roundProfile = view.findViewById(R.id.photo);
        photoButton = view.findViewById(R.id.actionCamera);


        mDb = new DAOProfil(view.getContext());
        mProfile = getProfil();

        /* Initialisation des valeurs */
        imgUtil = new ImageUtil(roundProfile);
        // ImageView must be set in OnStart. Not in OnCreateView

        /* Initialisation des boutons */

        genderEdit.setCustomDialogBuilder(view1 -> {
            return new SweetAlertDialog(view1.getContext(), SweetAlertDialog.NORMAL_TYPE)
                .setTitleText(getContext().getString(R.string.edit_value))
                .setNeutralText(getString(R.string.maleGender))
                .setCancelText(getString(R.string.femaleGender))
                .setConfirmText(getString(R.string.otherGender))
                .setNeutralClickListener(sDialog -> {
                    String oldValue = genderEdit.getText();
                    if (!oldValue.equals(getString(R.string.maleGender))) {
                        genderEdit.setText(getString(R.string.maleGender));
                        requestForSave(genderEdit);
                    }
                    sDialog.dismissWithAnimation();
                })
                .setCancelClickListener(sDialog -> {
                    String oldValue = genderEdit.getText();
                    if (!oldValue.equals(getString(R.string.femaleGender))) {
                        genderEdit.setText(getString(R.string.femaleGender));
                        requestForSave(genderEdit);
                    }
                    sDialog.dismissWithAnimation();
                })
                .setConfirmClickListener(sDialog -> {
                    String oldValue = genderEdit.getText();
                    if (!oldValue.equals(getString(R.string.otherGender))) {
                        genderEdit.setText(getString(R.string.otherGender));
                        requestForSave(genderEdit);
                    }
                    sDialog.dismissWithAnimation();
                });
        });

        photoButton.setOnClickListener(onClickMachinePhoto);

        imgUtil.setOnDeleteImageListener(imgUtil -> {
            imgUtil.getView().setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_profile_black));
            mCurrentPhotoPath = null;
            requestForSave(imgUtil.getView());
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        roundProfile.post(() -> {
            refreshData();
            sizeEdit.setOnTextChangeListener(itemOnTextChange);
            birthdayEdit.setOnTextChangeListener(itemOnTextChange);
            nameEdit.setOnTextChangeListener(itemOnTextChange);
            genderEdit.setOnTextChangeListener(itemOnTextChange);
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

    private void refreshData() {
        mProfile = getProfil();

        /* Initialisation des valeurs */
        if (mProfile.getSize() == 0) {
            sizeEdit.setText("");
            sizeEdit.setHint(getString(R.string.profileEnterYourSize));
        } else {
            sizeEdit.setText(String.valueOf(mProfile.getSize()));
        }

        switch (mProfile.getGender()) {
            case Gender.MALE:
                genderEdit.setText(getString(R.string.maleGender));
                break;
            case Gender.FEMALE:
                genderEdit.setText(getString(R.string.femaleGender));
                break;
            case Gender.OTHER:
                genderEdit.setText(getString(R.string.otherGender));
                break;
            default:
                genderEdit.setText("");
                genderEdit.setHint(getString(R.string.enter_gender_here));
        }

        if (mProfile.getBirthday().getTime() == 0) {
            birthdayEdit.setText("");
            birthdayEdit.setHint(getString(R.string.profileEnterYourBirthday));
        } else {
            birthdayEdit.setText(DateConverter.dateToLocalDateStr(mProfile.getBirthday(), getContext()));
            //sizeEdit.setNormalColor();
        }
        nameEdit.setText(mProfile.getName());

        if (mProfile.getPhoto() != null) {
            ImageUtil.setPic(roundProfile, mProfile.getPhoto());
            roundProfile.invalidate();
        } else
            roundProfile.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_profile_black));
    }

    private void requestForSave(View view) {
        boolean profileToUpdate = false;

        // Save all the fields in the Profile
        switch (view.getId()) {
            case R.id.name:
                mProfile.setName(nameEdit.getText());
                profileToUpdate = true;
                break;
            case R.id.size:
                try {
                    mProfile.setSize((int) Float.parseFloat(sizeEdit.getText()));
                } catch (NumberFormatException e) {
                    mProfile.setSize(0);
                }
                profileToUpdate = true;
                break;
            case R.id.birthday:
                mProfile.setBirthday(DateConverter.localDateStrToDate(birthdayEdit.getText(), getContext()));
                profileToUpdate = true;
                break;
            case R.id.photo:
                mProfile.setPhoto(mCurrentPhotoPath);
                profileToUpdate = true;
                break;
            case R.id.gender:
                int lGender = Gender.UNKNOWN;
                if (genderEdit.getText().equals(getString(R.string.maleGender))) {
                    lGender = Gender.MALE;
                } else if (genderEdit.getText().equals(getString(R.string.femaleGender))) {
                    lGender = Gender.FEMALE;
                } else if (genderEdit.getText().equals(getString(R.string.otherGender))) {
                    lGender = Gender.OTHER;
                }
                mProfile.setGender(lGender);
                profileToUpdate = true;
                break;
        }

        if (profileToUpdate) {
            mDb.updateProfile(mProfile);
            KToast.infoToast(getActivity(), mProfile.getName() + " updated", Gravity.BOTTOM, KToast.LENGTH_SHORT);
            mActivity.setCurrentProfil(mProfile);
        }
    }

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
                    ImageUtil.setPic(roundProfile, mCurrentPhotoPath);
                    ImageUtil.saveThumb(mCurrentPhotoPath);
                    imgUtil.galleryAddPic(this, mCurrentPhotoPath);
                    requestForSave(roundProfile);
                }
                break;
            case ImageUtil.REQUEST_PICK_GALERY_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    String realPath;
                    realPath = RealPathUtil.getRealPath(this.getContext(), data.getData());

                    ImageUtil.setPic(roundProfile, realPath);
                    ImageUtil.saveThumb(realPath);
                    mCurrentPhotoPath = realPath;
                    requestForSave(roundProfile);
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
                    new File(storageDir.getPath() + imageFileName);
                    File DestinationFile;

                    try {
                        DestinationFile = imgUtil.moveFile(SourceFile, storageDir);
                        Log.v("Moving", "Moving file successful.");
                        realPath = DestinationFile.getPath();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.v("Moving", "Moving file failed.");
                    }

                    ImageUtil.setPic(roundProfile, realPath);
                    ImageUtil.saveThumb(realPath);
                    mCurrentPhotoPath = realPath;
                    requestForSave(roundProfile);
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
                break;
        }
    }
}
