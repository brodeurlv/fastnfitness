package com.easyfitness;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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

import com.easyfitness.DAO.CVSManager;
import com.easyfitness.DAO.DAOFonte;
import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.DAO.DAOProfil;
import com.easyfitness.DAO.DatabaseHelper;
import com.easyfitness.DAO.Machine;
import com.easyfitness.DAO.Profile;
import com.easyfitness.bodymeasures.BodyPartListFragment;
import com.easyfitness.fonte.FontesPagerFragment;
import com.easyfitness.intro.MainIntroActivity;
import com.easyfitness.machines.MachineFragment;
import com.easyfitness.utils.CustomExceptionHandler;
import com.easyfitness.utils.FileChooserDialog;
import com.easyfitness.utils.ImageUtil;
import com.easyfitness.utils.MusicController;
import com.easyfitness.utils.RealPathUtil;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.onurkaganaldemir.ktoastlib.KToast;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.easyfitness.utils.ImageUtil.REQUEST_PICK_GALERY_PHOTO;

//import com.crashlytics.android.Crashlytics;

//import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity {

    public static String FONTESPAGER = "FontePager";
    public static String FONTES = "Fonte";
    public static String HISTORY = "History";
    public static String GRAPHIC = "Graphics";
    public static String CARDIO = "Cardio";
    public static String WEIGHT = "Weight";
    public static String PROFILE = "Profile";
    public static String BODYTRACKING = "BodyTracking";
    public static String BODYTRACKINGDETAILS = "BodyTrackingDetail";
    public static String ABOUT = "About";
    public static String SETTINGS = "Settings";
    public static String MACHINES = "Machines";
    public static String MACHINESDETAILS = "MachinesDetails";

    public static String PREFS_NAME = "prefsfile";
    CustomDrawerAdapter DrawerAdapter;
    List<DrawerItem> dataList;
    //private FontesFragment mpFontesFrag = FontesFragment.newInstance(FONTES, 1);
    private FontesPagerFragment mpFontesPagerFrag = null;
    private CardioFragment mpCardioFrag = null;
    //private HistoryFragment mpHistoryFrag = null;
    //private GraphFragment mpGraphFrag = null;
    private WeightFragment mpWeightFrag = null;
    private ProfileFragment mpProfileFrag = null;
    private MachineFragment mpMachineFrag = null;
    private SettingsFragment mpSettingFrag = null;
    private AboutFragment mpAboutFrag = null;
    private BodyPartListFragment mpBodyPartListFrag = null;
    private String currentFragmentName = "";
    private DAOProfil mDbProfils = null;
    private Profile mCurrentProfile = null;
    private long mCurrentProfilID = -1;
    private String m_importCVSchosenDir = "";
    private Toolbar top_toolbar = null;
    /* Navigation Drawer */
    private DrawerLayout mDrawerLayout = null;
    private ListView mDrawerList = null;
    private ActionBarDrawerToggle mDrawerToggle = null;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private MusicController musicController = new MusicController(this);
    private CircularImageView roundProfile = null;

    private String mCurrentMachine = "";

    private boolean mIntro014Launched = false;
    private boolean mMigrationBD05done = false;

    private final int REQUEST_CODE_INTRO = 111;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Fabric.with(this, new Crashlytics());

        //getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_main);

        top_toolbar = (Toolbar) this.findViewById(R.id.actionToolbar);
        setSupportActionBar(top_toolbar);
        top_toolbar.setTitle(getResources().getText(R.string.app_name));

        if (savedInstanceState == null) {
            //private FontesFragment mpFontesFrag = FontesFragment.newInstance(FONTES, 1);
            if (mpFontesPagerFrag == null)
                mpFontesPagerFrag = FontesPagerFragment.newInstance(FONTESPAGER, 6);
            if (mpCardioFrag == null) mpCardioFrag = CardioFragment.newInstance(CARDIO, 4);
            //private HistoryFragment mpHistoryFrag = HistoryFragment.newInstance(HISTORY, 3);
            //private GraphFragment mpGraphFrag = GraphFragment.newInstance(GRAPHIC, 4);
            if (mpWeightFrag == null) mpWeightFrag = WeightFragment.newInstance(WEIGHT, 5);
            if (mpProfileFrag == null) mpProfileFrag = ProfileFragment.newInstance(PROFILE, 10);
            if (mpSettingFrag == null) mpSettingFrag = SettingsFragment.newInstance(SETTINGS, 8);
            //private SettingsFragment    mpSettingFrag = new SettingsFragment();
            if (mpAboutFrag == null) mpAboutFrag = AboutFragment.newInstance(ABOUT, 6);
            if (mpMachineFrag == null) mpMachineFrag = MachineFragment.newInstance(MACHINES, 7);
            if (mpBodyPartListFrag == null)
                mpBodyPartListFrag = BodyPartListFragment.newInstance(BODYTRACKING, 9);
        } else {
            mpFontesPagerFrag = (FontesPagerFragment) getSupportFragmentManager().getFragment(savedInstanceState, FONTESPAGER);
            mpCardioFrag = (CardioFragment) getSupportFragmentManager().getFragment(savedInstanceState, CARDIO);
            mpWeightFrag = (WeightFragment) getSupportFragmentManager().getFragment(savedInstanceState, WEIGHT);
            mpProfileFrag = (ProfileFragment) getSupportFragmentManager().getFragment(savedInstanceState, PROFILE);
            mpSettingFrag = (SettingsFragment) getSupportFragmentManager().getFragment(savedInstanceState, SETTINGS);
            mpAboutFrag = (AboutFragment) getSupportFragmentManager().getFragment(savedInstanceState, ABOUT);
            mpMachineFrag = (MachineFragment) getSupportFragmentManager().getFragment(savedInstanceState, MACHINES);
            mpBodyPartListFrag = (BodyPartListFragment) getSupportFragmentManager().getFragment(savedInstanceState, BODYTRACKING);
        }
        /*else {
			mpFontesPagerFrag = (FontesPagerFragment) getSupportFragmentManager().findFragmentByTag(FONTESPAGER);
			mpCardioFrag = (CardioFragment) getSupportFragmentManager().findFragmentByTag(CARDIO);
			mpProfilFrag = (ProfilFragment) getSupportFragmentManager().findFragmentByTag(WEIGHT);
			mpAboutFrag = (AboutFragment) getSupportFragmentManager().findFragmentByTag(ABOUT);
			mpMachineFrag = (MachineFragment) getSupportFragmentManager().findFragmentByTag(MACHINES);
		}*/

        loadPreferences();

        DatabaseHelper.renameOldDatabase(this);
		
		/* creation de l'arborescence de l'application */
        File folder = new File(Environment.getExternalStorageDirectory() + "/FastnFitness");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdir();
        }
        if (success) {
            folder = new File(Environment.getExternalStorageDirectory() + "/FastnFitness/crashreport");
            success = folder.mkdir();
        }

        if (folder.exists()) {
            if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
                Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(
                        Environment.getExternalStorageDirectory() + "/FastnFitness/crashreport"));
            }
        }
		
		/*
		mDbMachines = new DAOMachine(this);
		if (mDbMachines.getCount()==0) {
			// recupere le premier ID de la liste.
			mDbMachines.populate();
		}*/

        if (savedInstanceState == null) {
            showFragment(FONTESPAGER, false); // Create fragment, do not add to backstack
            currentFragmentName = FONTESPAGER;
        }

        dataList = new ArrayList<DrawerItem>();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        DrawerItem drawerTitleItem = new DrawerItem("TITLE", R.drawable.ic_profile_black, true);

        dataList.add(drawerTitleItem);
        dataList.add(new DrawerItem(this.getResources().getString(R.string.FonteLabel), R.drawable.ic_barbell, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.CardioLabel), R.drawable.ic_running, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.MachinesLabel), R.drawable.ic_machine, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.WeightLabel), R.drawable.ic_scale, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.bodytracking), R.drawable.ic_measuring_tape, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.SettingLabel), R.drawable.ic_params, true));
        dataList.add(new DrawerItem(this.getResources().getString(R.string.AboutLabel), R.drawable.ic_action_info_outline, true));

        DrawerAdapter = new CustomDrawerAdapter(this, R.layout.custom_drawer_item,
                dataList);

        mDrawerList.setAdapter(DrawerAdapter);

        roundProfile = (CircularImageView) top_toolbar.findViewById(R.id.imageProfile);

        //String[] mTempString = new String[]{"toto","tata","titi"};
        // Set the adapter for the list view
        //mDrawerList.setAdapter(new ArrayAdapter<String>(this,
        //        R.layout.drawer_list_item, getDrawerList()));        

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

        // Lance l'intro
        // Tester si l'intro a déjà été lancé
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
        }

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean bShowMP3 = SP.getBoolean("prefShowMP3", false);
        this.showMP3Toolbar(bShowMP3);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        // Restore state members from saved instance
        // Example mCurrentScore = savedInstanceState.getInt(STATE_SCORE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's instance
        if (getFontesPagerFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, FONTESPAGER, mpFontesPagerFrag);
        if (getCardioFragment().isAdded())
            getSupportFragmentManager().putFragment(outState, CARDIO, mpCardioFrag);
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
        final MenuItem alertMenuItem = menu.findItem(R.id.action_profil);

        roundProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(getActivity(), v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.profile_actions, popup.getMenu());
                popup.setOnMenuItemClickListener(onMenuItemClick);
                popup.show();
            }
        });

        return super.onPrepareOptionsMenu(menu);
    }

    private PopupMenu.OnMenuItemClickListener onMenuItemClick = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.create_newprofil:
                    getActivity().CreateNewProfil();
                    return true;
                case R.id.photo_profil:
                    String[] optionListArray = new String[2];
                    optionListArray[0] = getActivity().getResources().getString(R.string.camera);
                    optionListArray[1] = getActivity().getResources().getString(R.string.gallery);
                    //profilListArray[2] = "Remove Image";

                    //requestPermissionForWriting(pF);

                    AlertDialog.Builder itemActionbuilder = new AlertDialog.Builder(getActivity());
                    itemActionbuilder.setTitle("").setItems(optionListArray, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ListView lv = ((AlertDialog) dialog).getListView();

                            switch (which) {
                                // Galery
                                case 1:
                                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                                    photoPickerIntent.setType("image/*");
                                    startActivityForResult(photoPickerIntent, REQUEST_PICK_GALERY_PHOTO);
                                    break;
                                // Camera with Cropping
                                case 0:
                                    //dispatchTakePictureIntent(mF);
                                    // start picker to get image for cropping and then use the image in cropping activity
                                    CropImage.activity()
                                            .setGuidelines(CropImageView.Guidelines.ON)
                                            .start(getActivity());
                                    break;
                                case 2: // Delete picture

                                    break;
                                // Camera
                                default:
                            }
                        }
                    });
                    itemActionbuilder.show();
                    return true;
                case R.id.change_profil:
                    String[] profilListArray = getActivity().mDbProfils.getAllProfil();

                    AlertDialog.Builder changeProfilbuilder = new AlertDialog.Builder(getActivity());
                    changeProfilbuilder.setTitle(getActivity().getResources().getText(R.string.profil_select_profil))
                            .setItems(profilListArray, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    ListView lv = ((AlertDialog) dialog).getListView();
                                    Object checkedItem = lv.getAdapter().getItem(which);
                                    setCurrentProfil(checkedItem.toString());
                                    KToast.infoToast(getActivity(), getActivity().getResources().getText(R.string.profileSelected) + " : " + checkedItem.toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                                    //Toast.makeText(getApplicationContext(), getActivity().getResources().getText(R.string.profileSelected) + " : " + checkedItem.toString(), Toast.LENGTH_LONG).show();
                                }
                            });
                    changeProfilbuilder.show();
                    return true;
                case R.id.delete_profil:
                    String[] profildeleteListArray = getActivity().mDbProfils.getAllProfil();

                    AlertDialog.Builder deleteProfilbuilder = new AlertDialog.Builder(getActivity());
                    deleteProfilbuilder.setTitle(getActivity().getResources().getText(R.string.profil_select_profil_to_delete))
                            .setItems(profildeleteListArray, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    ListView lv = ((AlertDialog) dialog).getListView();
                                    Object checkedItem = lv.getAdapter().getItem(which);
                                    if (getCurrentProfil().getName().equals(checkedItem.toString())) {
                                        KToast.errorToast(getActivity(), getActivity().getResources().getText(R.string.impossibleToDeleteProfile).toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                                    } else {
                                        Profile profileToDelete = mDbProfils.getProfil(checkedItem.toString());
                                        mDbProfils.deleteProfil(profileToDelete);
                                        KToast.infoToast(getActivity(), getString(R.string.profileDeleted) + ":" + checkedItem.toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                                    }
                                }
                            });
                    deleteProfilbuilder.show();
                    return true;
                case R.id.rename_profil:
                    getActivity().renameProfil();
                    return true;
                default:
                    return false;
            }
        }

    };


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.export_database:
                // Afficher une boite de dialogue pour confirmer
                AlertDialog.Builder exportDbBuilder = new AlertDialog.Builder(this);

                exportDbBuilder.setTitle(getActivity().getResources().getText(R.string.export_database));
                exportDbBuilder.setMessage(getActivity().getResources().getText(R.string.export_question));

                // Si oui, supprimer la base de donnee et refaire un Start.
                exportDbBuilder.setPositiveButton(getActivity().getResources().getText(R.string.global_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        CVSManager cvsMan = new CVSManager(getActivity().getBaseContext());
                        if (cvsMan.exportDatabase(getCurrentProfil())) {
                            KToast.successToast(getActivity(), getCurrentProfil().getName() + ": " + getActivity().getResources().getText(R.string.export_success), Gravity.BOTTOM, KToast.LENGTH_LONG);
                        } else {
                            KToast.errorToast(getActivity(), getCurrentProfil().getName() + ": " + getActivity().getResources().getText(R.string.export_failed), Gravity.BOTTOM, KToast.LENGTH_LONG);
                        }

                        // Do nothing but close the dialog
                        dialog.dismiss();
                    }
                });

                exportDbBuilder.setNegativeButton(getActivity().getResources().getText(R.string.global_no), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        dialog.dismiss();
                    }
                });

                AlertDialog exportDbDialog = exportDbBuilder.create();
                exportDbDialog.show();

                return true;
            case R.id.import_database:
                // Create DirectoryChooserDialog and register a callback
                FileChooserDialog fileChooserDialog =
                        new FileChooserDialog(this, new FileChooserDialog.ChosenFileListener() {
                            @Override
                            public void onChosenFile(String chosenDir) {
                                m_importCVSchosenDir = chosenDir;
                                //Toast.makeText(getActivity().getBaseContext(), "Chosen directory: " +
                                //  chosenDir, Toast.LENGTH_LONG).show();
                                CVSManager cvsMan = new CVSManager(getActivity().getBaseContext());
                                cvsMan.importDatabase(m_importCVSchosenDir, getCurrentProfil());
                            }
                        });

                fileChooserDialog.setFileFilter("csv");
                fileChooserDialog.chooseDirectory(Environment.getExternalStorageDirectory() + "/FastnFitness/export");
                return true;
            case R.id.action_deleteDB:
                // Afficher une boite de dialogue pour confirmer
                AlertDialog.Builder deleteDbBuilder = new AlertDialog.Builder(this);

                deleteDbBuilder.setTitle(getActivity().getResources().getText(R.string.global_confirm));
                deleteDbBuilder.setMessage(getActivity().getResources().getText(R.string.deleteDB_warning));

                // Si oui, supprimer la base de donnee et refaire un Start.
                deleteDbBuilder.setPositiveButton(getActivity().getResources().getText(R.string.global_yes), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // recupere le premier ID de la liste.
                        List<Profile> lList = mDbProfils.getAllProfils();
                        for (int i = 0; i < lList.size(); i++) {
                            Profile mTempProfile = lList.get(i);
                            mDbProfils.deleteProfil(mTempProfile.getId());
                        }
                        DAOMachine mDbMachines = new DAOMachine(getActivity());
                        // recupere le premier ID de la liste.
                        List<Machine> lList2 = mDbMachines.getAllMachinesArray();
                        for (int i = 0; i < lList2.size(); i++) {
                            Machine mTemp = lList2.get(i);
                            mDbMachines.deleteRecord(mTemp.getId());
                        }
                        // Do nothing but close the dialog
                        dialog.dismiss();

                        finish();
                    }
                });

                deleteDbBuilder.setNegativeButton(getActivity().getResources().getText(R.string.global_no), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        dialog.dismiss();
                    }
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

    public boolean CreateNewProfil() {
        AlertDialog.Builder newProfilBuilder = new AlertDialog.Builder(this);

        newProfilBuilder.setTitle(getActivity().getResources().getText(R.string.createProfilTitle));
        newProfilBuilder.setMessage(getActivity().getResources().getText(R.string.createProfilQuestion));

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        newProfilBuilder.setView(input);

        newProfilBuilder.setPositiveButton(getActivity().getResources().getText(R.string.global_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();

                if (value.isEmpty()) {
                    CreateNewProfil();
                } else {
                    // Create the new profil
                    mDbProfils.addProfil(value);
                    // Make it the current.
                    setCurrentProfil(value);
                }
            }
        });

        newProfilBuilder.setNegativeButton(getActivity().getResources().getText(R.string.global_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (getCurrentProfil() == null) {
                    CreateNewProfil();
                }
            }
        });

        newProfilBuilder.show();

        return true;
    }

    public boolean renameProfil() {
        AlertDialog.Builder newBuilder = new AlertDialog.Builder(this);

        newBuilder.setTitle(getActivity().getResources().getText(R.string.renameProfilTitle));
        newBuilder.setMessage(getActivity().getResources().getText(R.string.renameProfilQuestion));

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setText(getCurrentProfil().getName());
        newBuilder.setView(input);

        newBuilder.setPositiveButton(getActivity().getResources().getText(R.string.global_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();

                if (!value.isEmpty()) {
                    // Get current profil
                    Profile temp = getCurrentProfil();
                    // Rename it
                    temp.setName(value);
                    // Commit it
                    mDbProfils.updateProfile(temp);
                    // Make it the current.
                    setCurrentProfil(value);
                }
            }
        });

        newBuilder.setNegativeButton(getActivity().getResources().getText(R.string.global_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        newBuilder.show();

        return true;
    }

    private void setDrawerTitle(String pProfilName) {
        DrawerAdapter.getItem(0).setTitle(pProfilName);
        DrawerAdapter.notifyDataSetChanged();
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    private void showFragment(String pFragmentName)
    {
        showFragment(pFragmentName, true);
    }

    private void showFragment(String pFragmentName, boolean addToBackStack) {

        if (currentFragmentName == pFragmentName) return; // If this is already the current fragment, do no replace.

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        // Then show the fragments
        if (pFragmentName.equals(FONTESPAGER)) {
            ft.replace(R.id.fragment_container,getFontesPagerFragment(), FONTESPAGER);
        } else if (pFragmentName.equals(CARDIO)) {
            ft.replace(R.id.fragment_container,getCardioFragment(), CARDIO);
        } else if (pFragmentName.equals(WEIGHT)) {
            ft.replace(R.id.fragment_container, getWeightFragment(), WEIGHT);
        } else if (pFragmentName.equals(SETTINGS)) {
            ft.replace(R.id.fragment_container,getSettingsFragment(), SETTINGS);
        } else if (pFragmentName.equals(MACHINES)) {
            ft.replace(R.id.fragment_container,getMachineFragment(), MACHINES);
        } else if (pFragmentName.equals(ABOUT)) {
            ft.replace(R.id.fragment_container,getAboutFragment(), ABOUT);
        } else if (pFragmentName.equals(BODYTRACKING)) {
            ft.replace(R.id.fragment_container,getBodyPartFragment(), BODYTRACKING);
        } else if (pFragmentName.equals(PROFILE)) {
            ft.replace(R.id.fragment_container, getProfileFragment(), PROFILE);
        }
        currentFragmentName = pFragmentName;
        //if (addToBackStack)  ft.addToBackStack(null);
        ft.commit();

    }

    private void showFragment2(String pFragmentName) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();

        // Then show the fragments
        if (pFragmentName.equals(FONTESPAGER)) {
            if (getFontesPagerFragment().isAdded())
                ft.show(getFontesPagerFragment());
            else
                ft.add(R.id.fragment_container, getFontesPagerFragment(), FONTESPAGER);
            if (getCardioFragment().isAdded()) ft.hide(getCardioFragment());
            if (getWeightFragment().isAdded()) ft.hide(getWeightFragment());
            if (getProfileFragment().isAdded()) ft.hide(getProfileFragment());
            if (getMachineFragment().isAdded()) ft.hide(getMachineFragment());
            if (getAboutFragment().isAdded()) ft.hide(getAboutFragment());
            if (getSettingsFragment().isAdded()) ft.hide(getSettingsFragment());
            if (getBodyPartFragment().isAdded()) ft.hide(getBodyPartFragment());
            currentFragmentName = pFragmentName;
        } else if (pFragmentName.equals(CARDIO)) {
            if (getCardioFragment().isAdded())
                ft.show(getCardioFragment());
            else
                ft.add(R.id.fragment_container, getCardioFragment(), CARDIO);
            if (getFontesPagerFragment().isAdded()) ft.hide(getFontesPagerFragment());
            if (getWeightFragment().isAdded()) ft.hide(getWeightFragment());
            if (getProfileFragment().isAdded()) ft.hide(getProfileFragment());
            if (getMachineFragment().isAdded()) ft.hide(getMachineFragment());
            if (getAboutFragment().isAdded()) ft.hide(getAboutFragment());
            if (getSettingsFragment().isAdded()) ft.hide(getSettingsFragment());
            if (getBodyPartFragment().isAdded()) ft.hide(getBodyPartFragment());
            currentFragmentName = pFragmentName;
        } else if (pFragmentName.equals(WEIGHT)) {
            if (getWeightFragment().isAdded())
                ft.show(getWeightFragment());
            else
                ft.add(R.id.fragment_container, getWeightFragment(), WEIGHT);
            if (getFontesPagerFragment().isAdded()) ft.hide(getFontesPagerFragment());
            if (getProfileFragment().isAdded()) ft.hide(getProfileFragment());
            if (getCardioFragment().isAdded()) ft.hide(getCardioFragment());
            if (getMachineFragment().isAdded()) ft.hide(getMachineFragment());
            if (getAboutFragment().isAdded()) ft.hide(getAboutFragment());
            if (getSettingsFragment().isAdded()) ft.hide(getSettingsFragment());
            if (getBodyPartFragment().isAdded()) ft.hide(getBodyPartFragment());
            currentFragmentName = pFragmentName;
        } else if (pFragmentName.equals(SETTINGS)) {
            if (getSettingsFragment().isAdded())
                ft.show(getSettingsFragment());
            else
                ft.add(R.id.fragment_container, getSettingsFragment(), SETTINGS);
            if (getFontesPagerFragment().isAdded()) ft.hide(getFontesPagerFragment());
            if (getCardioFragment().isAdded()) ft.hide(getCardioFragment());
            if (getMachineFragment().isAdded()) ft.hide(getMachineFragment());
            if (getWeightFragment().isAdded()) ft.hide(getWeightFragment());
            if (getProfileFragment().isAdded()) ft.hide(getProfileFragment());
            if (getAboutFragment().isAdded()) ft.hide(getAboutFragment());
            if (getBodyPartFragment().isAdded()) ft.hide(getBodyPartFragment());
            currentFragmentName = pFragmentName;
        } else if (pFragmentName.equals(MACHINES)) {
            if (getMachineFragment().isAdded())
                ft.show(getMachineFragment());
            else
                ft.add(R.id.fragment_container, getMachineFragment(), MACHINES);
            if (getFontesPagerFragment().isAdded()) ft.hide(getFontesPagerFragment());
            if (getCardioFragment().isAdded()) ft.hide(getCardioFragment());
            if (getAboutFragment().isAdded()) ft.hide(getAboutFragment());
            if (getWeightFragment().isAdded()) ft.hide(getWeightFragment());
            if (getProfileFragment().isAdded()) ft.hide(getProfileFragment());
            if (getSettingsFragment().isAdded()) ft.hide(getSettingsFragment());
            if (getBodyPartFragment().isAdded()) ft.hide(getBodyPartFragment());
            currentFragmentName = pFragmentName;
        } else if (pFragmentName.equals(ABOUT)) {
            if (getAboutFragment().isAdded())
                ft.show(getAboutFragment());
            else
                ft.add(R.id.fragment_container, getAboutFragment(), ABOUT);
            if (getFontesPagerFragment().isAdded()) ft.hide(getFontesPagerFragment());
            if (getCardioFragment().isAdded()) ft.hide(getCardioFragment());
            if (getMachineFragment().isAdded()) ft.hide(getMachineFragment());
            if (getWeightFragment().isAdded()) ft.hide(getWeightFragment());
            if (getProfileFragment().isAdded()) ft.hide(getProfileFragment());
            if (getSettingsFragment().isAdded()) ft.hide(getSettingsFragment());
            if (getBodyPartFragment().isAdded()) ft.hide(getBodyPartFragment());
            currentFragmentName = pFragmentName;
        } else if (pFragmentName.equals(BODYTRACKING)) {
            if (getBodyPartFragment().isAdded())
                ft.show(getBodyPartFragment());
            else
                ft.add(R.id.fragment_container, getBodyPartFragment(), BODYTRACKING);
            if (getFontesPagerFragment().isAdded()) ft.hide(getFontesPagerFragment());
            if (getCardioFragment().isAdded()) ft.hide(getCardioFragment());
            if (getMachineFragment().isAdded()) ft.hide(getMachineFragment());
            if (getWeightFragment().isAdded()) ft.hide(getWeightFragment());
            if (getProfileFragment().isAdded()) ft.hide(getProfileFragment());
            if (getSettingsFragment().isAdded()) ft.hide(getSettingsFragment());
            if (getAboutFragment().isAdded()) ft.hide(getAboutFragment());
            currentFragmentName = pFragmentName;
        } else if (pFragmentName.equals(PROFILE)) {
            if (getProfileFragment().isAdded())
                ft.show(getProfileFragment());
            else
                ft.add(R.id.fragment_container, getProfileFragment(), BODYTRACKING);
            if (getFontesPagerFragment().isAdded()) ft.hide(getFontesPagerFragment());
            if (getCardioFragment().isAdded()) ft.hide(getCardioFragment());
            if (getMachineFragment().isAdded()) ft.hide(getMachineFragment());
            if (getWeightFragment().isAdded()) ft.hide(getWeightFragment());
            if (getBodyPartFragment().isAdded()) ft.hide(getBodyPartFragment());
            if (getSettingsFragment().isAdded()) ft.hide(getSettingsFragment());
            if (getAboutFragment().isAdded()) ft.hide(getAboutFragment());
            currentFragmentName = pFragmentName;
        }

        ft.commit();

    }

    @Override
    protected void onStop() {
        super.onStop();
        savePreferences();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public Profile getCurrentProfil() {
        return mCurrentProfile;
    }

    public long getCurrentProfilID() {
        return mCurrentProfile.getId();
    }

    //@SuppressLint("RestrictedApi")
    public void setCurrentProfil(String newProfilName) {
        mCurrentProfile = this.mDbProfils.getProfil(newProfilName);
        mCurrentProfilID = mCurrentProfile.getId();

        // rafraichit le fragment courant
        FragmentManager fragmentManager = getSupportFragmentManager();
        //FragmentTransaction ft=fragmentManager.beginTransaction();
        //showFragment(WEIGHT);

        // Moyen de rafraichir tous les fragments. Attention, les View des fragments peuvent avoir ete detruit.
        // Il faut donc que cela soit pris en compte dans le refresh des fragments.
        for (int i = 0; i < fragmentManager.getFragments().size(); i++) {
            if ( fragmentManager.getFragments().get(i) != null )
                fragmentManager.getFragments().get(i).onHiddenChanged(false);
        }

        setDrawerTitle(mCurrentProfile.getName());
        setPhotoProfile(mCurrentProfile.getPhoto());

        savePreferences();
    }

    private void setPhotoProfile(String path) {
        ImageUtil imgUtil = new ImageUtil();

        // Check if path is pointing to a thumb else create it and use it.
        String thumbPath = imgUtil.getThumbPath(path);
        if (thumbPath != null) {
            imgUtil.setPic(roundProfile, thumbPath);
            DrawerAdapter.getItem(0).setImg(thumbPath);
            DrawerAdapter.notifyDataSetChanged();
            mDrawerLayout.invalidate();
        } else {
            roundProfile.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_profile_black));
            DrawerAdapter.getItem(0).setImgResID(R.drawable.ic_profile_black);
            DrawerAdapter.notifyDataSetChanged();
            mDrawerLayout.invalidate();
        }
    }

    private void savePhotoProfile(String path) {
        mCurrentProfile.setPhoto(path);// Enregistrer sur le profile le path de la photo.
        mDbProfils.updateProfile(mCurrentProfile);
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
        mMigrationBD05done = settings.getBoolean("migrationBD05done", false);
    }

    private void savePreferences() {
        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        if (mCurrentProfile != null) editor.putLong("currentProfil", mCurrentProfile.getId());
        editor.putBoolean("intro014Launched", mIntro014Launched);
        editor.putBoolean("migrationBD05done", mIntro014Launched);
        editor.putBoolean("migrationBD05done", mMigrationBD05done);
        editor.commit();
    }

    private FontesPagerFragment getFontesPagerFragment() {
        if (mpFontesPagerFrag == null)
            mpFontesPagerFrag = (FontesPagerFragment) getSupportFragmentManager().findFragmentByTag(FONTESPAGER);
        if (mpFontesPagerFrag == null)
            mpFontesPagerFrag = FontesPagerFragment.newInstance(FONTESPAGER, 6);

        return mpFontesPagerFrag;
    }

    private CardioFragment getCardioFragment() {
        if (mpCardioFrag == null)
            mpCardioFrag = (CardioFragment) getSupportFragmentManager().findFragmentByTag(CARDIO);
        if (mpCardioFrag == null) mpCardioFrag = CardioFragment.newInstance(CARDIO, 2);

        return mpCardioFrag;
    }

    private WeightFragment getWeightFragment() {
        if (mpWeightFrag == null)
            mpWeightFrag = (WeightFragment) getSupportFragmentManager().findFragmentByTag(WEIGHT);
        if (mpWeightFrag == null) mpWeightFrag = WeightFragment.newInstance(WEIGHT, 5);

        return mpWeightFrag;
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

    private SettingsFragment getSettingsFragment() {
        if (mpSettingFrag == null)
            mpSettingFrag = (SettingsFragment) getSupportFragmentManager().findFragmentByTag(SETTINGS);
        if (mpSettingFrag == null) mpSettingFrag = SettingsFragment.newInstance(SETTINGS, 8);

        return mpSettingFrag;
    }

    public Toolbar getActivityToolbar() {
        return top_toolbar;
    }

    public void restoreToolbar() { if (top_toolbar != null) setSupportActionBar(top_toolbar); }

    public void showMP3Toolbar(boolean show) {
        Toolbar mp3toolbar = (Toolbar) this.findViewById(R.id.musicToolbar);
        if (!show) {
            mp3toolbar.setVisibility(View.GONE);
        } else {
            mp3toolbar.setVisibility(View.VISIBLE);
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {

            selectItem(position);

            // Insert the fragment by replacing any existing fragment
            switch (position) {
                case 0:
                    showFragment(PROFILE);
                    setTitle("Profile");
                    break;
                case 1:
                    showFragment(FONTESPAGER);
                    setTitle(getResources().getText(R.string.FonteLabel));
                    break;
                case 2:
                    showFragment(CARDIO);
                    setTitle(getResources().getText(R.string.CardioLabel));
                    break;
                case 3:
                    showFragment(MACHINES);
                    setTitle(getResources().getText(R.string.MachinesLabel));
                    break;
                case 4:
                    showFragment(WEIGHT);
                    setTitle(getResources().getText(R.string.WeightLabel));
                    break;
                case 5:
                    showFragment(BODYTRACKING);
                    setTitle(getResources().getText(R.string.bodytracking));
                    break;
                case 6:
                    showFragment(SETTINGS);
                    setTitle(getResources().getText(R.string.SettingLabel));
                    break;
                case 7:
                    showFragment(ABOUT);
                    setTitle(getResources().getText(R.string.AboutLabel));
                    break;
                default:
                    showFragment(FONTESPAGER);
                    setTitle(getResources().getText(R.string.FonteLabel));
            }
        }
    }

    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.
    private long mBackPressed;

    @Override
    public void onBackPressed() {
        int index = getActivity().getSupportFragmentManager().getBackStackEntryCount() - 1;
        if (index >= 0) { // Si on est dans une sous activité
            FragmentManager.BackStackEntry backEntry = getSupportFragmentManager().getBackStackEntryAt(index);
            String tag = backEntry.getName();
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_INTRO:
                if (resultCode == RESULT_OK) {
                    initActivity();
                    mIntro014Launched = true;
                    this.savePreferences();
                } else {
                    // Cancelled the intro. You can then e.g. finish this activity too.
                    finish();
                }
                break;
            case REQUEST_PICK_GALERY_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    ImageUtil imgUtil = new ImageUtil();
                    String realPath;
                    realPath = RealPathUtil.getRealPath(getApplicationContext(), data.getData());

                    String ret = imgUtil.saveThumb(realPath);// Crée la miniature
                    setPhotoProfile(realPath);
                    savePhotoProfile(realPath);
                }
                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                ImageUtil imgUtil = new ImageUtil();
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == Activity.RESULT_OK) {
                    Uri resultUri = result.getUri();
                    String realPath;
                    realPath = RealPathUtil.getRealPath(getApplicationContext(), resultUri);

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
                    File DestinationFile = new File(storageDir.getPath() + imageFileName);

                    try {
                        DestinationFile = imgUtil.moveFile(SourceFile, storageDir);
                        realPath = DestinationFile.getPath();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    String ret = imgUtil.saveThumb(realPath);// Crée la miniature
                    setPhotoProfile(realPath);
                    savePhotoProfile(realPath);

                    break;
                }
        }
    }

    public void initActivity() {
        // Initialisation des objets DB
        mDbProfils = new DAOProfil(this.getApplicationContext());

        // Pour la base de donnee profil, il faut toujours qu'il y ai au moins un profil
        /*if (mDbProfils.getCount() == 0 || mCurrentProfilID == -1) {
            // Ouvre la fenetre de creation de profil
            this.CreateNewProfil();
        } else {*/
        mCurrentProfile = mDbProfils.getProfil(mCurrentProfilID);
        if (mCurrentProfile == null) { // au cas ou il y aurait un probleme de synchro
            try {
                List<Profile> lList = mDbProfils.getAllProfils();
                mCurrentProfile = lList.get(0);
            } catch (IndexOutOfBoundsException e) {
                this.CreateNewProfil();
            }
        }

        if (mCurrentProfile != null) setCurrentProfil(mCurrentProfile.getName());

        // Initialisation de la base de donnee Machine dans le cas d'une migration de database < 4 vers 5 ou plus
        if (!mMigrationBD05done) {
            DAOFonte lDAOFonte = new DAOFonte(this.getApplicationContext());
            String[] machineListArray = lDAOFonte.getAllMachines();

            for (int i = 0; i < machineListArray.length; i++) {
                //Test is Machine exists. If not create it.
                DAOMachine lDAOMachine = new DAOMachine(this.getApplicationContext());
                if (!lDAOMachine.machineExists(machineListArray[i])) {
                    lDAOMachine.addMachine(machineListArray[i], "", DAOMachine.TYPE_FONTE, "");
                }
            }
            mMigrationBD05done = true;
        }
    }
}