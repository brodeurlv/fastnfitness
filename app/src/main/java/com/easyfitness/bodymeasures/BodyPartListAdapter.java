package com.easyfitness.bodymeasures;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.bodymeasures.BodyMeasure;
import com.easyfitness.DAO.bodymeasures.BodyPart;
import com.easyfitness.DAO.bodymeasures.DAOBodyMeasure;
import com.easyfitness.R;
import com.easyfitness.graph.MiniDateGraph;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.ImageUtil;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

public class BodyPartListAdapter extends ArrayAdapter<BodyPart> implements View.OnClickListener {

    Context mContext;
    private Profile mProfile = null;

    public BodyPartListAdapter(ArrayList<BodyPart> data, Context context) {
        super(context, R.layout.bodypart_row, data);
        this.mContext = context;
    }

    @Override
    public void onClick(View v) {
        /*
        int position = (Integer) v.getTag();
        Object object = getItem(position);
        BodyPart dataModel = (BodyPart) object;*/
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        BodyPart dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.bodypart_row, parent, false);
            viewHolder.txtID = convertView.findViewById(R.id.LIST_BODYPART_ID);
            viewHolder.txtName = convertView.findViewById(R.id.LIST_BODYPART);
            viewHolder.txtLastMeasure = convertView.findViewById(R.id.LIST_BODYPART_LASTRECORD);
            viewHolder.logo = convertView.findViewById(R.id.LIST_BODYPART_LOGO);
            viewHolder.miniGraph = new MiniDateGraph(getContext(), convertView.findViewById(R.id.LIST_BODYPART_MINIGRAPH), "");

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        //result.startAnimation(animation);
        //lastPosition = position;
        viewHolder.txtID.setText(String.valueOf(dataModel.getId()));
        viewHolder.txtName.setText(dataModel.getName(getContext()));

        if (dataModel.getLastMeasure() != null)
            viewHolder.txtLastMeasure.setText(String.valueOf(dataModel.getLastMeasure().getBodyMeasure().getValue()));
        else
            viewHolder.txtLastMeasure.setText("-");
        if (!dataModel.getCustomPicture().isEmpty()) {
            ImageUtil.setPic(viewHolder.logo, dataModel.getCustomPicture());
        } else {
            if (dataModel.getBodyPartResKey() != -1)
                viewHolder.logo.setImageDrawable(dataModel.getPicture(getContext()));
            else
                viewHolder.logo.setImageDrawable(null); // Remove the image, Custom is not managed yet
        }

        convertView.post(() -> {
                    DAOBodyMeasure mDbBodyMeasure = new DAOBodyMeasure(getContext());

                    List<BodyMeasure> valueList = mDbBodyMeasure.getBodyPartMeasuresListTop4(dataModel.getId(), getProfile());

                    if (valueList != null) {
                        // Recupere les enregistrements
                        if (valueList.size() < 1) {
                            viewHolder.miniGraph.getChart().clear();
                        } else {
                            ArrayList<Entry> yVals = new ArrayList<>();

                            if (valueList.size() > 0) {
                                for (int i = valueList.size() - 1; i >= 0; i--) {
                                    Entry value = new Entry((float) DateConverter.nbDays(valueList.get(i).getDate()), valueList.get(i).getBodyMeasure().getValue());
                                    yVals.add(value);
                                }

                                viewHolder.miniGraph.draw(yVals);
                            }
                        }
                    }
                }
        );

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
        TextView txtLastMeasure;
        ImageView logo;
        MiniDateGraph miniGraph;
    }
}
