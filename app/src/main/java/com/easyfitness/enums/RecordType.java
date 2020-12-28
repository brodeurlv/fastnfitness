package com.easyfitness.enums;

public enum RecordType {
    FREE_RECORD_TYPE,
    PROGRAM_RECORD_TYPE,
    TEMPLATE_TYPE;

    public static RecordType fromInteger(int x) {
        switch (x) {
            case 0:
                return FREE_RECORD_TYPE;
            case 1:
                return PROGRAM_RECORD_TYPE;
            case 2:
                return TEMPLATE_TYPE;
        }
        return null;
    }
}
