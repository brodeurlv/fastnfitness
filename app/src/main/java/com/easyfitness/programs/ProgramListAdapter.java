package com.easyfitness.programs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.program.Program;
import com.easyfitness.R;

import java.util.ArrayList;

public class ProgramListAdapter extends ArrayAdapter<Program> {

    Context mContext;
    private Profile mProfile = null;

    public ProgramListAdapter(ArrayList<Program> data, Context context) {
        super(context, R.layout.program_row, data);
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Program dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.program_row, parent, false);
            viewHolder.txtID = convertView.findViewById(R.id.LIST_WORKOUT_ID);
            viewHolder.txtName = convertView.findViewById(R.id.LIST_WORKOUT_NAME);
            viewHolder.txtDescription = convertView.findViewById(R.id.LIST_WORKOUT_DESCRIPTION);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.txtID.setText(String.valueOf(dataModel.getId()));
        viewHolder.txtName.setText(dataModel.getName());
        viewHolder.txtDescription.setText(dataModel.getDescription());

        // Return the completed view to render on screen
        return convertView;
    }

    private Profile getProfile() {
        return mProfile;
    }

    public void setProfile(Profile profileID) {
        mProfile = profileID;
    }

    // View lookup cache
    private static class ViewHolder {
        TextView txtID;
        TextView txtName;
        TextView txtDescription;
    }
}
