package jline.lang;

import jline.lang.constant.JobClassType;
import jline.lang.nodes.Station;

import java.io.Serializable;

public class SelfLoopingClass extends ClosedClass implements Serializable {

    public SelfLoopingClass(Network model, String name, long njobs, Station refstat, int priority) {
        super(model, name,njobs, refstat,priority);
    }
    public SelfLoopingClass(Network model, String name, long njobs, Station refstat) {
        this(model, name,njobs, refstat,0);
    }

}
