package com.easyfitness;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.easyfitness.DAO.ProgressImage;
import com.easyfitness.DAO.progressimages.DAOProgressImage;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.ImageUtil;
import com.easyfitness.utils.Keyboard;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.Date;

public class ProgressImagesFragment extends Fragment {

    private FloatingActionButton photoButton = null;
    private ImageView currentProgressImage = null;
    private Button newerImageButton;
    private Button olderImageButton;
    private TextView progressImageIndex;
    private TextView progressImageDate;
    private ImageUtil imgUtil = null;
    private DAOProgressImage daoProgressImage;
    private AppViMo appViMo;
    private int imageOffset;
    private ProgressImage image;
    private int imageCount = 0;
    private DatePickerDialogFragment mDateFrag;


    private final View.OnClickListener onClickAddProgressImage = v -> imgUtil.createPhotoSourceDialog();
    private final DatePickerDialog.OnDateSetListener dateSet = (view, year, month, day) -> {
        progressImageDate.setText(DateConverter.dateToLocalDateStr(year, month, day, getContext()));
        Keyboard.hide(getContext(), progressImageDate);
        if (image != null) {
            image.setCreated(DateConverter.dateToDate(year, month, day));
            daoProgressImage.updateImageDate(image);
            updateImageNavigation();
        }

    };


    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static ProgressImagesFragment newInstance(String name, int id) {
        ProgressImagesFragment f = new ProgressImagesFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putInt("id", id);
        f.setArguments(args);

        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_progress_images, container, false);

        appViMo = new ViewModelProvider(requireActivity()).get(AppViMo.class);
        daoProgressImage = new DAOProgressImage(view.getContext());
        photoButton = view.findViewById(R.id.addProgressImage);
        currentProgressImage = view.findViewById(R.id.currentProgressImage);
        newerImageButton = view.findViewById(R.id.newerProgressImage);
        olderImageButton = view.findViewById(R.id.olderProgressImage);
        progressImageIndex = view.findViewById(R.id.progressImageIndex);
        progressImageDate = view.findViewById(R.id.progressImageDate);
        newerImageButton.setOnClickListener(v -> showNewerImage());
        olderImageButton.setOnClickListener(v -> showOlderImage());
        photoButton.setOnClickListener(onClickAddProgressImage);

        progressImageDate.setOnClickListener((e) -> showDatePickerFragment());

        imgUtil = new ImageUtil(this);
        imgUtil.setOnDeleteImageListener(u -> {
            deleteCurrentImage();
        });
        imgUtil.setOnPicTakenListener(uriFilePath -> {
            File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            try {
                File newFile = ImageUtil.moveFile(new File(uriFilePath), storageDir, generateFileName());
                daoProgressImage.addProgressImage(
                        new Date(System.currentTimeMillis()), // maybe user input
                        newFile,
                        appViMo.getProfile().getValue().getId()
                );
                imageOffset = 0;
                showProgressImage();
                updateImageNavigation();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        updateImageNavigation();
        return view;
    }


    private void showDatePickerFragment() {
        if (mDateFrag == null) {
            mDateFrag = DatePickerDialogFragment.newInstance(dateSet);
            mDateFrag.show(getActivity().getSupportFragmentManager().beginTransaction(), "dialog");
        } else {
            if (!mDateFrag.isVisible())
                mDateFrag.show(getActivity().getSupportFragmentManager().beginTransaction(), "dialog");
        }
    }

    private void deleteCurrentImage() {
        daoProgressImage.deleteImage(image.getId());
        new File(image.getFile()).delete();

        imageOffset = imageOffset >= (imageCount - 1) ? imageOffset - 1 : imageOffset;
        updateImageNavigation();
    }

    private void showOlderImage() {
        imageOffset++;
        updateImageNavigation();
    }

    private void showNewerImage() {
        imageOffset--;
        updateImageNavigation();
    }

    private void updateImageNavigation() {
        showProgressImage();
        imageCount = daoProgressImage.count(appViMo.getProfile().getValue().getId());
        progressImageIndex.setText((imageOffset + 1) + " / " + imageCount);
        if (image != null) {
            progressImageDate.setText(
                    DateConverter.dateToLocalDateStr(image.getCreated(), getContext())
            );
        } else {
            progressImageDate.setText(DateConverter.currentDate(getContext()));
        }
        if ((imageOffset + 1) >= imageCount) {
            olderImageButton.setEnabled(false);
        } else {
            olderImageButton.setEnabled(true);
        }
        if (imageOffset <= 0) {
            newerImageButton.setEnabled(false);
        } else {
            newerImageButton.setEnabled(true);
        }
    }

    private void showProgressImage() {
        image = daoProgressImage.getImage(
                appViMo.getProfile().getValue().getId(), imageOffset);
        if (image != null) {
            ImageUtil.setPic(currentProgressImage, image.getFile());
        } else {
            currentProgressImage.setImageDrawable(
                    ContextCompat.getDrawable(getContext(), R.drawable.ic_gym_bench_50dp)
            );
        }
    }

    private String generateFileName() {
        // TODO hard coded ending ?
        return String.format("%10d.jpg", System.currentTimeMillis() / 1000);
    }
}
