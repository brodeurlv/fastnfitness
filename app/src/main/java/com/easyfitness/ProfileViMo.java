package com.easyfitness;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.easyfitness.DAO.Profile;
import com.easyfitness.enums.SizeUnit;
import com.easyfitness.enums.Unit;

import java.util.Date;

public class ProfileViMo extends ViewModel {
    private final MutableLiveData<Date> mBirthday = new MutableLiveData<>();
    public LiveData<Date> getBirthday() {
        return mBirthday;
    }
    public void setBirthday(Date pBirthday) {
        mBirthday.setValue(pBirthday);
    }

    private final MutableLiveData<Float> mSize = new MutableLiveData<>();
    public LiveData<Float> getSize() {
        return mSize;
    }
    public void setSize(float pSize) {
        mSize.setValue(pSize);
    }

    private final MutableLiveData<Unit> mSizeUnit = new MutableLiveData<>();
    public LiveData<Unit> getSizeUnit() {
        return mSizeUnit;
    }
    public void setSizeUnit(Unit pSizeUnit) {  mSizeUnit.setValue(pSizeUnit); }

    private final MutableLiveData<String> mName = new MutableLiveData<>();
    public LiveData<String> getName() {
        return mName;
    }
    public void setName(String pName) {  mName.setValue(pName); }

    private final MutableLiveData<String> mPhoto = new MutableLiveData<>();
    public LiveData<String> getPhoto() {
        return mPhoto;
    }
    public void setPhoto(String pPhoto) {  mPhoto.setValue(pPhoto); }

    private final MutableLiveData<Integer> mGender = new MutableLiveData<>();
    public LiveData<Integer> getGender() {
        return mGender;
    }
    public void setGender(int pGender) {  mGender.setValue(pGender); }
}
