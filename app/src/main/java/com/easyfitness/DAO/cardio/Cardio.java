package com.easyfitness.DAO.cardio;

import com.easyfitness.DAO.Profil;

import java.util.Date;

/* DataBase Object */
public class Cardio {
	  // Notez que l'identifiant est un long
	  private long id;
	  private Date mDate;
	  private String mExercice;
	  private float mDistance;
	  private long mDuration;
	  private Profil mProfil;
	  
	  public Cardio(Date pDate, String pExercice, float pDistance, long pDuration, Profil pProfil) {
	    super();
		  this.mDate = pDate;
		  this.mExercice = pExercice;
	    this.mDistance = pDistance;
	    this.mDuration = pDuration;
	    this.mProfil = pProfil;
	  }

	  public long getId() {
	    return id;
	  }

	  public void setId(long id) {
	    this.id = id;
	  }
	  
	  public Date getDate() {
		    return mDate;
		  }
	  
	  public String getExercice() {
		    return mExercice;
		  }
	  
	  public float getDistance() {
		    return mDistance;
		  }
	  
	  public long getDuration() {
		    return mDuration;
		  }
	  
	  public Profil getProfil() {
		    return mProfil;
		  }
	}