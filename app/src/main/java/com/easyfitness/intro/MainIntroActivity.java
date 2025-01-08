package com.easyfitness.intro;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.easyfitness.DAO.DAOProfile;
import com.easyfitness.R;
import com.github.appintro.AppIntro;
import com.github.appintro.AppIntroFragment;

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

        // Initialize the DB and add a profile slide if needed
        DAOProfile mDbProfils = new DAOProfile(this.getApplicationContext());

        // Check if there is any profile in the DB, if not show the profile creation screen
        if (mDbProfils.getCount() == 0) {
            addSlide(NewProfileFragment.newInstance(this));
        }

        // Disable the Skip button
        setSkipButtonEnabled(false);
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
        // Handle finish action
        setResult(RESULT_OK);
        finish();
    }
}
