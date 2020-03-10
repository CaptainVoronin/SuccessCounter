package org.max.successcounter.model;

import org.max.successcounter.model.excercise.Result;

import java.util.Comparator;

import lombok.Getter;
import lombok.Setter;

public class ResultDateComparator implements Comparator<Result>
{
    @Getter
    @Setter
    boolean reverseOrder;

    public ResultDateComparator()
    {
        reverseOrder = false;
    }

    public ResultDateComparator(boolean reverseOrder)
    {
        this.reverseOrder = reverseOrder;
    }

    @Override
    public int compare(Result o1, Result o2)
    {
        int directResult = 0;

        if( o1 == null )
            directResult = -1;

        if( o2 == null )
            directResult = 1;

        if(  o2 == null && o1 == null )
            directResult = 0;

        directResult = Long.compare(o1.getDate().getTime(), o2.getDate().getTime());

        return directResult * (reverseOrder ? -1 : 1);
    }
}
