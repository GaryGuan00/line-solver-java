package jline.lang.sections;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import jline.lang.*;
import jline.lang.distributions.*;
import jline.lang.nodes.*;
import jline.lang.sections.*;
import jline.util.CSFunInput;

public class ClassSwitcher extends ServiceSection implements Serializable {
	protected Function<CSFunInput, Double> csFun;
    protected List<JobClass> jobClasses;
    
    public ClassSwitcher(List<JobClass> jobClasses, String name) {
        super(name);

        this.jobClasses = jobClasses;
        this.numberOfServers = 1;
        this.serviceProcesses = new HashMap<JobClass, ServiceBinding>();
    }
    
    public double applyCsFun(int r, int s) {
    	return this.csFun.apply(new CSFunInput(r, s, null, null));
    }
}
