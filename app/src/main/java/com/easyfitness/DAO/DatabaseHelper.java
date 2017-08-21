package com.easyfitness.DAO;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.easyfitness.DAO.bodymeasures.DAOBodyMeasure;
import com.easyfitness.DAO.cardio.DAOCardio;

import java.io.File;

public class DatabaseHelper extends SQLiteOpenHelper {

	private static DatabaseHelper sInstance;

	public static final int DATABASE_VERSION = 9;
	public static final String OLD09_DATABASE_NAME = "easyfitness";
	public static final String DATABASE_NAME = "easyfitness.db";
	private Context mContext = null;	
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DAOFonte.TABLE_CREATE);
		db.execSQL(DAOProfil.TABLE_CREATE);
		db.execSQL(DAOWeight.TABLE_CREATE);
		db.execSQL(DAOCardio.TABLE_CREATE);
		db.execSQL(DAOMachine.TABLE_CREATE);
		db.execSQL(DAOBodyMeasure.TABLE_CREATE);
		//onUpgrade(db, 0, DATABASE_VERSION);	
	}

	@Override
    public void onUpgrade(
            final SQLiteDatabase db, final int oldVersion,
            final int newVersion)
        {
            int upgradeTo = oldVersion + 1;
            while (upgradeTo <= newVersion)
            {
                switch (upgradeTo)
                {
                	case 1:
                		db.execSQL(DAOCardio.TABLE_CREATE);
                    break;
                    case 2:
                		db.execSQL(DAOCardio.TABLE_CREATE);
                        break;
                    case 3: 
                    	db.execSQL(DAOCardio.TABLE_DROP);
                    	db.execSQL(DAOCardio.TABLE_CREATE);
                    	break;
                    case 4: // Easyfitness 0.7
                    	db.execSQL("ALTER TABLE "+ DAOFonte.TABLE_NAME + " ADD COLUMN " + DAOFonte.NOTES + " TEXT");
                    	db.execSQL("ALTER TABLE "+ DAOFonte.TABLE_NAME + " ADD COLUMN " + DAOFonte.UNIT + " INTEGER DEFAULT 0");
                    	break;
                    case 5:
                    	db.execSQL(DAOMachine.TABLE_CREATE_5);
                    	db.execSQL("ALTER TABLE "+ DAOFonte.TABLE_NAME + " ADD COLUMN " + DAOFonte.MACHINE_KEY + " INTEGER");
                    	break;
                    case 6: // Easyfitness 0.8
                    	if (!isFieldExist(db, DAOMachine.TABLE_NAME, DAOMachine.BODYPARTS)) // Easyfitness 0.9 : Probleme d'upgrade
                    		db.execSQL("ALTER TABLE "+ DAOMachine.TABLE_NAME + " ADD COLUMN " + DAOMachine.BODYPARTS + " TEXT");
                    	break;
                    case 7: // Easyfitness 0.10
                    		db.execSQL("ALTER TABLE "+ DAOMachine.TABLE_NAME + " ADD COLUMN " + DAOMachine.PICTURE + " TEXT");
                    	break;
					case 8: // Easyfitness 0.12
						db.execSQL("ALTER TABLE " + DAOFonte.TABLE_NAME + " ADD COLUMN " + DAOFonte.TIME + " TEXT");
						break;
					case 9: // Easyfitness 0.13
						db.execSQL(DAOBodyMeasure.TABLE_CREATE);
						break;
                }
                upgradeTo++;
            }
        }
	
	
	@Override
    public void onDowngrade(
            final SQLiteDatabase db, final int oldVersion,
            final int newVersion)
        {
            int upgradeTo = oldVersion - 1;
            while (upgradeTo >= newVersion)
            {
                switch (upgradeTo)
                {
                    case 2:
                		// Ne fonctionne pas pour ces versions
                        break;
                    case 3: 
                    	// Ne fonctionne pas pour ces versions
                    	//db.execSQL("ALTER TABLE "+ DAOFonte.TABLE_NAME + " DROP COLUMN " + DAOFonte.NOTES);
                    	//db.execSQL("ALTER TABLE "+ DAOFonte.TABLE_NAME + " DROP COLUMN " + DAOFonte.UNIT);
                    	break;
                    case 4:
                    	//db.execSQL(DAOMachine.TABLE_DROP);
                    	//db.execSQL("ALTER TABLE "+ DAOFonte.TABLE_NAME + " DROP COLUMN " + DAOFonte.MACHINE_KEY );
                    	break;
                    case 5:
                    	//db.execSQL("ALTER TABLE "+ DAOMachine.TABLE_NAME + " DROP COLUMN " + DAOMachine.BODYPARTS );
                    	break;
                }
                upgradeTo--;
            }
        }

	public static DatabaseHelper getInstance(Context context) {

		// Use the application context, which will ensure that you
		// don't accidentally leak an Activity's context.
		// See this article for more information: http://bit.ly/6LRzfx
		if (sInstance == null) {
			sInstance = new DatabaseHelper(context.getApplicationContext());
		}
		return sInstance;
	}
	
	// This method will return if your table exist a field or not
	public boolean isFieldExist(SQLiteDatabase db, String tableName, String fieldName)
	{
	    boolean isExist = true;
	    Cursor res;
	    
		try {
			res = db.rawQuery("SELECT " + fieldName + " FROM " + tableName,null);
		} catch (SQLiteException e) {
			isExist = false;
		}
	   
	    return isExist;
	}

	public static void renameOldDatabase(Activity activity)
	{
	    File oldDatabaseFile = activity.getDatabasePath(OLD09_DATABASE_NAME);
	    if ( oldDatabaseFile.exists() ) {
		    File newDatabaseFile = new File(oldDatabaseFile.getParentFile(), DATABASE_NAME);	
		    oldDatabaseFile.renameTo(newDatabaseFile);
	    }
	}
}
