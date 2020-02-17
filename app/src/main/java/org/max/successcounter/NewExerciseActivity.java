package org.max.successcounter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.Observable;
import io.reactivex.ObservableConverter;
import io.reactivex.functions.Predicate;
import io.reactivex.subjects.PublishSubject;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import org.max.successcounter.db.DatabaseHelper;
import org.max.successcounter.model.excercise.Template;
import java.sql.SQLException;

public class NewExerciseActivity extends AppCompatActivity
{
    final static String DEFAULT_TEXT = "*название";

    public final static String TEMPLATE_NAME = "TEMPLATE_NAME";

    EditText edName;
    ComplexButton btnNewSimpleUnlimited;
    ComplexButton btnNewSimpleLimited;
    ComplexButton btnNewCompound;
    ComplexButton btnNewSeries;

    Dao<Template, Integer> templateDao;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        PublishSubject mFocusChangeSubject = PublishSubject.create();

        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_new_exercise);
        boolean nameChanged = false;

        LinearLayout ll = findViewById( R.id.container );

        edName = findViewById( R.id.edName );
        edName.setOnFocusChangeListener( (View v, boolean hasFocus )->
        {
            mFocusChangeSubject.onNext( hasFocus );
        });

        Observable<Boolean> oHasFocus =  mFocusChangeSubject.filter(o -> (Boolean)o == true );

        oHasFocus.subscribe( o->{
            edName.setTypeface( getRegularFont() );
            if( !nameChanged )
                edName.setText("");
            else
                edName.setTypeface( getItalicFont() );
        } );

        Observable<Boolean> oNoFocus =  mFocusChangeSubject.filter(o -> (Boolean)o == false );
        oNoFocus.subscribe( o -> {
            if( edName.getText().length() == 0 )
            {
                edName.setText( DEFAULT_TEXT );
                edName.setTypeface( getItalicFont() );
            }
        });

        edName.addTextChangedListener( new NameChangeListener() );

        btnNewSimpleUnlimited = new ComplexButton( this, getString( R.string.msgNewSimpleUnlimExTitle ),
                getString( R.string.msgNewSimpleUnlimExComment ), new JustSaveAndReturn( Template.Type.simple ) );

        ll.addView( btnNewSimpleUnlimited.inflate( ) );
        btnNewSimpleUnlimited.setEnabled( false );


        btnNewSimpleLimited = new ComplexButton( this, getString( R.string.msgNewSimpleLimExTitle ),
                getString( R.string.msgNewSimpleLimExComment ), new OnBtnClickListener<NewLimitedActivity>( NewLimitedActivity.class ) );

        ll.addView( btnNewSimpleLimited.inflate( ) );
        btnNewSimpleLimited.setEnabled( false );

        btnNewCompound = new ComplexButton( this, getString( R.string.msgNewCompoundExTitle ),
                getString( R.string.msgNewCompoundExComment ), new OnBtnClickListener<NewCompoundActivity>( NewCompoundActivity.class ) );

        ll.addView( btnNewCompound.inflate( ) );
        btnNewCompound.setEnabled( false );

        btnNewSeries = new ComplexButton( this, getString( R.string.msgNewSerisExTitle ),
                getString( R.string.msgNewSerisExText ), new JustSaveAndReturn( Template.Type.series ) );
        ll.addView( btnNewSeries.inflate( ) );

        btnNewSeries.setEnabled( false );

        makeToolbar();
    }

    public void makeToolbar()
    {
        TextView tv = findViewById( R.id.tvTitle );
        tv.setText( R.string.msgNewExerciseActivityTitle );
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
                btnNewSeries.setEnabled( true );
            }
            else
            {
                btnNewSimpleUnlimited.setEnabled( false );
                btnNewSimpleLimited.setEnabled( false );
                btnNewCompound.setEnabled( false );
                btnNewSeries.setEnabled( false   );
            }
        }

        @Override
        public void afterTextChanged(Editable s)
        {

        }
    }

    class JustSaveAndReturn implements View.OnClickListener{

        Template.Type type;

        public JustSaveAndReturn( Template.Type type )
        {
            this.type = type;
        }

        @Override
        public void onClick(View v)
        {
            Template t = new Template();
            t.setExType( type );
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
    }
}
