package com.easyfitness;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.easyfitness.DAO.Profil;
import com.easyfitness.DAO.cardio.Cardio;
import com.easyfitness.DAO.cardio.DAOCardio;

public class CardioFragment extends Fragment implements OnDateSetListener, OnTimeSetListener { 
	private String name; 
	private int id;
	MainActivity mActivity = null;

	private DAOCardio mDb = null;

	EditText dateEdit = null;
	AutoCompleteTextView exerciceEdit = null;
	EditText distanceEdit = null;
	EditText durationEdit = null;
	Button addButton = null;
	Button chronoButton = null;
	Button paramButton = null;
	ListView recordList = null; 
	String[] exerciceListArray = null;
	ImageButton exerciceListButton = null;
	ImageButton launchChronoButton = null;
	
	DatePickerDialogFragment mDateFrag = null;
	TimePickerDialogFragment mDurationFrag = null;
	

	/**
	 * Create a new instance of DetailsFragment, initialized to
	 * show the text at 'index'.
	 */
	public static CardioFragment newInstance(String name, int id) {
		CardioFragment f = new CardioFragment();

		// Supply index input as an argument.
		Bundle args = new Bundle();
		args.putString("name", name);
		args.putInt("id", id);
		f.setArguments(args);

		return f;
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		this.mActivity = (MainActivity)activity;
	}

	@Override 
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) { 

		View view = inflater.inflate(R.layout.tab_cardio, container, false); 

		dateEdit = (EditText) view.findViewById(R.id.editCardioDate);
		exerciceEdit = (AutoCompleteTextView) view.findViewById(R.id.editExercice);
		distanceEdit = (EditText) view.findViewById(R.id.editDistance);
		durationEdit = (EditText) view.findViewById(R.id.editDuration);
		recordList = (ListView) view.findViewById(R.id.listCardioRecord);
		exerciceListButton = (ImageButton) view.findViewById(R.id.buttonListExercice);
		launchChronoButton = (ImageButton) view.findViewById(R.id.buttonLaunchChrono);
		addButton = (Button) view.findViewById(R.id.addExercice);

		/* Initialisation des boutons */		
		addButton.setOnClickListener(clickAddButton);
		exerciceListButton.setOnClickListener(onClickMachineList);
		//launchChronoButton.setOnClickListener();

		dateEdit.setOnClickListener(clickDateEdit);
		dateEdit.setOnFocusChangeListener(touchRazEdit);
		distanceEdit.setOnFocusChangeListener(touchRazEdit);
		durationEdit.setOnClickListener(clickDateEdit);
		durationEdit.setOnFocusChangeListener(touchRazEdit);
		exerciceEdit.setOnFocusChangeListener(touchRazEdit);
		exerciceEdit.setOnItemClickListener(onItemClickFilterList);
		recordList.setOnItemLongClickListener(itemlongclickDeleteRecord);

		// Initialisation de la base de donnee
		mDb = new DAOCardio(view.getContext());		

		// Inflate the layout for this fragment 
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		//refreshData();
	}     

	public String getName() { 
		return getArguments().getString("name");
	}

	public int getFragmentId() { 
		return getArguments().getInt("id", 0);
	}

	private OnClickListener clickAddButton = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			/* Reagir au clic pour les boutons 1 et 2*/

			// Verifie que les infos sont completes
			if (dateEdit.getText().toString().isEmpty() ||
					exerciceEdit.getText().toString().isEmpty() || 
					(distanceEdit.getText().toString().isEmpty() &&
							durationEdit.getText().toString().isEmpty()) )	{
				return;
			}

			Date date;
			try {
				date = new SimpleDateFormat("dd/MM/yyyy").parse(dateEdit.getText().toString());
			} catch (ParseException e) {
				e.printStackTrace();
				date = new Date();
			}

			long duration;
			try {
				SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
				dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
				Date tmpDate = dateFormat.parse(durationEdit.getText().toString());
				duration = tmpDate.getTime();
			} catch (ParseException e) {
				e.printStackTrace();
				duration = 0;
			}	

			float distance;
			if ( distanceEdit.getText().toString().isEmpty() ) {
				distance = 0;
			} else {
				distance = Float.parseFloat(distanceEdit.getText().toString());
			}	

			Cardio value = new Cardio(date,
					exerciceEdit.getText().toString(), 
					distance,
					duration,
					getProfil());

			mDb.addRecord(value);

			getActivity().findViewById(R.id.drawer_layout).requestFocus();

			FillRecordTable(exerciceEdit.getText().toString());

			/* Reinitialisation des machines */
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getView().getContext(),
					android.R.layout.simple_dropdown_item_1line, mDb.getAllMachines(getProfil()));
			exerciceEdit.setAdapter(adapter);
		}
	};

	private OnClickListener onClickMachineList = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			exerciceListArray = mDb.getAllMachines(getProfil());    

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("Select a Machine")
			.setItems(exerciceListArray, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					exerciceEdit.setText(exerciceListArray[which]); // Met a jour le text
					FillRecordTable(exerciceListArray[which]); // Met a jour le tableau
					//((ViewGroup)machineEdit.getParent()).requestFocus(); //Permet de reactiver le clavier lors du focus sur l'editText
				}
			});
			//builder.create();
			builder.show();
		}
	};  

	private OnClickListener clickDateEdit = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.editCardioDate:
				showDatePicker();
				break;
			case R.id.editDuration:
				showTimePicker();			
				break;
			}
		}
	};    	

	private OnFocusChangeListener touchRazEdit = new View.OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus == true) {
				switch(v.getId()) {
				case R.id.editCardioDate:
					showDatePicker();		
					break;
				case R.id.editDuration:
					showTimePicker();			
					break;
				case R.id.editSerie:
					distanceEdit.setText("");
					break;
				case R.id.editPoids:
					durationEdit.setText("");
					break;
				case R.id.editMachine:
					////InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					//imm.showSoftInput(machineEdit, InputMethodManager.SHOW_IMPLICIT);
					exerciceEdit.setText("");
					//machineEdit.set.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
					break;
				}
			}else if (hasFocus == false) {
				switch(v.getId()) {
				case R.id.editMachine:
					FillRecordTable(exerciceEdit.getText().toString());
					break;
				}
			}
		}
	};

	private OnItemLongClickListener itemlongclickDeleteRecord = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> listView, View view,
				int position, long id) {

			// Get the cursor, positioned to the corresponding row in the result set
			//Cursor cursor = (Cursor) listView.getItemAtPosition(position);

			//Log.v("long clicked","pos: " + position + " id: " + id);

			mDb.deleteRecord(id);

			//listView.removeViewInLayout(view);

			FillRecordTable(exerciceEdit.getText().toString());

			Toast.makeText(getActivity(), getActivity().getResources().getText(R.string.removedid).toString() + " " + id, Toast.LENGTH_SHORT).show();

			return true;
		}
	};

	private OnItemClickListener onItemClickFilterList = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			FillRecordTable(exerciceEdit.getText().toString());			
		}
	};      

	public DAOCardio getDB(){
		return mDb;
	}

	public Fragment getFragment() {
		return this;
	}

	private Profil getProfil()
	{
		return ((MainActivity)mActivity).getCurrentProfil();
	}

	/*  */
	private void FillRecordTable (String pMachines) {

		List<Cardio> records = null;

		// Recupere les valeurs
		if (pMachines==null || pMachines.isEmpty()) {
			records = mDb.getAllRecordsByProfil(getProfil());   	
		} else {
			records = mDb.getAllRecordByMachines(getProfil(), pMachines);    		
		}

		if(records.isEmpty()) {
			//Toast.makeText(getActivity(), "No records", Toast.LENGTH_SHORT).show();    
			recordList.setAdapter(null);
		} else {
			// ...
			CardioCursorAdapter mTableAdapter = new CardioCursorAdapter (this.getView().getContext(), mDb.GetCursor(), 0);
			recordList.setAdapter(mTableAdapter);
		}
	}
	
	private void showDatePicker() {
		if (mDateFrag==null) {
			DatePickerDialogFragment mDateFrag = new DatePickerDialogFragment(getFragment()); //TODO eviter de recreer le DatePicker a chaque fois.
			mDateFrag.show(getFragmentManager().beginTransaction(), "dialog_date");	
		} else {
			if (!mDateFrag.isVisible())
				mDateFrag.show(getFragmentManager().beginTransaction(), "dialog_date");
		}
	}
	
	private void showTimePicker() {
		if (mDurationFrag==null) {
			TimePickerDialogFragment mDateFrag = new TimePickerDialogFragment(getFragment()); //TODO eviter de recreer le DatePicker a chaque fois.
			mDateFrag.show(getFragmentManager().beginTransaction(), "dialog_time");	
		} else {
			if (!mDurationFrag.isVisible())
				mDurationFrag.show(getFragmentManager().beginTransaction(), "dialog_time");
		}
	}

	public void onDateSet(DatePicker view, int year,
			int month, int day) {
		// Do something with the date chosen by the user
		String date = Integer.toString(day) + "/" + Integer.toString(month+1) + "/" + Integer.toString(year);
		dateEdit.setText(date);
	}


	private void refreshData(){
		View fragmentView = getView();
		if(fragmentView != null) {
			if (getProfil() != null) {
			mDb.setProfil(getProfil());

			exerciceListArray = mDb.getAllMachines(getProfil());         

			// Set Initial text
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

			Date date = new Date();
			dateEdit.setText(dateFormat.format(date));

			/* Initialisation serie */ 
			Cardio lLastRecord = mDb.getLastRecord(getProfil());
			if (lLastRecord != null ) {
				exerciceEdit.setText(lLastRecord.getExercice());
				distanceEdit.setText("");
				durationEdit.setText("");
			} else {
				// valeur par defaut
				exerciceEdit.setText(""); //@TODO recuperer une valeur par defaut. 
				distanceEdit.setText("");
				durationEdit.setText("");
			}

			// Set Table
			FillRecordTable(exerciceEdit.getText().toString());

			/* Init machines list*/
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getView().getContext(),
					android.R.layout.simple_dropdown_item_1line, exerciceListArray);
			exerciceEdit.setAdapter(adapter);
			}
		}
	}

	@Override
	public void onHiddenChanged (boolean hidden) {
		if (!hidden) refreshData();
	}

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		// Do something with the date chosen by the user
		String strMinute = "00";
		String strHour = "00";

		if (minute < 10) strMinute = "0"+Integer.toString(minute);
		else strMinute = Integer.toString(minute);
		if (hourOfDay < 10) strHour = "0"+Integer.toString(hourOfDay);
		else strHour = Integer.toString(hourOfDay);		

		String date = strHour + ":" + strMinute;
		durationEdit.setText(date);		
	}

} 

