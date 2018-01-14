package com.easyfitness.DAO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/* DataBase Object */
public class Profile {
	  // Notez que l'identifiant est un long
	  private long id;
	  private Date mCreationDate;
	private Date mBirthday;
	  private String mName;
	private int mSize;
	  private List<Fonte> mListFonte = new ArrayList<Fonte>();
	  private List<Weight> mListWeight = new ArrayList<Weight>();


	public Profile(long mId, Date mDate, String pName, int pSize, Date pBirthday) {
		//super();
		this.id = mId;
		this.mCreationDate = mDate;
		this.mBirthday = pBirthday;
		this.mSize = pSize;
		this.mName = pName;
	}

	  public Profile(String pName, int pSize, Date pBirthday) {
	    //super();
		  this.mBirthday = pBirthday;
		  this.mSize = pSize;
	    this.mName = pName;
	  }

	  public long getId() {
		  return id;
	  }

	  public void setId(long id) {
		  this.id = id;
	  }
	  
	  public Date getCreationDate() {
		    return mCreationDate;
	  }

	public Date getBirthday() {
		return mBirthday;
	}

	public void setBirthday(Date mBirthday) {
		this.mBirthday = mBirthday;
	}

	public int getSize() {
		return mSize;
	}

	public void setSize(int mSize) {
		this.mSize = mSize;
	}

	public String getName() {
		    return mName;
	  }	  
	  
	  public void setName(String pName){
		  this.mName = pName;
	  }
	  
	  public List<Weight> getWeightList() {
		  // call DAO get List
		  return mListWeight;
	  }
	}