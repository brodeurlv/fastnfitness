package com.easyfitness;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.easyfitness.DAO.DAOProfil;
import com.easyfitness.DAO.DAOWeight;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.bodymeasures.BodyMeasure;
import com.easyfitness.DAO.bodymeasures.BodyPart;
import com.easyfitness.DAO.bodymeasures.DAOBodyMeasure;
import com.easyfitness.bodymeasures.BodyMeasureCursorAdapter;
import com.easyfitness.graph.Graph;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.EditableInputView.EditableInputView;
import com.easyfitness.utils.EditableInputView.EditableInputViewWithDate;
import com.easyfitness.utils.ExpandedListView;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class WeightFragment extends Fragment {
    private EditableInputViewWithDate weightEdit = null;
    private EditableInputView fatEdit = null;
    private EditableInputView musclesEdit = null;
    private EditableInputView waterEdit = null;
    private TextView imcText = null;
    private TextView imcRank = null;
    private TextView rfmText = null;
    private TextView rfmRank = null;
    private Button rfmHelpButton = null;
    private Button imcHelpButton = null;

    private Spinner valueSpinner = null;

    ExpandedListView weightList = null;
    MainActivity mActivity = null;
    private LineChart mChart = null;
    private Graph mGraph = null;
    private DAOWeight mWeightDb = null;
    private DAOBodyMeasure mDbBodyMeasure = null;
    private DAOProfil mDb = null;

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
        musclesEdit = view.findViewById(R.id.musclesInput);
        waterEdit = view.findViewById(R.id.waterInput);
        imcText = view.findViewById(R.id.imcValue);
        imcRank = view.findViewById(R.id.imcViewText);
        rfmText = view.findViewById(R.id.rfmValue);
        rfmRank = view.findViewById(R.id.rfmViewText);
        valueSpinner = view.findViewById(R.id.weightSpinner);
        weightList = view.findViewById(R.id.listRecord);
        rfmHelpButton = view.findViewById(R.id.rfmHelp);
        imcHelpButton = view.findViewById(R.id.imcHelp);

        /* Initialisation des evenements */
        weightEdit.setOnTextChangeListener(itemOnTextChange);
        fatEdit.setOnTextChangeListener(itemOnTextChange);
        musclesEdit.setOnTextChangeListener(itemOnTextChange);
        waterEdit.setOnTextChangeListener(itemOnTextChange);
        valueSpinner.setOnItemSelectedListener(itemOnItemSelectedChange);
        imcHelpButton.setOnClickListener(showHelp);
        rfmHelpButton.setOnClickListener(showHelp);

        // Add the other graph
        mChart = view.findViewById(R.id.weightChart);
        mChart.setDescription(null);
        mGraph = new Graph(getContext(), mChart, "");

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

    private void DrawGraph(List<BodyMeasure> valueList) {

        // Recupere les enregistrements
        if (valueList.size() < 1) {
            mChart.clear();
            return;
        }

        ArrayList<Entry> yVals = new ArrayList<Entry>();

        float minBodyMeasure = -1;

        for (int i = valueList.size() - 1; i >= 0; i--) {
            Entry value = new Entry((float) DateConverter.nbDays(valueList.get(i).getDate().getTime()), valueList.get(i).getBodyMeasure());
            yVals.add(value);
            if (minBodyMeasure == -1) minBodyMeasure = valueList.get(i).getBodyMeasure();
            else if (valueList.get(i).getBodyMeasure() < minBodyMeasure)
                minBodyMeasure = valueList.get(i).getBodyMeasure();
        }

        mGraph.draw(yVals);
    }

    /*  */
    private void FillRecordTable(List<BodyMeasure> bodyPartValueList) {
        Cursor oldCursor = null;

        if (bodyPartValueList.isEmpty()) {
            //Toast.makeText(getActivity(), "No records", Toast.LENGTH_SHORT).show();
            weightList.setAdapter(null);
        } else {
            // ...
            if (weightList.getAdapter() == null) {
                BodyMeasureCursorAdapter mTableAdapter = new BodyMeasureCursorAdapter(this.getView().getContext(), mDbBodyMeasure.getCursor(), 0, itemClickDeleteRecord);
                weightList.setAdapter(mTableAdapter);
            } else {
                oldCursor = ((BodyMeasureCursorAdapter) weightList.getAdapter()).swapCursor(mDbBodyMeasure.getCursor());
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
                BodyMeasure lastWeightValue = null;
                BodyMeasure lastWaterValue = null;
                BodyMeasure lastFatValue = null;
                BodyMeasure lastMusclesValue = null;

                if (getProfil()!=null) {
                    lastWeightValue = mDbBodyMeasure.getLastBodyMeasures(BodyPart.WEIGHT, getProfil());
                    lastWaterValue = mDbBodyMeasure.getLastBodyMeasures(BodyPart.WATER, getProfil());
                    lastFatValue = mDbBodyMeasure.getLastBodyMeasures(BodyPart.FAT, getProfil());
                    lastMusclesValue = mDbBodyMeasure.getLastBodyMeasures(BodyPart.MUSCLES, getProfil());
                }

                if (lastWeightValue != null) {
                    weightEdit.setText(String.valueOf(lastWeightValue.getBodyMeasure()));
                    // update IMC
                    int size = getProfil().getSize();
                    if (size == 0) {
                        imcText.setText("-");
                        imcRank.setText("no size available");
                    } else {
                        float imcValue = calculateImc(lastWeightValue.getBodyMeasure(), size);
                        imcText.setText(String.format("%.1f", imcValue));
                        imcRank.setText(getImcText(imcValue));
                    }
                } else {
                    weightEdit.setText("-");
                    imcText.setText("-");
                    imcRank.setText("no weight available");
                }

                if (lastWaterValue!=null)
                    waterEdit.setText(String.valueOf(lastWaterValue.getBodyMeasure()));
                else
                    waterEdit.setText("-");

                if (lastFatValue!=null)
                    fatEdit.setText(String.valueOf(lastFatValue.getBodyMeasure()));
                else
                    fatEdit.setText("-");

                if (lastMusclesValue != null)
                    musclesEdit.setText(String.valueOf(lastMusclesValue.getBodyMeasure()));
                else
                    musclesEdit.setText("-");

                int bodyPart = BodyPart.WEIGHT;
                ;

                switch (valueSpinner.getSelectedItemPosition()) {
                    case 0:
                        bodyPart = BodyPart.WEIGHT;
                        break;
                    case 1:
                        bodyPart = BodyPart.FAT;
                        break;
                    case 2:
                        bodyPart = BodyPart.MUSCLES;
                        break;
                    case 3:
                        bodyPart = BodyPart.WATER;
                        break;
                }

                List<BodyMeasure> bodyPartValueList = null;

                switch (bodyPart) {
                    case BodyPart.WEIGHT:
                        //valueList = mWeightDb.getWeightList(getProfil());
                        bodyPartValueList = mDbBodyMeasure.getBodyPartMeasuresList(BodyPart.WEIGHT, getProfil());
                        break;
                    case BodyPart.FAT:
                        bodyPartValueList = mDbBodyMeasure.getBodyPartMeasuresList(BodyPart.FAT, getProfil());
                        break;
                    case BodyPart.MUSCLES:
                        bodyPartValueList = mDbBodyMeasure.getBodyPartMeasuresList(BodyPart.MUSCLES, getProfil());
                        break;
                    case BodyPart.WATER:
                        bodyPartValueList = mDbBodyMeasure.getBodyPartMeasuresList(BodyPart.WATER, getProfil());
                        break;
                }

                //update graph
                DrawGraph(bodyPartValueList);

                // update table
                FillRecordTable(bodyPartValueList);
            }
        }
    }

    private BtnClickListener itemClickDeleteRecord = new BtnClickListener() {
        @Override
        public void onBtnClick(long id) {
            showDeleteDialog(id);
        }
    };

    private Spinner.OnItemSelectedListener itemOnItemSelectedChange = new Spinner.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            refreshData();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

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
                    float weightValue = Float.parseFloat(v.getText());
                    mDbBodyMeasure.addBodyMeasure(v.getDate(), BodyPart.WEIGHT, weightValue, getProfil().getId());
                    break;
                case R.id.fatInput:
                    float fatValue = Float.parseFloat(v.getText());
                    mDbBodyMeasure.addBodyMeasure(v.getDate(), BodyPart.FAT, fatValue, getProfil().getId());
                    break;
                case R.id.musclesInput:
                    float musclesValue = Float.parseFloat(v.getText());
                    mDbBodyMeasure.addBodyMeasure(v.getDate(), BodyPart.MUSCLES, musclesValue, getProfil().getId());
                    break;
                case R.id.waterInput:
                    float waterValue = Float.parseFloat(v.getText());
                    mDbBodyMeasure.addBodyMeasure(v.getDate(), BodyPart.WATER, waterValue, getProfil().getId());
                    break;
            }
            // update graph and table
            refreshData();
        }
    };

    private OnClickListener showHelp = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.imcHelp:
                    new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                            .setTitleText("Body Mass Index (BMI)")
                            .setContentText("BMI = weight (kg) / ( size(m) * size(m) ) ")
                            .setConfirmText(getResources().getText(R.string.global_ok).toString())
                            .showCancelButton(true)
                            .show();
                    break;
                case R.id.rfmHelp:
                    new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                            .setTitleText("Body Mass Index (BMI)")
                            .setContentText("BMI = weight (kg) / ( size(m) * size(m) ) ")
                            .setConfirmText(getResources().getText(R.string.global_ok).toString())
                            .showCancelButton(true)
                            .show();
                    break;
            }
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
                        mDbBodyMeasure.deleteMeasure(idToDelete);
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
