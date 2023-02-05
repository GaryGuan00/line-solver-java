package jline.util;

import java.util.Map;

import jline.lang.JLineMatrix;
import jline.lang.JobClass;
import jline.lang.nodes.Node;

public class GetRoutingMatrixReturnType {
	
	public JLineMatrix rt;
	public JLineMatrix rtnodes;
	public JLineMatrix linksmat;
	public JLineMatrix chains;
	public Map<JobClass, Map<JobClass, JLineMatrix>> rtNodesByClass;
	public Map<Node, Map<Node, JLineMatrix>> rtNodesByStation;
	
	public GetRoutingMatrixReturnType(JLineMatrix rt, JLineMatrix rtnodes, JLineMatrix linksmat,
			JLineMatrix chains, Map<JobClass, Map<JobClass, JLineMatrix>> rtNodesByClass, Map<Node, Map<Node, JLineMatrix>> rtNodesByStation) {
		this.rt = rt;
		this.rtnodes = rtnodes;
		this.linksmat = linksmat;
		this.chains = chains;
		this.rtNodesByClass = rtNodesByClass;
		this.rtNodesByStation = rtNodesByStation;
	}
}
