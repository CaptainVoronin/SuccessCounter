package org.max.successcounter;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import org.max.successcounter.model.excercise.CompoundExcercise;

public class CompoundExerciseActivity extends AExerciseActivity<CompoundExcercise>
{

    @Override
    public int getViewID()
    {
        return R.layout.activity_compound_exercise;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
            LinearLayout ll = findViewById( R.id.llOptionsLayout );
            CompoundExcercise cx = ( CompoundExcercise ) getExercise();
            cx.getOptions().forEach( item->{
                ll.addView( makeButton( item ) );
            } );

        setResult(RESULT_CANCELED);
    }

    @Override
    public void onExerciseFinished()
    {

    }

    public class OnOptionClick implements Button.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            Object tag = v.getTag();
            CompoundExcercise.Option opt = (CompoundExcercise.Option ) tag;
            addStep( opt.getPoints() );
        }
    }

    Button makeButton( CompoundExcercise.Option option )
    {
        Button btn = new Button( this );
        btn.setText( option.getDescription() );
        btn.setTag( option );
        btn.setOnClickListener( new OnOptionClick() );
        return btn;
    }
}
