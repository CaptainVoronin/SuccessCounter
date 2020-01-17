package org.max.successcounter.model.excercise;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ElementaryStep implements IStep
{
    Integer points;
    Float percent;
    IExercise exercise;
    Integer id;
}
