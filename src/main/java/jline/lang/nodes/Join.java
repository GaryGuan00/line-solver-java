package jline.lang.nodes;

import java.io.Serializable;

import jline.lang.*;
import jline.lang.sections.Joiner;

public class Join extends Station implements Serializable {
	public Node joinOf;
    protected Network model;
    public Join(Network model) {
        super("Join");
        model.addNode(this);
        this.model = model;
        this.input = new Joiner(model);
    }

    @Override
    public Network getModel() {
        return this.model;
    }
}
