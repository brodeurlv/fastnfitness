package com.easyfitness.nourriture;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.easyfitness.DAO.macros.FoodRecord;
import com.easyfitness.R;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;

import java.util.List;

public class FoodArrayFullAdapter extends ArrayAdapter<FoodRecord> {

    public FoodArrayFullAdapter(Context context, List<FoodRecord> foods) {
        super(context, 0, foods);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position

        FoodRecord food = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.exercise_list_row, parent, false);
        }
        TextView t0 = convertView.findViewById(R.id.LIST_MACHINE_ID);
        t0.setText(String.valueOf(food.getId()));

        TextView t1 = convertView.findViewById(R.id.LIST_MACHINE_NAME);
        t1.setText(food.getFoodName());

        TextView t2 = convertView.findViewById(R.id.LIST_MACHINE_SHORT_DESCRIPTION);
        t2.setText(food.getNote());

        ImageView i0 = convertView.findViewById(R.id.LIST_MACHINE_PHOTO);
        i0.setVisibility(View.VISIBLE);
        i0.setImageDrawable(convertView.getResources().getDrawable(R.drawable.ic_carrot, convertView.getContext().getTheme()));

        MaterialFavoriteButton iFav = convertView.findViewById(R.id.LIST_MACHINE_FAVORITE);
        iFav.setVisibility(View.VISIBLE);
        return convertView;
    }


}

