package jline.solvers.ssa.events;
import jline.lang.nodes.Node;

public interface NodeEvent {
    public Node getNode();
    public int getNodeStatefulIdx();
    public int getClassIdx();
    public boolean isStateful();
}
