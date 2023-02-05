package jline.solvers.ssa;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import jline.lang.constant.SchedStrategy;

public class SSAStruct  implements Serializable {
        public int nStateful;
        public int nClasses;
        public SchedStrategy[] schedStrategies;
        public int[][] capacities;
        public int[] nodeCapacity;
        public int[] numberOfServers;
        public boolean[] isDelay;
        public int[][] nPhases;
        public Map<Integer, List<Double>>[] startingPhaseProbabilities;
}
