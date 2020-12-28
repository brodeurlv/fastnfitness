package com.easyfitness.enums;

public enum ProgramStatus {
    RUNNING,
    CLOSED;

    public static ProgramStatus fromInteger(int x) {
        switch (x) {
            case 0:
                return RUNNING;
            case 1:
                return CLOSED;
        }
        return null;
    }
}
