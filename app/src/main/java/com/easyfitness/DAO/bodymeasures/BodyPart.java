package com.easyfitness.DAO.bodymeasures;

import android.content.Context;
import android.graphics.drawable.Drawable;

/* DataBase Object */
public class BodyPart {
    private int mBodyPartKey = 0;
    private String mCustomName = "";
    private String mCustomPicture = "";
    private int mDisplayOrder = 0;
    private int mType = BodyPartExtensions.TYPE_MUSCLE;
    private long id = 0;    // Notez que l'identifiant est un long

    private BodyMeasure mLastMeasure;

    public BodyPart(long id, int pBodyPartId, String pCustomName, String pCustomPicture, int pDisplayOrder, int pType) {
        super();
        this.id = id;
        mBodyPartKey = pBodyPartId;
        mDisplayOrder = pDisplayOrder;
        mCustomName = pCustomName;
        mCustomPicture = pCustomPicture;
        mType = pType;
        this.mLastMeasure = null;
    }

    public long getId() {
        return id;
    }

    /**
     * Return legacy Resource Key.
     *
     * @return
     */
    public int getBodyPartResKey() {
        return mBodyPartKey;
    }

    public String getName(Context context) {
        if (!mCustomName.isEmpty()) return mCustomName;
        else {
            if (mBodyPartKey != -1)
                return context.getResources().getString(BodyPartExtensions.getBodyStringID((int) mBodyPartKey));
            else
                return "";
        }
    }

    public Drawable getPicture(Context context) {
        if (mBodyPartKey != -1)
            if (BodyPartExtensions.getBodyLogoID((int) mBodyPartKey) != -1)
                return context.getDrawable(BodyPartExtensions.getBodyLogoID((int) mBodyPartKey));

        return null;
    }


    public String getCustomName() {
        return mCustomName;
    }

    public void setCustomName(String customName) {
        mCustomName = customName;
    }

    public String getCustomPicture() {
        return mCustomPicture;
    }

    public void setCustomPicture(String path) {
        mCustomPicture = path;
    }

    public int getDisplayOrder() {
        return mDisplayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        mDisplayOrder = displayOrder;
    }

    public int getType() {
        return mType;
    }


    /**
     * @return Resource ID of the name of the body part
     */
    public int getResourceNameID() {
        return BodyPartExtensions.getBodyStringID((int) id);
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
