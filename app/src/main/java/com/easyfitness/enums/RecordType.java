package com.easyfitness.enums;

public enum RecordType {
    FREE_RECORD,
    PROGRAM_RECORD,
    PROGRAM_TEMPLATE;

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
}
