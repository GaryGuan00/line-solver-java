package jline.lang.sections;

import java.io.Serializable;
import java.util.List;

import jline.lang.JobClass;

public class StatefulClassSwitcher extends ClassSwitcher implements Serializable {
    public StatefulClassSwitcher(List<JobClass> jobClasses, String name){
        super(jobClasses, name);
    }
	public StatefulClassSwitcher(List<JobClass> jobClasses) {
        this(jobClasses, "StatelessClassSwitcher");
    }
}
