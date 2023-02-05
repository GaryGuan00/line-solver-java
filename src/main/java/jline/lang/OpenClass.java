package jline.lang;

import java.io.Serializable;

import jline.lang.constant.JobClassType;
import jline.lang.nodes.Node;
import jline.lang.nodes.Source;

public class OpenClass extends JobClass  implements Serializable {
    protected int classIndex;
    protected Network model;
    public OpenClass(Network model, String name, int priority) {
        super(JobClassType.Open, name);
        model.addJobClass(this);
        this.classIndex = -1;
        this.model = model;
        this.priority = priority;
    }
    public OpenClass(Network model, String name) {
        this(model, name,0);
    }

    @Override
    public void setReference(Node source) throws Exception {
        if (!(source instanceof Source)) {
            throw new Exception("The reference station for an open class must be a jline.Source.");
        }
        super.setReference(source);
    }

    @Override
    public void printSummary() {
        System.out.format("Open class: %s\n", this.getName());
    }

    @Override
    public int getJobClassIdx() {
        if (this.classIndex == -1) {
            this.classIndex = this.model.getJobClassIndex(this);
        }
        return this.classIndex;
    }
}
