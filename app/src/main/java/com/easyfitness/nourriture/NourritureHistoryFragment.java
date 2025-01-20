package com.easyfitness.nourriture;

import android.database.Cursor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.easyfitness.AppViMo;
import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.DAO.DAOUtils;
import com.easyfitness.DAO.Machine;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.macros.DAOFoodRecord;
import com.easyfitness.DAO.macros.FoodRecord;
import com.easyfitness.DAO.record.DAORecord;
import com.easyfitness.DAO.record.Record;
import com.easyfitness.MainActivity;
import com.easyfitness.R;
import com.easyfitness.enums.DisplayType;
import com.easyfitness.fonte.RecordArrayAdapter;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class NourritureHistoryFragment extends Fragment {
    Spinner dateList = null;
    Spinner foodList = null;

    ListView filterList = null;

    List<String> mFoodArray = null;
    List<String> mDateArray = null;
    ArrayAdapter<String> mAdapaterFood = null;
    ArrayAdapter<String> mAdapterDate = null;
    FoodRecord mSelectedFood = null;
    private AppViMo appViMo;
    private DAOFoodRecord mDbRecord = null;
    private final OnItemLongClickListener itemlongclickDeleteRecord = (listView, view, position, id) -> {

        mDbRecord.deleteRecord(id);

        FillRecordTable(null, dateList.getSelectedItem().toString());

        KToast.infoToast(getActivity(), getResources().getText(R.string.removedid).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);

        return true;
    };
    private final OnItemSelectedListener onItemSelectedList = new OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {

            if (dateList.getCount() >= 1 && foodList.getCount() >= 1) {
                FillRecordTable(null, dateList.getSelectedItem().toString());
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };


    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static NourritureHistoryFragment newInstance(long foodId, long foodProfile) {
        NourritureHistoryFragment f = new NourritureHistoryFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.tab_history, container, false);
        view.findViewById(R.id.tableRowFilterMachine).setVisibility(View.GONE);

        dateList = view.findViewById(R.id.filterDate);
        foodList = view.findViewById(R.id.filterMachine);
        filterList = view.findViewById(R.id.listFilterRecord);

        // Initialisation de l'historique
        mDbRecord = new DAOFoodRecord(view.getContext());

        mFoodArray = new ArrayList<>();
        mFoodArray.add(getContext().getResources().getText(R.string.all).toString());
        mAdapaterFood = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item, //simple_spinner_dropdown_item
                mFoodArray);
        mAdapaterFood.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        foodList.setAdapter(mAdapaterFood);
        mDbRecord.closeCursor();

        mDateArray = new ArrayList<>();
        mDateArray.add(getContext().getResources().getText(R.string.all).toString());
        mAdapterDate = new ArrayAdapter<>(
                getContext(), android.R.layout.simple_spinner_item,
                mDateArray);
        mAdapterDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateList.setAdapter(mAdapterDate);

        // Initialisation des evenements
        filterList.setOnItemLongClickListener(itemlongclickDeleteRecord);
        dateList.setOnItemSelectedListener(onItemSelectedList);

        appViMo = new ViewModelProvider(requireActivity()).get(AppViMo.class);
        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        appViMo.getProfile().observe(getViewLifecycleOwner(), profile -> {
            // Update the UI, in this case, a TextView.
            refreshData();
            if (dateList.getCount() >= 1 && foodList.getCount() >= 1) {
                FillRecordTable(null, dateList.getSelectedItem().toString());
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshData();
    }

    public String getName() {
        return getArguments().getString("name");
    }

    /*  */
    private void FillRecordTable(String foodName, String pDate) {

        // Retransform date filter value in SQLLite date format
        if (!pDate.equals(getContext().getResources().getText(R.string.all).toString())) {
            Date date;
            try {
                DateFormat dateFormat3 = android.text.format.DateFormat.getDateFormat(getContext().getApplicationContext());
                dateFormat3.setTimeZone(TimeZone.getTimeZone("GMT"));
                date = dateFormat3.parse(pDate);
            } catch (ParseException e) {
                e.printStackTrace();
                date = new Date();
            }

            SimpleDateFormat dateFormat = DAOUtils.getDateFormat();
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            pDate = dateFormat.format(date);
        }

        // Get Values
        Cursor c = mDbRecord.getFilteredRecords(getProfile(),  foodName, pDate);

        List<FoodRecord> records = mDbRecord.fromCursorToList(c);
        c.close();

        if (records.isEmpty()) {
            filterList.setAdapter(null);
        } else {
            if (filterList.getAdapter() == null) {
                FoodRecordArrayAdapter mTableAdapter = new FoodRecordArrayAdapter(getActivity(), getContext(), records);
                filterList.setAdapter(mTableAdapter);
            } else {
                ((FoodRecordArrayAdapter) filterList.getAdapter()).setRecords(records);
            }
        }
    }

    private void refreshData() {
        View fragmentView = getView();
        if (fragmentView != null) {
            if (getProfile() != null) {
                refreshDates(mSelectedFood);
            }
        }
    }

    private void refreshDates(FoodRecord food) {
        View fragmentView = getView();
        if (fragmentView != null) {
            if (getProfile() != null) {
                mDateArray.clear();
                mDateArray.add(getView().getResources().getText(R.string.all).toString());
                if (food != null) {
                    mDateArray.addAll(mDbRecord.getAllDatesList(getProfile(), food.getFoodName()));
                }
                else {
                    mDateArray.addAll(mDbRecord.getAllDatesList(getProfile(), null));
                }
                if (mDateArray.size() > 1) {
                    dateList.setSelection(1);
                }
                mAdapterDate.notifyDataSetChanged();
                mDbRecord.closeCursor();
            }
        }
    }

    private Profile getProfile() {
        return appViMo.getProfile().getValue();
    }
}
