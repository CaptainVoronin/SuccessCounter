package org.max.successcounter.model.excercise;

import com.github.mikephil.charting.data.Entry;
import java.util.List;

public interface IExercise
{
    String getName();
    void setName( String name );
    void setTemplate( Template template );
    Template getTemplate(  );
    Integer getId();
    void setId( Integer id );
    List<IStep> getSteps();
    Float getPercentAtStep( int stepNum );
    IStep getLastStep( );
    IStep addStepByPoints( Integer points );
    void addStep( IStep step );
    int getAttemptsCount();
    IStep undo();
    List<Entry> getPercentHistory();
    boolean isFinished();
    Integer getSuccessCount();
    Result getResult();
    void setSteps(List<IStep> steps);
}
