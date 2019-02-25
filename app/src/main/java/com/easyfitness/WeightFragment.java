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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.easyfitness.DAO.DAOProfil;
import com.easyfitness.DAO.DAOWeight;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.ProfileWeight;
import com.easyfitness.DAO.bodymeasures.BodyMeasure;
import com.easyfitness.DAO.bodymeasures.BodyPart;
import com.easyfitness.DAO.bodymeasures.DAOBodyMeasure;
import com.easyfitness.graph.Graph;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.EditableInputView.EditableInputView;
import com.easyfitness.utils.EditableInputView.EditableInputViewWithDate;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class WeightFragment extends Fragment {
    private EditableInputViewWithDate weightEdit = null;
    private EditableInputView fatEdit = null;
    private EditableInputView bonesEdit = null;
    private EditableInputView waterEdit = null;
    private TextView imcText = null;
    private TextView imcRank = null;

    private Spinner valueSpinner = null;

    ExpandedListView weightList = null;
    MainActivity mActivity = null;
    private String name;
    private int id;
    private LineChart mChart = null;
    private Graph mGraph = null;
    private DAOWeight mWeightDb = null;
    private DAOBodyMeasure mDbBodyMeasure = null;
    private DAOProfil mDb = null;

    ViewPagerItemAdapter viewpagerAdapter = null;

    DatePickerDialogFragment mDateFrag = null;

    private OnClickListener onClickAddWeight = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!weightEdit.getText().toString().isEmpty()) {


                //mWeightDb.addWeight(date, Float.valueOf(weightEdit.getText().toString()), getProfil());
                refreshData();
                weightEdit.setText("");
                Keyboard.hide(getContext(), v);
            } else {
                KToast.errorToast(getActivity(), getString(R.string.weight_missing), Gravity.BOTTOM, KToast.LENGTH_SHORT);
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

        /* Views Initialisation */
        weightEdit = view.findViewById(R.id.weightInput);
        fatEdit = view.findViewById(R.id.fatInput);
        bonesEdit = view.findViewById(R.id.bonesInput);
        waterEdit = view.findViewById(R.id.waterInput);
        imcText = view.findViewById(R.id.imcValue);
        imcRank = view.findViewById(R.id.imcViewText);
        valueSpinner = view.findViewById(R.id.weightSpinner);
        weightList = view.findViewById(R.id.listRecord);

        /* Initialisation des evenements */
        weightEdit.setOnTextChangeListener(itemOnTextChange);
        fatEdit.setOnTextChangeListener(itemOnTextChange);
        bonesEdit.setOnTextChangeListener(itemOnTextChange);
        waterEdit.setOnTextChangeListener(itemOnTextChange);

        // Add the other graph
        mChart = view.findViewById(R.id.weightChart);
        mChart.setDescription(null);
        mGraph = new Graph(getContext(), mChart, getResources().getText(R.string.weightLabel).toString());
        mChart.getAxisRight().setEnabled(true);
        mChart.getAxisRight().setDrawGridLines(false);

        mWeightDb = new DAOWeight(view.getContext());
        mDbBodyMeasure = new DAOBodyMeasure(view.getContext());

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

    private void DrawGraph(int bodyPart) {
        List<ProfileWeight> valueList = null;
        List<BodyMeasure> bodyPartValueList = null;

        switch (bodyPart) {
            case BodyPart.WEIGHT:
                valueList = mWeightDb.getWeightList(getProfil());
            break;
            case BodyPart.FAT:
                bodyPartValueList = mDbBodyMeasure.getBodyPartMeasuresList(BodyPart.FAT, getProfil());
                break;
            case BodyPart.WATER:
                bodyPartValueList = mDbBodyMeasure.getBodyPartMeasuresList(BodyPart.WATER, getProfil());
                break;
            case BodyPart.BONES:
                bodyPartValueList = mDbBodyMeasure.getBodyPartMeasuresList(BodyPart.BONES, getProfil());
                break;
        }



        // Recupere les enregistrements
        if (valueList.size() < 1) {
            mChart.clear();
            return;
        }

        ArrayList<Entry> yVals = new ArrayList<Entry>();
        ArrayList<Entry> imcVals = new ArrayList<Entry>();

        float minImc = -1;
        float maxImc = -1;
        float currentImcValue = -1;

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

        // use the interface ILineDataSet
        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1);

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

    /**
     *
     * @param weight in kg
     * @param size in cm
     * @return
     */
    private float calculateImc(float weight, int size) {
        float imc = 0;

        if (size==0) return 0;

        imc = (float)(weight / (size / 100.0 * size / 100.0));

        return imc;
    }

    /**
     *
     * @param imc
     * @return text associated with imc value
     */
    private String getImcText(float imc) {
        if (imc<18.5) {
            return "underweight";
        } else if (imc < 25) {
            return "normal";
        } else if (imc < 30) {
            return "overweight";
        } else {
            return "obese";
        }
    }

    private void refreshData() {
        View fragmentView = getView();
        if (fragmentView != null) {
            if (getProfil() != null) {
                mWeightDb.setProfil(getProfil());
                ProfileWeight pWeight = mWeightDb.getLastMeasure();

                BodyMeasure lastWeightValue = null;
                BodyMeasure lastWaterValue = null;
                BodyMeasure lastFatValue = null;
                BodyMeasure lastBonesValue = null;

                if (getProfil()!=null) {
                    //lastWeightValue = mDbBodyMeasure.getLastBodyMeasures(BodyPart.WEIGHT, getProfil());
                    lastWaterValue = mDbBodyMeasure.getLastBodyMeasures(BodyPart.WATER, getProfil());
                    lastFatValue = mDbBodyMeasure.getLastBodyMeasures(BodyPart.FAT, getProfil());
                    lastBonesValue = mDbBodyMeasure.getLastBodyMeasures(BodyPart.BONES, getProfil());
                }

                if (pWeight!=null) {
                    weightEdit.setText(String.valueOf(pWeight.getWeight()));
                    // update IMC
                    int size = getProfil().getSize();
                    if (size == 0) {
                        imcText.setText("-");
                        imcRank.setText("no size available");
                    } else {
                        float imcValue = calculateImc(pWeight.getWeight(), size);
                        imcText.setText(String.format("%.1f", imcValue));
                        imcRank.setText(getImcText(imcValue));
                    }
                }
                else
                    weightEdit.setText("-");
                if (lastWaterValue!=null)
                    waterEdit.setText(String.valueOf(lastWaterValue.getBodyMeasure()));
                else
                    waterEdit.setText("-");
                if (lastFatValue!=null)
                    fatEdit.setText(String.valueOf(lastFatValue.getBodyMeasure()));
                else
                    fatEdit.setText("-");
                if (lastBonesValue!=null)
                    bonesEdit.setText(String.valueOf(lastBonesValue.getBodyMeasure()));
                else
                    bonesEdit.setText("-");

                getResources().getText(R.string.weightLabel).toString();

                List<ProfileWeight> valueList = null;


                int bodyPart=0;

                if (valueSpinner.getSelectedItem().toString() == getResources().getText(R.string.weightLabel)) {
                    //valueList = mWeightDb.getWeightList(getProfil());
                    bodyPart = BodyPart.WEIGHT;
                } else if (valueSpinner.getSelectedItem().toString() == getResources().getText(R.string.fatLabel)) {
                    //valueList = mDbBodyMeasure.getBodyPartMeasuresList(BodyPart.FAT, getProfil());
                    bodyPart = BodyPart.FAT;
                } else if (valueSpinner.getSelectedItem().toString() == getResources().getText(R.string.waterLabel)) {
                    bodyPart = BodyPart.WATER;
                } else if (valueSpinner.getSelectedItem().toString() == getResources().getText(R.string.bonesLabel)) {
                    bodyPart = BodyPart.BONES;
                }


                //update graph
                DrawGraph(bodyPart);

                // update table
                FillRecordTable(valueList);
                //List<ProfileWeight> valueList = mWeightDb.getWeightList(getProfil());
            }
        }
    }

    private BtnClickListener itemClickDeleteRecord = new BtnClickListener() {
        @Override
        public void onBtnClick(long id) {
            showDeleteDialog(id);
        }
    };

    private EditableInputView.OnTextChangedListener itemOnTextChange = new EditableInputView.OnTextChangedListener() {

        @Override
        public void onTextChanged(EditableInputView view) {
            EditableInputViewWithDate v = (EditableInputViewWithDate) view;
            //save values to databases
            switch (view.getId()) {
                case R.id.weightInput:
                    // push value to database
                    float weight = Float.parseFloat(v.getText());
                    getWeightDB().addWeight(v.getDate(), weight, getProfil());
                    break;
                case R.id.fatInput:
                    float fatValue = Float.parseFloat(v.getText());
                    mDbBodyMeasure.addBodyMeasure(v.getDate(), BodyPart.FAT, fatValue, getProfil());
                    break;
                case R.id.bonesInput:
                    float bonesValue = Float.parseFloat(v.getText());
                    mDbBodyMeasure.addBodyMeasure(v.getDate(), BodyPart.BONES, bonesValue, getProfil());
                    break;
                case R.id.waterInput:
                    float waterValue = Float.parseFloat(v.getText());
                    mDbBodyMeasure.addBodyMeasure(v.getDate(), BodyPart.WATER, waterValue, getProfil());
                    break;
            }
            // update graph and table
            refreshData();
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
