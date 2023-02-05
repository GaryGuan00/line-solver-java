package jline.lang.nodes;

import java.io.Serializable;

import jline.lang.Network;
import jline.lang.sections.Forker;

public class Logger extends Node implements Serializable {

    protected Network model;
    public Logger(Network model) {
        super("Logger");
        model.addNode(this);
        this.model = model;
    }

    @Override
    public Network getModel() {
        return this.model;
    }

}
