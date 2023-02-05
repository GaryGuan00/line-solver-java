package jline.lang.nodes;

import java.io.Serializable;

import jline.lang.sections.Forker;
import jline.lang.*;
import jline.lang.distributions.*;
import jline.lang.nodes.*;
import jline.lang.sections.*;

public class Fork extends Node implements Serializable {
    protected Network model;
    public Fork(Network model) {
        super("Fork");
        model.addNode(this);
        this.model = model;
        this.output = new Forker(model);
    }

    @Override
    public Network getModel() {
        return this.model;
    }
}
