package com.easyfitness.enums;

import android.content.res.Resources;

import com.easyfitness.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public enum Muscle implements Comparable<Muscle> {
    ABDOMINALS(R.string.abdominaux, 0),
    BACK(R.string.dorseaux, 1),
    BICEPS(R.string.biceps, 2),
    CALVES(R.string.mollets, 3),
    CHEST(R.string.pectoraux, 4),
    DELTOIDS(R.string.deltoids, 5),
    GLUTES(R.string.glutes, 6),
    HAMSTRINGS(R.string.ischio_jambiers, 7),
    OBLIQUES(R.string.obliques, 8),
    QUADRICEPS(R.string.quadriceps, 9),
    SHOULDERS(R.string.shoulders, 10),
    THIGHS(R.string.adducteurs, 11),
    TRAPEZIUS(R.string.trapezius, 12),
    TRICEPS(R.string.triceps, 13);

    private final int resourceId;
    private final int newId;

    Muscle(int resourceId, int newId) {
        this.resourceId = resourceId;
        this.newId = newId;
    }

    public static Muscle fromId(int id) {
        for (Muscle muscle : Muscle.values()) {
            if (muscle.newId == id) {
                return muscle;
            }
        }
        throw new IllegalArgumentException("No muscle with that id is present");
    }

    public static Set<Muscle> setFromBodyParts(String bodyParts, Resources resources) {
        if (bodyParts.equals("")) {
            return new HashSet<>();
        }
        Set<Integer> bodyPartIds = muscleDatabaseIdsFromBodyPartString(bodyParts);
        return new HashSet<Muscle>() {{
            for (int bodyPartId : bodyPartIds) {
                add(fromDatabaseId(bodyPartId, resources));
            }
        }};
    }

    private static Set<Integer> muscleDatabaseIdsFromBodyPartString(String bodyPartString) {
        Set<String> bodyParts = new HashSet<>(Arrays.asList(bodyPartString.split(";")));
        return new HashSet<Integer>() {{
            for (String bodyPartString : bodyParts) {
                add(Integer.parseInt(bodyPartString));
            }
        }};
    }

    public static String migratedBodyPartStringFor(Set<Muscle> muscles) {
        if (muscles.size() == 0) {
            return "";
        }
        List<Integer> sortedMuscleIds = sortedListOfMuscleIdsFrom(muscles);
        return bodyPartStringFor(sortedMuscleIds);
    }

    private static List<Integer> sortedListOfMuscleIdsFrom(Set<Muscle> muscles) {
        Set<Integer> muscleIds = new HashSet<Integer>() {{
            for (Muscle muscle : muscles) {
                add(muscle.getNewId());
            }
        }};
        List<Integer> sortedMuscleIds = new ArrayList<>(muscleIds);
        Collections.sort(sortedMuscleIds);
        return sortedMuscleIds;
    }

    private static String bodyPartStringFor(List<Integer> bodyPartIds) {
        StringBuilder bodyPartString = new StringBuilder();
        for (int bodyPartId : bodyPartIds) {
            bodyPartString.append(bodyPartId).append(";");
        }
        bodyPartString.setLength(bodyPartString.length() - 1);
        return bodyPartString.toString();
    }

    public int getNewId() {
        return newId;
    }

    public static Muscle fromDatabaseId(int id, Resources resources) {
        Map<Integer, Muscle> musclesFromDatabaseId = musclesFromDatabaseIdsWithResources(resources);
        return musclesFromDatabaseId.get(id);
    }

    private static Muscle fromResourceId(int resourceId) {
        for (Muscle muscle : Muscle.values()) {
            if (muscle.resourceId == resourceId) {
                return muscle;
            }
        }
        throw new NoSuchElementException("No element with that resource ID could be found");
    }

    private static Map<Integer, Muscle> musclesFromDatabaseIdsWithResources(Resources resources) {
        List<String> sortedLocalisedStrings = sorted(localisedMuscleNames(resources).keySet());
        List<Integer> sortedResourceIdsForThisLocale = sortedLocalisedResourceIdsFromLocalisedStrings(sortedLocalisedStrings, resources);
        return localisedOldDatabaseIdToMuscle(sortedResourceIdsForThisLocale);
    }

    private static List<String> sorted(Set<String> strings) {
        List<String> sorted = new ArrayList<>(strings);
        Collections.sort(sorted);
        return sorted;
    }

    private static Map<Integer, Muscle> localisedOldDatabaseIdToMuscle(List<Integer> sortedResourceIds) {
        Map<Integer, Muscle> oldDatabaseIdToMuscleForThisLocale = new HashMap<>();
        for (int i = 0; i < sortedResourceIds.size(); i++) {
            oldDatabaseIdToMuscleForThisLocale.put(i, Muscle.fromResourceId(sortedResourceIds.get(i)));
        }
        oldDatabaseIdToMuscleForThisLocale.put(oldDatabaseIdToMuscleForThisLocale.size(), oldDatabaseIdToMuscleForThisLocale.get(oldDatabaseIdToMuscleForThisLocale.size() - 1));
        return oldDatabaseIdToMuscleForThisLocale;
    }

    private static List<Integer> sortedLocalisedResourceIdsFromLocalisedStrings(List<String> localisedStrings, Resources resources) {
        List<Integer> sortedLocalisedResourceIds = new ArrayList<>();
        for (String muscleName : localisedStrings) {
            sortedLocalisedResourceIds.add(localisedMuscleNames(resources).get(muscleName));
        }
        return sortedLocalisedResourceIds;
    }

    private static Map<String, Integer> localisedMuscleNames(Resources resources) {
        Map<String, Integer> localisedMuscleNames = new HashMap<>();
        for (Muscle muscle : Muscle.values()) {
            if (muscle != Muscle.GLUTES) {
                localisedMuscleNames.put(resources.getString(muscle.resourceId), muscle.resourceId);
            }
        }
        return localisedMuscleNames;
    }
}
