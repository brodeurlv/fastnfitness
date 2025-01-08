package com.easyfitness.DAO;

import java.util.Date;

public class ProgressImage {
    public ProgressImage(long id, String file, Date created, long profileId) {
        this.id = id;
        this.file = file;
        this.created = created;
        this.profileId = profileId;
    }

    public ProgressImage() {
    }

    private long id;
    private String file;
    private Date created;
    private long profileId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public long getProfileId() {
        return profileId;
    }
}
