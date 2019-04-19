package com.easyfitness.DAO.bodymeasures;

import com.easyfitness.R;

/* DataBase Object */
public class BodyPart {
    public static final int ABDOMINAUX = 0;
    public static final int ADDUCTEURS = 1;
    public static final int BICEPS = 2;
    public static final int TRICEPS = 3;
    public static final int DELTOIDS = 4;
    public static final int MOLLETS = 5;
    public static final int PECTORAUX = 6;
    public static final int DORSEAUX = 7;
    public static final int QUADRICEPS = 8;
    public static final int ISCHIOJAMBIERS = 9;
    public static final int LEFTARM = 10;
    public static final int RIGHTARM = 11;
    public static final int LEFTTHIGH = 12;
    public static final int RIGHTTHIGH = 13;
    public static final int LEFTCALVES = 14;
    public static final int RIGHTCALVES = 15;
    public static final int WAIST = 16;
    public static final int NECK = 17;
    public static final int BEHIND = 18;
    public static final int WEIGHT = 19;
    public static final int FAT = 20;
    public static final int BONES = 21;
    public static final int WATER = 22;
    public static final int MUSCLES = 23;
    private int id;    // Notez que l'identifiant est un long
    private BodyMeasure mLastMeasure;

    public BodyPart(int id) {
        super();
        this.id = id;
        this.mLastMeasure = null;
    }

    public BodyPart(int id, BodyMeasure lastMeasure) {
        super();
        this.id = id;
        this.mLastMeasure = lastMeasure;
    }

    private static int getBodyResourceID(int pBodyID) {
        switch (pBodyID) {
            case ABDOMINAUX:
                return R.string.abdominaux;
            case ADDUCTEURS:
                return R.string.adducteurs;
            case BICEPS:
                return R.string.biceps;
            case TRICEPS:
                return R.string.triceps;
            case DELTOIDS:
                return R.string.deltoids;
            case MOLLETS:
                return R.string.mollets;
            case PECTORAUX:
                return R.string.pectoraux;
            case DORSEAUX:
                return R.string.dorseaux;
            case QUADRICEPS:
                return R.string.quadriceps;
            case ISCHIOJAMBIERS:
                return R.string.ischio_jambiers;
            case LEFTARM:
                return R.string.left_arm;
            case RIGHTARM:
                return R.string.right_arm;
            case LEFTTHIGH:
                return R.string.left_thigh;
            case RIGHTTHIGH:
                return R.string.right_thigh;
            case LEFTCALVES:
                return R.string.left_calves;
            case RIGHTCALVES:
                return R.string.right_calves;
            case WAIST:
                return R.string.waist;
            case NECK:
                return R.string.neck;
            case BEHIND:
                return R.string.behind;
            case WEIGHT:
                return R.string.weightLabel;
            case FAT:
                return R.string.fatLabel;
            case BONES:
                return R.string.bonesLabel;
            case WATER:
                return R.string.waterLabel;
            case MUSCLES:
                return R.string.musclesLabel;
        }

        return 0;
    }

    private static int getBodyLogoID(int pBodyID) {
        switch (pBodyID) {
            case ABDOMINAUX:
                return R.drawable.ic_chest;
            case ADDUCTEURS:
                return R.drawable.ic_leg;
            case BICEPS:
                return R.drawable.ic_arm;
            case TRICEPS:
                return R.drawable.ic_arm;
            case DELTOIDS:
                return R.drawable.ic_chest;
            case MOLLETS:
                return R.drawable.ic_leg;
            case PECTORAUX:
                return R.drawable.ic_chest_measure;
            case DORSEAUX:
                return R.drawable.ic_chest;
            case QUADRICEPS:
                return R.drawable.ic_leg;
            case ISCHIOJAMBIERS:
                return R.drawable.ic_leg;
            case LEFTARM:
                return R.drawable.ic_arm_measure;
            case RIGHTARM:
                return R.drawable.ic_arm_measure;
            case LEFTTHIGH:
                return R.drawable.ic_tight_measure;
            case RIGHTTHIGH:
                return R.drawable.ic_tight_measure;
            case LEFTCALVES:
                return R.drawable.ic_calve_measure;
            case RIGHTCALVES:
                return R.drawable.ic_calve_measure;
            case WAIST:
                return R.drawable.ic_waist_measure;
            case NECK:
                return R.drawable.ic_neck;
            case BEHIND:
                return R.drawable.ic_buttock_measure;
        }

        return 0;
    }

    public long getId() {
        return id;
    }

    /**
     * @return Resource ID of the name of the body part
     */
    public int getResourceNameID() {
        return getBodyResourceID((int) id);
    }

    /**
     * @return Resource ID of the logo
     */
    public int getResourceLogoID() {
        return getBodyLogoID((int) id);
    }

    public BodyMeasure getLastMeasure() {
        return this.mLastMeasure;
    }

    public void setLastMeasure(BodyMeasure lastmeasure) {
        this.mLastMeasure = lastmeasure;
    }
}
