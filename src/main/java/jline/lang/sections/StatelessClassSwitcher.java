package jline.lang.sections;

import java.io.Serializable;
import java.util.List;
import jline.lang.*;
import jline.lang.distributions.*;
import jline.lang.nodes.*;
import jline.lang.sections.*;

public class StatelessClassSwitcher extends ClassSwitcher implements Serializable {
    public StatelessClassSwitcher(List<JobClass> jobClasses, JLineMatrix csMatrix) {
        super(jobClasses, "jline.StatelessClassSwitcher");
        
        this.csFun = (input) -> csMatrix.get(input.r, input.s);
    }
    
    public void updateCsMatrix(JLineMatrix csMatrix) {
    	this.csFun = (input) -> csMatrix.get(input.r, input.s);
    }
}
