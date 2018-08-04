package com.easyfitness;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.easyfitness.DAO.DAOProfil;
import com.easyfitness.DAO.DAOWeight;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.ProfileWeight;
import com.easyfitness.graph.Graph;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.ExpandedListView;
import com.easyfitness.utils.Keyboard;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class WeightFragment extends Fragment {
    Button addWeightButton = null;
    EditText weightEdit = null;
    EditText dateEdit = null;
    ExpandedListView weightList = null;
    MainActivity mActivity = null;
    private String name;
    private int id;
    private LineChart mChart = null;
    private Graph mGraph = null;
    private DAOWeight mWeightDb = null;
    private DAOProfil mDb = null;

    DatePickerDialogFragment mDateFrag = null;

    private void showDatePickerFragment() {
        if (mDateFrag == null) {
            mDateFrag = DatePickerDialogFragment.newInstance(dateSet);
        }

        FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
        mDateFrag.show(ft, "dialog");
    }

    private DatePickerDialog.OnDateSetListener dateSet = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            dateEdit.setText(DateConverter.dateToString(year, month + 1, day));
        }
    };

    private OnClickListener onClickAddWeight = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!weightEdit.getText().toString().isEmpty()) {

                Date date = DateConverter.editToDate(dateEdit.getText().toString());

                mWeightDb.addWeight(date, Float.valueOf(weightEdit.getText().toString()), getProfil());
                refreshData();
                weightEdit.setText("");
                Keyboard.hide(getContext(), v);
            } else {
                KToast.errorToast(getActivity(), getString(R.string.weight_missing), Gravity.BOTTOM, KToast.LENGTH_SHORT);
            }
        }
    };
    private OnClickListener clickDateEdit = new OnClickListener() {
        @Override
        public void onClick(View v) {
            showDatePickerFragment();
        }
    };
    private OnFocusChangeListener focusDateEdit = new OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus == true) {
                showDatePickerFragment();
            }
        }
    };
    private OnItemLongClickListener itemlongclickDeleteRecord = new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> listView, View view,
                                       int position, long id) {

            // Get the cursor, positioned to the corresponding row in the result set
            //Cursor cursor = (Cursor) listView.getItemAtPosition(position);

            final long selectedID = id;

            String[] profilListArray = new String[2];
            profilListArray[0] = getActivity().getResources().getString(R.string.DeleteLabel);
            profilListArray[1] = getActivity().getResources().getString(R.string.ShareLabel);


            AlertDialog.Builder itemActionbuilder = new AlertDialog.Builder(getActivity());
            itemActionbuilder.setTitle("").setItems(profilListArray, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    switch (which) {
                        // Delete
                        case 0:
                            mWeightDb.deleteMeasure(selectedID);

                            refreshData();

                            KToast.infoToast(getActivity(), getResources().getText(R.string.removedid).toString(), Gravity.BOTTOM, KToast.LENGTH_SHORT);

                            break;
                        // Share
                        case 1:
                            KToast.infoToast(getActivity(), "Share soon available", Gravity.BOTTOM, KToast.LENGTH_SHORT);
                            break;
                        default:
                    }
                }
            });
            itemActionbuilder.show();

            return true;
        }
    };

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static WeightFragment newInstance(String name, int id) {
        WeightFragment f = new WeightFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putInt("id", id);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.tab_weight, container, false);

        addWeightButton = (Button) view.findViewById(R.id.buttonAddWeight);
        weightEdit = (EditText) view.findViewById(R.id.editWeight);
        //resumeText = (TextView) view.findViewById(R.id.textResume);
        //profilText = (TextView) view.findViewById(R.id.textProfil);
        dateEdit = (EditText) view.findViewById(R.id.profilEditDate);
        weightList = (ExpandedListView) view.findViewById(R.id.listWeightProfil);

        /* Initialisation serie */

        /* Initialisation des boutons */
        addWeightButton.setOnClickListener(onClickAddWeight);
        dateEdit.setOnClickListener(clickDateEdit);
        dateEdit.setOnFocusChangeListener(focusDateEdit);
        weightList.setOnItemLongClickListener(itemlongclickDeleteRecord);

        /* Initialisation des evenements */

        // Add the other graph
        mChart = (LineChart) view.findViewById(R.id.weightChart);
        mChart.setDescription(null);
        mGraph = new Graph(mChart, getResources().getText(R.string.weightLabel).toString());

        mWeightDb = new DAOWeight(view.getContext());

        // Set Initial text
        dateEdit.setText(DateConverter.currentDate());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        refreshData();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = (MainActivity) activity;
    }

    private void DrawGraph(List<ProfileWeight> valueList) {

        // Recupere les enregistrements
        if (valueList.size() < 1) {
            mChart.clear();
            return;
        }

        //ArrayList<String> xVals = new ArrayList<String>();
        ArrayList<Entry> yVals = new ArrayList<Entry>();

        // Draw second graph
        long maxDate = -1;
        long minDate = -1;

		/*for (int i = 0; i<valueList.size();i++) {
			long tmpDate = valueList.get(i).getDate().getTime();
			if (maxDate == -1)  maxDate = tmpDate;
			if (minDate == -1)  minDate = tmpDate;

			if (tmpDate > maxDate) maxDate = tmpDate;
			if (tmpDate < minDate) minDate = tmpDate;
		}*/

        // Crée toutes les dates, meme si il n'y a pas de poids associé
		/*for (long i = minDate; i<=maxDate;i=i+86400000) {
			SimpleDateFormat dt1 = new SimpleDateFormat("dd-MM-yy");
			//dt1.setTimeZone(TimeZone.getTimeZone("GMT")); // On se recalle sur le GMT car le GetTime est en GMT.
			xVals.add(dt1.format(i));
		}*/

        float minWeight = -1;

        for (int i = valueList.size() - 1; i >= 0; i--) {
            float x = (float) DateConverter.nbDays(valueList.get(i).getDate().getTime());
            Entry value = new Entry(x, valueList.get(i).getWeight());
            yVals.add(value);
            if (minWeight == -1) minWeight = valueList.get(i).getWeight();
            else if (valueList.get(i).getWeight() < minWeight)
                minWeight = valueList.get(i).getWeight();
        }

        mGraph.draw(yVals);
        //mGraph.getLineChart().
    }

    /*  */
    private void FillRecordTable(List<ProfileWeight> valueList) {
        Cursor oldCursor = null;

        if (valueList.isEmpty()) {
            //Toast.makeText(getActivity(), "No records", Toast.LENGTH_SHORT).show();
            weightList.setAdapter(null);
        } else {
            // ...
            if (weightList.getAdapter() == null) {
                ProfilWeightCursorAdapter mTableAdapter = new ProfilWeightCursorAdapter(this.getView().getContext(), mWeightDb.GetCursor(), 0, itemClickDeleteRecord);
                weightList.setAdapter(mTableAdapter);
            } else {
                oldCursor = ((ProfilWeightCursorAdapter) weightList.getAdapter()).swapCursor(mWeightDb.GetCursor());
                if (oldCursor != null)
                    oldCursor.close();
            }
        }
    }

    public String getName() {
        return getArguments().getString("name");
    }

    public int getFragmentId() {
        return getArguments().getInt("id", 0);
    }

    private DAOWeight getWeightDB() {
        return mWeightDb;
    }

    private DAOProfil getDB() {
        return mDb;
    }

    private void refreshData() {
        View fragmentView = getView();
        if (fragmentView != null) {
            if (getProfil() != null) {
                //this.profilText.setText(getProfil().getName());
                //this.resumeText.setText("");
                List<ProfileWeight> valueList = mWeightDb.getWeightList(getProfil());

                // update table
                DrawGraph(valueList);
                FillRecordTable(valueList);
            }
        }
    }

    private BtnClickListener itemClickDeleteRecord = new BtnClickListener() {
        @Override
        public void onBtnClick(long id) {
            showDeleteDialog(id);
        }
    };

    private void showDeleteDialog(final long idToDelete) {

        new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.DeleteRecordDialog))
                .setContentText(getResources().getText(R.string.areyousure).toString())
                .setCancelText(getResources().getText(R.string.global_no).toString())
                .setConfirmText(getResources().getText(R.string.global_yes).toString())
                .showCancelButton(true)
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        mWeightDb.deleteMeasure(idToDelete);
                        refreshData();
                        // Info
                        KToast.infoToast(getActivity(), getResources().getText(R.string.removedid).toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                        sDialog.dismissWithAnimation();
                    }
                })
                .show();
    }

    private Profile getProfil() {
        return ((MainActivity) getActivity()).getCurrentProfil();
    }


    public Fragment getFragment() {
        return this;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) refreshData();
    }
}
