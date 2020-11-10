package org.max.successcounter;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;

import org.max.successcounter.db.DatabaseHelper;
import org.max.successcounter.model.HistoryOperator;
import org.max.successcounter.model.TagsOperator;
import org.max.successcounter.model.excercise.BaseExercise;
import org.max.successcounter.model.excercise.ExerciseFactory;
import org.max.successcounter.model.excercise.IExerciseEvent;
import org.max.successcounter.model.excercise.IStep;
import org.max.successcounter.model.excercise.NewShotEvent;
import org.max.successcounter.model.excercise.Result;
import org.max.successcounter.model.excercise.Tag;
import org.max.successcounter.model.excercise.Template;
import org.max.successcounter.model.excercise.UndoEvent;

import java.sql.SQLException;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public abstract class AExerciseActivity<T extends BaseExercise> extends AppCompatActivity
        implements IExerciseForm<T>, DialogTags.DialogTagsResultListener,
        Observer<IExerciseEvent>
{
    public final static String RESULT_ID = "RESULT_ID";
    Template template;
    private DatabaseHelper db;
    private TextView lbPercent;
    private TextView lbAttempts;
    private TextView lbExName;
    private ImageView btnRollback;
    private ImageView btnComment;
    private ImageView btnShowTagsDialog;
    private T exercise;
    private Dao<Result, Integer> daoResult;
    private ImageButton btnBack;
    private EditText edCommentInDialog;

    /**
     * Returns the activity class for the template
     *
     * @param template
     * @return the class of the activity that works with this template
     */
    public static Class getExerciseActivityClass(Template template)
    {
        switch (template.getExType())
        {
            case series:
                return SeriesExerciseActivity.class;
            case compound:
                return CompoundExerciseActivity.class;
            case runTo:
                return RunToExerciseActivity.class;
            default:
                throw new IllegalArgumentException("Unknown template type");
        }
    }

    protected void prepareControls()
    {
        lbPercent = findViewById(R.id.lbPercent);
        lbAttempts = findViewById(R.id.lbAttempts);
        lbExName = findViewById(R.id.tvExName);

        lbExName.setText(getExercise().getTemplate().getName());
        btnRollback = findViewById(R.id.btnRollback);
        btnRollback.setOnClickListener(e -> undo());
        btnRollback.setEnabled(false);
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        LinearLayout ll = findViewById(R.id.viewChartPlaceholder);
        prepareChart(ll);

        ll = findViewById(R.id.buttonsPlaceholder);
        prepareControlButtons(ll);

        btnComment = findViewById(R.id.btnComment);
        btnComment.setOnClickListener(v -> showCommentDialog());
        btnComment.setEnabled(false);

        btnShowTagsDialog = findViewById(R.id.btnExerciseTags);
        btnShowTagsDialog.setOnClickListener(v -> showTagsDialog());
        btnShowTagsDialog.setEnabled(false);
    }

    /**
     * Show the dialog with exercise tags
     */
    private void showTagsDialog()
    {
        try
        {
            List<Tag> tags = null;
            DialogTags dlg = new DialogTags(this, TagsOperator.instance.instance, this, true);
            if (exercise.getAttemptsCount() != 0)
                tags = exercise.getResult().getTags();

            dlg.showDialog(tags);

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    protected abstract void prepareChart(LinearLayout placeholder);

    protected abstract void prepareControlButtons(LinearLayout placeholder);

    protected void addNewShot(int points)
    {
        getExercise().addNewShot(points);
    }

    /**
     * newShotAdded is called as a reaction on the NewStep event
     *
     * @param step
     */
    protected void newShotAdded(IStep step)
    {
        saveResult(step);
        updateStatsOnUI();
    }

    /**
     * Step miy be null because this function could be called
     * for saving the comment or tags
     *
     * @param step step to save
     */
    protected void saveResult(IStep step)
    {
        try
        {
            Result res = exercise.getResult();
            daoResult.createOrUpdate(res);
            if (step != null)
                HistoryOperator.instance.saveStep(getExercise().getResult(), step);
            onResultSaved();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected void updateStatsOnUI()
    {
        lbPercent.setText(getEfficiencyString());
        lbAttempts.setText(getAttemptsString());
        updateChart();
    }

    protected void loadTemplate() throws SQLException
    {
        Integer templateID = getIntent().getIntExtra(ProgressActivity.TEMPLATE_ID, -1);

        if (templateID == -1)
            throw new IllegalArgumentException("Exercise result id is missing");

        // Load the exercise template
        template = getTemplate(templateID);
        // Make an exercise instance
        T ex = (T) ExerciseFactory.instance.makeExercise(template);
        ex.getPublisher().subscribe(this);

        setExerсise(ex);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.aexercise_layout);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setFullScreenMode();

        db = new DatabaseHelper(this);

        try
        {
            TagsOperator.instance.init(getDb());
            loadTemplate();
            prepareControls();
            loadOrClearHistory();
            setTitle(getExercise().getTemplate().getName());
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        setResult(RESULT_CANCELED);
    }

    protected DatabaseHelper getDb()
    {
        return db;
    }

    void undo()
    {
        getExercise().undo();
    }

    @Override
    public <T> Dao<T, Integer> getDao(Class<T> exerciseClass, DatabaseHelper db) throws SQLException
    {
        return db.getDao(exerciseClass);
    }

    @Override
    public final void setExerсise(T exercise)
    {
        this.exercise = exercise;
    }

    @Override
    public final T getExercise()
    {
        return exercise;
    }

    protected abstract void updateChart();

    private Template getTemplate(Integer id) throws SQLException
    {
        Dao<Template, Integer> d = getDb().getDao(Template.class);
        Template et = d.queryForId(id);
        et.setMissOptionName(getString(R.string.missOptionName));
        et.setSuccessOptionName(getString(R.string.successOptionName));
        return et;
    }

    @Override
    public String getEfficiencyString()
    {
        String buff;
        List<IStep> steps = getExercise().getSteps();

        if (steps.size() != 0)
            buff = Result.getPercentString(steps.get(steps.size() - 1).getPercent());
        else
            buff = "0.0%";

        return buff;
    }

    @Override
    public String getAttemptsString()
    {
        return "" + getExercise().getTotalPoints() + "(" + getExercise().getAttemptsCount() + ")";
    }

    /**
     * Just a stub in the base class
     */
    public void onExerciseFinished()
    {
    }

    /**
     * It shows the dialog with the exercise comment
     * and the user can edit this comment
     */
    protected void showCommentDialog()
    {
        LayoutInflater li = LayoutInflater.from(getApplicationContext());
        View commentView = li.inflate(R.layout.comment_dialog, null);
        AlertDialog.Builder dlg = new AlertDialog.Builder(this);
        dlg.setView(commentView);
        edCommentInDialog = commentView.findViewById(R.id.edComment);
        edCommentInDialog.setText(exercise.getComment());
        dlg.setNegativeButton(android.R.string.cancel, (d, id) -> {
            d.cancel();
            setFullScreenMode();
        }).
                setPositiveButton(android.R.string.ok, (d, id) -> {
                    exercise.setComment(edCommentInDialog.getText().toString());
                    d.dismiss();
                    saveComment();
                    setFullScreenMode();
                }).show();
    }

    /**
     * It sets the fullscreen mode
     */
    private void setFullScreenMode()
    {
        View mContentView;
        mContentView = findViewById(R.id.fullscreen_content);
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

    }

    /**
     * The function saves the comment. It does it only if
     * the result hab been saved ere this moment. Unless that happened
     * the comment will be saved with the fist shot in the exercise.
     */
    protected void saveComment()
    {
        if (exercise.getAttemptsCount() == 0)
            return;

        Result res = exercise.getResult();
        try
        {
            daoResult.createOrUpdate(res);
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * It's callback for the tags dialog
     *
     * @param result  - true if user pressed OK
     * @param checked - list of selected tags
     */
    public void onResult(boolean result, List<Tag> checked)
    {
        setFullScreenMode();

        if (!result)
            return;

        // Count tags to delete
        long cnt = exercise.getResult().getTags().stream().filter(tag -> !checked.contains(tag)).count();

        // Set the new tag set
        exercise.getResult().setTags(checked);

        try
        {
            saveTags();

            // If there something must be deleted, delete
            if (cnt != 0)
                TagsOperator.instance.deleteUnusedTags();

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * This function saves the tags but only if the exercise result has been saved before.
     * Otherwise tags will be saved after the first saving if the result
     *
     * @throws SQLException
     */
    private void saveTags() throws SQLException
    {
        int inserted = TagsOperator.instance.setTagsForResult(exercise.getResult());
        if (inserted != exercise.getResult().getTags().size())
            throw new SQLException("Not all tags were inserted");
    }

    /**
     * Is is called  after saving the result
     */
    public void onResultSaved()
    {
        btnRollback.setEnabled(getExercise().getSteps().size() != 0);

        if (!btnComment.isEnabled())
        {
            btnComment.setEnabled(true);
            btnComment.setImageDrawable(getDrawable(R.drawable.ic_note_add_orange_18dp));
        }

        if (!btnShowTagsDialog.isEnabled())
        {
            btnShowTagsDialog.setEnabled(true);
            btnShowTagsDialog.setImageDrawable(getDrawable(R.drawable.ic_label_outline_orange_36dp));
        }
        setResult(RESULT_OK);
    }

    /**
     * It restores the fullscreen mode after resume
     */
    @Override
    protected void onPostResume()
    {
        super.onPostResume();
        setFullScreenMode();
    }

    /**
     * Sets the title for the activity
     *
     * @param title
     */
    @Override
    public void setTitle(CharSequence title)
    {
        TextView tv = findViewById(R.id.tvExName);
        tv.setText(title);
    }

    protected void loadOrClearHistory() throws SQLException
    {
        HistoryOperator.instance.init(getDb(), template);

        // Check if it is the continue of an exercise
        Integer id = getIntent().getIntExtra(AExerciseActivity.RESULT_ID, -1);
        daoResult = getDao(Result.class, getDb());

        // Yes, it is the continue
        if (id != -1)
        {
            List<IStep> steps = HistoryOperator.instance.getHistory();
            getExercise().setSteps(steps);
            updateStatsOnUI();
            onResultSaved();
        } else
        {
            // This is the new exercise,
            // so the the old history must be deleted
            HistoryOperator.instance.clearHistory();
        }
    }

    @Override
    public void onNext(IExerciseEvent event)
    {
        switch (event.getType())
        {
            case ShotAdded:
            {
                NewShotEvent e = (NewShotEvent) event;
                newShotAdded(e.getStep());
            }
            break;
            case Undo:
            {
                UndoEvent e = (UndoEvent) event;
                onUndo(e.getStep());
            }
            break;
            case Finished:
                onExerciseFinished();
                break;
            case Resume:
                onExerciseResumed();
                break;
            default:
                break;
        }
    }

    /**
     * Just a stub
     */
    protected void onExerciseResumed()
    {
    }

    /**
     * It is called on the undo event type
     *
     * @param step that has been removed from the sequence
     */
    private void onUndo(IStep step)
    {
        try
        {
            if (step != null)
                HistoryOperator.instance.deleteStep(step);
            saveResult(null);
            updateStatsOnUI();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onSubscribe(Disposable d)
    {

    }

    @Override
    public void onError(Throwable e)
    {

    }

    @Override
    public void onComplete()
    {

    }

    protected void setScreenProportions(float chartWeight, float buttonsWeight)
    {
        LinearLayout ll = findViewById(R.id.viewChartPlaceholder);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, chartWeight);
        ll.setLayoutParams(lp);

        ll = findViewById(R.id.buttonsPlaceholder);
        lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, buttonsWeight);
        ll.setLayoutParams(lp);
    }
}