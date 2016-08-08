package com.easyfitness.DAO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/* DataBase Object */
public class Profil {
	  // Notez que l'identifiant est un long
	  private long id;
	  private Date mCreationDate;
	  private String mName;
	  private List<Fonte> mListFonte = new ArrayList<Fonte>();
	  private List<Weight> mListWeight = new ArrayList<Weight>();
	  
	  public Profil(long id, Date pDate, String pName) {
	    //super();
	    this.id = id;
	    this.mCreationDate = pDate;
	    this.mName = pName;
	  }

	  public long getId() {
		  return id;
	  }

	  public void setId(long id) {
		  this.id = id;
	  }
	  
	  public Date getDate() {
		    return mCreationDate;
	  }
	  
	  public String getName() {
		    return mName;
	  }	  
	  
	  public void setName(String pName){
		  mName = pName;
	  }
	  
	  public List<Weight> getWeightList() {
		  // call DAO get List
		  return mListWeight;
	  }
	}