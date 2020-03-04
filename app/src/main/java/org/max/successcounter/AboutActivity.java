package org.max.successcounter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView tv = findViewById( R.id.tvProgramName );
        tv.setText( getString( R.string.app_name ) );

        tv = findViewById( R.id.tvVersion );
        tv.setText( "v " + Version.MAJOR + "." + Version.MINOR + "." + Version.BUILD );

        tv = findViewById( R.id.tvYear );
        tv.setText( Version.BUILD_DATE );

        tv = findViewById(R.id.tvTitle);
        tv.setText( getString( R.string.title_activity_about ) );

    }
}
