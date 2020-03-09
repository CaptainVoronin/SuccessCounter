package org.max.successcounter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import org.jetbrains.annotations.NotNull;
import org.max.successcounter.model.TagsOperator;
import org.max.successcounter.model.excercise.Tag;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DialogTags
{
    Activity ctx;
    TagsOperator tagsOperator;

    /**
     * All tags in tha database
     */
    List<Tag> allTagsList;
    AlertDialog.Builder dialog;
    View contentView;
    TagListAdapter adapter;
    EditText edTagName;
    ListView tagsListView;
    /**
     * Tha tags selected by user in the list
     */
    List<Tag> checkedSet;
    DialogTagsResultListener listener;

    /**
     * This flag enables "add a new tag" functionality
     */
    boolean allowNewTags;

    /**
     * The result code. It can be true if OK pressed and false otherwise
     */
    boolean result;

    public DialogTags(Activity ctx, TagsOperator tagsOperator, DialogTagsResultListener listener, boolean allowNewTags ) throws SQLException
    {
        this.ctx = ctx;
        this.tagsOperator = tagsOperator;
        result = false;
        checkedSet = new ArrayList<>();
        this.listener = listener;
        this.allowNewTags = allowNewTags;
        createDialog();
    }

    private void createDialog() throws SQLException
    {
        LayoutInflater inflater = ctx.getLayoutInflater();
        contentView = inflater.inflate(R.layout.tags_dialog, null);
        dialog = new AlertDialog.Builder(ctx);
        dialog.setTitle( R.string.defaultTagDialogTitle );
        dialog.setView(contentView);

        //Hide "plus" button if creation of new tags is disabled
        ImageView btn = contentView.findViewById( R.id.btnAddTag);

        if( !allowNewTags )
            btn.setVisibility( View.GONE );
        else
            btn.setOnClickListener( v -> saveNewTag());

        fillList();

        dialog.setNegativeButton( android.R.string.cancel, (d,id) -> {
            d.cancel();
            if( listener != null )
                listener.onResult( false, checkedSet );
        });

        dialog.setPositiveButton( android.R.string.ok, (d,id) -> {
            d.dismiss();
            if( listener != null )
                listener.onResult( true, checkedSet );
        });

        edTagName = contentView.findViewById( R.id.edTagName );
        edTagName.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                DialogTags.this.filterTags( s.toString() );
            }

            @Override
            public void afterTextChanged(Editable s)
            {

            }
        });
    }

    public void setTitle( String title )
    {
        dialog.setTitle( title );
    }

    /**
     * It saves a new tag
     */
    private void saveNewTag()
    {
        // Get the new tag name from editor
        String buff = edTagName.getText().toString().trim();

        // Empty lines are ignored
        if( buff.trim().length() == 0 )
            return;

        // Check if there is the same tag already
        Tag tag = new Tag(buff);
        if( allTagsList.contains( tag ) )
            return;

        try
        {
            // TODO: Exception handling must be moved in an other place
            // TODO: It seems better to move the whole operation outside the dialog
            tagsOperator.addTag(tag);
            checkedSet.add( tag );
            edTagName.setText( "" );
            fillList();
        }
        catch ( SQLException e)
        {
            e.printStackTrace();
        }
    }

    void filterTags( String pattern )
    {
        adapter.setFliterText( pattern );
        adapter.notifyDataSetChanged();
    }

    private void fillList() throws SQLException
    {
        allTagsList = tagsOperator.getAll();
        tagsListView = contentView.findViewById(R.id.lvTags);
        adapter = new TagListAdapter(ctx);
        tagsListView.setAdapter( adapter );
    }

    public void showDialog(List<Tag> selectedSet)
    {
        checkedSet = new ArrayList<>();
        if( selectedSet!=null )
            checkedSet.addAll(selectedSet);
        dialog.show();
    }

    class TagListAdapter extends ArrayAdapter<Tag>
    {
        NameFilter filter;
        List<Tag> filteredSet;

        public TagListAdapter(Context ctx)
        {
            super(ctx, R.layout.tags_list_row);
            filter = new NameFilter();
            filter.setPattern( "" );
            applyFilter();
        }

        private void applyFilter()
        {
            filteredSet = allTagsList.stream().filter(filter).collect(Collectors.toList());
        }

        @Override
        public int getCount()
        {
            return filteredSet.size();
        }

        public void setFliterText( String text )
        {
            filter.setPattern( text );
            applyFilter();
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
        {
            LayoutInflater lin = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = lin.inflate( R.layout.tags_list_row, parent, false );
            Tag tag = filteredSet.get(position);
            CheckBox chb = row.findViewById( R.id.chbTag );
            chb.setText( tag.getTag() );
            chb.setTag( tag );
            chb.setChecked( checkedSet.contains( tag ) );
            chb.setOnCheckedChangeListener( (v,status) -> checkChanged( v, status) );
            return row;
        }
    }

    /**
     * It's the callback for the click event
     * @param v - the view where the ckick happened
     * @param status - the status of the check box where the event happened
     */
    private void checkChanged(CompoundButton v, boolean status)
    {
        Tag t = ( Tag ) v.getTag();
        if( status )
            checkedSet.add( t );
        else
            checkedSet.remove( t );
    }

    /**
     * The class is used for filtering tags in the list
     * with a text pattern
     */
    class NameFilter implements Predicate<Tag>
    {
        String pattern;

        @Override
        public boolean test(Tag tag)
        {
            if( pattern.length() == 0 )
                return true;

            String buff = tag.getTag().toLowerCase();
            return buff.contains( pattern );
        }

        public void setPattern( @NotNull String pattern)
        {
            this.pattern = pattern.toLowerCase();
        }
    }

    /**
     * This interface must be implemented to get
     * dialog result
     */
    public interface DialogTagsResultListener
    {
        /**
         * This is callback which will be called when the user presses OK or CANCEL
         * @param result - true if the user pressed OK
         * @param checked - list of selected tags
         */
        void onResult(boolean result, List<Tag> checked);
    }
}