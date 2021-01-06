package com.easyfitness.enums;

public enum ProgramRecordStatus {
    SUCCESS,
    FAILED,
    PENDING,
    NONE;

    public static ProgramRecordStatus fromInteger(int x) {
        switch (x) {
            case 0:
                return SUCCESS;
            case 1:
                return FAILED;
            case 2:
                return PENDING;
            case 3:
                return NONE;
        }
        return null;
    }
}
