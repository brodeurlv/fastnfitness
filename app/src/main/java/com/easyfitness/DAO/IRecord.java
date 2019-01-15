package com.easyfitness.DAO;

import java.util.Date;

public interface IRecord {
    long getId();

    void setId(long id);

    Date getDate();

    String getExercise();

    void setExercise(String exercise);

    long getExerciseKey();

    void setExerciseKey(long id);

    Profile getProfil();

    long getProfilKey();

    String getTime();

    int getType();
}
