package com.easyfitness.DAO.macros;

import com.easyfitness.enums.DistanceUnit;
import com.easyfitness.enums.ExerciseType;
import com.easyfitness.enums.FoodQuantityUnit;
import com.easyfitness.enums.ProgramRecordStatus;
import com.easyfitness.enums.RecordType;
import com.easyfitness.enums.WeightUnit;

import java.util.Date;

/* DataBase Object */
public class FoodRecord {
    private long mId;

    private long mProfileId;

    private Date mDate;

    private String mNote;

    private float mCalories;
    private float mFats;
    private float mProtein;
    private float mCarbs;
    private float mQuantity;
    private FoodQuantityUnit mQuantityUnit;
    private String mFoodName;


    /**
     * Record from Free Workout
     */
    public FoodRecord(Date date, String foodName, long foodId, long profileId, float quantity, FoodQuantityUnit quantityUnit,
                      float calories, float carbs, float protein, float fats) {

        mDate = date;
        mFoodName = foodName;
        mId = foodId;
        mProfileId = profileId;
        mQuantity = quantity;
        mQuantityUnit = quantityUnit;
        mCalories = calories;
        mCarbs = carbs;
        mProtein = protein;
        mFats = fats;
    }


    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public long getProfileId() {
        return mProfileId;
    }

    public void setProfileId(long profileId) {
        this.mProfileId = profileId;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        this.mDate = date;
    }

    public String getNote() {
        return mNote;
    }

    public void setNote(String note) {
        this.mNote = note;
    }

    public float getCalories() {
        return mCalories;
    }

    public void setCalories(float calories) {
        this.mCalories = calories;
    }

    public float getFats() {
        return mFats;
    }

    public void setFats(float fats) {
        this.mFats = fats;
    }

    public float getProtein() {
        return mProtein;
    }

    public void setProtein(float protein) {
        this.mProtein = protein;
    }

    public float getCarbs() {
        return mCarbs;
    }

    public void setCarbs(float carbs) {
        this.mCarbs = carbs;
    }

    public float getQuantity() {
        return mQuantity;
    }

    public void setQuantity(float quantity) {
        this.mQuantity = quantity;
    }

    public FoodQuantityUnit getQuantityUnit() {
        return mQuantityUnit;
    }

    public void setQuantityUnit(FoodQuantityUnit quantityUnit) {
        this.mQuantityUnit = quantityUnit;
    }

    public String getFoodName() {
        return mFoodName;
    }

    public void setFoodName(String foodName) {
        this.mFoodName = foodName;
    }
}
