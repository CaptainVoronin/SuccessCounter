package org.max.successcounter.model.excercise;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import java.util.ArrayList;
import java.util.List;

@DatabaseTable(tableName = "template")
public class Template
{
    @DatabaseField
    String name;
    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    private Integer id;
    @DatabaseField
    private Integer limit;
    @DatabaseField
    private Boolean limited;
    @DatabaseField
    private Boolean compound;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<OptionDescription> options;

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
        compound = new Boolean(false);
        limited = new Boolean(false);
        limit = 0;
    }

    public ForeignCollection<Result> getResults()
    {
        return results;
    }

    public void setResults(ForeignCollection<Result> results)
    {
        this.results = results;
    }

    public Boolean getCompound()
    {
        return compound == null ? false : compound;
    }

    public void setCompound(Boolean compound)
    {
        this.compound = compound;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getLimit()
    {
        return limit == null ? 0 : limit;
    }

    public void setLimit(Integer limit)
    {
        this.limit = limit;
    }

    public Boolean getLimited()
    {
        return limited == null ? false : limited;
    }

    public void setLimited(Boolean limited)
    {
        this.limited = limited;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public ForeignCollection<OptionDescription> getOptions()
    {
        return options;
    }

    public void setOptions(ForeignCollection<OptionDescription> options)
    {
        this.options = options;
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
        OptionDescription op = new OptionDescription();
        op.setDescription( missOptionName );
        op.setPoints( 0 );
        op.setFirstDefault( true );
        return op;
    }

    private OptionDescription getSuccessOption()
    {
        OptionDescription op = new OptionDescription();
        op.setDescription( successOptionName );
        op.setPoints( getFullSuccessOptionPoints() );
        op.setLastDefault( true );
        return op;
    }

    public void removeOption(OptionDescription option)
    {
        options.remove( option );
    }
}
