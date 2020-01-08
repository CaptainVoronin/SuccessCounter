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
        setTitle(R.string.title_new_compound_template);
        Intent in = getIntent();
        templateName = in.getStringExtra(NewExerciseActivity.TEMPLATE_NAME);

        DatabaseHelper db = new DatabaseHelper(this);

        template = new Template();
        template.setName(templateName);
        String str = getString( R.string.missOptionName );
        template.setMissOptionName( str );
        str = getString( R.string.successOptionName );
        template.setSuccessOptionName( str );

        try
        {
            templateDao = db.getDao(Template.class);
            templateDao.assignEmptyForeignCollection(template, "options");
            optionDao = db.getDao( OptionDescription.class );
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        prepareNewExercise();

        outcomes = new ExerciseOutcomes( this, R.id.partsTable, template );
        outcomes.makeTable();

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

    }

    private void prepareNewExercise()
    {
        template.setCompound(true);
        template.setLimited(false);
        template.setLimit(0);

        OptionDescription op = new OptionDescription();
        op.setDescription("Outcome 1");
        op.setPoints(1);
        template.addOption(op);

        op = new OptionDescription();
        op.setDescription("Outcome 2");
        op.setPoints(2);
        template.addOption(op);
    }

    void save()
    {
        try
        {
            templateDao.create(template);
            for( OptionDescription op : template.getOptions() )
                optionDao.create( op );

            setResult( RESULT_OK );

        } catch (SQLException e)
        {
            e.printStackTrace();
            setResult( RESULT_CANCELED );
        }
        finish();
    }
}
