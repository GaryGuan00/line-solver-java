package jline.lang.nodes;

import java.io.Serializable;
import java.util.List;

import jline.lang.*;
import jline.lang.sections.Dispatcher;
import jline.lang.sections.Joiner;
import jline.lang.sections.ServiceTunnel;

public class Join extends Station implements Serializable {
	public Node joinOf;

    public Join(Network model){
        this(model, "Join");
    }

    public Join(Network model, String name){
        this(model, name, null);
    }

    public Join(Network model, String name, Node fork) {
        super(name);
        List<JobClass> classes = model.getClasses();
        this.input = new Joiner(classes);
        this.output = new Dispatcher(classes);
        this.server = new ServiceTunnel();
        this.numberOfServers = Integer.MAX_VALUE;
        this.model = model;
        this.joinOf = fork;
        model.addNode(this);
    }

    @Override
    public Network getModel() {
        return this.model;
    }
}
