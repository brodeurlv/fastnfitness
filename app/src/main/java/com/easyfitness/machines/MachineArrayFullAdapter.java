package com.easyfitness.machines;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.easyfitness.DAO.Machine;
import com.easyfitness.R;
import com.easyfitness.enums.ExerciseType;
import com.easyfitness.utils.ImageUtil;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;

import java.util.ArrayList;

/**
 * Adapter pour les listes qui ne peuvent pas utiliser les curseurs a cause
 * de jonction de table
 */


public class MachineArrayFullAdapter extends ArrayAdapter<Machine> {

    public MachineArrayFullAdapter(Context context, ArrayList<Machine> machines) {
        super(context, 0, machines);
    }

    public boolean containsExercise(String exerciseName) {
        for (int i = 0; i < this.getCount(); i++) {
            if (this.getItem(i).getName().equals(exerciseName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Machine machine = getItem(position);
        if (machine == null) return convertView;

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.exercise_list_row, parent, false);
        }
        TextView t0 = convertView.findViewById(R.id.LIST_MACHINE_ID);
        t0.setText(String.valueOf(machine.getId()));

        TextView t1 = convertView.findViewById(R.id.LIST_MACHINE_NAME);
        t1.setText(machine.getName());

        TextView t2 = convertView.findViewById(R.id.LIST_MACHINE_SHORT_DESCRIPTION);
        t2.setText(machine.getDescription());

        ImageView i0 = convertView.findViewById(R.id.LIST_MACHINE_PHOTO);
        String lPath = machine.getPicture();
        boolean success = false;
        if (lPath != null && !lPath.isEmpty()) {
            String lThumbPath = ImageUtil.getThumbPath(lPath);
            success = ImageUtil.setPic(i0, lThumbPath);
        }
        if (!success) {
            if (machine.getType() == ExerciseType.STRENGTH) {
                i0.setImageResource(R.drawable.ic_gym_bench_50dp);
            } else if (machine.getType() == ExerciseType.ISOMETRIC) {
                i0.setImageResource(R.drawable.ic_static_50dp);
            } else {
                i0.setImageResource(R.drawable.ic_training_50dp);
                i0.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }
        }

        MaterialFavoriteButton iFav = convertView.findViewById(R.id.LIST_MACHINE_FAVORITE);
        iFav.setFavorite(machine.getFavorite());
        return convertView;
    }


}

