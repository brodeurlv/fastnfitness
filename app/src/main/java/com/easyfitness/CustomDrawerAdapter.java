package com.easyfitness;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.easyfitness.utils.ImageUtil;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;

public class CustomDrawerAdapter extends ArrayAdapter<DrawerItem> {

    Context context;
    List<DrawerItem> drawerItemList;
    int layoutResID;

    public CustomDrawerAdapter(Context context, int layoutResourceID,
                               List<DrawerItem> listItems) {
        super(context, layoutResourceID, listItems);
        this.context = context;
        this.drawerItemList = listItems;
        this.layoutResID = layoutResourceID;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        DrawerItemHolder drawerHolder;
        View view = convertView;

        if (view == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            drawerHolder = new DrawerItemHolder();

            view = inflater.inflate(layoutResID, parent, false);
            drawerHolder.ItemName = view.findViewById(R.id.drawer_itemName);
            drawerHolder.icon = view.findViewById(R.id.drawer_icon);

            drawerHolder.spinner = view.findViewById(R.id.drawerSpinner);

            drawerHolder.title = view.findViewById(R.id.drawerTitle);

            drawerHolder.headerLayout = view.findViewById(R.id.headerLayout);
            drawerHolder.itemLayout = view.findViewById(R.id.itemLayout);
            drawerHolder.spinnerLayout = view.findViewById(R.id.spinnerLayout);
            drawerHolder.roundProfile = view.findViewById(R.id.header_icon);

            view.setTag(drawerHolder);

        } else {
            drawerHolder = (DrawerItemHolder) view.getTag();

        }

        DrawerItem dItem = this.drawerItemList.get(position);
        if (dItem.isSpinner()) {
            drawerHolder.headerLayout.setVisibility(LinearLayout.GONE);
            drawerHolder.itemLayout.setVisibility(LinearLayout.GONE);
            drawerHolder.spinnerLayout.setVisibility(LinearLayout.VISIBLE);

            List<SpinnerItem> userList = new ArrayList<>();

            userList.add(new SpinnerItem(R.drawable.profilebw, "bloop",
                "bloop@gmail.com"));
            userList.add(new SpinnerItem(R.drawable.profilebw, "blip",
                "blip@gmail.com"));

            CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(context, R.layout.custom_spinner_item, userList);

            drawerHolder.spinner.setAdapter(adapter);

            drawerHolder.spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> arg0,
                                           View arg1, int arg2, long arg3) {
                    Toast.makeText(context, context.getResources().getString(R.string.userChanged),
                        Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {

                }
            });

        } else if (dItem.getTitle() != null) {
            drawerHolder.headerLayout.setVisibility(LinearLayout.VISIBLE);
            drawerHolder.itemLayout.setVisibility(LinearLayout.GONE);
            drawerHolder.spinnerLayout.setVisibility(LinearLayout.GONE);
            drawerHolder.title.setText(dItem.getTitle());
            //drawerHolder.icon = view.findViewById(R.id.header_icon);

            //drawerHolder.icon.setImageDrawable(view.getResources().getDrawable(dItem.getImgResID()));

            ImageUtil imgUtil = new ImageUtil();
            // Check if path is pointing to a thumb else create it and use it.
            String thumbPath = imgUtil.getThumbPath(dItem.getImg());
            if (thumbPath != null)
                ImageUtil.setPic(drawerHolder.roundProfile, thumbPath);
            else
                drawerHolder.roundProfile.setImageDrawable(view.getResources().getDrawable(dItem.getImgResID()));
        } else {

            drawerHolder.headerLayout.setVisibility(LinearLayout.GONE);
            drawerHolder.spinnerLayout.setVisibility(LinearLayout.GONE);
            drawerHolder.itemLayout.setVisibility(LinearLayout.VISIBLE);

            drawerHolder.icon.setImageDrawable(view.getResources().getDrawable(dItem.getImgResID()));

            if (!dItem.isActive()) {
                drawerHolder.ItemName.setAlpha((float) 0.5);
                drawerHolder.ItemName.setText(dItem.getItemName() + "(soon)");
            } else {
                drawerHolder.ItemName.setText(dItem.getItemName());
            }

            //Log.d("Getview", "Passed5");
        }
        return view;
    }

    private static class DrawerItemHolder {
        TextView ItemName, title;
        ImageView icon;
        CircularImageView roundProfile;
        RelativeLayout headerLayout, itemLayout;
        LinearLayout spinnerLayout;
        Spinner spinner;
    }
}
