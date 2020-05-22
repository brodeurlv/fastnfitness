package com.easyfitness.utils;

public enum ExerciseType {
    STRENGTH,
    CARDIO,
    ISOMETRIC;

    public static ExerciseType fromInteger(int x) {
        switch(x) {
            case 0:
                return STRENGTH;
            case 1:
                return CARDIO;
            case 3:
                return ISOMETRIC;
        }
        return null;
    }
}
