package com.easyfitness.DAO;

/* DataBase Object */
public class Machine {
    // Notez que l'identifiant est un long
    private long id;
    private String mName;
    private int mType; // Cardio or Fonte
    private String mPicture = null;
    private String mDescription;
    private String mBodyParts;
    private Boolean mFavorite;

    public Machine(String pName, String pDescription, int pType, String pBodyParts, String pPicture, Boolean pFavorite) {
        super();
        this.mName = pName;
        this.mDescription = pDescription;
        this.mType = pType;
        this.mPicture = pPicture;
        this.mBodyParts = pBodyParts;
        this.mFavorite = pFavorite;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String desc) {
        this.mDescription = desc;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public String getPicture() {
        return mPicture;
    }

    public void setPicture(String picture) {
        this.mPicture = picture;
    }

    public String getBodyParts() {
        if (mBodyParts == null) return "";
        else return mBodyParts;
    }

    public void setBodyParts(String bodyParts) {
        mBodyParts = bodyParts;
    }

    public Boolean getFavorite() {
        return mFavorite;
    }

    public void setFavorite(Boolean favorite) {
        mFavorite = favorite;
    }

    @Override
    public String toString() {
        return getName();
    }

}
