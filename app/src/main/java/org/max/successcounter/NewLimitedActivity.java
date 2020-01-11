package org.max.successcounter;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import org.max.successcounter.db.DatabaseHelper;
import org.max.successcounter.model.excercise.Template;
import java.sql.SQLException;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class NewLimitedActivity extends AppCompatActivity
{

    EditText edLimit;
    TextView lbName;
    Button btnSave;
    String templateName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_limited);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle( R.string.title_new_simple_excercise );

        Intent in = getIntent();
        templateName = in.getStringExtra( NewExerciseActivity.TEMPLATE_NAME );

        btnSave = findViewById( R.id.btnSave );
        btnSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                save();
            }
        });

        setTitle( R.string.title_new_limited_template );
        btnSave.setEnabled( false );

        lbName = findViewById( R.id.lbName );
        lbName.setText( templateName );

        edLimit = findViewById( R.id.edLimit );
        edLimit.addTextChangedListener( new LimitTextChangeListener () );
        edLimit.requestFocus();
        //setResult( RESULT_CANCELED );
    }

    private void save()
    {
        Template template = new Template();
        template.setLimited( true );
        template.setName( templateName );
        template.setLimit( Integer.parseInt( edLimit.getText().toString() ) );

        DatabaseHelper dbHelper = new DatabaseHelper(this);

        try
        {
            Dao<Template, Integer> dao = dbHelper.getDao( Template.class );
            dao.create( template );
            setResult( RESULT_OK );
            finish();

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }


    class LimitTextChangeListener implements TextWatcher
    {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            String buff = s.toString();
            try
            {
                Integer.parseInt( buff );
                btnSave.setEnabled( true );
            }
            catch (NumberFormatException e )
            {
                btnSave.setEnabled( false );
            }
        }

        @Override
        public void afterTextChanged(Editable s)
        {

        }
    }

}
