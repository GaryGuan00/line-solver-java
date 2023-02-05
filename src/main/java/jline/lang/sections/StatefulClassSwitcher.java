package jline.lang.sections;

import java.io.Serializable;
import java.util.List;

import jline.lang.JLineMatrix;
import jline.lang.JobClass;

public class StatefulClassSwitcher extends ClassSwitcher implements Serializable {
	public StatefulClassSwitcher(List<JobClass> jobClasses) {
        super(jobClasses, "jline.StatelessClassSwitcher");
    }
}
