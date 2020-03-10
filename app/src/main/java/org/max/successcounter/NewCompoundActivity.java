package org.max.successcounter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;

import org.max.successcounter.db.DatabaseHelper;
import org.max.successcounter.model.ExerciseOutcomes;
import org.max.successcounter.model.excercise.OptionDescription;
import org.max.successcounter.model.excercise.Template;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class NewCompoundActivity extends AppCompatActivity
{
    Template template;
    Dao<Template, Integer> templateDao;
    Dao<OptionDescription, Integer> optionDao;
    String templateName;
    ExerciseOutcomes outcomes;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_compound);

        makeToolbar();

        Intent in = getIntent();
        templateName = in.getStringExtra(NewExerciseActivity.TEMPLATE_NAME);

        DatabaseHelper db = new DatabaseHelper(this);

        template = new Template();
        template.setName(templateName);
        String str = getString(R.string.missOptionName);
        template.setMissOptionName(str);
        str = getString(R.string.successOptionName);
        template.setSuccessOptionName(str);

        try
        {
            templateDao = db.getDao(Template.class);
            templateDao.assignEmptyForeignCollection(template, "options");
            optionDao = db.getDao(OptionDescription.class);
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        prepareNewExercise();

        outcomes = new ExerciseOutcomes(this, R.id.partsTable, template);
        outcomes.makeTable();

        TextView tv = findViewById(R.id.lbName);
        tv.setText(templateName);

        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                save();
            }
        });

    }

    private void makeToolbar()
    {
        TextView tv = findViewById(R.id.tvTitle);
        tv.setText(R.string.title_new_compound_template);
    }

    private void prepareNewExercise()
    {
        template.setExType(Template.Type.compound);
        template.setLimited(false);
        template.setLimit(0);

        OptionDescription op = new OptionDescription();
        op.setDescription("Outcome 1");
        op.setPoints(1);
        op.setOrderNum(0);
        template.addOption(op);

        op = new OptionDescription();
        op.setDescription("Outcome 2");
        op.setPoints(2);
        op.setOrderNum(1);
        template.addOption(op);
    }

    void save()
    {
        try
        {
            // The template and it's options must be saved separately
            // because at the moment the template saves it doesn't have the id
            // so it's options don't have parent id.
            // Because of that reason we remove all the options
            // from the template and save them in a list
            List<OptionDescription> ops = new ArrayList<>();
            template.getOptions().stream().forEach(item -> ops.add(item));
            template.getOptions().clear();

            // Than the template is saved
            templateDao.create(template);

            // restore the options
            // It is stupid, really
            ops.stream().forEach(item -> item.setParent(template));

            // Finally, I'd say OrmLite is crap

            for (OptionDescription op : ops)
                optionDao.create(op);

            setResult(RESULT_OK);

        } catch (SQLException e)
        {
            e.printStackTrace();
            setResult(RESULT_CANCELED);
        }
        finish();
    }
}
