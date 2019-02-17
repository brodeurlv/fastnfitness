package com.easyfitness;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.ogaclejapan.smarttablayout.utils.ViewPagerItemAdapter;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.text.DecimalFormat;
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

    ViewPagerItemAdapter viewpagerAdapter = null;

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

        /*addWeightButton = view.findViewById(R.id.buttonAddWeight);
        weightEdit = view.findViewById(R.id.editWeight);
        dateEdit = view.findViewById(R.id.profilEditDate);*/
        //weightList = view.findViewById(R.id.listWeightProfil);

        /* Initialisation serie */

        /* Initialisation des boutons */
        /* addWeightButton.setOnClickListener(onClickAddWeight);
        dateEdit.setOnClickListener(clickDateEdit);
        dateEdit.setOnFocusChangeListener(focusDateEdit);*/
        //weightList.setOnItemLongClickListener(itemlongclickDeleteRecord);

        /* Initialisation des evenements */

        // Add the other graph
        mChart = view.findViewById(R.id.weightChart);
        mChart.setDescription(null);
        mGraph = new Graph(getContext(), mChart, getResources().getText(R.string.weightLabel).toString());
        mChart.getAxisRight().setEnabled(true);
        mChart.getAxisRight().setDrawGridLines(false);

        mWeightDb = new DAOWeight(view.getContext());

        // Set Initial text
        //dateEdit.setText(DateConverter.currentDate());

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

        ArrayList<Entry> yVals = new ArrayList<Entry>();
        ArrayList<Entry> imcVals = new ArrayList<Entry>();

        long minImc = -1;
        long maxImc = -1;
        long currentImcValue = -1;

        for (int i = valueList.size() - 1; i >= 0; i--) {
            float x = (float) DateConverter.nbDays(valueList.get(i).getDate().getTime());
            Entry value = new Entry(x, valueList.get(i).getWeight());
            yVals.add(value);
            if (getProfil().getSize() > 0) {
                currentImcValue = calculateImc(valueList.get(i).getWeight(), getProfil().getSize());
                Entry valueImc = new Entry(x, currentImcValue);
                imcVals.add(valueImc);
                if (minImc == -1) minImc = currentImcValue;
                else if (currentImcValue < minImc)
                    minImc = currentImcValue;
                if (maxImc == -1) maxImc = currentImcValue;
                else if (currentImcValue > maxImc)
                    maxImc = currentImcValue;
            }
        }
        if (minImc > 15f)
            mChart.getAxisRight().setAxisMinimum(15f); // start at 15
        if (maxImc < 40f)
            mChart.getAxisRight().setAxisMaximum(30f); // the axis maximum is 40

        // Defines yVals
        LineDataSet set1 = new LineDataSet(yVals, "Weight");
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setLineWidth(3f);
        set1.setCircleRadius(4f);
        set1.setDrawFilled(true);
        if (Utils.getSDKInt() >= 18) {
            // fill drawable only supported on api level 18 and above
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.fade_blue);
            set1.setFillDrawable(drawable);
        } else {
            set1.setFillColor(ColorTemplate.getHoloBlue());
        }
        set1.setFillAlpha(100);
        set1.setColor(getContext().getResources().getColor(R.color.toolbar_background));
        set1.setCircleColor(getContext().getResources().getColor(R.color.toolbar_background));

        // Create a data object with the datasets
        LineData data = new LineData(set1);

        data.setValueFormatter(new IValueFormatter() {
            private DecimalFormat mFormat = new DecimalFormat("#.##");

            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return mFormat.format(value);
            }
        });

        LineDataSet set2 = new LineDataSet(imcVals, "IMC");
        set2.setAxisDependency(YAxis.AxisDependency.RIGHT);
        set2.setLineWidth(3f);
        set2.setCircleRadius(4f);
        set2.setDrawFilled(false);

        set2.setFillAlpha(100);
        set2.setColor(getContext().getResources().getColor(R.color.red));
        set2.setCircleColor(getContext().getResources().getColor(R.color.red));

        // Create a data object with the datasets
        LineData data2 = new LineData(set2);

        data2.setValueFormatter(new IValueFormatter() {
            private DecimalFormat mFormat = new DecimalFormat("#.##");

            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return mFormat.format(value);
            }
        });

        // use the interface ILineDataSet
        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1);
        dataSets.add(set2);

        LineData lineData = new LineData(dataSets);
        mChart.setData(lineData);
        mChart.invalidate(); // refresh
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

    private long calculateImc(float weight, int size) {
        long imc = 0;

        imc = (long) (weight / (size / 100.0 * size / 100.0));

        return imc;
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
                //FillRecordTable(valueList);
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
