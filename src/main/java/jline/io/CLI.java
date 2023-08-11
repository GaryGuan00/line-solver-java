package jline.io;

import jline.lang.LayeredNetwork;
import jline.solvers.ln.SolverLN;

import java.util.ArrayList;
import java.util.List;

public class CLI {
    
    public static void solverLN(String[] args) throws  Exception{
        List<LayeredNetwork> layeredNetworks = new ArrayList<>(args.length);
        for (String arg : args) {
            layeredNetworks.add(LayeredNetwork.parseXML(arg, 0));
        }
        List<SolverLN> solverLNs = new ArrayList<>(layeredNetworks.size());
        for (LayeredNetwork layeredNetwork : layeredNetworks) {
            solverLNs.add(new SolverLN(layeredNetwork));
        }

        for(int i = 0;i<solverLNs.size();i++){
            System.out.println("--------------------------------------------------------------------------------------");
            System.out.println("The result for LayerNetwork"+ i+":");
            solverLNs.get(i).getEnsembleAvg();
            System.out.println("--------------------------------------------------------------------------------------");
        }

    }
}
