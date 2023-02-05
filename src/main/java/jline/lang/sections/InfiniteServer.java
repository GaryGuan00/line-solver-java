package jline.lang.sections;

import java.io.Serializable;
import java.util.List;

import jline.lang.*;
import jline.lang.distributions.*;
import jline.lang.nodes.*;
import jline.lang.sections.*;

public class InfiniteServer extends Server implements Serializable {
    public InfiniteServer(List<JobClass> jobClasses) {
        super(jobClasses);
        this.numberOfServers = Double.POSITIVE_INFINITY;
        this.className = "jline.InfiniteServer";
    }
}
