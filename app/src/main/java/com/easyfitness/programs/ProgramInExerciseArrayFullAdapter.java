package com.easyfitness.programs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.easyfitness.DAO.ExerciseInProgram;
import com.easyfitness.R;
import java.util.ArrayList;

import androidx.annotation.NonNull;

public class ProgramInExerciseArrayFullAdapter extends ArrayAdapter<ExerciseInProgram> {

    ProgramInExerciseArrayFullAdapter(Context context, ArrayList<ExerciseInProgram> machines) {
        super(context, 0, machines);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ExerciseInProgram exercise = getItem(position);
        if (exercise == null) {
            assert convertView != null;
            return convertView;
        }

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.machinelist_row, parent, false);
        }
        TextView t0 = convertView.findViewById(R.id.LIST_MACHINE_ID);
        t0.setText(String.valueOf(exercise.getId()));

        TextView t1 = convertView.findViewById(R.id.LIST_MACHINE_NAME);
        t1.setText(exercise.getExercise());

        TextView t2 = convertView.findViewById(R.id.LIST_MACHINE_SHORT_DESCRIPTION);
        t2.setText(exercise.getNote());

//        ImageView i0 = convertView.findViewById(R.id.LIST_MACHINE_PHOTO);
//        String lPath = exercise.();
//        if (lPath != null && !lPath.isEmpty()) {
//            try {
//                ImageUtil imgUtil = new ImageUtil();
//                String lThumbPath = imgUtil.getThumbPath(lPath);
//                ImageUtil.setThumb(i0, lThumbPath);
//            } catch (Exception e) {
//                i0.setImageResource(R.drawable.ic_machine);
//                e.printStackTrace();
//            }
//        } else {
//            i0.setImageResource(R.drawable.ic_machine);
//        }

//        MaterialFavoriteButton iFav = convertView.findViewById(R.id.LIST_MACHINE_FAVORITE);
//        iFav.setFavorite(exercise.getFavorite());
        return convertView;
    }
}

