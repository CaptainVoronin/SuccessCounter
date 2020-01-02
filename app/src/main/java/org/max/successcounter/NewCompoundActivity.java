package org.max.successcounter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;

import org.max.successcounter.db.DatabaseHelper;
import org.max.successcounter.model.excercise.OptionDescription;
import org.max.successcounter.model.excercise.Template;

import java.sql.SQLException;

public class NewCompoundActivity extends AppCompatActivity
{
    TableLayout table;
    Template template;
    Dao<Template, Integer> templateDao;
    String templateName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_compound);
        setTitle( R.string.title_new_compound_template );
        Intent in = getIntent();
        templateName = in.getStringExtra( NewExerciseActivity.TEMPLATE_NAME );

        DatabaseHelper db = new DatabaseHelper( this );

        template = new Template();
        template.setName( templateName  );
        try
        {
            templateDao = db.getDao( Template.class );
            templateDao.assignEmptyForeignCollection( template, "options" );
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        template.setCompound( true );
        template.setLimited( false );
        template.setLimit( 0 );
        OptionDescription op = new OptionDescription();
        op.setDescription( "Miss" );
        op.setPoints( 0 );
        op.setFirstDefault( true );
        template.addOption( op );

        op = new OptionDescription();
        op.setDescription( "Full success" );
        op.setPoints( 1 );
        op.setLastDefault( true );
        template.addOption( op );

        TextView tv = findViewById( R.id.lbName );
        tv.setText( templateName );
        prepareTable();
    }

    private void prepareTable()
    {
        table = findViewById( R.id.partsTable );
        OptionDescription op = template.getFirstDefault();
        table.addView( makeDefaultStepRow( op.getDescription(), op.getPoints()  ));
        table.addView( makePlaceholderRow( ));
        op = template.getLastDefault();
        table.addView( makeDefaultStepRow( op.getDescription(), op.getPoints()  ));
    }

    private View makePlaceholderRow()
    {
        TableRow tr = ( TableRow ) getLayoutInflater().inflate( R.layout.add_step_placeholder, null, false );
        ImageButton btn = tr.findViewById( R.id.btnAddStep );
        return tr;
    }

    private View makeDefaultStepRow(String name, Integer poins)
    {
        TableRow tr = ( TableRow ) getLayoutInflater().inflate( R.layout.compound_default_part, null, false );
        TextView tv = tr.findViewById( R.id.lbStepName );
        tv.setText( name );
        tv = tr.findViewById( R.id.lbStepPoints );
        tv.setText( poins.toString() );
        return tr;
    }
}
