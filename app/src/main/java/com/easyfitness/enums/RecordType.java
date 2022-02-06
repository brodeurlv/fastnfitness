package com.easyfitness.enums;

import java.util.IllegalFormatException;

public enum RecordType {
    FREE_RECORD("free_record"),
    PROGRAM_RECORD("program_record"),
    PROGRAM_TEMPLATE("program_template");

    private String mDisplayName = "";

    RecordType(String displayName) {
        this.mDisplayName = displayName;
    }

    public String toString() {
        return mDisplayName;
    }

    public static RecordType fromInteger(int x) {
        switch (x) {
            case 0:
                return FREE_RECORD;
            case 1:
                return PROGRAM_RECORD;
            case 2:
                return PROGRAM_TEMPLATE;
        }
        return null;
    }

    public static RecordType fromString(String x) throws Exception {
        if (x.equals(FREE_RECORD.mDisplayName)) return FREE_RECORD;
        else if (x.equals(PROGRAM_RECORD.mDisplayName)) return PROGRAM_RECORD;
        else if (x.equals(PROGRAM_TEMPLATE.mDisplayName)) return PROGRAM_TEMPLATE;
        else {
            throw new Exception("Illegal record type string");
        }
    }
}
