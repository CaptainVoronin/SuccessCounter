package org.max.successcounter;

import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import org.max.successcounter.model.excercise.CompoundExercise;

public class CompoundExerciseActivity extends LineChartExerciseActivity<CompoundExercise>
{

    @Override
    protected void prepareControlButtons(LinearLayout placeholder)
    {
        CompoundExercise cx = getExercise();

        LayoutInflater lif = getLayoutInflater();
        LinearLayout ll = (LinearLayout) lif.inflate( R.layout.compound_exercise_buttons, placeholder, true );
        ll.setOrientation( LinearLayout.VERTICAL );
        ll.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL);
        ll.setWeightSum( 0.5f );
        cx.getOptions().forEach( item->{
            ll.addView( makeButton( item ) );
        } );
    }

    Button makeButton(CompoundExercise.Option option)
    {
        GradientDrawable gdw = new GradientDrawable();
        gdw.setColor(option.getColor());
        gdw.setCornerRadius( 20 );
        gdw.setStroke(3, getColor(R.color.colorAccent));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        lp.setMargins( 15, 10, 15, 10 );

        Button btn = new Button( this );
        btn.setText( option.getDescription() );
        btn.setTextColor(getColor(R.color.colorAccent));
        btn.setTag( option );
        btn.setOnClickListener( new OnOptionClick() );
        btn.setBackground( gdw );
        btn.setLayoutParams( lp );
        return btn;
    }

    public class OnOptionClick implements Button.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            Object tag = v.getTag();
            CompoundExercise.Option opt = (CompoundExercise.Option) tag;
            addStep(opt.getPoints());
        }
    }
}
