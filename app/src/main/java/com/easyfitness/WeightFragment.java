package com.easyfitness;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.easyfitness.DAO.DAOProfil;
import com.easyfitness.DAO.DAOWeight;
import com.easyfitness.DAO.Profile;
import com.easyfitness.DAO.bodymeasures.BodyMeasure;
import com.easyfitness.DAO.bodymeasures.BodyPart;
import com.easyfitness.DAO.bodymeasures.BodyPartExtensions;
import com.easyfitness.DAO.bodymeasures.DAOBodyMeasure;
import com.easyfitness.DAO.bodymeasures.DAOBodyPart;
import com.easyfitness.bodymeasures.BodyPartDetailsFragment;
import com.easyfitness.graph.MiniDateGraph;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.views.EditableInputView;
import com.easyfitness.views.EditableInputViewWithDate;
import com.easyfitness.utils.Gender;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class WeightFragment extends Fragment {
    MainActivity mActivity = null;
    private EditableInputViewWithDate weightEdit = null;
    private EditableInputViewWithDate fatEdit = null;
    private EditableInputViewWithDate musclesEdit = null;
    private EditableInputViewWithDate waterEdit = null;
    private TextView imcText = null;
    private TextView imcRank = null;
    private TextView ffmiText = null;
    private TextView ffmiRank = null;
    private TextView rfmText = null;
    private TextView rfmRank = null;

    private LineChart mWeightLineChart;
    private LineChart mFatLineChart;
    private LineChart mMusclesLineChart;
    private LineChart mWaterLineChart;


    private DAOWeight mWeightDb = null;
    private DAOBodyMeasure mDbBodyMeasure = null;
    private DAOBodyPart mDbBodyPart;
    private DAOProfil mDb = null;

    private AdapterView.OnClickListener showDetailsFragment = v -> {
        int bodyPartID = BodyPartExtensions.WEIGHT;
        switch (v.getId()) {
            case R.id.weightDetailsButton:
                bodyPartID = BodyPartExtensions.WEIGHT;
                break;
            case R.id.fatDetailsButton:
                bodyPartID = BodyPartExtensions.FAT;
                break;
            case R.id.musclesDetailsButton:
                bodyPartID = BodyPartExtensions.MUSCLES;
                break;
            case R.id.waterDetailsButton:
                bodyPartID = BodyPartExtensions.WATER;
                break;
        }

        BodyPartDetailsFragment fragment = BodyPartDetailsFragment.newInstance(bodyPartID, false);
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, fragment, MainActivity.BODYTRACKINGDETAILS);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    };
    private BtnClickListener itemClickDeleteRecord = this::showDeleteDialog;
    private Spinner.OnItemSelectedListener itemOnItemSelectedChange = new Spinner.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            refreshData();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
    private EditableInputView.OnTextChangedListener itemOnTextChange = view -> {
        EditableInputViewWithDate v = (EditableInputViewWithDate) view;
        //save values to databases
        try {
            switch (view.getId()) {
                case R.id.weightInput:
                    // push value to database
                    float weightValue = Float.parseFloat(v.getText());
                    mDbBodyMeasure.addBodyMeasure(v.getDate(), BodyPartExtensions.WEIGHT, weightValue, getProfile().getId());
                    break;
                case R.id.fatInput:
                    float fatValue = Float.parseFloat(v.getText());
                    mDbBodyMeasure.addBodyMeasure(v.getDate(), BodyPartExtensions.FAT, fatValue, getProfile().getId());
                    break;
                case R.id.musclesInput:
                    float musclesValue = Float.parseFloat(v.getText());
                    mDbBodyMeasure.addBodyMeasure(v.getDate(), BodyPartExtensions.MUSCLES, musclesValue, getProfile().getId());
                    break;
                case R.id.waterInput:
                    float waterValue = Float.parseFloat(v.getText());
                    mDbBodyMeasure.addBodyMeasure(v.getDate(), BodyPartExtensions.WATER, waterValue, getProfile().getId());
                    break;
            }
        } catch (NumberFormatException e) {
            // Nothing to be done
        }
        // update graph and table
        refreshData();
    };
    private OnClickListener showHelp = v -> {
        switch (v.getId()) {
            case R.id.imcHelp:
                new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText(R.string.BMI_dialog_title)
                    .setContentText(getString(R.string.BMI_formula))
                    .setConfirmText(getResources().getText(R.string.global_ok).toString())
                    .showCancelButton(true)
                    .show();
                break;
            case R.id.ffmiHelp:
                new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText(R.string.FFMI_dialog_title)
                    .setContentText(getString(R.string.FFMI_formula))
                    .setConfirmText(getResources().getText(R.string.global_ok).toString())
                    .showCancelButton(true)
                    .show();
                break;
            case R.id.rfmHelp:
                new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                    .setTitleText(R.string.RFM_dialog_title)
                    .setContentText(getString(R.string.RFM_female_formula) +
                        getString(R.string.RFM_male_formula))
                    .setConfirmText(getResources().getText(R.string.global_ok).toString())
                    .showCancelButton(true)
                    .show();
                break;
        }
    };

    private MiniDateGraph mWeightGraph;
    private MiniDateGraph mFatGraph;
    private MiniDateGraph mMusclesGraph;
    private MiniDateGraph mWaterGraph;
    private BodyPart weightBobyPart;
    private BodyPart fatBobyPart;
    private BodyPart musclesBobyPart;
    private BodyPart waterBobyPart;

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
        Button weightDetailsButton = view.findViewById(R.id.weightDetailsButton);
        Button fatDetailsButton = view.findViewById(R.id.fatDetailsButton);
        Button musclesDetailsButton = view.findViewById(R.id.musclesDetailsButton);
        Button waterDetailsButton = view.findViewById(R.id.waterDetailsButton);
        imcText = view.findViewById(R.id.imcValue);
        imcRank = view.findViewById(R.id.imcViewText);
        ffmiText = view.findViewById(R.id.ffmiValue);
        ffmiRank = view.findViewById(R.id.ffmiViewText);
        rfmText = view.findViewById(R.id.rfmValue);
        rfmRank = view.findViewById(R.id.rfmViewText);

        ImageButton ffmiHelpButton = view.findViewById(R.id.ffmiHelp);
        ImageButton imcHelpButton = view.findViewById(R.id.imcHelp);
        ImageButton rfmHelpButton = view.findViewById(R.id.rfmHelp);

        /* Initialisation des evenements */
        weightEdit.setOnTextChangeListener(itemOnTextChange);
        fatEdit.setOnTextChangeListener(itemOnTextChange);
        musclesEdit.setOnTextChangeListener(itemOnTextChange);
        waterEdit.setOnTextChangeListener(itemOnTextChange);
        imcHelpButton.setOnClickListener(showHelp);
        ffmiHelpButton.setOnClickListener(showHelp);
        rfmHelpButton.setOnClickListener(showHelp);
        weightDetailsButton.setOnClickListener(showDetailsFragment);
        fatDetailsButton.setOnClickListener(showDetailsFragment);
        musclesDetailsButton.setOnClickListener(showDetailsFragment);
        waterDetailsButton.setOnClickListener(showDetailsFragment);

        mWeightDb = new DAOWeight(view.getContext());
        mDbBodyPart = new DAOBodyPart(view.getContext());
        mDbBodyMeasure = new DAOBodyMeasure(view.getContext());

        mWeightLineChart = view.findViewById(R.id.weightGraph);
        mWeightGraph = new MiniDateGraph(getContext(), mWeightLineChart, "");
        weightBobyPart = mDbBodyPart.getBodyPartfromBodyPartKey(BodyPartExtensions.WEIGHT);

        mFatLineChart = view.findViewById(R.id.fatGraph);
        mFatGraph = new MiniDateGraph(getContext(), mFatLineChart, "");
        fatBobyPart = mDbBodyPart.getBodyPartfromBodyPartKey(BodyPartExtensions.FAT);

        mMusclesLineChart = view.findViewById(R.id.musclesGraph);
        mMusclesGraph = new MiniDateGraph(getContext(), mMusclesLineChart, "");
        musclesBobyPart = mDbBodyPart.getBodyPartfromBodyPartKey(BodyPartExtensions.MUSCLES);

        mWaterLineChart = view.findViewById(R.id.waterGraph);
        mWaterGraph = new MiniDateGraph(getContext(), mWaterLineChart, "");
        waterBobyPart = mDbBodyPart.getBodyPartfromBodyPartKey(BodyPartExtensions.WATER);

        return view;
    }


    private void DrawGraph() {
        if (getView()==null) return;
        getView().post(() -> {
            if ( weightBobyPart == null ) return;

            List<BodyMeasure> valueList = mDbBodyMeasure.getBodyPartMeasuresListTop4(weightBobyPart.getId(), getProfile());

            // Recupere les enregistrements
            if (valueList.size() < 1) {
                mWeightLineChart.clear();
                return;
            }

            ArrayList<Entry> yVals = new ArrayList<>();

            if (valueList.size() > 0) {
                for (int i = valueList.size() - 1; i >= 0; i--) {
                    Entry value = new Entry((float) DateConverter.nbDays(valueList.get(i).getDate().getTime()), valueList.get(i).getBodyMeasure());
                    yVals.add(value);
        /*if (minBodyMeasure == -1) minBodyMeasure = valueList.get(i).getBodyMeasure();
        else if (valueList.get(i).getBodyMeasure() < minBodyMeasure)
            minBodyMeasure = valueList.get(i).getBodyMeasure();*/
                }

                mWeightGraph.draw(yVals);
            }

        });

        getView().post(() -> {
            if (fatBobyPart==null) return;

            List<BodyMeasure> valueList = mDbBodyMeasure.getBodyPartMeasuresListTop4(fatBobyPart.getId(), getProfile());

            // Recupere les enregistrements
            if (valueList.size() < 1) {
                mFatLineChart.clear();
                return;
            }

            ArrayList<Entry> yVals = new ArrayList<>();

            if ( valueList.size() > 0) {
                for (int i = valueList.size() - 1; i >= 0; i--) {
                    Entry value = new Entry((float) DateConverter.nbDays(valueList.get(i).getDate().getTime()), valueList.get(i).getBodyMeasure());
                    yVals.add(value);
            /*if (minBodyMeasure == -1) minBodyMeasure = valueList.get(i).getBodyMeasure();
            else if (valueList.get(i).getBodyMeasure() < minBodyMeasure)
                minBodyMeasure = valueList.get(i).getBodyMeasure();*/
                }

                mFatGraph.draw(yVals);
            }
        });
        getView().post(() -> {
            if (musclesBobyPart==null) return;
            List<BodyMeasure> valueList = mDbBodyMeasure.getBodyPartMeasuresListTop4(musclesBobyPart.getId(), getProfile());

            // Recupere les enregistrements
            if (valueList.size() < 1) {
                mMusclesLineChart.clear();
                return;
            }

            ArrayList<Entry> yVals = new ArrayList<>();

            if ( valueList.size() > 0) {
                for (int i = valueList.size() - 1; i >= 0; i--) {
                    Entry value = new Entry((float) DateConverter.nbDays(valueList.get(i).getDate().getTime()), valueList.get(i).getBodyMeasure());
                    yVals.add(value);
            /*if (minBodyMeasure == -1) minBodyMeasure = valueList.get(i).getBodyMeasure();
            else if (valueList.get(i).getBodyMeasure() < minBodyMeasure)
                minBodyMeasure = valueList.get(i).getBodyMeasure();*/
                }

                mMusclesGraph.draw(yVals);
            }
        });

        getView().post(() -> {
            if (waterBobyPart==null) return;
            List<BodyMeasure> valueList = mDbBodyMeasure.getBodyPartMeasuresListTop4(waterBobyPart.getId(), getProfile());

            // Recupere les enregistrements
            if (valueList.size() < 1) {
                mWaterLineChart.clear();
                return;
            }

            ArrayList<Entry> yVals = new ArrayList<>();

            if ( valueList.size() > 0) {
                for (int i = valueList.size() - 1; i >= 0; i--) {
                    Entry value = new Entry((float) DateConverter.nbDays(valueList.get(i).getDate().getTime()), valueList.get(i).getBodyMeasure());
                    yVals.add(value);
            /*if (minBodyMeasure == -1) minBodyMeasure = valueList.get(i).getBodyMeasure();
            else if (valueList.get(i).getBodyMeasure() < minBodyMeasure)
                minBodyMeasure = valueList.get(i).getBodyMeasure();*/
                }

                mWaterGraph.draw(yVals);
            }
        });
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

    public String getName() {
        return getArguments().getString("name");
    }

    /**
     * @param weight in kg
     * @param size   in cm
     * @return
     */
    private float calculateImc(float weight, int size) {
        float imc = 0;

        if (size == 0) return 0;

        imc = (float) (weight / (size / 100.0 * size / 100.0));

        return imc;
    }

    /**
     * @param imc
     * @return text associated with imc value
     */
    private String getImcText(float imc) {
        if (imc < 18.5) {
            return getString(R.string.underweight);
        } else if (imc < 25) {
            return getString(R.string.normal);
        } else if (imc < 30) {
            return getString(R.string.overweight);
        } else {
            return getString(R.string.obese);
        }
    }

    private float calculateRfm(float waistCirc, int sex, int size) {
        float rfm = 0;

        if (waistCirc == 0) return 0;

        return 0;
    }

    /**
     * @param rfm index
     * @return text associated with Rfm value
     */
    private String getRfmText(float rfm) {
        if (rfm < 18.5) {
            return "underweight";
        } else if (rfm < 25) {
            return "normal";
        } else if (rfm < 30) {
            return "overweight";
        } else {
            return "obese";
        }
    }

    /**
     * Fat-Free Mass (FFM): FFM [kg] = weight [kg] × (1 − (body fat [%] / 100))
     * Fat-Free Mass Index (FFMI): FFMI [kg/m2] = FFM [kg] / (height [m])2
     * Normalized Fat-Free Mass Index: Normalized FFMI [kg/m2] = FFM [kg] / (height [m])2 + 6.1 × (1.8 − height [m])
     * https://goodcalculators.com/ffmi-fat-free-mass-index-calculator/
     */
    private double calculateFfmi(float weight, int size, float bodyFat) {
        double ffmi = 0;

        if (bodyFat == 0) return 0;

        ffmi = weight * (1-(bodyFat/100)) / (size/ 100.0*size/ 100.0);

        return ffmi;
    }

    /**
     * Fat-Free Mass (FFM): FFM [kg] = weight [kg] × (1 − (body fat [%] / 100))
     * Fat-Free Mass Index (FFMI): FFMI [kg/m2] = FFM [kg] / (height [m])2
     * Normalized Fat-Free Mass Index: Normalized FFMI [kg/m2] = FFM [kg] / (height [m])2 + 6.1 × (1.8 − height [m])
     * https://goodcalculators.com/ffmi-fat-free-mass-index-calculator/
     */
    private double calculateNormalizedFfmi(float weight, int size, float bodyFat) {
        double ffmi = 0;

        if (bodyFat == 0) return 0;

        ffmi = weight * (1-(bodyFat/100)) / (size*size) + 6.1*(1.8-size);

        return ffmi;
    }

    /**
     * 16 – 17: below average     *
     * 18 – 19: average     *
     * 20 - 21: above average     *
     * 22: excellent     *
     * 23 – 25: superior     *
     * 26 – 27: scores considered suspicious but still attainable naturally     */
    private String getFfmiTextForMen(double ffmi) {
        if (ffmi < 17) {
            return "below average";
        } else if (ffmi < 19) {
            return "average";
        } else if (ffmi < 21) {
            return "above average";
        }else if (ffmi < 23) {
            return "excellent";
        }else if (ffmi < 25) {
            return "superior";
        }else if (ffmi < 27) {
            return "suspicious";
        } else {
            return "very suspicious";
        }
    }

    /**
     * 16 – 17: below average     *
     * 18 – 19: average     *
     * 20 - 21: above average     *
     * 22: excellent     *
     * 23 – 25: superior     *
     * 26 – 27: scores considered suspicious but still attainable naturally     */
    private String getFfmiTextForWomen(double ffmi) {
        if (ffmi < 14) {
            return "below average";
        } else if (ffmi < 16) {
            return "average";
        } else if (ffmi < 18) {
            return "above average";
        }else if (ffmi < 20) {
            return "excellent";
        }else if (ffmi < 22) {
            return "superior";
        }else if (ffmi < 24) {
            return "suspicious";
        } else {
            return "very suspicious";
        }
    }

    private void refreshData() {
        View fragmentView = getView();
        if (fragmentView != null) {
            if (getProfile() != null) {
                BodyMeasure lastWeightValue = null;
                BodyMeasure lastWaterValue = null;
                BodyMeasure lastFatValue = null;
                BodyMeasure lastMusclesValue = null;

                if (getProfile() != null) {
                    lastWeightValue = mDbBodyMeasure.getLastBodyMeasures(weightBobyPart.getId(), getProfile());
                    lastWaterValue = mDbBodyMeasure.getLastBodyMeasures(waterBobyPart.getId(), getProfile());
                    lastFatValue = mDbBodyMeasure.getLastBodyMeasures(fatBobyPart.getId(), getProfile());
                    lastMusclesValue = mDbBodyMeasure.getLastBodyMeasures(musclesBobyPart.getId(), getProfile());
                }

                if (lastWeightValue != null) {
                    weightEdit.setText(String.valueOf(lastWeightValue.getBodyMeasure()));
                    // update IMC
                    int size = getProfile().getSize();
                    if (size == 0) {
                        imcText.setText("-");
                        imcRank.setText(R.string.no_size_available);
                        ffmiText.setText("-");
                        ffmiRank.setText(R.string.no_size_available);
                    } else {
                        float imcValue = calculateImc(lastWeightValue.getBodyMeasure(), size);
                        imcText.setText(String.format("%.1f", imcValue));
                        imcRank.setText(getImcText(imcValue));
                        if (lastFatValue!=null) {
                            double ffmiValue = calculateFfmi(lastWeightValue.getBodyMeasure(), size, lastFatValue.getBodyMeasure());
                            ffmiText.setText(String.format("%.1f", ffmiValue));
                            if(getProfile().getGender()== Gender.FEMALE)
                                ffmiRank.setText(getFfmiTextForWomen(ffmiValue));
                            else if(getProfile().getGender()== Gender.MALE)
                                ffmiRank.setText(getFfmiTextForMen(ffmiValue));
                            else if(getProfile().getGender()== Gender.OTHER)
                                ffmiRank.setText(getFfmiTextForMen(ffmiValue));
                            else
                                ffmiRank.setText("no gender defined");
                        } else {
                            ffmiText.setText("-");
                            ffmiRank.setText(R.string.no_fat_available);
                        }

                    }
                } else {
                    weightEdit.setText("-");
                    imcText.setText("-");
                    imcRank.setText(R.string.no_weight_available);
                    ffmiText.setText("-");
                    ffmiRank.setText(R.string.no_weight_available);
                }

                if (lastWaterValue != null)
                    waterEdit.setText(String.valueOf(lastWaterValue.getBodyMeasure()));
                else
                    waterEdit.setText("-");

                if (lastFatValue != null)
                    fatEdit.setText(String.valueOf(lastFatValue.getBodyMeasure()));
                else
                    fatEdit.setText("-");

                if (lastMusclesValue != null)
                    musclesEdit.setText(String.valueOf(lastMusclesValue.getBodyMeasure()));
                else
                    musclesEdit.setText("-");


                DrawGraph();
            }
        }
    }

    private void showDeleteDialog(final long idToDelete) {

        new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
            .setTitleText(getString(R.string.DeleteRecordDialog))
            .setContentText(getResources().getText(R.string.areyousure).toString())
            .setCancelText(getResources().getText(R.string.global_no).toString())
            .setConfirmText(getResources().getText(R.string.global_yes).toString())
            .showCancelButton(true)
            .setConfirmClickListener(sDialog -> {
                mDbBodyMeasure.deleteMeasure(idToDelete);
                refreshData();
                // Info
                KToast.infoToast(getActivity(), getResources().getText(R.string.removedid).toString(), Gravity.BOTTOM, KToast.LENGTH_LONG);
                sDialog.dismissWithAnimation();
            })
            .show();
    }

    private Profile getProfile() {
        return ((MainActivity) getActivity()).getCurrentProfile();
    }

    public Fragment getFragment() {
        return this;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (!hidden) refreshData();
    }
}
