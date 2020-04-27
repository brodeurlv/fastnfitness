package com.easyfitness.programs

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.easyfitness.DAO.ARecord
import com.easyfitness.R
import java.util.*

class ProgramInExerciseArrayFullAdapter internal constructor(context: Context?, machines: ArrayList<ARecord?>?) : ArrayAdapter<ARecord?>(context, 0, machines) {
    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        // Get the data item for this position
        var convertView = convertView
        val exercise = getItem(position)
        if (exercise == null) {
            assert(convertView != null)
            return convertView
        }

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.machinelist_row, parent, false)
        }
        val t0 = convertView.findViewById<TextView>(R.id.LIST_MACHINE_ID)
        t0.text = exercise.id.toString()
        val t1 = convertView.findViewById<TextView>(R.id.LIST_MACHINE_NAME)
        t1.text = exercise.exercise

//        TextView t2 = convertView.findViewById(R.id.LIST_MACHINE_SHORT_DESCRIPTION);
//        t2.setText(exercise.getNote());
//        TODO if this is not used than probably could be removed, still not sure what this is for
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
        return convertView
    }
}
