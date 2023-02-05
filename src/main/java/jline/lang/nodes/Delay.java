package jline.lang.nodes;

import java.io.Serializable;
import jline.lang.*;
import jline.lang.constant.SchedStrategy;
import jline.lang.*;

public class Delay extends Queue implements Serializable {
    public Delay(Network model, String name) {
        super(model, name, SchedStrategy.INF);
        this.numberOfServers = Integer.MAX_VALUE;
    }
}
