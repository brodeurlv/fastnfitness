package com.easyfitness.intro;

import android.os.Bundle;
import android.view.Gravity;

import androidx.fragment.app.Fragment;

import com.easyfitness.DAO.DAOProfile;
import com.easyfitness.DAO.Profile;
import com.easyfitness.R;
import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;
import com.onurkaganaldemir.ktoastlib.KToast;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainIntroActivity extends AppIntro {

    public static final String EXTRA_FULLSCREEN = "com.heinrichreimersoftware.materialintro.demo.EXTRA_FULLSCREEN";
    public static final String EXTRA_SCROLLABLE = "com.heinrichreimersoftware.materialintro.demo.EXTRA_SCROLLABLE";
    public static final String EXTRA_CUSTOM_FRAGMENTS = "com.heinrichreimersoftware.materialintro.demo.EXTRA_CUSTOM_FRAGMENTS";
    public static final String EXTRA_PERMISSIONS = "com.heinrichreimersoftware.materialintro.demo.EXTRA_PERMISSIONS";
    public static final String EXTRA_SHOW_BACK = "com.heinrichreimersoftware.materialintro.demo.EXTRA_SHOW_BACK";
    public static final String EXTRA_SHOW_NEXT = "com.heinrichreimersoftware.materialintro.demo.EXTRA_SHOW_NEXT";
    public static final String EXTRA_SKIP_ENABLED = "com.heinrichreimersoftware.materialintro.demo.EXTRA_SKIP_ENABLED";
    public static final String EXTRA_FINISH_ENABLED = "com.heinrichreimersoftware.materialintro.demo.EXTRA_FINISH_ENABLED";
    public static final String EXTRA_GET_STARTED_ENABLED = "com.heinrichreimersoftware.materialintro.demo.EXTRA_GET_STARTED_ENABLED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add intro slides using AppIntroFragment
        addSlide(AppIntroFragment.newInstance(
                getString(R.string.introSlide1Title),
                getString(R.string.introSlide1Text),
                R.drawable.web_hi_res_512,
                getResources().getColor(R.color.launcher_background)
        ));

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.introSlide2Title),
                getString(R.string.introSlide2Text),
                R.drawable.bench_hi_res_512,
                getResources().getColor(R.color.launcher_background)
        ));

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.titleSlideEssential),
                getString(R.string.textSlideEssential),
                R.drawable.idea_hi_res_485,
                getResources().getColor(R.color.launcher_background)
        ));

        addSlide(AppIntroFragment.newInstance(
                getString(R.string.titleSlideOpenSource),
                getString(R.string.textSlideOpenSource),
                R.drawable.group_hi_res_512,
                getResources().getColor(R.color.launcher_background)
        ));

        if(!wasProfileCreated()) {
            addSlide(NewProfileFragment.newInstance(this));
        }

        // Disable the Skip button
        setSkipButtonEnabled(false);
    }

    protected boolean wasProfileCreated() {
        // Initialize the DB and add a profile slide if needed
        DAOProfile mDbProfils = new DAOProfile(this.getApplicationContext());

        // Check if there is any profile in the DB, if not show the profile creation screen
        return mDbProfils.getCount() > 0;
    }

    protected void onNextPressed(Fragment currentFragment) {
        this.goToNextSlide(false);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        // Handle skip action
        finish();
    }


    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);

        if(currentFragment instanceof NewProfileFragment newProFrag) {
            Profile looseProfile = newProFrag.gatherLooseProfileFromUI();
            if(looseProfile == null) {
                KToast.warningToast(this, getResources().getText(R.string.fillNameField).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                return;
            }

            final SweetAlertDialog disabledBtnDialog = new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText("Create profile " + looseProfile.getName() + "?")
                    .setConfirmText("Confirm")
                    .setCancelText("Cancel")
                    .setConfirmClickListener((dialog) -> {
                            DAOProfile mDbProfiles = new DAOProfile(MainIntroActivity.this);
                            mDbProfiles.addProfile(looseProfile);
                            dialog.dismiss();
                            finishOk();
                    });
            disabledBtnDialog.show();
            return;
        }


        finishOk();
    }

    public void finishOk() {
        setResult(RESULT_OK);
        finish();
    }
}
