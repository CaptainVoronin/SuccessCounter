package org.max.successcounter;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import com.j256.ormlite.dao.Dao;
import org.max.successcounter.db.DatabaseHelper;
import org.max.successcounter.model.excercise.Template;
import java.sql.SQLException;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class NewSimpleExActivity extends AppCompatActivity
{

    EditText edLimit;
    EditText edName;
    Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_simple_ex);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle( R.string.title_new_simple_excercise );

        btnSave = findViewById( R.id.btnSave );
        btnSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                save();
            }
        });

        edName = findViewById( R.id.edName );
        edLimit = findViewById( R.id.edLimit );
        edLimit.setEnabled( false );

        RadioGroup rg = findViewById( R.id.rgLimits );
        rg.setOnCheckedChangeListener( new RadioGroup.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                if( checkedId == R.id.rbLimited)
                    edLimit.setEnabled( true );
                else
                    edLimit.setEnabled( false );
            }
        });
        setResult( RESULT_CANCELED );
    }

    private void save()
    {
        Template ex = new Template();

        ex.setName( edName.getText().toString() );
        RadioGroup rg = findViewById( R.id.rgLimits );
        if( rg.getCheckedRadioButtonId() == R.id.rbUnlimited )
        {
            ex.setLimited(false);
            ex.setLimit(0);
        }
        else
        {
            ex.setLimited( true );
            ex.setLimit( Integer.parseInt( edLimit.getText().toString() ) );
        }
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        try
        {
            Dao<Template, Integer> dao = dbHelper.getDao( Template.class );
            dao.create( ex );
            setResult( RESULT_OK );
            finish();

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}
