package com.easyfitness.machines;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import com.easyfitness.DAO.DAOFonte;
import com.easyfitness.DAO.DAOMachine;
import com.easyfitness.DAO.DAOProfil;
import com.easyfitness.DAO.Fonte;
import com.easyfitness.DAO.Machine;
import com.easyfitness.DAO.Profil;
import com.easyfitness.R;
import com.easyfitness.utils.RealPathUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MachineDetailsFragment extends Fragment {
	private String name;
	private int id;

	Spinner typeList = null; /*Halteres, Machines avec Poids, Cardio*/
	EditText musclesList = null;
	EditText machineName = null;
	EditText machineDescription = null;
	ImageView machinePhoto = null;
	FloatingActionButton machineAction = null;
	
	Toolbar top_toolbar = null;
	
	String machineNameArg = null;
	long machineIdArg = 0;
	long machineProfilIdArg = 0;
	
	boolean toBeSaved = false;
	
	boolean isImageFitToScreen = false;
	
	ArrayList<Integer> selectMuscleList=new ArrayList();
	
	// http://labs.makemachine.net/2010/03/android-multi-selection-dialogs/
	protected CharSequence[] _muscles = { "Biceps", "Triceps", "Epaules", "Pectoraux", "Dorseaux", "Quadriceps", "Adducteurs", "Uranus", "Neptune", "Neptune" };
	protected boolean[] _selections =  new boolean[ _muscles.length ];
	DAOMachine mDbMachine = null;
	
	View fragmentView = null;
	
	/**
	 * Create a new instance of DetailsFragment, initialized to
	 * show the text at 'index'.
	 */
	public static MachineDetailsFragment newInstance(String name, int id) {
		MachineDetailsFragment f = new MachineDetailsFragment();

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
		View view = inflater.inflate(R.layout.machine_details, container, false);
		fragmentView = view;

		// Initialisation de l'historique
		mDbMachine = new DAOMachine(view.getContext());
		
		top_toolbar = (Toolbar) view.findViewById(R.id.actionToolbarMachine);
		((MachineDetailsActivity)getActivity()).setSupportActionBar(top_toolbar);
		((MachineDetailsActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		((MachineDetailsActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
		top_toolbar.setTitle("");
		
		machineName = (EditText) view.findViewById(R.id.machine_name);
		machineDescription = (EditText) view.findViewById(R.id.machine_description);
		musclesList = (EditText) view.findViewById(R.id.machine_muscles);
		machinePhoto = (ImageView) view.findViewById(R.id.machine_photo);
		machineAction = (FloatingActionButton) view.findViewById(R.id.actionCamera);
		
		
		buildMusclesTable();
				
		// get Arguments
		if (savedInstanceState == null) {
		    Bundle args = this.getArguments();
		    if(args == null) {
		    	machineNameArg= null;
		    } else {
		    	machineNameArg= args.getString("machineName");
		    	machineIdArg = args.getLong("machineId");
		    	machineProfilIdArg= args.getLong("machineProfilId");
		    }
		} else {
			machineNameArg= (String) savedInstanceState.getSerializable("machineName");
			machineIdArg = (long) savedInstanceState.getLong("machineId");
			machineProfilIdArg= (long) savedInstanceState.getLong("machineProfilId");
		}
		
		// set events
		musclesList.setOnClickListener(onClickMachineList);
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
		        			int scaleFactor = (int)(photoW/(machinePhoto.getWidth())); //Math.min(photoW/targetW, photoH/targetH);
		        			machinePhoto.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
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
		machineName.setText(temp.getName());
		machineDescription.setText(temp.getDescription());	
		musclesList.setText(this.getInputFromDBString(temp.getBodyParts()));
		mCurrentPhotoPath = temp.getPicture();		
		
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
	        		setPic(machinePhoto, mCurrentPhotoPath);
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
	    setHasOptionsMenu(true);
	}

	@Override
	public void onStart() {
		super.onStart();
	}
	
	boolean isCreateMuscleDialogActive = false;
	
	private boolean CreateMuscleDialog()
	{
		if ( isCreateMuscleDialogActive == true) return true; // Si la boite de dialog est deja active, alors n'en cree pas une deuxieme.
		
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
					    	if (_selections[i]==true && firstSelection == true)  { msg=_muscles[i].toString(); firstSelection = false;}
					    	else if (_selections[i]==true && firstSelection == false)  { msg=msg+";" +_muscles[i]; }					    	
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
				final long selectedID = id;
				
				String[] profilListArray = new String[2];
				profilListArray[0] = getResources().getString(R.string.camera); 
				profilListArray[1] = getResources().getString(R.string.gallery);


				AlertDialog.Builder itemActionbuilder = new AlertDialog.Builder(getActivity());
				itemActionbuilder.setTitle("").setItems(profilListArray, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						ListView lv = ((AlertDialog)dialog).getListView();
					
						switch (which) {
						// Galery
						case 1 : 
							getGaleryPict();
							break;
						// Camera
						case 0:
							dispatchTakePictureIntent();
							break;						
						default:
						}
					}				
				});
				itemActionbuilder.show();
				
				return true;
	}	
	
	private OnClickListener onClickMachineList = new View.OnClickListener() {
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

	private File createImageFile() throws IOException {
	    // Create an image file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    String imageFileName = "JPEG_" + timeStamp + "_";
	    File storageDir = this.getActivity().getExternalFilesDir(
	            Environment.DIRECTORY_DCIM);
	    File image = File.createTempFile(
	        imageFileName,  /* prefix */
	        ".jpg",         /* suffix */
	        storageDir      /* directory */
	    );

	    // Save a file: path for use with ACTION_VIEW intents
	    mCurrentPhotoPath = image.getAbsolutePath();
	    return image;
	}
	
	
	static final int REQUEST_TAKE_PHOTO = 1;
	static final int REQUEST_PICK_GALERY_PHOTO = 2;

	private void dispatchTakePictureIntent() {
	    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    // Ensure that there's a camera activity to handle the intent
	    if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
	        // Create the File where the photo should go
	        File photoFile = null;
	        try {
	            photoFile = createImageFile();
	        } catch (IOException ex) {
	            // Error occurred while creating the File
	        }
	        // Continue only if the File was successfully created
	        if (photoFile != null) {
	            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
	            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
	        }
	    }
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch(requestCode) { 
	    case REQUEST_TAKE_PHOTO:
	    	 if(resultCode == Activity.RESULT_OK){  
	 	        //Bundle extras = data.getExtras();
	 	        //Bitmap imageBitmap = (Bitmap) extras.get("data")	
	 	    		setPic(machinePhoto, mCurrentPhotoPath);//.setImageBitmap(imageBitmap);
	 	    		saveThumb(mCurrentPhotoPath);
	 				galleryAddPic(mCurrentPhotoPath);
	 				requestForSave();
	    	 }
	    	 break;
	    case REQUEST_PICK_GALERY_PHOTO:
	    	 if(resultCode == Activity.RESULT_OK){  
	    		 Uri selectedImage = data.getData();
	             InputStream imageStream;
					
					String realPath;
		            // SDK < API11
		            if (Build.VERSION.SDK_INT < 11)
		                realPath = RealPathUtil.getRealPathFromURI_BelowAPI11(this.getActivity().getBaseContext(), data.getData());
		            
		            // SDK >= 11 && SDK < 19
		            else if (Build.VERSION.SDK_INT < 19)
		                realPath = RealPathUtil.getRealPathFromURI_API11to18(this.getActivity().getBaseContext(), data.getData());
		            
		            // SDK > 19 (Android 4.4)
		            else
		                realPath = RealPathUtil.getRealPathFromURI_API19(this.getActivity().getBaseContext(), data.getData());
		                
					
					//imageStream = getActivity().getBaseContext().getContentResolver().openInputStream(selectedImage);
					//Bitmap yourSelectedImage = BitmapFactory.decodeStream(imageStream);
	 	    		setPic(machinePhoto, realPath);//.setImageBitmap(imageBitmap);
	 	    		saveThumb(realPath);
	 	    		mCurrentPhotoPath=realPath;
					requestForSave();
	    	 }
	    	 break;
		}

	}
	
	private void getGaleryPict() {
		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
		photoPickerIntent.setType("image/*");
		startActivityForResult(photoPickerIntent, REQUEST_PICK_GALERY_PHOTO);    
	}
	
	public void galleryAddPic(String file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(file);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.getActivity().sendBroadcast(mediaScanIntent);
    }
	
	private void setPic(ImageView mImageView, String pPath) {
	    try {
	    	if (pPath == null) return;
	    	File f = new File(pPath);
	    	if(!f.exists() || f.isDirectory()) return;
	    	
			// Get the dimensions of the View
			int targetW = mImageView.getWidth();
			int targetH = mImageView.getHeight();

			// Get the dimensions of the bitmap
			BitmapFactory.Options bmOptions = new BitmapFactory.Options();
			bmOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(pPath, bmOptions);
			int photoW = bmOptions.outWidth;
			int photoH = bmOptions.outHeight;

			// Determine how much to scale down the image
			int scaleFactor = photoW/targetW; //Math.min(photoW/targetW, photoH/targetH);

			// Decode the image file into a Bitmap sized to fill the View
			bmOptions.inJustDecodeBounds = false;
			bmOptions.inSampleSize = scaleFactor;
			bmOptions.inPurgeable = true;

			Bitmap bitmap = BitmapFactory.decodeFile(pPath, bmOptions);
			mImageView.setImageBitmap(bitmap);
			
			mImageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			mImageView.setAdjustViewBounds(true);
			mImageView.setMaxHeight((int)(getView().getHeight()*0.2));
			mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			
		} catch (Exception e) {
			mCurrentPhotoPath = null;
			e.printStackTrace();
		}
	}
	
	private void saveThumb(String pPath) {
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(pPath, bmOptions);
		float photoW = bmOptions.outWidth;
		float photoH = bmOptions.outHeight;

		// Determine how much to scale down the image
		float scaleFactor = photoW/photoH; //Math.min(photoW/targetW, photoH/targetH);		
		
		Bitmap ThumbImage =null;
		//if (photoW < photoH)
			ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(pPath), 128, (int)(128/scaleFactor));
		//else 
		//	ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(pPath), (int)(96/scaleFactor), 96);
		
		// extract path without the .jpg
		String pathOfOutputImage = "";
		pathOfOutputImage = pPath.substring(0, pPath.lastIndexOf('.')) + "_TH.jpg";		
		
		try
	    {
	        FileOutputStream out = new FileOutputStream(pathOfOutputImage);
	        ThumbImage.compress(Bitmap.CompressFormat.JPEG, 80, out);
	    }
	    catch (Exception e)
	    {
	        Log.e("Image", e.getMessage(), e);
	    }
	}
	
	private OnFocusChangeListener onFocusMachineList = new View.OnFocusChangeListener() {

		@Override
		public void onFocusChange(View arg0, boolean arg1) {
			if (arg1==true) {
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
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
  
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case R.id.action_machine_save:
				saveMachine();
				getActivity().findViewById(R.id.tab_machine_details).requestFocus();
				return true;
			case R.id.action_machine_delete:
				deleteMachine();
				return true;
			case android.R.id.home:
				 if (toBeSaved) {
					// Afficher une boite de dialogue pour confirmer
						AlertDialog.Builder backDialogBuilder = new AlertDialog.Builder(this.getActivity());

						backDialogBuilder.setTitle(getActivity().getResources().getText(R.string.global_confirm));
						backDialogBuilder.setMessage(getActivity().getResources().getText(R.string.backDialog_confirm_text));
						
						// Si oui, supprimer la base de donnee et refaire un Start.
						backDialogBuilder.setPositiveButton(this.getResources().getString(R.string.global_yes), new DialogInterface.OnClickListener() {
							@Override
					        public void onClick(DialogInterface dialog, int which) {
					        	saveMachine();
					        	((MachineDetailsActivity)getActivity()).onBackPressed();
					        }
					    });

						backDialogBuilder.setNegativeButton(this.getResources().getString(R.string.global_no), new DialogInterface.OnClickListener() {

					        @Override
					        public void onClick(DialogInterface dialog, int which) {
					            // Do nothing
					        	((MachineDetailsActivity)getActivity()).onBackPressed();
					        }
					    });

					    AlertDialog backDialog = backDialogBuilder.create();
					    backDialog.show();

				 } else {
				     ((MachineDetailsActivity)getActivity()).onBackPressed();					 
				 }				 

			     return true;
			default:
				
		}
		
		return true;
	}
	
	public void setMuscleText(String t) {
		musclesList.setText(t);
	}

	public String getName() { 
		return getArguments().getString("name");
	}

	public int getFragmentId() { 
		return getArguments().getInt("id", 0);
	}
	
	public MachineDetailsFragment getThis() {
		return this;
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
			        	Profil lProfil = mDbProfil.getProfil(machineProfilIdArg);
			        	String lMachineName = machineName.getText().toString();
			        	Machine m = mDbMachine.getMachine(machineNameArg);
			        	Machine m2 = mDbMachine.getMachine(lMachineName);
			        	
						List<Fonte> listRecords = lDbFonte.getAllRecordByMachines(lProfil, machineNameArg); // Recupere tous les records de la machine courante
						for (Fonte record : listRecords) {
							record.setMachine(lMachineName); // Change avec le nouveau nom
							record.setMachineKey(m2.getId()); // Met l'ID de la nouvelle machine
							lDbFonte.updateRecord(record); // Met a jour
						}	
						
						mDbMachine.deleteRecord(m); // Supprime l'ancienne machine
						
						toBeSaved = false;
						getThis().getActivity().invalidateOptionsMenu(); 		
						((MachineDetailsActivity)getActivity()).onBackPressed();
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


				this.mDbMachine.updateMachine(m);
				
	        	// Rename all the records with that machine and rename them
	        	DAOFonte lDbFonte = new DAOFonte(getThis().getView().getContext());
	        	DAOProfil mDbProfil = new DAOProfil(getView().getContext());						
	        	Profil lProfil = mDbProfil.getProfil(machineProfilIdArg);
				List<Fonte> listRecords = lDbFonte.getAllRecordByMachines(lProfil, machineNameArg); // Recupere tous les records de la machine courante
				for (Fonte record : listRecords) {
					record.setMachine(lMachineName); // Change avec le nouveau nom (DEPRECTED)
					//record.setMachineKey(m.getId()); // Change l'id de la machine dans le record // pas necessaire car l'ID ne change pas.
					lDbFonte.updateRecord(record); // met a jour
				}
				
				toBeSaved = false;
				getThis().getActivity().invalidateOptionsMenu(); 
			}
		} else {
			// Si le nom n'a pas ete modifie.
			//m.setName(this.machineName.getText().toString());
			m.setDescription(this.machineDescription.getText().toString());
			m.setBodyParts(this.getDBStringFromInput(this.musclesList.getText().toString()));
			m.setPicture(this.mCurrentPhotoPath);


			this.mDbMachine.updateMachine(m);
			
			toBeSaved = false;
			getThis().getActivity().invalidateOptionsMenu(); 
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
	        	((MachineDetailsActivity)getActivity()).onBackPressed();

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
			DAOFonte mDbFonte = new DAOFonte(this.getView().getContext());
			DAOProfil mDbProfil = new DAOProfil(this.getView().getContext());
			
			Profil lProfil = mDbProfil.getProfil(this.machineProfilIdArg);

			List<Fonte> listRecords = mDbFonte.getAllRecordByMachines(lProfil, this.machineNameArg);
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
		getThis().getActivity().invalidateOptionsMenu(); // now onCreateOptionsMenu(...) is called again
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

	
	
}

