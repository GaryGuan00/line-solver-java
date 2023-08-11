package jline.util;

import java.util.Map;

import jline.lang.JobClass;
import jline.lang.nodes.Node;

public class RoutingMatrix {
	
	public Matrix rt;
	public Matrix rtnodes;
	public Matrix linksmat;
	public Matrix chains;
	public Map<JobClass, Map<JobClass, Matrix>> rtNodesByClass;
	public Map<Node, Map<Node, Matrix>> rtNodesByStation;
	
	public RoutingMatrix(Matrix rt, Matrix rtnodes, Matrix linksmat,
                         Matrix chains, Map<JobClass, Map<JobClass, Matrix>> rtNodesByClass, Map<Node, Map<Node, Matrix>> rtNodesByStation) {
		this.rt = rt;
		this.rtnodes = rtnodes;
		this.linksmat = linksmat;
		this.chains = chains;
		this.rtNodesByClass = rtNodesByClass;
		this.rtNodesByStation = rtNodesByStation;
	}
}
