package com.easyfitness.machines;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.canhub.cropper.CropImage;
import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.DAO.DAOProfile;
import com.easyfitness.DAO.Machine;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.record.DAORecord;
import com.easyfitness.DAO.record.Record;
import com.easyfitness.R;
import com.easyfitness.enums.ExerciseType;
import com.easyfitness.enums.Muscle;
import com.easyfitness.utils.ImageUtil;
import com.easyfitness.utils.Keyboard;
import com.easyfitness.utils.RealPathUtil;
import com.easyfitness.views.EditableInputView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class MachineDetailsFragment extends Fragment {
    private final Set<Muscle> selectedMuscles = new HashSet<>();
    TextView musclesList = null;
    EditableInputView machineName = null;
    EditableInputView machineDescription = null;
    ImageView machinePhoto = null;
    FloatingActionButton machineAction = null;
    LinearLayout machinePhotoLayout = null;
    ExerciseType selectedType = ExerciseType.STRENGTH;
    String machineNameArg = null;
    long machineIdArg = 0;
    long machineProfilIdArg = 0;
    boolean isImageFitToScreen = false;
    ExerciseDetailsPager pager = null;
    DAOMachine mDbMachine = null;
    DAORecord mDbRecord = null;
    Machine mMachine;

    View fragmentView = null;

    ImageUtil imgUtil = null;
    private final OnLongClickListener onLongClickMachinePhoto = v -> {
        imgUtil.createPhotoSourceDialog();
        return true;
    };
    private final OnClickListener onClickMachinePhoto = v -> imgUtil.createPhotoSourceDialog();
    boolean isCreateMuscleDialogActive = false;
    private final OnClickListener onClickMusclesList = v -> CreateMuscleDialog();
    private final OnFocusChangeListener onFocusMachineList = (arg0, arg1) -> {
        if (arg1) {
            CreateMuscleDialog();
        }
    };
    String mCurrentPhotoPath = null;
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
    private final EditableInputView.OnTextChangedListener textChangeListener = view -> requestForSave();

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static MachineDetailsFragment newInstance(long machineId, long machineProfile) {
        MachineDetailsFragment f = new MachineDetailsFragment();

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
        View view = inflater.inflate(R.layout.exercise_details, container, false);
        fragmentView = view;

        // Initialisation de l'historique
        mDbMachine = new DAOMachine(view.getContext());
        mDbRecord = new DAORecord(view.getContext());

        machineName = view.findViewById(R.id.machine_name);
        machineDescription = view.findViewById(R.id.machine_description);
        musclesList = view.findViewById(R.id.machine_muscles);
        machinePhoto = view.findViewById(R.id.machine_photo);

        machinePhotoLayout = view.findViewById(R.id.machine_photo_layout);
        machineAction = view.findViewById(R.id.actionCamera);

        imgUtil = new ImageUtil(this, machinePhoto);

        Bundle args = this.getArguments();

        machineIdArg = args.getLong("machineID");
        machineProfilIdArg = args.getLong("machineProfile");

        // set events
        musclesList.setOnClickListener(onClickMusclesList);
        musclesList.setOnFocusChangeListener(onFocusMachineList);
        machinePhoto.setOnLongClickListener(onLongClickMachinePhoto);
        machinePhoto.setOnClickListener(v -> {
            if (isImageFitToScreen) {
                isImageFitToScreen = false;
                machinePhoto.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
                machinePhoto.setAdjustViewBounds(true);
                machinePhoto.setMaxHeight((int) (getView().getHeight() * 0.2));
                machinePhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                if (mCurrentPhotoPath != null && !mCurrentPhotoPath.isEmpty()) {
                    File f = new File(mCurrentPhotoPath);
                    if (f.exists()) {

                        isImageFitToScreen = true;

                        // Get the dimensions of the bitmap
                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                        bmOptions.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
                        float photoW = bmOptions.outWidth;
                        float photoH = bmOptions.outHeight;

                        // Determine how much to scale down the image
                        int scaleFactor = (int) (photoW / machinePhoto.getWidth());//Math.min(photoW/targetW, photoH/targetH);machinePhoto.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        machinePhoto.setAdjustViewBounds(true);
                        machinePhoto.setMaxHeight((int) (photoH / scaleFactor));
                        machinePhoto.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    }
                }
            }
        });
        machineAction.setOnClickListener(onClickMachinePhoto);

        mMachine = mDbMachine.getMachine(machineIdArg);
        selectedMuscles.addAll(Muscle.setFromMigratedBodyPartString(mMachine.getBodyParts()));
        machineNameArg = mMachine.getName();

        if (machineNameArg.isEmpty()) {
            machineName.setText("Default exercise");
        } else {
            machineName.setText(machineNameArg);
        }

        machineDescription.setText(mMachine.getDescription());
        updateMuscleListText();
        mCurrentPhotoPath = mMachine.getPicture();

        if (mMachine.getType() == ExerciseType.CARDIO) {
            selectedType = mMachine.getType();
            view.findViewById(R.id.machine_muscles).setVisibility(View.GONE);
            view.findViewById(R.id.machine_muscles_textview).setVisibility(View.GONE);
        } else {
            selectedType = mMachine.getType();
            view.findViewById(R.id.machine_muscles).setVisibility(View.VISIBLE);
            view.findViewById(R.id.machine_muscles_textview).setVisibility(View.VISIBLE);
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
                    if (mMachine.getType() == ExerciseType.STRENGTH) {
                        imgUtil.getView().setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_gym_bench_50dp));
                    } else if (mMachine.getType() == ExerciseType.ISOMETRIC) {
                        imgUtil.getView().setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_static_50dp));
                    } else {
                        imgUtil.getView().setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_training_50dp));
                    }
                    machinePhoto.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                }
                machinePhoto.setMaxHeight((int) (getView().getHeight() * 0.2));
            }
        });

        machineName.setOnTextChangeListener(textChangeListener);
        machineDescription.setOnTextChangeListener(textChangeListener);
        musclesList.addTextChangedListener(watcher);

        imgUtil.setOnDeleteImageListener(imgUtil -> {
            if (mMachine.getType() == ExerciseType.STRENGTH) {
                imgUtil.getView().setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_gym_bench_50dp));
            } else if (mMachine.getType() == ExerciseType.ISOMETRIC) {
                imgUtil.getView().setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_static_50dp));
            } else {
                imgUtil.getView().setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_training_50dp));
            }
            machinePhoto.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            mCurrentPhotoPath = null;
            requestForSave();
        });

        imgUtil.setOnPicTakenListener(uriFilePath -> {
            ImageUtil.setPic(machinePhoto, uriFilePath);
            ImageUtil.saveThumb(uriFilePath);
            mCurrentPhotoPath = uriFilePath;
            requestForSave();
        });

        if (getParentFragment() instanceof ExerciseDetailsPager) {
            pager = (ExerciseDetailsPager) getParentFragment();
        }

        return view;
    }

    private void updateMuscleListText() {
        musclesList.setText(stringOfSelectedMuscles());
    }

    private String stringOfSelectedMuscles() {
        if (selectedMuscles.isEmpty()) {
            return "";
        }
        StringBuilder muscleString = new StringBuilder();
        for (String muscleName : sortedSelectedMuscles()) {
            muscleString.append(muscleName).append(";");
        }
        muscleString.setLength(muscleString.length() - 1);
        return muscleString.toString();
    }

    private List<String> sortedSelectedMuscles() {
        List<String> selectedMuscleNames = new ArrayList<String>() {{
            for (Muscle muscle : selectedMuscles) {
                add(muscle.nameFromResources(getResources()));
            }
        }};
        Collections.sort(selectedMuscleNames);
        return selectedMuscleNames;
    }

    private boolean CreateMuscleDialog() {
        if (isCreateMuscleDialogActive)
            return true; // Si la boite de dialog est deja active, alors n'en cree pas une deuxieme.

        isCreateMuscleDialogActive = true;

        Keyboard.hide(getContext(), musclesList);

        drawMuscleSelectionDialog();

        return true;
    }

    private void drawMuscleSelectionDialog() {
        AlertDialog.Builder muscleSelectionDialog = new AlertDialog.Builder(this.getActivity());
        muscleSelectionDialog.setTitle(this.getResources().getString(R.string.selectMuscles));

        setMultiChoiceItemsFor(muscleSelectionDialog);
        registerPositiveButtonFor(muscleSelectionDialog);
        registerNegativeButtonFor(muscleSelectionDialog);

        muscleSelectionDialog.show();
    }

    private void setMultiChoiceItemsFor(AlertDialog.Builder muscleSelectionDialog) {
        List<Muscle> sortedMuscles = Muscle.sortedListOfMusclesUsing(getResources());
        List<String> sortedMuscleStrings = new ArrayList<String>() {{
            for (Muscle muscle : sortedMuscles) {
                add(muscle.nameFromResources(getResources()));
            }
        }};
        boolean[] selections = arrayOfSelectedMusclesUsingOrdering(sortedMuscles);
        muscleSelectionDialog.setMultiChoiceItems(sortedMuscleStrings.toArray(new CharSequence[0]), selections, (arg0, selectionIndex, isSelected) -> {
            Muscle modifiedMuscle = sortedMuscles.get(selectionIndex);
            if (isSelected) selectedMuscles.add(modifiedMuscle);
            else selectedMuscles.remove(modifiedMuscle);
        });
    }

    private boolean[] arrayOfSelectedMusclesUsingOrdering(List<Muscle> musclesToCheck) {
        boolean[] arrayOfSelectedMuscles = new boolean[musclesToCheck.size()];
        for (int i = 0; i < musclesToCheck.size(); i++) {
            if (selectedMuscles.contains(musclesToCheck.get(i))) {
                arrayOfSelectedMuscles[i] = true;
            }
        }
        return arrayOfSelectedMuscles;
    }

    private void registerPositiveButtonFor(AlertDialog.Builder muscleSelectionDialog) {
        muscleSelectionDialog.setPositiveButton(getResources().getString(android.R.string.ok), (dialog, whichButton) -> {
            updateMuscleListText();
            Keyboard.hide(getContext(), musclesList);
            isCreateMuscleDialogActive = false;
        });
    }

    private void registerNegativeButtonFor(AlertDialog.Builder muscleSelectionDialog) {
        muscleSelectionDialog.setNegativeButton(getResources().getString(android.R.string.cancel), (dialog, whichButton) -> isCreateMuscleDialogActive = false);
    }

    public void setMuscleText(String t) {
        musclesList.setText(t);
    }

    public void requestForSave() {
        saveMachine();
    }

    public Machine getMachine() {
        Machine m = new Machine(machineName.getText(),
                machineDescription.getText(),
                selectedType,
                Muscle.migratedBodyPartStringFor(selectedMuscles),
                mCurrentPhotoPath, mMachine.getFavorite());
        m.setId(mMachine.getId());
        /*m.setName(machineName.getText());
        m.setDescription(machineDescription.getText());
        m.setBodyParts(getDBStringFromInput(musclesList.getText().toString()));
        m.setPicture(mCurrentPhotoPath);
        m.setFavorite(false);
        m.setType(selectedType);*/
        return m;
    }

    private boolean saveMachine() {
        boolean result = false;
        final Machine initialMachine = mMachine;
        final Machine newMachine = getMachine();
        final String lMachineName = newMachine.getName(); // Potentiel nouveau nom dans le EditText

        // Si le nom est different du nom actuel
        if (lMachineName.isEmpty()) {
            KToast.warningToast(getActivity(), getResources().getText(R.string.name_is_required).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);
        } else if (!initialMachine.getName().equals(lMachineName)) {
            final Machine machineWithSameName = mDbMachine.getMachine(lMachineName);
            // if an exercise exists with the same name but different types then block.
            if (machineWithSameName != null && newMachine.getId() != machineWithSameName.getId() && newMachine.getType() != machineWithSameName.getType()) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this.getActivity());

                dialogBuilder.setTitle(getActivity().getResources().getText(R.string.global_warning));
                dialogBuilder.setMessage(R.string.renameMachine_error_text2);
                dialogBuilder.setPositiveButton(getResources().getText(R.string.global_yes), (dialog, which) -> dialog.dismiss());

                AlertDialog dialog = dialogBuilder.create();
                dialog.show();

            } else if (machineWithSameName != null && newMachine.getId() != machineWithSameName.getId() && newMachine.getType() == machineWithSameName.getType()) {
                // if an exercise exists with the same name but with same types then merge.
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this.getActivity());

                dialogBuilder.setTitle(getActivity().getResources().getText(R.string.global_warning));
                dialogBuilder.setMessage(getActivity().getResources().getText(R.string.renameMachine_warning_text));
                // Si oui, supprimer la base de donnee et refaire un Start.
                dialogBuilder.setPositiveButton(getResources().getText(R.string.global_yes), (dialog, which) -> {
                    // Rename all the records with that machine and rename them
                    DAORecord lDbRecord = new DAORecord(getView().getContext());
                    DAOProfile mDbProfil = new DAOProfile(getView().getContext());
                    Profile lProfile = mDbProfil.getProfile(machineProfilIdArg);

                    List<Record> listRecords = lDbRecord.getAllRecordByMachineStrArray(lProfile, initialMachine.getName()); // Recupere tous les records de la machine courante
                    for (Record record : listRecords) {
                        record.setExercise(newMachine.getName()); // Change avec le nouveau nom. Normalement pas utile.
                        record.setExerciseId(machineWithSameName.getId()); // Met l'ID de la nouvelle machine
                        lDbRecord.updateRecord(record); // Met a jour
                    }

                    mDbMachine.delete(initialMachine); // Supprime l'ancienne machine

                    getActivity().onBackPressed();
                });

                dialogBuilder.setNegativeButton(getResources().getText(R.string.global_no), (dialog, which) -> {
                    // Do nothing but close the dialog
                    dialog.dismiss();
                });

                AlertDialog dialog = dialogBuilder.create();
                dialog.show();
            } else {
                this.mDbMachine.updateMachine(newMachine);

                // Rename all the records with that machine and rename them
                DAORecord lDbRecord = new DAORecord(getContext());
                DAOProfile mDbProfil = new DAOProfile(getContext());
                Profile lProfile = mDbProfil.getProfile(machineProfilIdArg);
                // Recupere tous les records de la machine courante
                List<Record> listRecords = lDbRecord.getAllRecordByMachineIdArray(lProfile, initialMachine.getId());
                for (Record record : listRecords) {
                    record.setExercise(lMachineName); // Change avec le nouveau nom (DEPRECATED)
                    lDbRecord.updateRecord(record); // met a jour
                }

                result = true;
            }
        } else {
            // Si le nom n'a pas ete modifie.
            mDbMachine.updateMachine(newMachine);
            result = true;
        }

        if (result) {
            mMachine = newMachine;
        }

        return result;
    }
}

