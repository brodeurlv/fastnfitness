package com.easyfitness.DAO;

public class Program extends ARecord {
    private String programName;
    private long profileId;
    public Program(String programName, long profileId){
        super();
        this.profileId = profileId;
        this.setProgramName(programName);
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }
}
