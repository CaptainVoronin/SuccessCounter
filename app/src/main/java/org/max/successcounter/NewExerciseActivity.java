package org.max.successcounter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.j256.ormlite.dao.Dao;
import org.max.successcounter.db.DatabaseHelper;
import org.max.successcounter.model.excercise.Template;
import java.sql.SQLException;

public class NewExerciseActivity extends AppCompatActivity
{
    final static String DEFAULT_TEXT = "*название";

    public final static String TEMPLATE_NAME = "TEMPLATE_NAME";

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
                        edName.setText( DEFAULT_TEXT );
                        edName.setTypeface( getItalicFont() );
                    }
                }
            }
        });

        edName.addTextChangedListener( new NameChangeListener() );

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
        btnNewSimpleLimited.setOnClickListener( new OnBtnClickListener<NewLimitedActivity>( NewLimitedActivity.class ) );

        btnNewCompound = findViewById( R.id.btnCompaund );
        btnNewCompound.setOnClickListener( new OnBtnClickListener<NewCompoundActivity>( NewCompoundActivity.class ) );
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
            in.putExtra( TEMPLATE_NAME, edName.getText().toString()  );
            startActivityForResult( in, 10 );
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if( resultCode == RESULT_OK )
        {
            setResult( RESULT_OK );
            finish();
        }
    }

    class NameChangeListener implements  TextWatcher
    {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            if( !s.toString().equals( DEFAULT_TEXT ) && s.toString().length() != 0 )
            {
                btnNewSimpleUnlimited.setEnabled( true );
                btnNewSimpleLimited.setEnabled( true );
                btnNewCompound.setEnabled( true );
            }
            else
            {
                btnNewSimpleUnlimited.setEnabled( false );
                btnNewSimpleLimited.setEnabled( false );
                btnNewCompound.setEnabled( false );
            }
        }

        @Override
        public void afterTextChanged(Editable s)
        {

        }
    }
}
