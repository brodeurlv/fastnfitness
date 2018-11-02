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
import com.easyfitness.utils.ImageUtil;

import java.util.ArrayList;

/**
 * Adapter pour les listes qui ne peuvent pas utiliser les curseurs a cause
 * de jonction de table
 */


    public class MachineArrayFullAdapter extends ArrayAdapter<Machine> {

    public MachineArrayFullAdapter(Context context, ArrayList<Machine> machines) {
        super(context, 0, machines);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Machine machine = getItem(position);
            if (machine==null) return convertView;

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.machinelist_row, parent, false);
            }
            TextView t0 = convertView.findViewById(R.id.LIST_MACHINE_ID);
            t0.setText(String.valueOf(machine.getId()));

            TextView t1 = convertView.findViewById(R.id.LIST_MACHINE_NAME);
            t1.setText(machine.getName());

            TextView t2 = convertView.findViewById(R.id.LIST_MACHINE_SHORT_DESCRIPTION);
            t2.setText(machine.getDescription());

            ImageView i0 = convertView.findViewById(R.id.LIST_MACHINE_PHOTO);
            String lPath = machine.getPicture();
            if( lPath != null && !lPath.isEmpty() ) {
                try {
                    ImageUtil imgUtil = new ImageUtil();
                    String lThumbPath = imgUtil.getThumbPath(lPath);
                    imgUtil.setThumb(i0, lThumbPath);
                } catch (Exception e) {
                    i0.setImageResource(R.drawable.ic_machine);
                    e.printStackTrace();
                }
            } else {
                i0.setImageResource(R.drawable.ic_machine);
            }

            ImageView iFav = convertView.findViewById(R.id.LIST_MACHINE_FAVORITE);
            //DAOFavorites favDB = new DAOFavorites(getContext());

            if (machine.getFavorite()) { //favDB.isFavorite(machine.getId(), mProfile.getId())) {
                iFav.setImageDrawable(convertView.getResources().getDrawable(android.R.drawable.btn_star_big_on));
            } else {
                iFav.setImageDrawable(convertView.getResources().getDrawable(android.R.drawable.btn_star_big_off));
            }

            return convertView;
        }
    }

