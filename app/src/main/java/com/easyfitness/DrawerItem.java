package com.easyfitness;

public class DrawerItem {

    String itemName;
    int imgResID;
    String imgSrc;
    String title;
    boolean isSpinner;
    boolean isActive;

    public DrawerItem(String itemName, int imgResID, boolean isActive) {
        this.itemName = itemName;
        this.imgResID = imgResID;
        this.isActive = isActive;
    }

    public DrawerItem(boolean isSpinner) {
        this.isSpinner = isSpinner;
    }

    public DrawerItem(String title) {
        this.title = title;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getImgResID() {
        return imgResID;
    }

    public void setImgResID(int imgResID) {
        this.imgResID = imgResID;
    }

    public String getImg() {
        return imgSrc;
    }

    public void setImg(String imgSrc) {
        this.imgSrc = imgSrc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isSpinner() {
        return isSpinner;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

}
