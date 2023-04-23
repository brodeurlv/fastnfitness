package com.easyfitness.DAO;

import java.util.Date;

public class ProgressImage {
    public ProgressImage(long id, String file, Date created) {
        this.id = id;
        this.file = file;
        this.created = created;
    }

    public ProgressImage() {
    }

    private long id;
    private String file;
    private Date created;

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
}
