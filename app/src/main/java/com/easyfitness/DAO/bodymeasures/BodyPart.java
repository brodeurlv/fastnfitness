package com.easyfitness.DAO.bodymeasures;

import com.easyfitness.R;

/* DataBase Object */
public class BodyPart {
    private long id;    // Notez que l'identifiant est un long

    public BodyPart(long id) {
        super();
        this.id = id;
    }

    public long getId() {
        return id;
    }

    /**
     *
     * @return Resource ID of the name of the body part
     */
    public int getResourceNameID() {
        return getBodyResourceID((int)id);
    }

    /**
     *
     * @return Resource ID of the logo
     */
    public int getResourceLogoID() {
        return getBodyLogoID((int)id);
    }

    private int getBodyResourceID(int pBodyID) {
        switch(pBodyID){
            case 0: return R.string.abdominaux;
            case 1: return R.string.adducteurs;
            case 2: return R.string.biceps;
            case 3: return R.string.triceps;
            case 4: return R.string.deltoids;
            case 5: return R.string.mollets;
        };

        return 0;
    }

    private int getBodyLogoID(int pBodyID) {
        switch(pBodyID){
            case 0: return R.drawable.silhouette;
            case 1: return R.drawable.silhouette;
            case 2: return R.drawable.silhouette;
            case 3: return R.drawable.silhouette;
            case 4: return R.drawable.silhouette;
            case 5: return R.drawable.silhouette;
        };

        return 0;
    }




}