package com.easyfitness;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.easyfitness.DAO.DAOProfil;
import com.easyfitness.DAO.DAOWeight;
import com.easyfitness.DAO.Profile;
import com.easyfitness.graph.Graph;
import com.easyfitness.utils.DateConverter;
import com.easyfitness.utils.ExpandedListView;
import com.github.mikephil.charting.charts.LineChart;
import com.onurkaganaldemir.ktoastlib.KToast;

import java.util.Date;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class ProfileFragment extends Fragment {
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
                weightEdit.setText("");
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

    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static ProfileFragment newInstance(String name, int id) {
        ProfileFragment f = new ProfileFragment();

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
        View view = inflater.inflate(R.layout.profile, container, false);

//		addWeightButton = (Button) view.findViewById(R.id.buttonAddWeight);

        /* Initialisation serie */

        /* Initialisation des boutons */
//		addWeightButton.setOnClickListener(onClickAddWeight);

        /* Initialisation des evenements */
        mDb = new DAOProfil(view.getContext());

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

    public String getName() {
        return getArguments().getString("name");
    }

    public int getFragmentId() {
        return getArguments().getInt("id", 0);
    }

    private DAOProfil getDB() {
        return mDb;
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

    private void refreshData() {

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
