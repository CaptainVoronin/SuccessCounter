package org.max.successcounter.model.excercise;

import com.github.mikephil.charting.data.Entry;

import java.util.List;

/**
 * This interface contains methods that are used at runtime
 * by exercise activities
 */
public interface IExercise
{
    Template getTemplate();

    void setTemplate(Template template);

    /**
     * getSteps return all the shots being made
     *
     * @return
     */
    List<IStep> getSteps();

    /**
     * The function is used for loading the history from DB
     *
     * @param steps — steps red from the DB
     */
    void setSteps(List<IStep> steps);

    /**
     * addNewShot is used by activity to add info about new shots
     * use done
     *
     * @param points
     * @return — the new steps added
     */
    IStep addNewShot(int points);

    /**
     * getAttemptsCount — return the total number of shots made
     *
     * @return — the total number of shots
     */
    int getAttemptsCount();

    /**
     * undo is called by activity
     *
     * @return
     */
    IStep undo();

    // TODO: this is crap. Making the history
    // is not business of IExercise
    List<Entry> getPercentHistory();

    /**
     * isFinished returns the state of an exercise with the limit
     * for unlimited exercises it always returns false
     *
     * @return
     */
    boolean isFinished();

    /**
     * getTotalPoints returns total points collected by the player
     *
     * @return — total points
     */
    int getTotalPoints();

    Result getResult();

    /**
     * getMaxPossiblePoints return the maximum possible points which may bring a single shot
     * It always equals to 1 for simple exercises and varies for compound ones
     *
     * @return
     */
    int getMaxPossiblePoints();

    // TODO: is seems as crap too
    boolean isMinOrMax(int index, float value);

    String getComment();

    void setComment(String comment);
}
