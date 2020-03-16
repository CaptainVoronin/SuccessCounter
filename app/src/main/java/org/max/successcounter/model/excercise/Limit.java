package org.max.successcounter.model.excercise;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Limit
{
    @Getter
    int value;
    @Getter
    Type type;

    enum Type
    {total, success}

}
