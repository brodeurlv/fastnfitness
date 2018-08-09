package com.easyfitness.machines;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;

import com.easyfitness.R;

public class MachineDetailsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            // During initial setup, plug in the details fragment.
        	//MachineDetailsFragment details = MachineDetailsFragment.newInstance("Machine_Details", 90);
            //details.setArguments(getIntent().getExtras());
            //this.getSupportFragmentManager().beginTransaction().add(android.R.id.content, details).commit();
        }
    }
    
	@Override
	public void onBackPressed(){
			super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
	    // Inflate the menu items for use in the action bar
		inflater.inflate(R.menu.machine_details_menu, menu);
		return super.onCreateOptionsMenu(menu);  
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
}
