package org.max.successcounter;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;

import org.max.successcounter.db.DatabaseHelper;
import org.max.successcounter.model.excercise.Template;

import java.sql.SQLException;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class NewExerciseActivity extends AppCompatActivity
{
    public final static String TEMPLATE_NAME = "TEMPLATE_NAME";
    public static final String TEMPLATE_LIMIT = "TEMPLATE_LIMIT";
    public static final String SUCCESS_LIMITED = "SUCCESS_LIMITED";

    final static String DEFAULT_TEXT = "*название";

    EditText edName;
    EditText edLimitValue;
    ComplexButton btnNewSimple;
    ComplexButton btnNewCompound;
    ComplexButton btnNewSeries;
    RadioButton rbLimitSuccess;
    RadioButton rbLimitTotal;

    Dao<Template, Integer> templateDao;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_exercise);
        boolean nameChanged = false;

        LinearLayout ll = findViewById(R.id.container);

        edName = findViewById(R.id.edName);
        edName.setOnFocusChangeListener((View v, boolean hasFocus) ->
        {
            if (hasFocus)
            {
                edName.setTypeface(getRegularFont());
                if (!nameChanged)
                    edName.setText("");
                else
                    edName.setTypeface(getItalicFont());
            } else
            {
                if (edName.getText().length() == 0)
                {
                    edName.setText(DEFAULT_TEXT);
                    edName.setTypeface(getItalicFont());
                }
            }
        });

        edLimitValue = findViewById(R.id.edLimit);
        edLimitValue.addTextChangedListener(new LimitTextWatcher());

        edName.addTextChangedListener(new NameChangeListener());
        rbLimitSuccess = findViewById(R.id.rbSuccess);
        rbLimitTotal = findViewById(R.id.rbTotal);
        createExerciseTypeButtons(ll);

        makeToolbar();
    }

    private void createExerciseTypeButtons(LinearLayout ll)
    {
        btnNewSimple = new ComplexButton(this, getString(R.string.msgNewSimpleUnlimExTitle),
                getString(R.string.msgNewSimpleExComment), new JustSaveAndReturn(Template.Type.series));

        ll.addView(btnNewSimple.inflate());
        btnNewSimple.setEnabled(false);

        btnNewCompound = new ComplexButton(this, getString(R.string.msgNewCompoundExTitle),
                getString(R.string.msgNewCompoundExComment), new OnBtnClickListener(NewCompoundActivity.class));

        ll.addView(btnNewCompound.inflate());
        btnNewCompound.setEnabled(false);

        btnNewSeries = new ComplexButton(this, getString(R.string.msgNewSerisExTitle),
                getString(R.string.msgNewSerisExText), new JustSaveAndReturn(Template.Type.runTo));
        ll.addView(btnNewSeries.inflate());

        btnNewSeries.setEnabled(false);
    }

    public void makeToolbar()
    {
        TextView tv = findViewById(R.id.tvTitle);
        tv.setText(R.string.msgNewExerciseActivityTitle);
    }

    Typeface getItalicFont()
    {
        return Typeface.create("serif", Typeface.ITALIC);
    }

    Typeface getRegularFont()
    {
        return Typeface.create("serif", Typeface.NORMAL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if (resultCode == RESULT_OK)
        {
            setResult(RESULT_OK);
            finish();
        }
    }

    private boolean isNameCorrect()
    {
        String name = edName.getText().toString();

        if (name.length() == 0)
            return false;

        if (name.equals(DEFAULT_TEXT))
            return false;

        return true;
    }

    private int getLimit()
    {
        String buff = edLimitValue.getText().toString();
        if (buff.trim().length() != 0)
            return Integer.parseInt(buff);
        else
            return 0;
    }

    class OnBtnClickListener implements View.OnClickListener
    {
        Class clazz;

        public OnBtnClickListener(Class clazz)
        {
            this.clazz = clazz;
        }

        @Override
        public void onClick(View v)
        {
            if (!isNameCorrect())
                return;

            Intent in = new Intent(NewExerciseActivity.this, clazz);
            in.putExtra(TEMPLATE_NAME, edName.getText().toString());
            in.putExtra(TEMPLATE_LIMIT, getLimit());
            in.putExtra(SUCCESS_LIMITED, rbLimitSuccess.isChecked());
            // TODO : What is 10?
            startActivityForResult(in, 10);
        }
    }

    class NameChangeListener implements TextWatcher
    {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            if (isNameCorrect())
            {
                btnNewSimple.setEnabled(true);
                btnNewCompound.setEnabled(true);
                if (getLimit() != 0)
                    btnNewSeries.setEnabled(true);
            } else
            {
                btnNewSimple.setEnabled(false);
                btnNewCompound.setEnabled(false);
                btnNewSeries.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s)
        {

        }
    }

    class JustSaveAndReturn implements View.OnClickListener
    {
        Template.Type type;

        public JustSaveAndReturn(Template.Type type)
        {
            this.type = type;
        }

        @Override
        public void onClick(View v)
        {
            if (!isNameCorrect())
                return;

            Template t = new Template();
            t.setExType(type);
            t.setName(edName.getText().toString());

            if (getLimit() != 0)
            {
                t.setSuccesLimited(rbLimitSuccess.isChecked());
                t.setLimit(getLimit());
            }

            DatabaseHelper db = new DatabaseHelper(NewExerciseActivity.this);
            try
            {
                templateDao = db.getDao(Template.class);
                templateDao.create(t);
                setResult(RESULT_OK);
                finish();
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

    private class LimitTextWatcher implements TextWatcher
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
                int value = Integer.parseInt(buff);
                if (value != 0)
                {
                    rbLimitSuccess.setEnabled(true);
                    rbLimitTotal.setEnabled(true);
                    if (isNameCorrect())
                        btnNewSeries.setEnabled(true);
                } else
                {
                    rbLimitSuccess.setEnabled(false);
                    rbLimitTotal.setEnabled(false);
                    btnNewSeries.setEnabled(false);
                }
            } catch (NumberFormatException e)
            {
                rbLimitSuccess.setEnabled(false);
                rbLimitTotal.setEnabled(false);
                btnNewSeries.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s)
        {

        }
    }

}
