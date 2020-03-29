package com.easyfitness.DAO.bodymeasures;

/* DataBase Object */
public class BodyPart {
    private long mNameRes = 0;
    private long mPictureRes = 0;
    private String mCustomName ="";
    private String mCustomPicture ="";
    private int mDisplayOrder = 0;
    private int mType = BodyPartExtensions.TYPE_MUSCLE;
    private long id=0;    // Notez que l'identifiant est un long

    private BodyMeasure mLastMeasure;

    public BodyPart(long id, long pNameRes, long pPictureRes, String pCustomName, String pCustomPicture, int pDisplayOrder, int pType) {
        super();
        this.id = id;
        mNameRes = pNameRes;
        mPictureRes = pPictureRes;
        mDisplayOrder = pDisplayOrder;
        mCustomName = pCustomName;
        mCustomPicture = pCustomPicture;
        mType = pType;
        this.mLastMeasure = null;
    }

    /*public BodyPart(long id, BodyMeasure lastMeasure) {
        super();
        this.id = id;
        this.mLastMeasure = lastMeasure;
    }*/


    public long getId() {
        return id;
    }

    public long getNameRes() {return mNameRes;}
    public long getPictureRes() {return mPictureRes;}
    public String getCustomName() {return mCustomName;}
    public String getCustomPicture() {return mCustomPicture;}
    public int getDisplayOrder() {return mDisplayOrder;}
    public int getType() {return mType;}

    /**
     * @return Resource ID of the name of the body part
     */
    public int getResourceNameID() {
        return BodyPartExtensions.getBodyResourceID((int) id);
    }

    /**
     * @return Resource ID of the logo
     */
    public int getResourceLogoID() {
        return BodyPartExtensions.getBodyLogoID((int) id);
    }


    public BodyMeasure getLastMeasure() {
        return this.mLastMeasure;
    }
    public void setLastMeasure(BodyMeasure lastmeasure) {
        this.mLastMeasure = lastmeasure;
    }
}
