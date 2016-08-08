package com.easyfitness.fonte;

import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.easyfitness.MainActivity;
import com.easyfitness.R;
import com.easyfitness.DAO.DAOFonte;
import com.easyfitness.DAO.Fonte;
import com.easyfitness.DAO.Profil;

public class FonteHistoryFragment extends Fragment {
	private String name;
	private int id;

	Spinner dateList = null;
	Spinner machineList = null;

	Button paramButton = null;
	ListView filterList = null;
	
	AppCompatActivity mActivity = null;


    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    public static FonteHistoryFragment newInstance(String name, int id) {
    	FonteHistoryFragment f = new FonteHistoryFragment();

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
		View view = inflater.inflate(R.layout.tab_history, container, false);
		
		dateList = (Spinner) view.findViewById(R.id.filterDate);
		machineList = (Spinner) view.findViewById(R.id.filterMachine);
		filterList = (ListView) view.findViewById(R.id.listFilterRecord);
		
		// Initialisation des evenements
		filterList.setOnItemLongClickListener(itemlongclickDeleteRecord);
		machineList.setOnItemSelectedListener(onItemSelectedList);
		dateList.setOnItemSelectedListener(onItemSelectedList);

		// Initialisation de l'historique
		mDb = new DAOFonte(view.getContext());
		
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		
		refreshData();
	}
	
	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		this.mActivity = (AppCompatActivity) activity;
	}

	private static String[] prepend(String[] a, String el) {
		String[] c = new String[a.length + 1];
		c[0] = el;
		System.arraycopy(a, 0, c, 1, a.length);
		return c;
	}

	public String getName() { 
		return getArguments().getString("name");
	}

	public int getFragmentId() { 
		return getArguments().getInt("id", 0);
	}

	private OnItemLongClickListener itemlongclickDeleteRecord = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> listView, View view,
				int position, long id) {

			mDb.deleteRecord(id);

			// listView.removeViewInLayout(view);

			FillRecordTable(machineList.getSelectedItem().toString(), dateList
					.getSelectedItem().toString());

			Toast.makeText(mActivity, getResources().getText(R.string.removedid) + " " + id, Toast.LENGTH_SHORT) 
			.show();

			return true;
		}
	};

	private OnItemSelectedListener onItemSelectedList = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			FillRecordTable(machineList.getSelectedItem().toString(), dateList
					.getSelectedItem().toString());
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {

		}
	};

	private DAOFonte mDb = null;

	public DAOFonte getDB() {
		return mDb;
	}

	/*  */
	private void FillRecordTable(String pMachine, String pDate) {
		List<Fonte> records = null;
		Cursor oldCursor = null;

		// Recupere les valeurs
		records = mDb.getFilteredRecords(getProfil(), pMachine, pDate);  		

		if(records.isEmpty()) {
			//Toast.makeText(mActivity, "No records", Toast.LENGTH_SHORT).show();    
			filterList.setAdapter(null);
		} else {
			// ...
			if ( filterList.getAdapter() == null ) {
				FonteCursorAdapter mTableAdapter = new FonteCursorAdapter (this.getView().getContext(), mDb.GetCursor(), 0);
				filterList.setAdapter(mTableAdapter);
			} else {
				oldCursor = ((FonteCursorAdapter)filterList.getAdapter()).swapCursor(mDb.GetCursor());
				if (oldCursor!=null)
					oldCursor.close();
			}
		}
	}

	private void refreshData(){
		View fragmentView = getView();
		if(fragmentView != null) {
			if (getProfil() != null) {
				
					// Initialisation des machines				
					String[] lMachinesArray = mDb.getAllMachines(getProfil());
					lMachinesArray = prepend(lMachinesArray, getView().getResources().getText(R.string.all).toString());
					ArrayAdapter<String> adapterMachine = new ArrayAdapter<String>(
							getView().getContext(), android.R.layout.simple_spinner_item, //simple_spinner_dropdown_item
							lMachinesArray);
					adapterMachine.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					machineList.setAdapter(adapterMachine);
					mDb.closeCursor();
		
					// Initialisation de la date
					String[] lDateArray = mDb.getAllDatesAsString(getProfil());
					lDateArray = prepend(lDateArray, getView().getResources().getText(R.string.all).toString());
					ArrayAdapter<String> adapterDate = new ArrayAdapter<String>(
							getView().getContext(), android.R.layout.simple_spinner_item,
							lDateArray);
					adapterDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					dateList.setAdapter(adapterDate);
					mDb.closeCursor();				

					if ( adapterMachine.getPosition(this.getFontesMachine()) != -1 ) {
						machineList.setSelection(adapterMachine.getPosition(this.getFontesMachine()));
					} 								

					FillRecordTable(machineList.getSelectedItem().toString(), dateList
							.getSelectedItem().toString());
				}
		}
	}

	private Profil getProfil()	{
		return ((MainActivity)mActivity).getCurrentProfil();
	}
	
	private String getFontesMachine()	{
		String temp = null;
		try {
			FontesPagerFragment temp2 = (FontesPagerFragment)(mActivity.getSupportFragmentManager().findFragmentByTag(MainActivity.FONTESPAGER));
			FontesFragment temp3 = ((FontesFragment)(temp2.getViewPagerAdapter().getItem(0)));
			temp = temp3.getMachine();
		}catch (NullPointerException e) {
			temp = "";
		}
		return temp;
	}
	
	@Override
	public void onHiddenChanged (boolean hidden) {
		if (!hidden) { 
			refreshData();
		}
	}
}
