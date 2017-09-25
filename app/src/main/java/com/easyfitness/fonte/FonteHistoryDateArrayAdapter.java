package com.easyfitness.fonte;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.easyfitness.DAO.bodymeasures.BodyPart;
import com.easyfitness.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class FonteHistoryDateArrayAdapter extends ArrayAdapter<String>{

    private ArrayList<String> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView txtDate;
    }

    public FonteHistoryDateArrayAdapter(ArrayList<String> data, Context context) {
        super(context, android.R.layout.simple_spinner_item, data);
        this.dataSet = data;
        this.mContext=context;
    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
            viewHolder.txtDate = (TextView) convertView.findViewById(android.R.id.text1);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }


        return convertView;
    }
}
