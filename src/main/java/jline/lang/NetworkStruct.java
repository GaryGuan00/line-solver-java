package jline.lang;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import jline.lang.constant.DropStrategy;
import jline.lang.constant.NodeType;
import jline.lang.constant.ProcessType;
import jline.lang.constant.RoutingStrategy;
import jline.lang.constant.SchedStrategy;
import jline.lang.nodes.Node;
import jline.lang.nodes.Station;
import jline.util.NodeParam;
import jline.util.Pair;
import jline.util.Sync;

public class NetworkStruct implements Serializable {
    public int nStateful;
    public int nClasses;
    public SchedStrategy[] schedStrategies;
    public int[][] capacities;
    public int[] nodeCapacity;
    public int[] numberOfServers;
    public boolean[] isDelay;
    //Followings are newly added
    //For data structure, {} is represented by HashMap, [] is represented by ArrayList, [][] is represented by matrix;
    //For the matrix that stores Constant. Use double list instead.
    public int nNodes;
    public double nclosedjobs;
    public int nstations;
    public int nchains;

    public Map<JobClass, Map<JobClass, JLineMatrix>> rtorig;
    public Map<Station, Map<JobClass, Function<Double, Double>>> lst;
    public Map<Station, JLineMatrix> state;
    public Map<Station, JLineMatrix> statePrior;
    public Map<Station, JLineMatrix> space;
    public Map<Node, Map<JobClass, RoutingStrategy>> routing;
    public Map<Station, Map<JobClass, ProcessType>> proctype;
    public Map<Station, Map<JobClass, JLineMatrix>> mu;
    public Map<Station, Map<JobClass, JLineMatrix>> phi;
    public Map<Station, Map<JobClass, Map<Integer, JLineMatrix>>> proc;
    public Map<Station, Map<JobClass, JLineMatrix>> pie;
    public Map<Station, SchedStrategy> sched;
    public Map<Integer, JLineMatrix> inchain;
    public Map<Integer, JLineMatrix> visits;	//The integer represents the chain's ID (inchain)
    public Map<Integer, JLineMatrix> nodevisits; //The integer represents the chain's ID (inchain)
    public Map<Station, Map<JobClass, DropStrategy>> dropRule;	//This represents dropid in LINE
	public Map<Node, NodeParam> nodeparam;
	public Map<Integer, Sync> sync;
	public Map<Station, Function<JLineMatrix, Double>> cdscaling;
    
    public JLineMatrix refstat;
    public JLineMatrix njobs;
    public JLineMatrix nservers;
    public JLineMatrix connmatrix;
    public JLineMatrix scv;
    public JLineMatrix isstation;
    public JLineMatrix isstateful;
    public JLineMatrix isstatedep;
    public JLineMatrix nodeToStateful;
    public JLineMatrix nodeToStation;
    public JLineMatrix stationToNode;
    public JLineMatrix stationToStateful;
    public JLineMatrix statefulToNode;
    public JLineMatrix rates;
    public JLineMatrix classprio;
    public JLineMatrix phases;
    public JLineMatrix phasessz;
    public JLineMatrix phaseshift;
    public JLineMatrix schedparam;
    public JLineMatrix chains;
    public JLineMatrix rt;
    public JLineMatrix nvars;
    public JLineMatrix rtnodes;
    public JLineMatrix csmask;
    public JLineMatrix isslc;
    public JLineMatrix cap;
    public JLineMatrix classcap;
    public JLineMatrix refclass;
    public JLineMatrix lldscaling;
    public JLineMatrix fj;
    
    Function<Pair<Map<Node, JLineMatrix>, Map<Node, JLineMatrix>>, JLineMatrix> rtfun;
    
    public List<NodeType> nodetypes;
    public List<String> classnames;
    public List<String> nodenames;
    public List<Station> stations;
    public List<JobClass> jobClasses;
    public List<Node> nodes;
}
