package com.easyfitness.enums;

public enum DisplayType {
    FREE_WORKOUT_DISPLAY,
    PROGRAM_WORKOUT_DISPLAY,
    ALL_WORKOUT_DISPLAY,
    PROGRAM_WORKOUT_PREVIEW_DISPLAY,
    PROGRAM_EDIT_DISPLAY;

    public static DisplayType fromInteger(int x) {
        switch(x) {
            case 0:
                return FREE_WORKOUT_DISPLAY;
            case 1:
                return PROGRAM_WORKOUT_DISPLAY;
            case 2:
                return ALL_WORKOUT_DISPLAY;
            case 3:
                return PROGRAM_WORKOUT_PREVIEW_DISPLAY;
            case 4:
                return PROGRAM_EDIT_DISPLAY;
        }
        return null;
    }
}
