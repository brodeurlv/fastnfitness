/*
 * MIT License
 *
 * Copyright (c) 2017 Jan Heinrich Reimer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.easyfitness.intro;

import android.app.Activity;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;

import com.easyfitness.DAO.DAOProfil;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DatePickerDialogFragment;
import com.easyfitness.MainActivity;
import com.easyfitness.R;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.Gender;
import com.heinrichreimersoftware.materialintro.app.SlideFragment;
import com.onurkaganaldemir.ktoastlib.KToast;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class NewProfileFragment extends SlideFragment {

    private EditText mName;
    private EditText mSize;
    private EditText mBirthday;
    private Button mBtCreate;
    private RadioButton mRbMale;
    private RadioButton mRbFemale;
    private RadioButton mRbOtherGender;

    private boolean mProfilCreated = false;
    private final View.OnClickListener clickCreateButton = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Initialisation des objets DB
            DAOProfil mDbProfils = new DAOProfil(v.getContext());

            if (mName.getText().toString().isEmpty()) {
                //Toast.makeText(getActivity().getBaseContext(), R.string.fillAllFields, Toast.LENGTH_SHORT).show();
                KToast.warningToast(getActivity(), getResources().getText(R.string.fillNameField).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
            } else {
                int size = 0;
                try {
                    if (!mSize.getText().toString().isEmpty()) {
                        size = Double.valueOf(mSize.getText().toString()).intValue();
                    }
                } catch (NumberFormatException e) {
                    size = 0;
                }

                int lGender = Gender.UNKNOWN;
                if (mRbMale.isChecked()) {
                    lGender = Gender.MALE;
                } else if (mRbFemale.isChecked()) {
                    lGender = Gender.FEMALE;
                } else if (mRbOtherGender.isChecked()) {
                    lGender = Gender.OTHER;
                }

                Profile p = new Profile(mName.getText().toString(), size, DateConverter.editToDate(mBirthday.getText().toString()), lGender);
                // Create the new profil
                mDbProfils.addProfil(p);
                //Toast.makeText(getActivity().getBaseContext(), R.string.profileCreated, Toast.LENGTH_SHORT).show();

                new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText(p.getName())
                    .setContentText(getContext().getResources().getText(R.string.profileCreated).toString())
                    .setConfirmClickListener(sDialog -> nextSlide())
                    .show();
                mProfilCreated = true;
            }
        }
    };
    private DatePickerDialogFragment mDateFrag = null;
    private MainActivity motherActivity;
    private OnDateSetListener dateSet = new OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            mBirthday.setText(DateConverter.dateToString(year, month + 1, day));
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(mBirthday.getWindowToken(), 0);
        }
    };

    public NewProfileFragment() {
        // Required empty public constructor
    }

    public static NewProfileFragment newInstance() {
        return new NewProfileFragment();
    }

    private void showDatePickerFragment() {
        if (mDateFrag == null) {
            mDateFrag = DatePickerDialogFragment.newInstance(dateSet);
        }

        FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
        mDateFrag.show(ft, "dialog");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.introfragment_newprofile, container, false);

        mName = view.findViewById(R.id.profileName);
        mSize = view.findViewById(R.id.profileSize);
        mBirthday = view.findViewById(R.id.profileBirthday);
        mBtCreate = view.findViewById(R.id.create_newprofil);
        mRbMale = view.findViewById(R.id.radioButtonMale);
        mRbFemale = view.findViewById(R.id.radioButtonFemale);
        mRbOtherGender = view.findViewById(R.id.radioButtonOtherGender);

        mBirthday.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(mBirthday.getWindowToken(), 0);
                showDatePickerFragment();
            }
        });

        /* Initialisation des boutons */
        mBtCreate.setOnClickListener(clickCreateButton);

        getIntroActivity().addOnNavigationBlockedListener((position, direction) -> {
            //Slide slide = getIntroActivity().getSlide(position);

            if (position == 4) {
                mBtCreate.callOnClick();
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean canGoForward() {
        return mProfilCreated;
    }

    public MainIntroActivity getIntroActivity() {
        if (getActivity() instanceof MainIntroActivity) {
            return (MainIntroActivity) getActivity();
        } else {
            throw new IllegalStateException("SlideFragments must be attached to MainIntroActivity.");
        }
    }
}
