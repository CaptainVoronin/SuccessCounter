package org.max.successcounter.model.excercise;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

public interface IExercise
{
    String getName();
    Integer getId();
    List<IStep> getSteps();
    Float getPercentAtStep( int stepNum );
    IStep getLastStep( );
    IStep addStepByPoints( Integer points );
    void addStep( IStep step );
    int getAttemptsCount();
    IStep undo();
    List<Entry> getPercentHistory();
    boolean isFinished();
}
