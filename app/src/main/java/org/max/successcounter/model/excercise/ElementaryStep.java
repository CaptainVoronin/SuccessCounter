package org.max.successcounter.model.excercise;

import lombok.Data;

@Data
public class ElementaryStep implements IStep
{
    Integer points;
    Float percent;
    IExercise exercise;
    Integer id;
}
