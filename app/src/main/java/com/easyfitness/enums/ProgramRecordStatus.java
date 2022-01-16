package com.easyfitness.enums;

public enum ProgramRecordStatus {
    SUCCESS("success"),
    FAILED("failed"),
    PENDING("pending"),
    NONE("none");

    private String mDisplayName = "";

    ProgramRecordStatus(String displayName) {
        this.mDisplayName = displayName;
    }

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

    public String toString() {
        return mDisplayName;
    }

    public static ProgramRecordStatus fromString(String x) throws Exception{
        if (x.equals(SUCCESS.mDisplayName)) return SUCCESS;
        else if (x.equals(FAILED.mDisplayName)) return FAILED;
        else if (x.equals(PENDING.mDisplayName)) return PENDING;
        else if (x.equals(NONE.mDisplayName) || x.isEmpty())return NONE;

        throw new Exception("Illegal record type string");
    }
}
