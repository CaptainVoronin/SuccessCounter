package org.max.successcounter;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

import org.max.successcounter.model.HistoryItem;
import org.max.successcounter.model.excercise.CompoundExcercise;
import org.max.successcounter.model.excercise.OptionDescription;
import org.max.successcounter.model.excercise.Result;
import org.max.successcounter.model.excercise.Template;

public class DatabaseConfigUtil extends OrmLiteConfigUtil
{
    private static final Class<?>[] classes = new Class[] {
            Result.class, Template.class, HistoryItem.class, OptionDescription.class
    };

    public static void main(String[] args) throws Exception {
        writeConfigFile("ormlite_config.txt", classes);
    }
}