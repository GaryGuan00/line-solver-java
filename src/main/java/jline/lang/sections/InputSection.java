package jline.lang.sections;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import jline.lang.*;
import jline.lang.constant.SchedStrategyType;
import jline.lang.distributions.*;
import jline.lang.nodes.*;
import jline.lang.sections.*;


public class InputSection extends Section  implements Serializable {
    protected SchedStrategyType schedPolicy;
    protected List<InputBinding> inputJobProcesses;

    public InputSection(String className) {
        super(className);
        this.inputJobProcesses = new ArrayList<InputBinding>();
    }

    public void setInputJobProcess(InputBinding process) {
        removeInputJobProcess(process.getJobClass());
        inputJobProcesses.add(process);
    }

    protected void removeInputJobProcess(JobClass jobClass) {
        Iterator<InputBinding> inputJobProcessIter = this.inputJobProcesses.iterator();
        while (inputJobProcessIter.hasNext()) {
            if (inputJobProcessIter.next().getJobClass() == jobClass) {
                inputJobProcessIter.remove();
            }
        }
    }

    public void setServiceProcess(ServiceBinding serviceProcess) {

    }
}
