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
        public MachineArrayFullAdapter(Context context, ArrayList<Machine> users) {
            super(context, 0, users);
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
            TextView t0 = (TextView) convertView.findViewById(R.id.LIST_MACHINE_ID);
            t0.setText(String.valueOf(machine.getId()));

            TextView t1 = (TextView) convertView.findViewById(R.id.LIST_MACHINE_NAME);
            t1.setText(machine.getName());

            TextView t2 = (TextView) convertView.findViewById(R.id.LIST_MACHINE_SHORT_DESCRIPTION);
            t2.setText(machine.getDescription());

            ImageView i0 = (ImageView) convertView.findViewById(R.id.LIST_MACHINE_PHOTO);
            String lPath = machine.getPicture();
            if( lPath != null && !lPath.isEmpty() ) {
                try {
                    lPath = lPath.substring(0, lPath.lastIndexOf('.')) + "_TH.jpg";
                    ImageUtil.setThumb(i0, lPath);
                } catch (Exception e) {
                    i0.setImageResource(R.drawable.ic_machine);
                    e.printStackTrace();
                }
            } else {
                i0.setImageResource(R.drawable.ic_machine);
            }
            return convertView;
        }
    }

