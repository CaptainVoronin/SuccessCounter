package org.max.successcounter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;

import org.max.successcounter.db.DatabaseHelper;
import org.max.successcounter.model.ExerciseOutcomes;
import org.max.successcounter.model.excercise.OptionDescription;
import org.max.successcounter.model.excercise.Template;

import java.sql.SQLException;

import androidx.appcompat.app.AppCompatActivity;

public class NewCompoundActivity extends AppCompatActivity
{
    TableLayout table;
    Template template;
    Dao<Template, Integer> templateDao;
    String templateName;
    ExerciseOutcomes outcomes;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_compound);
        setTitle(R.string.title_new_compound_template);
        Intent in = getIntent();
        templateName = in.getStringExtra(NewExerciseActivity.TEMPLATE_NAME);

        DatabaseHelper db = new DatabaseHelper(this);

        template = new Template();
        template.setName(templateName);
        try
        {
            templateDao = db.getDao(Template.class);
            templateDao.assignEmptyForeignCollection(template, "options");
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        template.setCompound(true);
        template.setLimited(false);
        template.setLimit(0);
        OptionDescription op = new OptionDescription();
        op.setDescription("Miss");
        op.setPoints(0);
        op.setFirstDefault(true);
        template.addOption(op);

        op = new OptionDescription();
        op.setDescription("Full success");
        op.setPoints(1);
        op.setLastDefault(true);
        template.addOption(op);

        TextView tv = findViewById(R.id.lbName);
        tv.setText(templateName);

        Button btnSave = findViewById( R.id.btnSave );
        btnSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                save();
            }
        } );

        outcomes = new ExerciseOutcomes( this, R.id.partsTable, template );
        outcomes.makeTable();
    }

    void save()
    {
        try
        {
            templateDao.create(template);
            setResult( RESULT_OK );

        } catch (SQLException e)
        {
            e.printStackTrace();
            setResult( RESULT_CANCELED );
        }
        finish();
    }
}
