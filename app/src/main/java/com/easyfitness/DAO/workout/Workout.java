package com.easyfitness.DAO.workout;

import androidx.annotation.NonNull;

/* DataBase Object */
public class Workout {

    private long id;
    private String mDescription = "";
    private String mName = "";

    public Workout(long mId, String pName, String pDesription) {
        this.id = mId;
        this.mDescription = pDesription;
        this.mName = pName;
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

    public void setName(String pName) {
        this.mName = pName;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String pDescription) {
        this.mDescription = pDescription;
    }

    @NonNull
    @Override
    public String toString() {
        return getName();
    }
}
