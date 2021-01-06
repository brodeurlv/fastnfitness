package com.easyfitness.enums;

public enum ExerciseType {
    STRENGTH,
    CARDIO,
    ISOMETRIC;

    public static ExerciseType fromInteger(int x) {
        switch (x) {
            case 0:
                return STRENGTH;
            case 1:
                return CARDIO;
            case 2:
                return ISOMETRIC;
        }
        return null;
    }
}
