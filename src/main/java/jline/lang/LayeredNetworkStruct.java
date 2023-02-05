package jline.lang;

import jline.lang.constant.SchedStrategy;
import jline.lang.distributions.Distribution;

import java.util.List;
import java.util.Map;

public class LayeredNetworkStruct {

    protected int nidx;
    protected int nhosts;
    protected int ntasks;
    protected int nreftasks;
    protected int nacts;
    protected int nentries;
    protected JLineMatrix ntasksof;
    protected JLineMatrix nentriesof;
    protected JLineMatrix nactsof;
    protected int tshift;
    protected int eshift;
    protected int ashift;
    protected JLineMatrix hostidx;
    protected JLineMatrix taskidx;
    protected JLineMatrix entryidx;
    protected JLineMatrix actidx;
    protected Map<Integer, Integer> tasksof;
    protected Map<Integer, List<Integer>> entriesof;
    protected Map<Integer, List<Integer>> actsof;
    protected Map<Integer, List<Integer>> callsof;
    protected Map<Integer, Distribution> hostdem;
    protected Map<Integer, Distribution> think;
    protected Map<Integer, SchedStrategy> sched;
    protected JLineMatrix schedid;
    protected Map<Integer, String> names;
    protected Map<Integer, String> hashnames;
    protected JLineMatrix mult;
    protected JLineMatrix repl;
    protected JLineMatrix type;
    protected JLineMatrix graph;
    protected List<Boolean> replies;
    protected JLineMatrix replygraph;
    protected Map<Integer, Integer> nitems;
    protected Map<Integer, Integer> itemlevelcap;
    protected Map<Integer, String> replacementpolicy;
    protected Map<Integer, Integer> nitemsof;
    protected Map<Integer, Distribution> itemsdistribution;
    protected JLineMatrix iscache;
    protected JLineMatrix parent;
    protected JLineMatrix callidx;
    protected JLineMatrix calltype;
    protected JLineMatrix iscaller;
    protected JLineMatrix issynccaller;
    protected JLineMatrix isasynccaller;
    protected JLineMatrix callpair;
    protected JLineMatrix taskgraph;
    protected Map<Integer, Geometric> callproc;
    protected Map<Integer, String> callnames;
    protected Map<Integer, String> callhashnames;
    protected JLineMatrix taskgrpaph;
    protected JLineMatrix actpretype;
    protected JLineMatrix actposttype;

    protected int ncalls;
    protected boolean isref;

};
