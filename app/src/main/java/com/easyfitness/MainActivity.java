package com.easyfitness;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.DAO.DAOProfile;
import com.easyfitness.DAO.DatabaseHelper;
import com.easyfitness.DAO.Machine;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.cardio.DAOOldCardio;
import com.easyfitness.DAO.cardio.OldCardio;
import com.easyfitness.DAO.export.CVSManager;
import com.easyfitness.DAO.program.DAOProgram;
import com.easyfitness.DAO.program.Program;
import com.easyfitness.DAO.record.DAOCardio;
import com.easyfitness.DAO.record.DAOFonte;
import com.easyfitness.DAO.record.DAORecord;
import com.easyfitness.DAO.record.DAOStatic;
import com.easyfitness.DAO.record.Record;
import com.easyfitness.bodymeasures.BodyPartListFragment;
import com.easyfitness.enums.DistanceUnit;
import com.easyfitness.enums.ExerciseType;
import com.easyfitness.enums.WeightUnit;
import com.easyfitness.fonte.FontesPagerFragment;
import com.easyfitness.intro.MainIntroActivity;
import com.easyfitness.machines.MachineFragment;
import com.easyfitness.programs.ProgramListFragment;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.FileNameUtil;
import com.easyfitness.utils.ImageUtil;
import com.easyfitness.utils.MusicController;
import com.easyfitness.utils.UnitConverter;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipFile;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {

    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.

    public static String FONTESPAGER = "FontePager";
    public static String WEIGHT = "Weight";
    public static String PROFILE = "Profile";
    public static String BODYTRACKING = "BodyTracking";

    public static String PROGRESSIMAGES = "ProgressImages";
    public static String BODYTRACKINGDETAILS = "BodyTrackingDetail";
    public static String ABOUT = "About";
    public static String SETTINGS = "Settings";
    public static String MACHINES = "Machines";
    public static String MACHINESDETAILS = "MachinesDetails";
    public static String WORKOUTS = "Workouts";
    public static String WORKOUTPAGER = "WorkoutPager";
    public static String PREFS_NAME = "prefsfile";
    private final int REQUEST_CODE_INTRO = 111;
    private final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_FOR_EXPORT = 1001;
    private final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_FOR_IMPORT = 1002;
    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 103;


    private static final int IMPORT_DATABASE = 2;
    public static final int OPEN_MUSIC_FILE = 3;

    private final MusicController musicController = new MusicController(this);
    CustomDrawerAdapter mDrawerAdapter;
    List<DrawerItem> dataList;
    /* Fragments */
    private FontesPagerFragment mpFontesPagerFrag = null;
    private WeightFragment mpWeightFrag = null;
    private ProfileFragment mpProfileFrag = null;
    private MachineFragment mpMachineFrag = null;
    private ProgressImagesFragment mpProgressImageFrag = null;
    private SettingsFragment mpSettingFrag = null;
    private AboutFragment mpAboutFrag = null;
    private BodyPartListFragment mpBodyPartListFrag = null;
    private ProgramListFragment mpWorkoutListFrag;
    private String currentFragmentName = "";
    private DAOProfile mDbProfils = null;
    private Profile mCurrentProfile = null;
    private long mCurrentProfilID = -1;
    private Toolbar top_toolbar = null;
    /* Navigation Drawer */
    private DrawerLayout mDrawerLayout = null;
    private ListView mDrawerList = null;
    private ActionBarDrawerToggle mDrawerToggle = null;
    private CircularImageView roundProfile = null;
    private String mCurrentMachine = "";
    private boolean mIntro014Launched = false;
    private boolean mMigrationBD15done = false;
    private boolean mMigrationToScopedStoragedone = false;
    private long mBackPressed;
    private AppViMo appViMo;

    private final PopupMenu.OnMenuItemClickListener onMenuItemClick = item -> {
        int id = item.getItemId();
        if (id == R.id.create_newprofil) {
            getActivity().CreateNewProfile();
            return true;
        } else if (id == R.id.change_profil) {
            String[] profilListArray = getActivity().mDbProfils.getAllProfile();

            AlertDialog.Builder changeProfilbuilder = new AlertDialog.Builder(getActivity());
            changeProfilbuilder.setTitle(getActivity().getResources().getText(R.string.profil_select_profil))
                    .setItems(profilListArray, (dialog, which) -> {
                        ListView lv = ((AlertDialog) dialog).getListView();
                        Object checkedItem = lv.getAdapter().getItem(which);
                        setCurrentProfile(checkedItem.toString());
                        KToast.infoToast(getActivity(), getActivity().getResources().getText(R.string.profileSelected) + " : " + checkedItem.toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                        //Toast.makeText(getApplicationContext(), getActivity().getResources().getText(R.string.profileSelected) + " : " + checkedItem.toString(), Toast.LENGTH_LONG).show();
                    });
            changeProfilbuilder.show();
            return true;
        } else if (id == R.id.delete_profil) {
            String[] profildeleteListArray = getActivity().mDbProfils.getAllProfile();

            AlertDialog.Builder deleteProfilbuilder = new AlertDialog.Builder(getActivity());
            deleteProfilbuilder.setTitle(getActivity().getResources().getText(R.string.profil_select_profil_to_delete))
                    .setItems(profildeleteListArray, (dialog, which) -> {
                        ListView lv = ((AlertDialog) dialog).getListView();
                        Object checkedItem = lv.getAdapter().getItem(which);
                        if (getCurrentProfile().getName().equals(checkedItem.toString())) {
                            KToast.errorToast(getActivity(), getActivity().getResources().getText(R.string.impossibleToDeleteProfile).toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                        } else {
                            Profile profileToDelete = mDbProfils.getProfile(checkedItem.toString());
                            mDbProfils.deleteProfile(profileToDelete);
                            KToast.infoToast(getActivity(), getString(R.string.profileDeleted) + ":" + checkedItem.toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                        }
                    });
            deleteProfilbuilder.show();
            return true;
        } else if (id == R.id.rename_profil) {
                getActivity().renameProfile();
                return true;
        } else if (id == R.id.param_profil) {
                showFragment(PROFILE);
                return true;
        }

        return false;
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String dayNightAuto = SP.getString("dayNightAuto", "2");
        int dayNightAutoValue;
        try {
            dayNightAutoValue = Integer.parseInt(dayNightAuto);
        } catch (NumberFormatException e) {
            dayNightAutoValue = 2;
        }
        if (dayNightAutoValue == getResources().getInteger(R.integer.dark_mode_value)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            SweetAlertDialog.DARK_STYLE = true;
        } else if (dayNightAutoValue == getResources().getInteger(R.integer.light_mode_value)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            SweetAlertDialog.DARK_STYLE = false;
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            int currentNightMode = getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK;
            switch (currentNightMode) {
                case Configuration.UI_MODE_NIGHT_YES:
                    SweetAlertDialog.DARK_STYLE = true;
                    break;
                case Configuration.UI_MODE_NIGHT_NO:
                default:
                    SweetAlertDialog.DARK_STYLE = false;
            }
        }

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        loadPreferences();

        top_toolbar = this.findViewById(R.id.actionToolbar);
        setSupportActionBar(top_toolbar);
        top_toolbar.setTitle(getResources().getText(R.string.app_name));

        if (savedInstanceState == null) {
            if (mpFontesPagerFrag == null)
                mpFontesPagerFrag = FontesPagerFragment.newInstance(FONTESPAGER, 6);
            if (mpWeightFrag == null) mpWeightFrag = WeightFragment.newInstance(WEIGHT, 5);
            if (mpProfileFrag == null) mpProfileFrag = ProfileFragment.newInstance(PROFILE, 10);
            if (mpSettingFrag == null) mpSettingFrag = SettingsFragment.newInstance(SETTINGS, 8);
            if (mpAboutFrag == null) mpAboutFrag = AboutFragment.newInstance(ABOUT, 4);
            if (mpMachineFrag == null) mpMachineFrag = MachineFragment.newInstance(MACHINES, 7);
            if (mpBodyPartListFrag == null)
                mpBodyPartListFrag = BodyPartListFragment.newInstance(BODYTRACKING, 9);
            if (mpWorkoutListFrag == null)
                mpWorkoutListFrag = ProgramListFragment.newInstance(WORKOUTS, 11);
        } else {
            mpFontesPagerFrag = (FontesPagerFragment) getSupportFragmentManager().getFragment(savedInstanceState, FONTESPAGER);
            mpWeightFrag = (WeightFragment) getSupportFragmentManager().getFragment(savedInstanceState, WEIGHT);
            mpProfileFrag = (ProfileFragment) getSupportFragmentManager().getFragment(savedInstanceState, PROFILE);
            mpSettingFrag = (SettingsFragment) getSupportFragmentManager().getFragment(savedInstanceState, SETTINGS);
            mpAboutFrag = (AboutFragment) getSupportFragmentManager().getFragment(savedInstanceState, ABOUT);
            mpMachineFrag = (MachineFragment) getSupportFragmentManager().getFragment(savedInstanceState, MACHINES);
            mpBodyPartListFrag = (BodyPartListFragment) getSupportFragmentManager().getFragment(savedInstanceState, BODYTRACKING);
            mpWorkoutListFrag = (ProgramListFragment) getSupportFragmentManager().getFragment(savedInstanceState, WORKOUTS);
        }

        appViMo = new ViewModelProvider(this).get(AppViMo.class);
        appViMo.getProfile().observe(this, profile -> {
            // Update UI
            setDrawerTitle(profile.getName());
            setPhotoProfile(profile.getPhoto());
            mCurrentProfilID = profile.getId();
            savePreferences();
        });

        DatabaseHelper.renameOldDatabase(this);

        if (DatabaseHelper.DATABASE_VERSION >= 15 && !mMigrationBD15done) {
            DAOOldCardio mDbOldCardio = new DAOOldCardio(this);
            DAOMachine lDAOMachine = new DAOMachine(this);
            if (mDbOldCardio.tableExists()) {
                DAOCardio mDbCardio = new DAOCardio(this);
                List<OldCardio> mList = mDbOldCardio.getAllRecords();
                for (OldCardio record : mList) {
                    Machine m = lDAOMachine.getMachine(record.getExercice());
                    String exerciseName = record.getExercice();
                    if (m != null) { // if a machine exists
                        if (m.getType() == ExerciseType.STRENGTH) { // if it is not a Cardio type
                            exerciseName = exerciseName + "-Cardio"; // add a suffix to
                        }
                    }

                    mDbCardio.addCardioRecordToFreeWorkout(record.getDate(), exerciseName, record.getDistance(), record.getDuration(), record.getProfil().getId(), DistanceUnit.KM);
                }
                mDbOldCardio.dropTable();

                DAORecord daoRecord = new DAORecord(this);
                List<Record> mFonteList = daoRecord.getAllRecords();
                for (Record record : mFonteList) {
                    record.setExerciseType(ExerciseType.STRENGTH);
                    daoRecord.updateRecord(record); // Automatically update record Type
                }
                List<Machine> machineList = lDAOMachine.getAllMachinesArray();
                for (Machine record : machineList) {
                    lDAOMachine.updateMachine(record); // Reset all the fields on machines.
                }
            }
            mMigrationBD15done = true;
            savePreferences();
        }



        if (savedInstanceState == null) {
            showFragment(FONTESPAGER); // Create fragment, do not add to backstack
            currentFragmentName = FONTESPAGER;
        }

        dataList = new ArrayList<>();
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerList = findViewById(R.id.left_drawer);

        DrawerItem drawerTitleItem = new DrawerItem("TITLE", R.drawable.ic_person, true);

        dataList.add(drawerTitleItem);
        dataList.add(new DrawerItem(this.getResources().getString(R.string.menu_Workout), R.drawable.ic_fitness_center, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.MachinesLabel), R.drawable.ic_exercises, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.ProgramListLabel), R.drawable.ic_exam, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.weightMenuLabel), R.drawable.ic_bathroom_scale, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.bodytracking), R.drawable.ic_ruler, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.progress_images), R.drawable.ic_drawer_photo_camera, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.SettingLabel), R.drawable.ic_settings, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.AboutLabel), R.drawable.ic_info_outline, true));

        mDrawerAdapter = new CustomDrawerAdapter(this, R.layout.custom_drawer_item,
                dataList);

        mDrawerList.setAdapter(mDrawerAdapter);

        roundProfile = top_toolbar.findViewById(R.id.imageProfile);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                top_toolbar,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open, R.string.drawer_close
        );

        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        musicController.initView();

        // Test if intro has been launched already
        if (!mIntro014Launched) {
            Intent intent = new Intent(this, MainIntroActivity.class);
            startActivityForResult(intent, REQUEST_CODE_INTRO);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();  // Always call the superclass method first

        if (mIntro014Launched) {
            initActivity();
            initDEBUGdata();
        }

        //if ( !mMigrationToScopedStoragedone) { //do the migration only once.
        //    migrateDatabase();
        //}

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean bShowMP3 = SP.getBoolean("prefShowMP3", false);
        this.showMP3Toolbar(bShowMP3);
        this.checkLastBackup(SP);
    }

    private void checkLastBackup(SharedPreferences SP) {
        int milliSecondsOfDay = 86400000; // 24 * 60 * 60 * 1000
        int milliSecondsOfWeek = 604800000; // 7 * 24 * 60 * 60 * 1000
        long milliSecondsOfMonth = 2419200000L; // 4 * 7 * 24 * 60 * 60 * 1000

        long lastBackupUTCTime = SP.getLong("prefLastTimeBackupUTCTime", -1);
        int prefBackupSetting = Integer.parseInt(SP.getString("defaultBackupSetting", "0"));
        if (lastBackupUTCTime == -1 && prefBackupSetting > 0) {
            //KToast.warningToast(getActivity(), getActivity().getResources().getText(R.string.backup_warning_never).toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
            exportDatabase(getActivity().getResources().getText(R.string.backup_warning_never).toString());
        } else {
            if (prefBackupSetting == 1 && System.currentTimeMillis() - lastBackupUTCTime > milliSecondsOfDay) {
                //KToast.warningToast(getActivity(), getActivity().getResources().getText(R.string.backup_warning_day).toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                exportDatabase(getActivity().getResources().getText(R.string.backup_warning_day).toString());
            } else if (prefBackupSetting == 2 && System.currentTimeMillis() - lastBackupUTCTime > milliSecondsOfWeek) {
                //KToast.warningToast(getActivity(), getActivity().getResources().getText(R.string.backup_warning_week).toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                exportDatabase(getActivity().getResources().getText(R.string.backup_warning_week).toString());
            } else if (prefBackupSetting == 3 && System.currentTimeMillis() - lastBackupUTCTime > milliSecondsOfMonth) {
                //KToast.warningToast(getActivity(), getActivity().getResources().getText(R.string.backup_warning_month).toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                exportDatabase(getActivity().getResources().getText(R.string.backup_warning_month).toString());
            }
        }
    }

    private void initDEBUGdata() {
        if (BuildConfig.DEBUG_MODE) {
            // do something for a debug build
            DAOFonte lDbFonte = new DAOFonte(this);
            if (lDbFonte.getCount() == 0) {
                lDbFonte.addStrengthRecordToFreeWorkout(DateConverter.dateToDate(2019, 7, 1, 12, 34, 56), "Example 1", 1, 10, 40, WeightUnit.KG, "", this.getCurrentProfile().getId());
                lDbFonte.addStrengthRecordToFreeWorkout(DateConverter.dateToDate(2019, 6, 30, 12, 34, 56), "Example 2", 1, 10, UnitConverter.LbstoKg(60), WeightUnit.LBS, "", this.getCurrentProfile().getId());
            }
            DAOCardio lDbCardio = new DAOCardio(this);
            if (lDbCardio.getCount() == 0) {
                lDbCardio.addCardioRecordToFreeWorkout(DateConverter.dateToDate(2019, 7, 1), "Running Example", 1, 10000, this.getCurrentProfile().getId(), DistanceUnit.KM);
                lDbCardio.addCardioRecordToFreeWorkout(DateConverter.dateToDate(2019, 7, 31), "Cardio Example", UnitConverter.MilesToKm(2), 20000, this.getCurrentProfile().getId(), DistanceUnit.MILES);
            }
            DAOStatic lDbStatic = new DAOStatic(this);
            if (lDbStatic.getCount() == 0) {
                lDbStatic.addStaticRecordToFreeWorkout(DateConverter.dateToDate(2019, 7, 1, 12, 34, 56), "Exercise ISO 1", 1, 50, 40, this.getCurrentProfile().getId(), WeightUnit.KG, "");
                lDbStatic.addStaticRecordToFreeWorkout(DateConverter.dateToDate(2019, 7, 31, 12, 34, 56), "Exercise ISO 2", 1, 60, UnitConverter.LbstoKg(40), this.getCurrentProfile().getId(), WeightUnit.LBS, "");
            }
            DAOProgram lDbWorkout = new DAOProgram(this);
            if (lDbWorkout.getCount() == 0) {
                lDbWorkout.populate();
            }
        }
    }

    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's instance
        if (getFontesPagerFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, FONTESPAGER, mpFontesPagerFrag);
        if (getWeightFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, WEIGHT, mpWeightFrag);
        if (getProfileFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, PROFILE, mpProfileFrag);
        if (getMachineFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, MACHINES, mpMachineFrag);
        if (getAboutFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, ABOUT, mpAboutFrag);
        if (getSettingsFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, SETTINGS, mpSettingFrag);
        if (getBodyPartFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, BODYTRACKING, mpBodyPartListFrag);
        if (getWorkoutListFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, WORKOUTS, mpWorkoutListFrag);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);

        // restore the profile picture in case it was overwritten during the menu inflate
        if (mCurrentProfile != null) setPhotoProfile(mCurrentProfile.getPhoto());

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        roundProfile.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(getActivity(), v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.profile_actions, popup.getMenu());
            popup.setOnMenuItemClickListener(onMenuItemClick);
            popup.show();
        });

        return super.onPrepareOptionsMenu(menu);
    }

    private boolean migrateToScopedStorage() {
        boolean success = true;
        File folder = new File(Environment.getExternalStorageDirectory() + "/FastnFitness");

        if (folder.exists()) { //migrate all pictures
            //Migrate profile pictures
            DAOProfile daoProfile = new DAOProfile(getBaseContext());
            List<Profile> profileList = daoProfile.getAllProfiles(daoProfile.getWritableDatabase());
            for (Profile profile:profileList) {
                if(profile.getPhoto()!=null && !profile.getPhoto().isEmpty()) {
                    File sourceFile = new File(profile.getPhoto());
                    File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    if (sourceFile.exists()) {
                        try {
                            File destFile = ImageUtil.copyFile(sourceFile, storageDir);
                            profile.setPhoto(destFile.getAbsolutePath());
                            ImageUtil.saveThumb(destFile.getAbsolutePath());
                            daoProfile.updateProfile(profile);
                        } catch (IOException e) {
                            Log.e("FastNFitness", "Error", e);
                            profile.setPhoto("");
                            daoProfile.updateProfile(profile);
                            success = false;
                        }
                    } else {
                        profile.setPhoto("");
                        daoProfile.updateProfile(profile);
                    }
                }
            }

            //Migrate exercises pictures
            DAOMachine daoMachine = new DAOMachine(getBaseContext());
            List<Machine> machineList = daoMachine.getAllMachinesArray();
            for (Machine machine:machineList) {
                if(machine.getPicture()!=null && !machine.getPicture().isEmpty()) {
                    File sourceFile = new File(machine.getPicture());
                    File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    if (sourceFile.exists()) {
                        machine.setPicture("");
                        try {
                            File destFile = ImageUtil.copyFile(sourceFile, storageDir);
                            ImageUtil.saveThumb(destFile.getAbsolutePath());
                            machine.setPicture(destFile.getAbsolutePath());
                            daoMachine.updateMachine(machine);
                        } catch (Exception e) {
                            Log.e("FastNFitness", "Error", e);
                            success = false;
                        }
                    }
                    daoMachine.updateMachine(machine);
                }
            }
        }
        return success;
    }

    private void migrateDatabase() {
        File folder = new File(Environment.getExternalStorageDirectory() + "/FastnFitness");
        if (!folder.exists()) {
            mMigrationToScopedStoragedone = true;
            savePreferences();
            return;
        }

        AlertDialog.Builder exportDbBuilder = new AlertDialog.Builder(this);
        exportDbBuilder.setTitle(getActivity().getResources().getText(R.string.database_migration));
        exportDbBuilder.setMessage(R.string.disclaimer_scoped_storage);
        exportDbBuilder.setPositiveButton(getActivity().getResources().getText(R.string.global_yes), (dialog, which) -> {
            if (!migrateToScopedStorage()) {
                // Display error box for information
                AlertDialog.Builder errorDialogBuilder = new AlertDialog.Builder(this);
                errorDialogBuilder.setTitle(R.string.database_migration);
                errorDialogBuilder.setMessage(R.string.something_went_wrong);
                AlertDialog errorDialog = errorDialogBuilder.create();
                errorDialog.show();
            } else {
                KToast.infoToast(this, getString(R.string.database_migration_success), Gravity.BOTTOM, KToast.LENGTH_SHORT);
            }
            mMigrationToScopedStoragedone = true;
            savePreferences();
        });

        AlertDialog exportDbDialog = exportDbBuilder.create();
        exportDbDialog.show();
    }

    private void openExportDatabaseDialog(String autoExportMessage) {
        AlertDialog.Builder exportDbBuilder = new AlertDialog.Builder(this);

        exportDbBuilder.setTitle(getActivity().getResources().getText(R.string.export_database));
        exportDbBuilder.setMessage(autoExportMessage + " " + getActivity().getResources().getText(R.string.export_question) + " " + getCurrentProfile().getName() + "?");
        exportDbBuilder.setPositiveButton(getActivity().getResources().getText(R.string.global_yes), (dialog, which) -> {
            CVSManager cvsMan = new CVSManager(getActivity().getBaseContext());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_H_m_s", Locale.getDefault());
            Date date = new Date();
            String folderName = Environment.DIRECTORY_DOWNLOADS + "/FastnFitness/export/" +  dateFormat.format(date);
            if (cvsMan.exportDatabase(getCurrentProfile(),folderName)) {
                SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                long currentTime = System.currentTimeMillis();
                SP.edit().putLong("prefLastTimeBackupUTCTime", currentTime).apply();
                if (mpSettingFrag.getContext() != null) {
                    mpSettingFrag.updateLastBackupSummary(SP, currentTime);
                }
                KToast.successToast(getActivity(), getCurrentProfile().getName() + ": " + getActivity().getResources().getText(R.string.export_success) + " - " + folderName, Gravity.BOTTOM, KToast.LENGTH_LONG);
            } else {
                KToast.errorToast(getActivity(), getCurrentProfile().getName() + ": " + getActivity().getResources().getText(R.string.export_failed), Gravity.BOTTOM, KToast.LENGTH_LONG);
            }
            dialog.dismiss();
        });

        exportDbBuilder.setNegativeButton(getActivity().getResources().getText(R.string.global_no), (dialog, which) -> dialog.dismiss());

        AlertDialog exportDbDialog = exportDbBuilder.create();
        exportDbDialog.show();
    }

    private void exportDatabase(String autoExportMessage) {
        openExportDatabaseDialog(autoExportMessage);
    }

    private void importDatabase() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimetypes = {"text/*", "application/zip"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        startActivityForResult(intent, IMPORT_DATABASE);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.export_database:
                exportDatabase("");
                return true;
            case R.id.import_database:
                importDatabase();
                return true;
            case R.id.action_deleteDB:
                // Afficher une boite de dialogue pour confirmer
                AlertDialog.Builder deleteDbBuilder = new AlertDialog.Builder(this);

                deleteDbBuilder.setTitle(getActivity().getResources().getText(R.string.global_confirm));
                deleteDbBuilder.setMessage(getActivity().getResources().getText(R.string.deleteDB_warning));

                // Si oui, supprimer la base de donnee et refaire un Start.
                deleteDbBuilder.setPositiveButton(getActivity().getResources().getText(R.string.global_yes), (dialog, which) -> {
                    List<Profile> lList = mDbProfils.getAllProfiles(mDbProfils.getReadableDatabase());
                    for (int i = 0; i < lList.size(); i++) {
                        Profile mTempProfile = lList.get(i);
                        mDbProfils.deleteProfile(mTempProfile.getId());
                    }

                    DAOMachine mDbMachines = new DAOMachine(getActivity());
                    List<Machine> lList2 = mDbMachines.getAllMachinesArray();
                    for (int i = 0; i < lList2.size(); i++) {
                        Machine mTemp = lList2.get(i);
                        mDbMachines.delete(mTemp.getId());
                    }

                    DAOProgram mDbPrograms = new DAOProgram(getActivity());
                    List<Program> programList = mDbPrograms.getAll();
                    for (int i = 0; i < programList.size(); i++) {
                        Program mTemp = programList.get(i);
                        mDbPrograms.delete(mTemp.getId());

                        DAORecord mDbRecords = new DAORecord(getActivity());
                        List<Record> recordList = mDbRecords.getAllTemplateRecordByProgramArray(mTemp.getId());
                        for (int j = 0; j < recordList.size(); j++) {
                            Record mTempRecord = recordList.get(j);
                            mDbRecords.deleteRecord(mTempRecord.getId());
                        }
                    }

                    mIntro014Launched = false; // redisplay the intro

                    dialog.dismiss(); // Close the dialog
                    finish(); // Close app
                });

                deleteDbBuilder.setNegativeButton(getActivity().getResources().getText(R.string.global_no), (dialog, which) -> {
                    // Do nothing
                    dialog.dismiss();
                });

                AlertDialog deleteDbDialog = deleteDbBuilder.create();
                deleteDbDialog.show();

                return true;
            case R.id.action_apropos:
                // Display the fragment as the main content.
                showFragment(ABOUT);
                //getAboutFragment().setHasOptionsMenu(true);
                return true;
            //case android.R.id.home:
            //onBackPressed();
            //	return true;
            case R.id.action_chrono:
                ChronoDialogbox cdd = new ChronoDialogbox(MainActivity.this);
                cdd.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // If request is cancelled, the result arrays are empty.
        if (requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_FOR_EXPORT) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                KToast.infoToast(this, getString(R.string.access_granted), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                openExportDatabaseDialog("");
            } else {
                KToast.infoToast(this, getString(R.string.another_time_maybe), Gravity.BOTTOM, KToast.LENGTH_SHORT);
            }
        } else if (requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_FOR_IMPORT) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                KToast.infoToast(this, getString(R.string.access_granted), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                importDatabase();
            } else {
                KToast.infoToast(this, getString(R.string.another_time_maybe), Gravity.BOTTOM, KToast.LENGTH_SHORT);
            }
        } else if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                KToast.infoToast(this, getString(R.string.access_granted), Gravity.BOTTOM, KToast.LENGTH_SHORT);
                musicController.chooseDirectory();
            } else {
                KToast.infoToast(this, getString(R.string.another_time_maybe), Gravity.BOTTOM, KToast.LENGTH_SHORT);
            }
        }
    }

        public boolean CreateNewProfile() {
        AlertDialog.Builder newProfilBuilder = new AlertDialog.Builder(this);

        newProfilBuilder.setTitle(getActivity().getResources().getText(R.string.createProfilTitle));
        newProfilBuilder.setMessage(getActivity().getResources().getText(R.string.createProfilQuestion));

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        newProfilBuilder.setView(input);

        newProfilBuilder.setPositiveButton(getActivity().getResources().getText(android.R.string.ok), (dialog, whichButton) -> {
            String value = input.getText().toString();

            if (value.isEmpty()) {
                CreateNewProfile();
            } else {
                // Create the new profil
                mDbProfils.addProfile(value);
                // Make it the current.
                setCurrentProfile(value);
            }
        });

        newProfilBuilder.setNegativeButton(getActivity().getResources().getText(android.R.string.cancel), (dialog, whichButton) -> {
            if (getCurrentProfile() == null) {
                CreateNewProfile();
            }
        });

        newProfilBuilder.show();

        return true;
    }

    public void renameProfile() {
        AlertDialog.Builder newBuilder = new AlertDialog.Builder(this);

        newBuilder.setTitle(getActivity().getResources().getText(R.string.renameProfilTitle));
        newBuilder.setMessage(getActivity().getResources().getText(R.string.renameProfilQuestion));

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setText(getCurrentProfile().getName());
        newBuilder.setView(input);

        newBuilder.setPositiveButton(getActivity().getResources().getText(android.R.string.ok), (dialog, whichButton) -> {
            String value = input.getText().toString();

            if (!value.isEmpty()) {
                // Get current profile
                Profile temp = getCurrentProfile();
                // Rename it
                temp.setName(value);
                // Commit it
                mDbProfils.updateProfile(temp);
                // Make it the current.
                setCurrentProfile(value);
            }
        });

        newBuilder.setNegativeButton(getActivity().getResources().getText(android.R.string.cancel), (dialog, whichButton) -> {
        });

        newBuilder.show();

    }

    private void setDrawerTitle(String pProfilName) {
        mDrawerAdapter.getItem(0).setTitle(pProfilName);
        mDrawerAdapter.notifyDataSetChanged();
        mDrawerLayout.invalidate();
    }

    /**
     * Swaps fragments in the main content view
     */
    private void selectItem(int position) {
        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        //setTitle(mPlanetTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void showFragment(String pFragmentName) {

        if (currentFragmentName.equals(pFragmentName))
            return; // If this is already the current fragment, do no replace.

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        // Then show the fragments
        if (pFragmentName.equals(FONTESPAGER)) {
            ft.replace(R.id.fragment_container, getFontesPagerFragment(), FONTESPAGER);
        } else if (pFragmentName.equals(WEIGHT)) {
            ft.replace(R.id.fragment_container, getWeightFragment(), WEIGHT);
        } else if (pFragmentName.equals(SETTINGS)) {
            ft.replace(R.id.fragment_container, getSettingsFragment(), SETTINGS);
        } else if (pFragmentName.equals(MACHINES)) {
            ft.replace(R.id.fragment_container, getMachineFragment(), MACHINES);
        } else if (pFragmentName.equals(WORKOUTS)) {
            ft.replace(R.id.fragment_container, getWorkoutListFragment(), WORKOUTS);
        } else if (pFragmentName.equals(ABOUT)) {
            ft.replace(R.id.fragment_container, getAboutFragment(), ABOUT);
        } else if (pFragmentName.equals(BODYTRACKING)) {
            ft.replace(R.id.fragment_container, getBodyPartFragment(), BODYTRACKING);
        } else if (pFragmentName.equals(PROFILE)) {
            ft.replace(R.id.fragment_container, getProfileFragment(), PROFILE);
        } else if (pFragmentName.equals(PROGRESSIMAGES)) {
            ft.replace(R.id.fragment_container, getProgressImagesFragment(), PROGRESSIMAGES);
        }
        currentFragmentName = pFragmentName;
        ft.commit();

    }

    @Override
    protected void onStop() {
        super.onStop();
        savePreferences();
    }

    public Profile getCurrentProfile() {
        return appViMo.getProfile().getValue();
    }

    public void setCurrentProfile(String newProfileName) {
        Profile newProfile = this.mDbProfils.getProfile(newProfileName);
        setCurrentProfile(newProfile);
    }

    public void setCurrentProfile(Profile newProfil) {
        appViMo.setProfile(newProfil);
    }

    private void setPhotoProfile(String path) {
        // Check if path is pointing to a thumb else create it and use it.
        String thumbPath = ImageUtil.getThumbPath(path);
        boolean success = false;
        if (thumbPath != null) {
            success = ImageUtil.setPic(roundProfile, thumbPath);
            mDrawerAdapter.getItem(0).setImg(thumbPath);
        }

        if (!success) {
            roundProfile.setImageDrawable(AppCompatResources.getDrawable(getActivity(), R.drawable.ic_person));
            mDrawerAdapter.getItem(0).setImg(null); // Img has priority over Resource so it needs to be reset
            mDrawerAdapter.getItem(0).setImgResID(R.drawable.ic_person);
        }

        mDrawerAdapter.notifyDataSetChanged();
        mDrawerLayout.invalidate();
    }

    public String getCurrentMachine() {
        return mCurrentMachine;
    }

    public void setCurrentMachine(String newMachine) {
        mCurrentMachine = newMachine;
    }

    public MainActivity getActivity() {
        return this;
    }

    private void loadPreferences() {
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        mCurrentProfilID = settings.getLong("currentProfil", -1); // return -1 if it doesn't exist
        mIntro014Launched = settings.getBoolean("intro014Launched", false);
        mMigrationBD15done = settings.getBoolean("migrationBD15done", false);
        mMigrationToScopedStoragedone = settings.getBoolean("migrationToScopedStoragedone", false);
    }

    private void savePreferences() {
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("currentProfil", mCurrentProfilID);
        editor.putBoolean("intro014Launched", mIntro014Launched);
        editor.putBoolean("migrationBD15done", mMigrationBD15done);
        editor.putBoolean("migrationToScopedStoragedone", mMigrationToScopedStoragedone);
        editor.apply();
    }

    private FontesPagerFragment getFontesPagerFragment() {
        if (mpFontesPagerFrag == null)
            mpFontesPagerFrag = (FontesPagerFragment) getSupportFragmentManager().findFragmentByTag(FONTESPAGER);
        if (mpFontesPagerFrag == null)
            mpFontesPagerFrag = FontesPagerFragment.newInstance(FONTESPAGER, 6);

        return mpFontesPagerFrag;
    }

    private WeightFragment getWeightFragment() {
        if (mpWeightFrag == null)
            mpWeightFrag = (WeightFragment) getSupportFragmentManager().findFragmentByTag(WEIGHT);
        if (mpWeightFrag == null) mpWeightFrag = WeightFragment.newInstance(WEIGHT, 5);

        return mpWeightFrag;
    }


    private ProgressImagesFragment getProgressImagesFragment() {
        if (mpProgressImageFrag == null)
            mpProgressImageFrag = (ProgressImagesFragment) getSupportFragmentManager().findFragmentByTag(PROGRESSIMAGES);
        if (mpProgressImageFrag == null) mpProgressImageFrag = ProgressImagesFragment.newInstance(PROGRESSIMAGES, 12);

        return mpProgressImageFrag;
    }

    private ProfileFragment getProfileFragment() {
        if (mpProfileFrag == null)
            mpProfileFrag = (ProfileFragment) getSupportFragmentManager().findFragmentByTag(PROFILE);
        if (mpProfileFrag == null) mpProfileFrag = ProfileFragment.newInstance(PROFILE, 10);

        return mpProfileFrag;
    }

    private MachineFragment getMachineFragment() {
        if (mpMachineFrag == null)
            mpMachineFrag = (MachineFragment) getSupportFragmentManager().findFragmentByTag(MACHINES);
        if (mpMachineFrag == null) mpMachineFrag = MachineFragment.newInstance(MACHINES, 7);
        return mpMachineFrag;
    }

    private AboutFragment getAboutFragment() {
        if (mpAboutFrag == null)
            mpAboutFrag = (AboutFragment) getSupportFragmentManager().findFragmentByTag(ABOUT);
        if (mpAboutFrag == null) mpAboutFrag = AboutFragment.newInstance(ABOUT, 6);

        return mpAboutFrag;
    }

    private BodyPartListFragment getBodyPartFragment() {
        if (mpBodyPartListFrag == null)
            mpBodyPartListFrag = (BodyPartListFragment) getSupportFragmentManager().findFragmentByTag(BODYTRACKING);
        if (mpBodyPartListFrag == null)
            mpBodyPartListFrag = BodyPartListFragment.newInstance(BODYTRACKING, 9);

        return mpBodyPartListFrag;
    }


    private ProgramListFragment getWorkoutListFragment() {
        if (mpWorkoutListFrag == null)
            mpWorkoutListFrag = (ProgramListFragment) getSupportFragmentManager().findFragmentByTag(WORKOUTS);
        if (mpWorkoutListFrag == null)
            mpWorkoutListFrag = ProgramListFragment.newInstance(WORKOUTS, 10);

        return mpWorkoutListFrag;
    }

    private SettingsFragment getSettingsFragment() {
        if (mpSettingFrag == null)
            mpSettingFrag = (SettingsFragment) getSupportFragmentManager().findFragmentByTag(SETTINGS);
        if (mpSettingFrag == null) mpSettingFrag = SettingsFragment.newInstance(SETTINGS, 8);

        return mpSettingFrag;
    }

    public Toolbar getActivityToolbar() {
        return top_toolbar;
    }

    public void showMP3Toolbar(boolean show) {
        Toolbar mp3toolbar = this.findViewById(R.id.musicToolbar);
        if (!show) {
            mp3toolbar.setVisibility(View.GONE);
        } else {
            mp3toolbar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_INTRO) {
            if (resultCode == RESULT_OK) {
                initActivity();
                mIntro014Launched = true;
                initDEBUGdata();
                this.savePreferences();
            } else {
                // Cancelled the intro. You can then e.g. finish this activity too.
                finish();
            }
        } else if (resultCode == RESULT_OK && requestCode == IMPORT_DATABASE) {
            Uri file;
            if (data != null) {
                file = data.getData();
                CVSManager cvsMan = new CVSManager(getActivity().getBaseContext());

                final String extension = getContentResolver().getType(file);
                if (FileNameUtil.MINE_TYPE_CSV.equalsIgnoreCase(extension)) {
                    InputStream inputStream;
                    try {
                        inputStream = getContentResolver().openInputStream(file);
                    } catch (FileNotFoundException e) {
                        Log.e("FastNFitness", "Error", e);
                        inputStream = null;
                    }

                    if (cvsMan.importDatabase(inputStream, appViMo.getProfile().getValue())) {
                        KToast.successToast(getActivity(), getCurrentProfile().getName() + ": " + getActivity().getResources().getText(R.string.imported_successfully), Gravity.BOTTOM, KToast.LENGTH_LONG);
                    } else {
                        KToast.errorToast(getActivity(), getCurrentProfile().getName() + ": " + getActivity().getResources().getText(R.string.import_failed), Gravity.BOTTOM, KToast.LENGTH_LONG);
                    }
                } else if (FileNameUtil.MINE_TYPE_ZIP.equalsIgnoreCase(extension)) {
                    File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                    File zipFile = ImageUtil.copyFileFromUri(getActivity().getBaseContext(), file, storageDir, "zipImportTemp");
                    ZipFile importZipFile;
                    try {
                        importZipFile = new ZipFile(zipFile);
                    } catch (IOException e) {
                        Log.e(getClass().getName(), "Can't read zip file", e);
                        KToast.errorToast(getActivity(), getCurrentProfile().getName() + ": " + getActivity().getResources().getText(R.string.import_failed), Gravity.BOTTOM, KToast.LENGTH_LONG);
                        return;
                    }
                    if (cvsMan.importDatabase(importZipFile, appViMo.getProfile().getValue())) {
                        KToast.successToast(getActivity(), getCurrentProfile().getName() + ": " + getActivity().getResources().getText(R.string.imported_successfully), Gravity.BOTTOM, KToast.LENGTH_LONG);
                    } else {
                        KToast.errorToast(getActivity(), getCurrentProfile().getName() + ": " + getActivity().getResources().getText(R.string.import_failed), Gravity.BOTTOM, KToast.LENGTH_LONG);
                    }
                } else {
                    KToast.errorToast(getActivity(), getCurrentProfile().getName() + ": " + getActivity().getResources().getText(R.string.import_unknow_file_extensions), Gravity.BOTTOM, KToast.LENGTH_LONG);
                }
            }
        } else if (resultCode == RESULT_OK && requestCode == OPEN_MUSIC_FILE) {
            Uri uri;
            if (data != null) {
                uri = data.getData();
                // Return for MusicController Choose file
                musicController.OpenMusicFileIntentResult(uri);
                int takeFlags = data.getFlags();
                takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                // Check for the freshest data.
                getContentResolver().takePersistableUriPermission(uri, takeFlags);
            }
        }
    }

    @Override
    public void onBackPressed() {
        int index = getActivity().getSupportFragmentManager().getBackStackEntryCount() - 1;
        if (index >= 0) { // Si on est dans une sous activité
            super.onBackPressed();
            getActivity().getSupportActionBar().show();
        } else { // Si on est la racine, avec il faut cliquer deux fois
            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
                super.onBackPressed();
                return;
            } else {
                Toast.makeText(getBaseContext(), R.string.pressBackAgain, Toast.LENGTH_SHORT).show();
            }

            mBackPressed = System.currentTimeMillis();
        }
    }

    public void initActivity() {
        // Initialisation des objets DB
        mDbProfils = new DAOProfile(this.getApplicationContext());

        // Pour la base de donnee profil, il faut toujours qu'il y ai au moins un profil
        mCurrentProfile = mDbProfils.getProfile(mCurrentProfilID);
        if (mCurrentProfile == null) { // au cas ou il y aurait un probleme de synchro
            try {
                List<Profile> lList = mDbProfils.getAllProfiles(mDbProfils.getReadableDatabase());
                mCurrentProfile = lList.get(0);
            } catch (IndexOutOfBoundsException e) {
                this.CreateNewProfile();
            }
        }

        if (mCurrentProfile != null) setCurrentProfile(mCurrentProfile.getName());

    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {

            selectItem(position);

            // Insert the fragment by replacing any existing fragment
            switch (position) {
                case 0:
                    showFragment(PROFILE);
                    setTitle(getString(R.string.ProfileLabel));
                    break;
                case 1:
                    showFragment(FONTESPAGER);
                    setTitle(getResources().getText(R.string.menu_Workout));
                    break;
                case 2:
                    showFragment(MACHINES);
                    setTitle(getResources().getText(R.string.MachinesLabel));
                    break;
                case 3:
                    showFragment(WORKOUTS);
                    setTitle(getString(R.string.workout_list_menu_item));
                    break;
                case 4:
                    showFragment(WEIGHT);
                    setTitle(getResources().getText(R.string.weightMenuLabel));
                    break;
                case 5:
                    showFragment(BODYTRACKING);
                    setTitle(getResources().getText(R.string.bodytracking));
                    break;
                case 6:
                    showFragment(PROGRESSIMAGES);
                    setTitle(getResources().getText(R.string.progress_images));
                    break;
                case 7:
                    showFragment(SETTINGS);
                    setTitle(getResources().getText(R.string.SettingLabel));
                    break;
                case 8:
                    showFragment(ABOUT);
                    setTitle(getResources().getText(R.string.AboutLabel));
                    break;
                default:
                    showFragment(FONTESPAGER);
                    setTitle(getResources().getText(R.string.FonteLabel));
            }
        }
    }
}
