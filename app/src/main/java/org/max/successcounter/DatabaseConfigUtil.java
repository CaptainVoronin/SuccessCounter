package org.max.successcounter;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

import org.max.successcounter.model.Exercise;
import org.max.successcounter.model.ExerciseSet;

public class DatabaseConfigUtil extends OrmLiteConfigUtil
{
    private static final Class<?>[] classes = new Class[] {
            ExerciseSet.class, Exercise.class
    };
    public static void main(String[] args) throws Exception {
        writeConfigFile("ormlite_config.txt", classes);
    }
}