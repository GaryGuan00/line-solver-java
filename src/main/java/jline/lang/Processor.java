package jline.lang;


import jline.lang.constant.SchedStrategy;

public class Processor extends Host{
    public Processor(LayeredNetwork myLN, String name, int multiplicity, SchedStrategy scheduling, double quantum, double speedFactor) {
        super(myLN, name, multiplicity, scheduling, quantum, speedFactor);
    }

    public Processor(LayeredNetwork myLN, String name, int multiplicity, SchedStrategy scheduling, double quantum) {
        super(myLN, name, multiplicity, scheduling, quantum, 1);
    }

    public Processor(LayeredNetwork myLN, String name, int multiplicity, SchedStrategy scheduling) {
        super(myLN, name, multiplicity, scheduling, 0.001, 1);
    }

    public Processor(LayeredNetwork myLN, String name, int multiplicity) {
        super(myLN, name, multiplicity, SchedStrategy.PS,0.001, 1);
    }

    public Processor(LayeredNetwork myLN, String name) {
        super(myLN, name, 1, SchedStrategy.PS,0.001, 1);
    }
}
