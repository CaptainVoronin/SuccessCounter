package org.max.successcounter;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ViewSwitcher;

import org.max.successcounter.model.excercise.CompoundExercise;

public class CompoundExerciseActivity extends LineChartExerciseActivity<CompoundExercise>
{
    ViewSwitcher switcher;

    @Override
    protected void prepareControlButtons(LinearLayout placeholder)
    {
        CompoundExercise cx = getExercise();
        LayoutInflater lif = getLayoutInflater();
        LinearLayout lc = (LinearLayout) lif.inflate(R.layout.compound_exercise_buttons, placeholder, true);
        switcher = lc.findViewById(R.id.btnSwitcher);

        LinearLayout ll = placeholder.findViewById(R.id.llButtons);
        cx.getOptions().stream().sorted((o1, o2) -> Integer.compare(o1.getOrderNum(), o2.getOrderNum()))
                .forEach(item -> {
                    ll.addView(makeButton(item));
                });
    }

    Button makeButton(CompoundExercise.Option option)
    {
        GradientDrawable gdw = new GradientDrawable();
        gdw.setColor(option.getColor());
        gdw.setCornerRadius(20);
        gdw.setStroke(3, getColor(R.color.colorAccent));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                1, 0.25f);

        lp.setMargins(15, 0, 15, 5);

        Button btn = new Button(this);
        btn.setText(option.getDescription());
        btn.setTextColor(getColor(R.color.colorAccent));
        btn.setTag(option);
        btn.setOnClickListener(new OnOptionClick());
        btn.setBackground(gdw);
        btn.setLayoutParams(lp);

        return btn;
    }

    @Override
    public void onExerciseFinished()
    {
        switcher.showNext();
    }

    @Override
    protected void onExerciseResumed()
    {
        switcher.showNext();
    }

    public class OnOptionClick implements Button.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            Object tag = v.getTag();
            CompoundExercise.Option opt = (CompoundExercise.Option) tag;
            addNewShot(opt.getPoints());
        }
    }
}
