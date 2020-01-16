package org.max.successcounter.model.excercise;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@DatabaseTable(tableName = "result")
public class Result
{

    final static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy ");

    @Getter
    @Setter
    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    Integer id;

    @Getter @Setter
    @DatabaseField(foreign = true, columnName = "parent_id")
    Template parent;

    @Getter @Setter
    @DatabaseField
    Float percent;

    @Getter @Setter
    @DatabaseField
    Integer shots;

    @Getter @Setter
    @DatabaseField
    Date date;

    @Getter @Setter
    @DatabaseField
    Integer points;

    @Getter @Setter
    @DatabaseField
    String comment;

    public Result()
    {
        date = Calendar.getInstance().getTime();
    }

    public static final String getPercentString(Result res)
    {
        if (res.getPercent() == null)
            return "0.0%";

        double d = Math.round(res.getPercent() * 10) / 10.0;
        return String.format("%.1f%%", d);
    }

    public static final String getPercentString(float value)
    {

        double d = Math.round(value * 10) / 10.0;
        return String.format("%.1f%%", d);
    }

    public static final String getFormattedDate(Result ex)
    {
        return sdf.format(ex.getDate());
    }

}
