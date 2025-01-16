package com.easyfitness.nourriture;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.devzone.fillprogresslayout.FillProgressLayout;
import com.easyfitness.BtnClickListener;
import com.easyfitness.CountdownDialogbox;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.macros.DAOFoodRecord;
import com.easyfitness.DAO.macros.FoodRecord;
import com.easyfitness.FoodRecordEditorDialogbox;
import com.easyfitness.MainActivity;
import com.easyfitness.R;
import com.easyfitness.RecordEditorDialogbox;
import com.easyfitness.enums.FoodQuantityUnit;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.Keyboard;
import com.easyfitness.utils.OnCustomEventListener;
import com.easyfitness.utils.UnitConverter;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class FoodRecordArrayAdapter extends ArrayAdapter {

    private final Activity mActivity;
    private final int mFirstColorOdd = 0;
    private final Context mContext;
    private final DAOFoodRecord mDbRecord;
    List<FoodRecord> mRecordList;
    private BtnClickListener mAction2ClickListener = null;
    private OnCustomEventListener mProgramCompletedListener;

    public FoodRecordArrayAdapter(Activity activity, Context context, List<FoodRecord> objects, BtnClickListener clickAction2) {
        super(context, R.layout.row_nourriture, objects);
        mActivity = activity;
        mContext = context;
        mRecordList = objects;
        mAction2ClickListener = clickAction2;
        mDbRecord = new DAOFoodRecord(context);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        FoodRecord record = mRecordList.get(position);
        ViewHolder viewHolder;

        if (view == null) {

            // inflate the layout
            LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = mInflater.inflate(R.layout.row_nourriture, null);

            viewHolder = new ViewHolder();
            viewHolder.FoodName = view.findViewById(R.id.FOOD_NAME);
            viewHolder.CardView = view.findViewById(R.id.CARDVIEW);

            viewHolder.Separator = view.findViewById(R.id.SEPARATOR_CELL);
            viewHolder.Date = view.findViewById(R.id.DATE_CELL);
            viewHolder.Time = view.findViewById(R.id.TIME_CELL);
            viewHolder.CaloriesValue = view.findViewById(R.id.CALORIES_CELL);
            viewHolder.CarbsValue = view.findViewById(R.id.CARBS_CELL);
            viewHolder.ProteinValue = view.findViewById(R.id.PROTEIN_CELL);
            viewHolder.FatsValue = view.findViewById(R.id.FATS_CELL);
            viewHolder.QuantityText = view.findViewById(R.id.QUANTITY_CELL);
            viewHolder.BtActionDelete = view.findViewById(R.id.deleteButton);
            viewHolder.BtActionEdit = view.findViewById(R.id.editButton);

            viewHolder.SecondColumn = view.findViewById(R.id.second_column);

            // store the holder with the view.
            view.setTag(viewHolder);
        } else {
            // we've just avoided calling findViewById() on resource everytime
            // just use the viewHolder
            viewHolder = (ViewHolder) view.getTag();
        }

        // Common display
        UpdateValues(record, position, viewHolder);

        return view;
    }

    private void UpdateValues(FoodRecord record, int position, ViewHolder viewHolder) {

        viewHolder.BtActionDelete.setTag(record.getId());
        viewHolder.BtActionDelete.setOnClickListener(v -> showDeleteDialog(record));

        viewHolder.BtActionEdit.setTag(record.getId());
        viewHolder.BtActionEdit.setOnClickListener(v -> showEditorDialog(record, position, viewHolder));

        viewHolder.FoodName.setText(record.getFoodName());
        viewHolder.CaloriesValue.setText(String.format(Locale.getDefault(), "%.1f", record.getCalories()));
        viewHolder.CarbsValue.setText(String.format(Locale.getDefault(), "%.1f", record.getCarbs()) + "g");
        viewHolder.ProteinValue.setText(String.format(Locale.getDefault(), "%.1f", record.getProtein()) + "g");
        viewHolder.FatsValue.setText(String.format(Locale.getDefault(), "%.1f", record.getFats())  + "g");
        viewHolder.QuantityText.setText("Quantity: " + String.format(Locale.getDefault(), "%.1f", record.getQuantity()) + " " + record.getQuantityUnit().toString());
        viewHolder.Date.setText(DateConverter.dateToLocalDateStr(record.getDate(), mContext));
        viewHolder.Time.setText(DateConverter.dateToLocalTimeStr(record.getDate(), mContext));

        if (isSeparatorNeeded(position, record.getDate())) {
            viewHolder.Separator.setText(String.format("- %s -", DateConverter.dateToLocalDateStr(record.getDate(), mContext)));
            viewHolder.Separator.setVisibility(View.VISIBLE);
        } else {
            viewHolder.Separator.setText("");
            viewHolder.Separator.setVisibility(View.GONE);
        }
    }

    private void showEditorDialog(FoodRecord record, int position, ViewHolder viewHolder) {
        FoodRecordEditorDialogbox recordEditorDialogbox = new FoodRecordEditorDialogbox(mActivity, record);
        recordEditorDialogbox.setOnCancelListener(dialog -> {
            notifyDataSetChanged();
            Keyboard.hide(getContext(), viewHolder.CardView);
        });
        recordEditorDialogbox.setOnDismissListener(dialog -> {
            if (!recordEditorDialogbox.isCancelled()) {
                notifyDataSetChanged();
                Keyboard.hide(getContext(), viewHolder.CardView);
            }
        });
        recordEditorDialogbox.show();
    }

    private boolean isSeparatorNeeded(int position, Date date) {
        // Add separator if needed
        if (position == 0) {
            return true;
        } else {
            FoodRecord record = mRecordList.get(position - 1);
            Date datePrevious = record.getDate();
            String dateString = DateConverter.dateTimeToDBDateStr(date);
            String datePreviousString = DateConverter.dateTimeToDBDateStr(datePrevious);
            return !datePreviousString.equals(dateString);
        }
    }

    public void setRecords(List<FoodRecord> data) {
        mRecordList.clear();
        mRecordList.addAll(data);
        notifyDataSetChanged();
    }

    private void showDeleteDialog(final FoodRecord record) {

        new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getContext().getString(R.string.DeleteRecordDialog))
                .setContentText(getContext().getString(R.string.areyousure))
                .setCancelText(getContext().getString(R.string.global_no))
                .setConfirmText(getContext().getString(R.string.global_yes))
                .showCancelButton(true)
                .setConfirmClickListener(sDialog -> {
                    int ret = mDbRecord.deleteRecord(record.getId());
                    if (ret != 0) mRecordList.remove(record);
                    notifyDataSetChanged();

                    KToast.infoToast(mActivity, getContext().getString(R.string.removedid), Gravity.BOTTOM, KToast.LENGTH_LONG);
                    sDialog.dismissWithAnimation();
                })
                .show();
    }

    public void setOnProgramCompletedListener(OnCustomEventListener eventListener) {
        mProgramCompletedListener = eventListener;
    }

    private Profile getProfile() {
        return ((MainActivity) mActivity).getCurrentProfile();
    }

    // View lookup cache
    private static class ViewHolder {

        CardView CardView;
        TextView Separator;
        TextView FoodName;
        TextView Date;
        TextView Time;
        TextView CaloriesValue;
        TextView CarbsValue;
        LinearLayout SecondColumn;
        TextView FatsValue;
        TextView ProteinValue;
        TextView QuantityText;

        ImageView BtActionDelete;
        ImageView BtActionEdit;

    }
}
