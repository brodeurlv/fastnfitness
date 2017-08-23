package com.easyfitness.DAO.bodymeasures;

/* DataBase Object */
public class BodyPart {
    private long id;    // Notez que l'identifiant est un long
    private int mresName;
    private int mresLogo;

    public BodyPart(long id, int resNameId, int resLogoId) {
        super();
        this.id = id;
        this.mresName = resNameId;
        this.mresLogo = resLogoId;
    }

    public long getId() {
        return id;
    }

    /**
     *
     * @return Resource ID of the name of the body part
     */
    public int getResourceName() {
        return mresName;
    }

    /**
     *
     * @return Resource ID of the logo
     */
    public int getResourceLogo() {
        return mresLogo;
    }

}