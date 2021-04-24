package com.easyfitness;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.easyfitness.DAO.DAOProfile;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.bodymeasures.BodyMeasure;
import com.easyfitness.DAO.bodymeasures.BodyPart;
import com.easyfitness.DAO.bodymeasures.BodyPartExtensions;
import com.easyfitness.DAO.bodymeasures.DAOBodyMeasure;
import com.easyfitness.DAO.bodymeasures.DAOBodyPart;
import com.easyfitness.enums.Unit;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.Gender;
import com.easyfitness.utils.ImageUtil;
import com.easyfitness.utils.RealPathUtil;
import com.easyfitness.views.EditableInputView;
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
    TextView sizeEdit = null;
    EditableInputView birthdayEdit = null;
    EditableInputView nameEdit = null;
    EditableInputView genderEdit = null;
    CircularImageView roundProfile = null;
    FloatingActionButton photoButton = null;
    String mCurrentPhotoPath = null;

    MainActivity mActivity = null;
    private DAOProfile daoProfile = null;
    private DAOBodyMeasure daoBodyMeasure = null;
    private DAOBodyPart daoBodyPart = null;
    private ImageUtil imgUtil = null;
    private final OnClickListener onClickMachinePhoto = v -> CreatePhotoSourceDialog();
    private AppViMo appViMo;
    private ProfileViMo profileViMo;
    private final OnClickListener mOnClickListener = view -> {
        ValueEditorDialogbox editorDialogbox;
        if (view.getId() == R.id.size) {
                BodyPart sizeBodyPart = daoBodyPart.getBodyPartfromBodyPartKey(BodyPartExtensions.SIZE);
                BodyMeasure lastSizeValue = daoBodyMeasure.getLastBodyMeasures(sizeBodyPart.getId(), appViMo.getProfile().getValue());
                if (lastSizeValue == null) {
                    editorDialogbox = new ValueEditorDialogbox(getActivity(), new Date(), "", 0, SettingsFragment.getDefaultSizeUnit(getActivity()));
                } else {
                    editorDialogbox = new ValueEditorDialogbox(getActivity(), new Date(), "", lastSizeValue.getBodyMeasure(), lastSizeValue.getUnit());
                }
                editorDialogbox.setTitle(R.string.AddLabel);
                editorDialogbox.setPositiveButton(R.string.AddLabel);
                editorDialogbox.setOnDismissListener(dialog -> {
                    if (!editorDialogbox.isCancelled()) {
                        Date date = DateConverter.localDateStrToDate(editorDialogbox.getDate(), getContext());
                        float value = Float.parseFloat(editorDialogbox.getValue().replaceAll(",", "."));
                        Unit unit = Unit.fromString(editorDialogbox.getUnit());
                        daoBodyMeasure.addBodyMeasure(date, sizeBodyPart.getId(), value, appViMo.getProfile().getValue().getId(), unit);
                        profileViMo.setSize(value);
                        profileViMo.setSizeUnit(unit);
                        requestForSave();
                    }
                });
                editorDialogbox.show();
        }
    };
    private boolean isSaving;


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
        View view = inflater.inflate(R.layout.tab_profile, container, false);

        sizeEdit = view.findViewById(R.id.size);
        birthdayEdit = view.findViewById(R.id.birthday);
        nameEdit = view.findViewById(R.id.name);
        genderEdit = view.findViewById(R.id.gender);
        roundProfile = view.findViewById(R.id.photo);
        photoButton = view.findViewById(R.id.actionCamera);

        daoProfile = new DAOProfile(view.getContext());
        daoBodyMeasure = new DAOBodyMeasure(view.getContext());
        daoBodyPart = new DAOBodyPart(view.getContext());

        appViMo = new ViewModelProvider(requireActivity()).get(AppViMo.class);


        /* Initialisation des valeurs */
        imgUtil = new ImageUtil(roundProfile);
        // ImageView must be set in OnStart. Not in OnCreateView

        /* Initialisation des boutons */

        genderEdit.setCustomDialogBuilder(view1 -> {
            SweetAlertDialog dlg = new SweetAlertDialog(view1.getContext(), SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText(getContext().getString(R.string.edit_value))
                    .setNeutralText(getString(R.string.maleGender))
                    .setCancelText(getString(R.string.femaleGender))
                    .setConfirmText(getString(R.string.otherGender))
                    .setNeutralClickListener(sDialog -> {
                        int oldValue = profileViMo.getGender().getValue();
                        if (oldValue != Gender.MALE) {
                            profileViMo.setGender(Gender.MALE);
                            requestForSave();
                        }
                        sDialog.dismissWithAnimation();
                    })
                    .setCancelClickListener(sDialog -> {
                        int oldValue = profileViMo.getGender().getValue();
                        if (oldValue != Gender.FEMALE) {
                            profileViMo.setGender(Gender.FEMALE);
                            requestForSave();
                        }
                        sDialog.dismissWithAnimation();
                    })
                    .setConfirmClickListener(sDialog -> {
                        int oldValue = profileViMo.getGender().getValue();
                        if (oldValue != Gender.OTHER) {
                            profileViMo.setGender(Gender.OTHER);
                            requestForSave();
                        }
                        sDialog.dismissWithAnimation();
                    });

            dlg.setOnShowListener(sDialog -> {
                SweetAlertDialog sweetDlg = (SweetAlertDialog) sDialog;
                sweetDlg.getButton(SweetAlertDialog.BUTTON_CONFIRM).setBackgroundResource(R.color.record_background_odd);
                sweetDlg.getButton(SweetAlertDialog.BUTTON_CONFIRM).setPadding(0, 0, 0, 0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sweetDlg.getButton(SweetAlertDialog.BUTTON_CONFIRM).setAutoSizeTextTypeUniformWithConfiguration(8, 12, 1, TypedValue.COMPLEX_UNIT_SP);
                }
                sweetDlg.getButton(SweetAlertDialog.BUTTON_CANCEL).setBackgroundResource(R.color.record_background_odd);
                sweetDlg.getButton(SweetAlertDialog.BUTTON_CANCEL).setPadding(0, 0, 0, 0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sweetDlg.getButton(SweetAlertDialog.BUTTON_CANCEL).setAutoSizeTextTypeUniformWithConfiguration(8, 12, 1, TypedValue.COMPLEX_UNIT_SP);
                }
                sweetDlg.getButton(SweetAlertDialog.BUTTON_NEUTRAL).setBackgroundResource(R.color.record_background_odd);
                sweetDlg.getButton(SweetAlertDialog.BUTTON_NEUTRAL).setPadding(0, 0, 0, 0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sweetDlg.getButton(SweetAlertDialog.BUTTON_NEUTRAL).setAutoSizeTextTypeUniformWithConfiguration(8, 12, 1, TypedValue.COMPLEX_UNIT_SP);
                }
            });

            return dlg;
        });

        photoButton.setOnClickListener(onClickMachinePhoto);
        sizeEdit.setOnClickListener(mOnClickListener);

        imgUtil.setOnDeleteImageListener(imgUtil -> {
            imgUtil.getView().setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_person));
            profileViMo.setPhoto("");
            requestForSave();
        });

        profileViMo = new ViewModelProvider(this).get(ProfileViMo.class);
        profileViMo.getBirthday().observe(getViewLifecycleOwner(), birthday -> {
            if (birthday.getTime() == 0) {
                birthdayEdit.setText("");
                birthdayEdit.setHint(getString(R.string.profileEnterYourBirthday));
            } else {
                birthdayEdit.setText(DateConverter.dateToLocalDateStr(birthday, getContext()));
            }
        });
        profileViMo.getSize().observe(getViewLifecycleOwner(), size -> {
            if (size == 0) {
                sizeEdit.setText("");
                sizeEdit.setHint(getString(R.string.profileEnterYourSize));
            } else {
                sizeEdit.setText(String.valueOf(size) + profileViMo.getSizeUnit().getValue().toString());
            }
        });
        profileViMo.getSizeUnit().observe(getViewLifecycleOwner(), sizeUnit -> {
            if (profileViMo.getSize().getValue() == 0) {
                sizeEdit.setText("");
                sizeEdit.setHint(getString(R.string.profileEnterYourSize));
            } else {
                sizeEdit.setText(String.valueOf(profileViMo.getSize().getValue().toString()) + sizeUnit);
            }
            });
        profileViMo.getName().observe(getViewLifecycleOwner(), name -> {
            nameEdit.setText(name);
        });
        profileViMo.getPhoto().observe(getViewLifecycleOwner(), photo -> {
            if (photo != null) {
                ImageUtil.setPic(roundProfile, photo);
                roundProfile.invalidate();
            } else
                roundProfile.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.profile));
        });
        profileViMo.getGender().observe(getViewLifecycleOwner(), gender -> {
            switch (gender) {
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
        });

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        appViMo.getProfile().observe(getViewLifecycleOwner(), newProfile -> {
            if (!isSaving) {
                updateProfileViMo(newProfile);
            }
        });

        return view;
    }



    @Override
    public void onStart() {
        super.onStart();

        Profile profile = appViMo.getProfile().getValue();

        //Init View Model
        updateProfileViMo(profile);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mActivity = (MainActivity) context;
    }

    private void updateProfileViMo(Profile profile) {
        profileViMo.setBirthday(profile.getBirthday());
        profileViMo.setGender(profile.getGender());
        profileViMo.setPhoto(profile.getPhoto());
        profileViMo.setName(profile.getName());
        BodyMeasure sizeMeasure = daoBodyMeasure.getLastBodyMeasures(BodyPartExtensions.SIZE, profile);
        if (sizeMeasure!=null) {
            profileViMo.setSize(sizeMeasure.getBodyMeasure());
            profileViMo.setSizeUnit(sizeMeasure.getUnit());
        } else {
            profileViMo.setSize(0);
            profileViMo.setSizeUnit(Unit.CM);
        }
    }

    public String getName() {
        return getArguments().getString("name");
    }

    private void requestForSave() {
        isSaving = true;

        Profile profile = appViMo.getProfile().getValue();

        profile.setBirthday(profileViMo.getBirthday().getValue());
        profile.setGender(profileViMo.getGender().getValue());
        profile.setName(profileViMo.getName().getValue());
        profile.setPhoto(profileViMo.getPhoto().getValue());

        daoProfile.updateProfile(profile);
        KToast.infoToast(getActivity(), profile.getName() + " updated", Gravity.BOTTOM, KToast.LENGTH_SHORT);
        appViMo.setProfile(profile);

        isSaving = false;
    }

    public Fragment getFragment() {
        return this;
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
                    profileViMo.setPhoto(imgUtil.getFilePath());
                    requestForSave();
                    ImageUtil.setPic(roundProfile, mCurrentPhotoPath);
                    ImageUtil.saveThumb(mCurrentPhotoPath);
                    imgUtil.galleryAddPic(this, mCurrentPhotoPath);
                }
                break;
            case ImageUtil.REQUEST_PICK_GALERY_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    String realPath = RealPathUtil.getRealPath(this.getContext(), data.getData());
                    ImageUtil.setPic(roundProfile, realPath);
                    ImageUtil.saveThumb(realPath);
                    profileViMo.setPhoto(realPath);
                    requestForSave();
                }
                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == Activity.RESULT_OK) {
                    Uri resultUri = result.getUri();
                    String realPath = RealPathUtil.getRealPath(this.getContext(), resultUri);

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
                    profileViMo.setPhoto(realPath);
                    requestForSave();
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                }
                break;
        }
    }
}
