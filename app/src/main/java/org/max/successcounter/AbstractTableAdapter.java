package org.max.successcounter;

import android.app.Activity;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.List;

public abstract class AbstractTableAdapter<T>
{
    TableLayout table;
    Activity ctx;
    List<T> items;
    int row_layout_id;

    public AbstractTableAdapter(Activity ctx, int view_id, int row_layout_id, List<T> items)
    {
        super();
        this.ctx = ctx;
        table = ctx.findViewById(view_id);
        this.items = items;
        this.row_layout_id = row_layout_id;
    }

    public Activity getContext()
    {
        return ctx;
    }

    public int getRow_layout_id()
    {
        return row_layout_id;
    }

    public void makeTable()
    {
        table.removeAllViews();
        int i = 0;
        for (T item : items)
        {
            insertRow(item, i);
            i++;
        }
    }

    public TableLayout getTable()
    {
        return table;
    }

    public TableRow insertRow(T item, int index)
    {
        TableRow row = makeRow(item);
        table.addView(row, index);
        return row;
    }

    public final void removeRow(int index)
    {
        table.removeViewAt(index);
    }

    public final List<T> getItems()
    {
        return items;
    }

    public void setItems( List<T> items )
    {
        this.items = items;
    }

    protected abstract TableRow makeRow(T item);

}
