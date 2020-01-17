package org.max.successcounter;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

@FieldDefaults( makeFinal = true, level = AccessLevel.MODULE)
public class ActivityIDs
{
    static int HISTORYACTIVITY_ID = 1;
    static int EXERCISEACTIVITY_ID = 2;
    static int EXERCISE_PROGRESS_ACTIVITY_ID = 3;
    static int NEWSIMPLEEXERCISE_ID = 4;
    static int NEW_TEMPLATE_ACTIVITY_ID = 5;
}
