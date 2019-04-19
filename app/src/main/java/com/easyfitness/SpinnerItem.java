package com.easyfitness;

public class SpinnerItem {

    int drawableResID;
    String name;
    String email;


    public SpinnerItem(int drawableResID, String name, String email) {
        super();
        this.drawableResID = drawableResID;
        this.name = name;
        this.email = email;
    }


    public int getDrawableResID() {
        return drawableResID;
    }

    public void setDrawableResID(int drawableResID) {
        this.drawableResID = drawableResID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
