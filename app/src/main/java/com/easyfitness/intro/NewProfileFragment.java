package com.easyfitness.intro;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.easyfitness.DAO.DAOProfile;
import com.easyfitness.DAO.Profile;
import com.easyfitness.R;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.enums.Gender;
import com.google.android.material.datepicker.MaterialDatePicker;
//import com.heinrichreimersoftware.materialintro.app.SlideFragment;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.text.DateFormat;
import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class NewProfileFragment extends Fragment {

    private MainIntroActivity mMainIntroActivity;
    private EditText mName;
    private TextView mBirthday;
    private Button mBtCreate;
    private RadioButton mRbMale;
    private RadioButton mRbFemale;
    private RadioButton mRbOtherGender;
    private boolean mProfileCreated = false;

    public NewProfileFragment(MainIntroActivity mainIntroActivity) {
        // Required empty public constructor
        mMainIntroActivity = mainIntroActivity;
    }

    public static NewProfileFragment newInstance(MainIntroActivity mainIntroActivity) {

        return new NewProfileFragment(mainIntroActivity);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.introfragment_newprofile, container, false);

        mName = view.findViewById(R.id.profileName);
        mBirthday = view.findViewById(R.id.profileBirthday);
        mBtCreate = view.findViewById(R.id.create_newprofil);
        mRbMale = view.findViewById(R.id.radioButtonMale);
        mRbFemale = view.findViewById(R.id.radioButtonFemale);
        mRbOtherGender = view.findViewById(R.id.radioButtonOtherGender);

        mBirthday.setOnClickListener(v -> showDatePickerFragment());

        mBtCreate.setOnClickListener(v -> createProfile());

        return view;
    }

    private void showDatePickerFragment() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select a Date")
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            // Convert selection (a Long timestamp) into a formatted date string
            String formattedDate = DateFormat.getDateInstance().format(new Date(selection));
            mBirthday.setText(formattedDate);
        });

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }

    private void createProfile() {
        DAOProfile mDbProfiles = new DAOProfile(getContext());

        if (mName.getText().toString().isEmpty()) {
            KToast.warningToast(getActivity(), getResources().getText(R.string.fillNameField).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
        } else {
            int gender = Gender.UNKNOWN;
            if (mRbMale.isChecked()) {
                gender = Gender.MALE;
            } else if (mRbFemale.isChecked()) {
                gender = Gender.FEMALE;
            } else if (mRbOtherGender.isChecked()) {
                gender = Gender.OTHER;
            }

            Profile p = new Profile(mName.getText().toString(), 0, DateConverter.localDateStrToDate(mBirthday.getText().toString(), getActivity()), gender);
            mDbProfiles.addProfile(p);

            new SweetAlertDialog(getActivity(), SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText(p.getName())
                    .setContentText(getContext().getResources().getText(R.string.profileCreated).toString())
                    .setConfirmClickListener(sDialog -> {
                        // Profile created, enable the next button and proceed
                        mMainIntroActivity.onNextPressed(this);  // Transition to the next slide
                    })
                    .show();

            mProfileCreated = true;
        }
    }

}
