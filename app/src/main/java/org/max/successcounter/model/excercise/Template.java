package org.max.successcounter.model.excercise;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import static java.lang.Math.abs;

@DatabaseTable(tableName = "template")
public class Template
{
    public Template()
    {
        exType = Type.series;
        limit = 0;
        hasSummaryStep = new Boolean(true);
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
    private Boolean succesLimited;

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

    @Setter
    String missOptionName;

    @Setter
    String successOptionName;

    public enum Type
    {
        series,
        compound,
        runTo
    }

    public List<Result> getResultsAsList()
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
        if (hasSummaryStep)
            list.add(getSuccessOption());
        return list;
    }

    public List<OptionDescription> getOptionsAsListAllSteps()
    {
        List<OptionDescription> list = new ArrayList<>();
        options.forEach(item -> list.add(item));
        list.add(0, getMissOption());
        list.add(getSuccessOption());
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

    /**
     * Create and return the "full success option@
     *
     * @return an option with maximum possible points
     */
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
        return regressionDirection(y1, y2, results.size() - 1);
    }

    public static int regressionDirection(float startY, float endY, int resultsLength)
    {
        float dy = endY - startY;
        float tan = dy / resultsLength / 10;

        if (-0.087 <= abs(tan) && abs(tan) <= 0.087) return 0;
        else if (dy >= 0) return 1;
        else return -1;

    }
}