package org.max.successcounter.model.excercise;

import com.github.mikephil.charting.data.Entry;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import static java.lang.Math.abs;

//TODO: There is some problem with the math library dependency

@DatabaseTable(tableName = "template")
public class Template
{
    public enum Type{
        simple,
        compound,
        series
    }

    @Getter
    @Setter
    @DatabaseField
    Type exType;

    @Getter @Setter
    @DatabaseField
    String name;

    @Getter @Setter
    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    private Integer id;

    @Getter @Setter
    @DatabaseField
    private Integer limit;

    @Getter @Setter
    @DatabaseField
    private Boolean limited;

    @DatabaseField
    private Boolean hasSummaryStep;

    public boolean isHasSummaryStep()
    {
        return hasSummaryStep != null ? hasSummaryStep : true;
    }

    public void setHasSummaryStep(boolean hasSummaryStep)
    {
        this.hasSummaryStep = hasSummaryStep;
    }

    @Getter @Setter
    @ForeignCollectionField(eager = true)
    private ForeignCollection<OptionDescription> options;

    @Getter @Setter
    @ForeignCollectionField(eager = true)
    private ForeignCollection<Result> results;

    String missOptionName;
    String successOptionName;

    public void setMissOptionName(String missOptionName)
    {
        this.missOptionName = missOptionName;
    }

    public void setSuccessOptionName(String successOptionName)
    {
        this.successOptionName = successOptionName;
    }

    public Template()
    {
        exType = Type.simple;
        limited = new Boolean(false);
        limit = 0;
        hasSummaryStep = new Boolean( true );
    }

    public List<Result> getExercisesAsList()
    {
        List<Result> list = new ArrayList<>();
        results.forEach(item -> list.add(item));
        return list;
    }

    public List<OptionDescription> getOptionsAsList()
    {
        List<OptionDescription> list = new ArrayList<>();
        options.forEach(item -> list.add(item));
        list.add( 0, getMissOption() );
        list.add( getSuccessOption() );

        return list;
    }

    public List<Result> getResultAsList()
    {
        List<Result> res = new ArrayList<>( this.results.size() );
        results.forEach( result -> res.add( result ) );
        return res;
    }

    public void addOption(OptionDescription op )
    {
        op.setParent(this);
        options.add( op );
    }

    public int getFullSuccessOptionPoints()
    {
        int count = 0;
        for( OptionDescription op : options )
                count += op.getPoints();
        return count;
    }

    private OptionDescription getMissOption()
    {
        OptionDescription op = makeDefaultOption( missOptionName, 0 );
        op.setFirstDefault( true );
        return op;
    }

    private OptionDescription getSuccessOption()
    {
        OptionDescription op = makeDefaultOption( successOptionName, getFullSuccessOptionPoints() );
        op.setLastDefault( true );
        return op;
    }

    private OptionDescription makeDefaultOption( String description, Integer points )
    {
        OptionDescription op = new OptionDescription();
        op.setDescription( description );
        op.setPoints( points );
        return op;
    }

    public void removeOption(OptionDescription option)
    {
        options.remove( option );
    }

    /**
     * 1 - upper trend
     * 0 - almost horisontal
     * -1 - down
     * @param results
     * @return
     */
    public static int regressionDirection( List<Result>  results )
    {
        if( results.size() < 3 )
            return 0;

        SimpleRegression sr = new SimpleRegression();

        for (int i = 1; i <= results.size(); i++)
            sr.addData(i, results.get(i - 1).getPercent());

        float y1 = (float) sr.predict(1);
        float y2 = (float) sr.predict(results.size());
        float dy = y2 - y1;
        float dx = results.size() - 1;
        float tan = dy / dx;

        if( 0.75 <= abs( tan ) && abs( tan ) <= 1.25 ) return 0;
        else if( dy >= 0  ) return 1;
        else return -1;
    }
}