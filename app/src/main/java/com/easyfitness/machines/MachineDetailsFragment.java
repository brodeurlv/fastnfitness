package com.easyfitness.machines;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.easyfitness.DAO.DAOFonte;
import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.DAO.DAOProfil;
import com.easyfitness.DAO.Fonte;
import com.easyfitness.DAO.Machine;
import com.easyfitness.DAO.Profile;
import com.easyfitness.MainActivity;
import com.easyfitness.R;
import com.easyfitness.utils.ImageUtil;
import com.easyfitness.utils.RealPathUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;



public class MachineDetailsFragment extends Fragment {
	private String name;
	private int id;

	public final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 101;

	Spinner typeList = null; /*Halteres, Machines avec Poids, Cardio*/
	EditText musclesList = null;
	EditText machineName = null;
	EditText machineDescription = null;
	ImageView machinePhoto = null;
	FloatingActionButton machineAction = null;
    ImageButton machineDelete = null;
    ImageButton machineSave = null;
	ImageButton machineFavorite = null;
    LinearLayout machinePhotoLayout = null;

	Toolbar top_toolbar = null;
	
	String machineNameArg = null;
	long machineIdArg = 0;
	long machineProfilIdArg = 0;
	boolean isFavorite = false;

	boolean toBeSaved = false;
	
	boolean isImageFitToScreen = false;


	ArrayList<Integer> selectMuscleList=new ArrayList();
	
	// http://labs.makemachine.net/2010/03/android-multi-selection-dialogs/
	protected CharSequence[] _muscles = { "Biceps", "Triceps", "Epaules", "Pectoraux", "Dorseaux", "Quadriceps", "Adducteurs", "Uranus", "Neptune", "Neptune" };
	protected boolean[] _selections =  new boolean[ _muscles.length ];
	DAOMachine mDbMachine = null;
	
	View fragmentView = null;

	ImageUtil imgUtil = null;
	
	/**
	 * Create a new instance of DetailsFragment, initialized to
	 * show the text at 'index'.
	 */
	public static MachineDetailsFragment newInstance(long machineId, long machineProfile) {
		MachineDetailsFragment f = new MachineDetailsFragment();

		// Supply index input as an argument.
		Bundle args = new Bundle();
		args.putLong("machineID", machineId);
		args.putLong("machineProfile", machineProfile);
		f.setArguments(args);

		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.machine_details, container, false);
		fragmentView = view;

		// Initialisation de l'historique
		mDbMachine = new DAOMachine(view.getContext());

        ((MainActivity)getActivity()).getActivityToolbar().setVisibility(View.GONE);
        top_toolbar = (Toolbar) view.findViewById(R.id.actionToolbarMachine);
		top_toolbar.setNavigationIcon(R.drawable.ic_back);
		top_toolbar.setNavigationOnClickListener(onClickToolbarItem);
		
		machineName = (EditText) view.findViewById(R.id.machine_name);
		machineDescription = (EditText) view.findViewById(R.id.machine_description);
		musclesList = (EditText) view.findViewById(R.id.machine_muscles);
		machinePhoto = (ImageView) view.findViewById(R.id.machine_photo);
        machineDelete = (ImageButton) view.findViewById(R.id.action_machine_delete);
        machineSave = (ImageButton) view.findViewById(R.id.action_machine_save);
		machineFavorite = (ImageButton) view.findViewById(R.id.favButton);
        machinePhotoLayout = (LinearLayout) view.findViewById(R.id.machine_photo_layout);

		machineSave.setVisibility(View.GONE); // Hide Save button by default

		machineAction = (FloatingActionButton) view.findViewById(R.id.actionCamera);


		buildMusclesTable();

        Bundle args = this.getArguments();

        machineIdArg = args.getLong("machineID");
        machineProfilIdArg = args.getLong("machineProfile");

		// set events
        machineSave.setOnClickListener(onClickToolbarItem);
        machineDelete.setOnClickListener(onClickToolbarItem);
		machineFavorite.setOnClickListener(onClickFavoriteItem);
		musclesList.setOnClickListener(onClickMusclesList);
		musclesList.setOnFocusChangeListener(onFocusMachineList);
		machinePhoto.setOnLongClickListener(onLongClickMachinePhoto);
		machinePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isImageFitToScreen) {
                    isImageFitToScreen=false;
                    machinePhoto.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    machinePhoto.setAdjustViewBounds(true);
                    machinePhoto.setMaxHeight((int)(getView().getHeight()*0.2));
                    machinePhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
                }else{
                	if (mCurrentPhotoPath != null && !mCurrentPhotoPath.isEmpty()) {
	                		File f = new File(mCurrentPhotoPath);
	                		if(f.exists()) {
	                		
		                    isImageFitToScreen=true;	        			
		                    
		                    // Get the dimensions of the bitmap
		        			BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		        			bmOptions.inJustDecodeBounds = true;
		        			BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		        			float photoW = bmOptions.outWidth;
		        			float photoH = bmOptions.outHeight;
		        			
		        			// Determine how much to scale down the image
                                int scaleFactor = (int) (photoW / (machinePhoto.getWidth())); //Math.min(photoW/targetW, photoH/targetH);machinePhoto.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
		                    machinePhoto.setAdjustViewBounds(true);
		                    machinePhoto.setMaxHeight((int)(photoH/scaleFactor));
		                    machinePhoto.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                		}
                	}
                }
            }
        });
		machineAction.setOnClickListener(onClickMachinePhoto);

		Machine temp = mDbMachine.getMachine(machineIdArg);
		machineNameArg = temp.getName();
		machineName.setText(machineNameArg);
		machineDescription.setText(temp.getDescription());	
		musclesList.setText(this.getInputFromDBString(temp.getBodyParts()));
		mCurrentPhotoPath = temp.getPicture();
        isFavorite=temp.getFavorite();
        setFavImage(isFavorite);
		
	    view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

	        @Override
	        public void onGlobalLayout() {
	            // Ensure you call it only once :
	            if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
	            	fragmentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
	            }
	            else {
	            	fragmentView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
	            }
	            // Here you can get the size :)
	            
	    		if (mCurrentPhotoPath != null && !mCurrentPhotoPath.isEmpty()) {
	        		ImageUtil.setPic(machinePhoto, mCurrentPhotoPath);
	        	} else {
	        		machinePhoto.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
	        	}
	    		machinePhoto.setMaxHeight((int)(getView().getHeight()*0.2)); // Taille initiale
	        }
	    });
		
		machineName.addTextChangedListener(watcher);
		machineDescription.addTextChangedListener(watcher);
		musclesList.addTextChangedListener(watcher);
				
		return view;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    //setHasOptionsMenu(true);
	}

	@Override
	public void onStart() {
		super.onStart();
	}
	
	boolean isCreateMuscleDialogActive = false;
	
	private boolean CreateMuscleDialog()
	{
        if (isCreateMuscleDialogActive)
            return true; // Si la boite de dialog est deja active, alors n'en cree pas une deuxieme.
		
		isCreateMuscleDialogActive = true;
		
		AlertDialog.Builder newProfilBuilder = new AlertDialog.Builder(this.getActivity());

			newProfilBuilder.setTitle(this.getResources().getString(R.string.selectMuscles));
			newProfilBuilder.setMultiChoiceItems( _muscles, _selections, new OnMultiChoiceClickListener() {
				   
				   @Override
				   public void onClick(DialogInterface arg0, int arg1, boolean arg2) {				    
					    if(arg2)
					    {
					     // If user select a item then add it in selected items
					    	selectMuscleList.add(arg1);
					    }
					    else if (selectMuscleList.contains(arg1))
					    {
					        // if the item is already selected then remove it
					    	selectMuscleList.remove(Integer.valueOf(arg1));
					    }
				   }
				  });

			// Set an EditText view to get user input 
			newProfilBuilder.setPositiveButton(getResources().getString(R.string.global_ok), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String msg="";
					int i = 0;
					boolean firstSelection = true;
					// ( selectMuscleList.size() > 0 ) { // Si on a au moins selectionne un muscle
					    for (i = 0; i < _selections.length; i++) {					     
					    	if (_selections[i] && firstSelection)  { msg=_muscles[i].toString(); firstSelection = false;}
					    	else if (_selections[i]&& !firstSelection)  { msg=msg+";" +_muscles[i]; }
					    }
					//}
				    setMuscleText(msg);
				    isCreateMuscleDialogActive = false;
				}
			});

			newProfilBuilder.setNegativeButton(getResources().getString(R.string.global_cancel), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					isCreateMuscleDialogActive = false;
				}
			});

			newProfilBuilder.show();
			
			return true;
	}
	
	// Get the cursor, positioned to the corresponding row in the result set
	//Cursor cursor = (Cursor) listView.getItemAtPosition(position);

	private boolean CreatePhotoSourceDialog() {
		if (imgUtil==null)
			imgUtil = new ImageUtil();

		return imgUtil.CreatePhotoSourceDialog(this);
	}

	private OnClickListener onClickMusclesList = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			CreateMuscleDialog();
		}
	};  
	
	private OnLongClickListener onLongClickMachinePhoto = new View.OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			return CreatePhotoSourceDialog();
		}
	};

	private OnClickListener onClickMachinePhoto = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			CreatePhotoSourceDialog();
		}
	};


	String mCurrentPhotoPath = null;

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
			case ImageUtil.REQUEST_TAKE_PHOTO:
				if (resultCode == Activity.RESULT_OK) {
					mCurrentPhotoPath = imgUtil.getFilePath();
					imgUtil.setPic(machinePhoto, mCurrentPhotoPath);
					imgUtil.saveThumb(mCurrentPhotoPath);
					imgUtil.galleryAddPic(this, mCurrentPhotoPath);
					requestForSave();
				}
				break;
			case ImageUtil.REQUEST_PICK_GALERY_PHOTO:
				if (resultCode == Activity.RESULT_OK) {
					String realPath;
					realPath = RealPathUtil.getRealPath(this.getContext(), data.getData());

					imgUtil.setPic(machinePhoto, realPath);
					imgUtil.saveThumb(realPath);
					mCurrentPhotoPath = realPath;
					requestForSave();
				}
				break;
		}
	}

	private OnFocusChangeListener onFocusMachineList = new View.OnFocusChangeListener() {

		@Override
		public void onFocusChange(View arg0, boolean arg1) {
            if (arg1) {
				CreateMuscleDialog();
			}
		}
	};  
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		menu.clear();
		
	    // Inflate the menu items for use in the action bar
		inflater.inflate(R.menu.machine_details_menu, menu);
		
		MenuItem item = menu.findItem(R.id.action_machine_save);
		item.setVisible(toBeSaved);			
		
		super.onCreateOptionsMenu(menu, inflater);  
	}

    private OnClickListener onClickToolbarItem = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Handle presses on the action bar items
            switch (v.getId()) {
                case R.id.action_machine_save:
                    saveMachine();
                    getActivity().findViewById(R.id.tab_machine_details).requestFocus();
                    break;
                case R.id.action_machine_delete:
                    deleteMachine();
                    break;
                default:
                	saveMachineDialog();
            }
 	       }
	};

	private OnClickListener onClickFavoriteItem = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
                isFavorite=!isFavorite;
                setFavImage(isFavorite);
                requestForSave();
        }
	};

	public void setMuscleText(String t) {
		musclesList.setText(t);
	}

	public MachineDetailsFragment getThis() {
		return this;
	}

	public void saveMachineDialog() {
		if (toBeSaved) {
			// Afficher une boite de dialogue pour confirmer
			AlertDialog.Builder backDialogBuilder = new AlertDialog.Builder(getActivity());

			backDialogBuilder.setTitle(getActivity().getResources().getText(R.string.global_confirm));
			backDialogBuilder.setMessage(getActivity().getResources().getText(R.string.backDialog_confirm_text));

			// Si oui, supprimer la base de donnee et refaire un Start.
			backDialogBuilder.setPositiveButton(getResources().getString(R.string.global_yes), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					saveMachine();
					getActivity().onBackPressed();
				}
			});

			backDialogBuilder.setNegativeButton(getResources().getString(R.string.global_no), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					getActivity().onBackPressed();
				}
			});

			AlertDialog backDialog = backDialogBuilder.create();
			backDialog.show();

		} else {
			getActivity().onBackPressed();
		}
	}

	private void saveMachine() {		
		Machine m = this.mDbMachine.getMachine(machineIdArg); // machine d'origine
		String lMachineName = this.machineName.getText().toString(); // Potentiel nouveau nom dans le EditText
		
		// Si le nom est different du nom actuel
		if (!machineNameArg.equals(lMachineName))
		{ 
			Machine m2 = this.mDbMachine.getMachine(lMachineName);
		
			// Si une machine existe avec le meme nom => Merge
			if (m2!=null && m2.getId()!=m.getId())	
			{
				
				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this.getActivity());
	
				dialogBuilder.setTitle(getActivity().getResources().getText(R.string.global_warning));
				dialogBuilder.setMessage(getActivity().getResources().getText(R.string.renameMachine_warning_text));
				// Si oui, supprimer la base de donnee et refaire un Start.
				dialogBuilder.setPositiveButton(getResources().getText(R.string.global_yes), new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) {	
			        	// Rename all the records with that machine and rename them
			        	DAOFonte lDbFonte = new DAOFonte(getThis().getView().getContext());
			        	DAOProfil mDbProfil = new DAOProfil(getView().getContext());						
			        	Profile lProfile = mDbProfil.getProfil(machineProfilIdArg);
			        	String lMachineName = machineName.getText().toString();
			        	Machine m = mDbMachine.getMachine(machineNameArg);
			        	Machine m2 = mDbMachine.getMachine(lMachineName);

                        List<Fonte> listRecords = lDbFonte.getAllRecordByMachinesArray(lProfile, machineNameArg); // Recupere tous les records de la machine courante
						for (Fonte record : listRecords) {
							record.setMachine(lMachineName); // Change avec le nouveau nom
							record.setMachineKey(m2.getId()); // Met l'ID de la nouvelle machine
							lDbFonte.updateRecord(record); // Met a jour
						}	
						
						mDbMachine.deleteRecord(m); // Supprime l'ancienne machine
						
						toBeSaved = false;
						//getThis().getActivity().invalidateOptionsMenu();
						machineSave.setVisibility(View.GONE);
                        getActivity().onBackPressed();
			        }	
			    });
				
				dialogBuilder.setNegativeButton(getResources().getText(R.string.global_no), new DialogInterface.OnClickListener() { 
			        public void onClick(DialogInterface dialog, int which) {	
			            // Do nothing but close the dialog
			            dialog.dismiss();
			        }	
			    });
				
				AlertDialog dialog = dialogBuilder.create();
			    dialog.show();
			} else {
				// Si on ne donne pas un nom deja existant
				m.setName(this.machineName.getText().toString());
				m.setDescription(this.machineDescription.getText().toString());
				m.setBodyParts(this.getDBStringFromInput(this.musclesList.getText().toString()));
				m.setPicture(this.mCurrentPhotoPath);
                m.setFavorite(isFavorite);

				this.mDbMachine.updateMachine(m);
				
	        	// Rename all the records with that machine and rename them
                DAOFonte lDbFonte = new DAOFonte(getContext());
                DAOProfil mDbProfil = new DAOProfil(getContext());
	        	Profile lProfile = mDbProfil.getProfil(machineProfilIdArg);
                List<Fonte> listRecords = lDbFonte.getAllRecordByMachinesArray(lProfile, machineNameArg); // Recupere tous les records de la machine courante
				for (Fonte record : listRecords) {
					record.setMachine(lMachineName); // Change avec le nouveau nom (DEPRECTED)
					//record.setMachineKey(m.getId()); // Change l'id de la machine dans le record // pas necessaire car l'ID ne change pas.
					lDbFonte.updateRecord(record); // met a jour
				}

				machineSave.setVisibility(View.GONE);
				toBeSaved = false;
				//getThis().getActivity().invalidateOptionsMenu();
			}
		} else {
			// Si le nom n'a pas ete modifie.
			//m.setName(this.machineName.getText().toString());
			m.setDescription(this.machineDescription.getText().toString());
			m.setBodyParts(this.getDBStringFromInput(this.musclesList.getText().toString()));
			m.setPicture(this.mCurrentPhotoPath);
            m.setFavorite(isFavorite);

			this.mDbMachine.updateMachine(m);

            machineSave.setVisibility(View.GONE);
            toBeSaved = false;
			//getThis().getActivity().invalidateOptionsMenu();
		}
	}
	
	private void deleteMachine() {
		// afficher un message d'alerte
		AlertDialog.Builder deleteDialogBuilder = new AlertDialog.Builder(this.getActivity());

		deleteDialogBuilder.setTitle(getActivity().getResources().getText(R.string.global_confirm));
		deleteDialogBuilder.setMessage(getActivity().getResources().getText(R.string.deleteMachine_confirm_text));
		
		// Si oui, supprimer la base de donnee et refaire un Start.
		deleteDialogBuilder.setPositiveButton(this.getResources().getString(R.string.global_yes), new DialogInterface.OnClickListener() { 
			@Override
	        public void onClick(DialogInterface dialog, int which) {
				// Suppress the machine
				Machine m = mDbMachine.getMachine(machineIdArg);
				mDbMachine.deleteRecord(m);
	        	// Suppress the associated Fontes records
	        	deleteRecordsAssociatedToMachine();
	        	getActivity().onBackPressed();

	        }
	    });

		deleteDialogBuilder.setNegativeButton(this.getResources().getString(R.string.global_no), new DialogInterface.OnClickListener() {

	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	            // Do nothing
	        }
	    });

	    AlertDialog deleteDialog = deleteDialogBuilder.create();
	    deleteDialog.show();		
	}
	
	private void deleteRecordsAssociatedToMachine() {
			DAOFonte mDbFonte = new DAOFonte(getContext());
			DAOProfil mDbProfil = new DAOProfil(getContext());
			
			Profile lProfile = mDbProfil.getProfil(this.machineProfilIdArg);

        List<Fonte> listRecords = mDbFonte.getAllRecordByMachinesArray(lProfile, this.machineNameArg);
			for (Fonte record : listRecords) {
				mDbFonte.deleteRecord(record);
			}						
	}
	
	public TextWatcher watcher = new TextWatcher() {
		   @Override    
		   public void onTextChanged(CharSequence s, int start,
		     int before, int count) {
			   requestForSave();
		   }

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {			
			}
	
			@Override
			public void afterTextChanged(Editable s) {
			}
	};
	
	private void requestForSave() {
		toBeSaved = true; // setting state
		machineSave.setVisibility(View.VISIBLE);
 	}
	
	
	public void buildMusclesTable()	{
		_muscles[0]= getActivity().getResources().getString(R.string.biceps);
		_muscles[1]= getActivity().getResources().getString(R.string.triceps);
		_muscles[2]= getActivity().getResources().getString(R.string.pectoraux);
		_muscles[3]= getActivity().getResources().getString(R.string.dorseaux);
		_muscles[4]= getActivity().getResources().getString(R.string.abdominaux);
		_muscles[5]= getActivity().getResources().getString(R.string.quadriceps);
		_muscles[6]= getActivity().getResources().getString(R.string.ischio_jambiers);
		_muscles[7]= getActivity().getResources().getString(R.string.adducteurs);
		_muscles[8]= getActivity().getResources().getString(R.string.mollets);
		_muscles[9]= getActivity().getResources().getString(R.string.deltoids);
	}
	
	/*
	 * @return the name of the Muscle depending on the language
	 */
	public String getMuscleNameFromId(int id){
		String ret = "";
		try {
			ret = _muscles[id].toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	/*
	 * @return the name of the Muscle depending on the language
	 */
	public int getMuscleIdFromName(String pName){
		for (int i=0; i<_muscles.length; i++) {
			if (_muscles[i].toString().equals(pName)) return i;
		}
		return -1;
	}
	
	/*
	 * @return the name of the Muscle depending on the language
	 */
	public String getDBStringFromInput(String pInput){
		String[] data = pInput.split(";");
		String output = "";
		
		if (pInput.isEmpty()) return "";
		
		int i=0;
		if (data.length > 0 ) {
			output = String.valueOf(getMuscleIdFromName(data[i])); 
			for (i=1; i < data.length; i++) {
				output = output  + ";"+ getMuscleIdFromName(data[i]);
			}
		}
		
		return output;
	}
	
	
	/*
	 * @return the name of the Muscle depending on the language
	 */
	public String getInputFromDBString(String pDBString){
		String[] data = pDBString.split(";");
		String output = "";
		
		int i=0;

		try {
			if (data.length > 0 ) {
				if (data[0].isEmpty()) return "";
				
				if ( ! data[i].equals("-1") ) {
					output = getMuscleNameFromId(Integer.valueOf(data[i]));
					_selections[Integer.valueOf(data[i])]=true;
					for (i=1; i < data.length; i++) {
						if ( ! data[i].equals("-1") ) {
							output = output + ";" + getMuscleNameFromId(Integer.valueOf(data[i]));
							_selections[Integer.valueOf(data[i])]=true;
						}
					}			
				}
			}
		} catch (NumberFormatException e) {
			output="";
			e.printStackTrace();
		}
		
		return output;
	}
	
	public int getCameraPhotoOrientation(Context context, Uri imageUri, String imagePath){
	    int rotate = 0;
	    try {
	        context.getContentResolver().notifyChange(imageUri, null);
	        File imageFile = new File(imagePath);

	        ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
	        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

	        switch (orientation) {
	        case ExifInterface.ORIENTATION_ROTATE_270:
	            rotate = 270;
	            break;
	        case ExifInterface.ORIENTATION_ROTATE_180:
	            rotate = 180;
	            break;
	        case ExifInterface.ORIENTATION_ROTATE_90:
	            rotate = 90;
	            break;
	        }

	        //Log.i("RotateImage", "Exif orientation: " + orientation);
	        //Log.i("RotateImage", "Rotate value: " + rotate);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return rotate;
	}

	private void setFavImage(boolean fav)
    {
        if(fav) {
            machineFavorite.setImageDrawable(getActivity().getResources().getDrawable(android.R.drawable.btn_star_big_on));
        } else {
            machineFavorite.setImageDrawable(getActivity().getResources().getDrawable(android.R.drawable.btn_star_big_off));
        }
    }
	
}

