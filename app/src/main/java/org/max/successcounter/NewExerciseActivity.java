package org.max.successcounter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.j256.ormlite.dao.Dao;

import org.max.successcounter.db.DatabaseHelper;
import org.max.successcounter.model.excercise.Template;

import java.sql.SQLException;

public class NewExerciseActivity extends AppCompatActivity
{

    EditText edName;
    Button btnNewSimpleUnlimited;
    Button btnNewSimpleLimited;
    Button btnNewCompound;
    Dao<Template, Integer> templateDao;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_new_exercise);
        boolean nameChanged = false;

        edName = findViewById( R.id.edName );
        edName.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if( hasFocus )
                {
                    edName.setTypeface( getRegularFont() );
                    if( !nameChanged )
                        edName.setText("");
                    else
                        edName.setTypeface( getItalicFont() );
                } else
                {
                    if( edName.getText().length() == 0 )
                    {
                        edName.setText("*название");
                        edName.setTypeface( getItalicFont() );
                    }
                }
            }
        });

        btnNewSimpleUnlimited = findViewById( R.id.btnSimpleUnlim);

        btnNewSimpleUnlimited.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Template t = new Template();
                t.setLimited( false );
                t.setName( edName.getText().toString() );
                DatabaseHelper db = new DatabaseHelper( NewExerciseActivity.this);
                try
                {
                    templateDao = db.getDao( Template.class );
                    templateDao.create(t);
                    setResult( RESULT_OK );
                    finish();
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        });

        btnNewSimpleLimited = findViewById( R.id.btnSimpleLimited );

        btnNewSimpleLimited.setOnClickListener( new OnBtnClickListener<NewSimpleExActivity>( NewSimpleExActivity.class ) );
        btnNewCompound = findViewById( R.id.btnCompaund );
    }

    Typeface getItalicFont()
    {
        return Typeface.create( "serif", Typeface.ITALIC );
    }

    Typeface getRegularFont()
    {
        return Typeface.create( "serif", Typeface.NORMAL );
    }

    class OnBtnClickListener<T> implements View.OnClickListener
    {
        Class<T> clazz;

        public OnBtnClickListener( Class<T> clazz )
        {
            this.clazz = clazz;
        }

        @Override
        public void onClick(View v)
        {
            Intent in = new Intent( NewExerciseActivity.this, clazz );
            startActivity( in );
        }
    }
}
