package com.easyfitness;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.easyfitness.DAO.DAOFonte;
import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.DAO.Fonte;
import com.easyfitness.DAO.Machine;
import com.easyfitness.DAO.Profil;
import com.easyfitness.machines.MachineCursorAdapter;

public class MachineFragment extends Fragment {
	private String name;
	private int id;
	MainActivity mActivity = null;

	Spinner machineList = null;
	Spinner typeList = null;
	Spinner musclesList = null;
	EditText description = null;
	ImageButton renameMachineButton = null;
	ListView machineList2 = null;

	private DAOFonte mDbFonte = null;

	private DAOMachine mDbMachine = null;
	
	/**
	 * Create a new instance of DetailsFragment, initialized to
	 * show the text at 'index'.
	 */
	public static MachineFragment newInstance(String name, int id) {
		MachineFragment f = new MachineFragment();

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
		View view = inflater.inflate(R.layout.tab_machine, container, false);

		//typeList = (Spinner) view.findViewById(R.id.filterDate);
		machineList = (Spinner) view.findViewById(R.id.filterMachine);
		renameMachineButton = (ImageButton) view.findViewById(R.id.imageMachineRename);
		machineList2 = (ListView) view.findViewById(R.id.listMachine);
		//musclesList = (Spinner) view.findViewById(R.id.listFilterRecord);

		// Initialisation de l'historique
		mDbFonte = new DAOFonte(view.getContext());
		mDbMachine = new DAOMachine(view.getContext());

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		refreshData();
		
		// Initialisation des evenements
		machineList2.setOnItemSelectedListener(onItemSelectedList);
		renameMachineButton.setOnClickListener(onClickListenerFunction);
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		this.mActivity = (MainActivity)activity;
	}

	public String getName() { 
		return getArguments().getString("name");
	}

	public int getFragmentId() { 
		return getArguments().getInt("id", 0);
	}

	private OnClickListener onClickListenerFunction = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.imageMachineRename :
					renameMachine() ;
					break;
				default :
					break;
			}
		}
	};
	
	private void renameMachine() {
		AlertDialog.Builder renameBuilder = new AlertDialog.Builder(mActivity);
		
		renameBuilder.setTitle("Rename Machine " + machineList.getSelectedItem().toString()); //TODO change static string
		renameBuilder.setMessage("WARNING: if this name already exists, records will be merged with the other machine."); //TODO change static string

		// Set an EditText view to get user input
		final EditText input = new EditText(mActivity);
		input.setText(machineList.getSelectedItem().toString());
		renameBuilder.setView(input);

		renameBuilder.setPositiveButton(getResources().getText(R.string.global_ok), new DialogInterface.OnClickListener() { 
			public void onClick(DialogInterface dialog, int whichButton) {
				// Rename all the records with that machine and rename them
				List<Fonte> listRecords = mDbFonte.getAllRecordByMachines(getProfil(), machineList.getSelectedItem().toString());
				for (Fonte record : listRecords) {
					record.setMachine(input.getText().toString());
					mDbFonte.updateRecord(record);
				}	
				
				Toast.makeText(mActivity, "Done", Toast.LENGTH_SHORT).show(); //TODO change static string
				refreshData();
			}
		});

		renameBuilder.setNegativeButton(getResources().getText(R.string.global_cancel), new DialogInterface.OnClickListener() { 
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});

		renameBuilder.show();
	}

	private OnItemSelectedListener onItemSelectedList = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			//refreshData();
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {

		}
	};


	public DAOFonte getDB() {
		return mDbFonte;
	}

	private void refreshData(){
		Cursor oldCursor = null;
		List<Machine> records = null;
		
		View fragmentView = getView();
		if(fragmentView != null) {
			if (getProfil() != null) {
				// Initialisation des machines
				String[] lMachinesArray = mDbFonte.getAllMachines(getProfil());
				ArrayAdapter<String> adapterMachine = new ArrayAdapter<String>(
						getView().getContext(), android.R.layout.simple_spinner_dropdown_item,
						lMachinesArray);
				machineList.setAdapter(adapterMachine);
				mDbFonte.closeCursor();
				
				
				// Version avec table Machine
				records = mDbMachine.getAllMachines();
				if(records.isEmpty()) {
					//Toast.makeText(mActivity, "No records", Toast.LENGTH_SHORT).show();    
					machineList2.setAdapter(null);
				} else {
					if ( machineList2.getAdapter() == null ) {
						MachineCursorAdapter mTableAdapter = new MachineCursorAdapter (this.getView().getContext(), mDbMachine.getCursor(), 0);
						machineList2.setAdapter(mTableAdapter);
					} else {				
						MachineCursorAdapter mTableAdapter = ((MachineCursorAdapter)machineList2.getAdapter());
						oldCursor = mTableAdapter.swapCursor(mDbMachine.getCursor());
						if (oldCursor!=null) oldCursor.close();
					}
				}
			}
		}
	}

	@Override
	public void onHiddenChanged (boolean hidden) {
		if (!hidden) refreshData();
	}

	private Profil getProfil()	{
		return mActivity.getCurrentProfil();
	}

}
