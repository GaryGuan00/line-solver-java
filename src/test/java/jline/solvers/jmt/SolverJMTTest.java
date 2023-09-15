package jline.solvers.jmt;

import jline.examples.*;
import jline.lang.Network;
import jline.lang.constant.SolverType;
import jline.lang.constant.VerboseLevel;
import jline.solvers.NetworkAvgTable;
import jline.solvers.SolverOptions;
import org.junit.jupiter.api.Test;

import javax.xml.parsers.ParserConfigurationException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SolverJMTTest {

    @Test
    public void test_view() throws ParserConfigurationException {
        Network model = ClosedModel.ex1();

        SolverOptions options = new SolverOptions(SolverType.JMT);
        options.seed = 23000;
        options.keep = true;
        SolverJMT solver = new SolverJMT(model, options);

        solver.runAnalyzer();
        solver.jsimgView();
    }

    @Test
    public void test_getting_started_example_1() {
        Network model = GettingStarted.ex1_line();

        SolverOptions options = new SolverOptions(SolverType.JMT);
        options.seed = 23000;
        options.keep = true;
        options.verbose = VerboseLevel.STD;
        options.samples = 10000;
        SolverJMT solver = new SolverJMT(model, options);

        NetworkAvgTable avgTable = solver.getAvgTable();

        List<Double> QLen = avgTable.get(0);
        assertEquals(0, QLen.get(0), 1e-13);
        assertEquals(0.955501010809008, QLen.get(1), 1e-13);

        List<Double> Util = avgTable.get(1);
        assertEquals(0, Util.get(0), 1e-13);
        assertEquals(0.487360218010475, Util.get(1), 1e-13);


        List<Double> RespT = avgTable.get(2);
        assertEquals(0, RespT.get(0), 1e-13);
        assertEquals(0.954292928096683, RespT.get(1), 1e-13);


        List<Double> ResidT = avgTable.get(3);
        assertEquals(0, ResidT.get(0), 1e-13);
        assertEquals(0.954292928096683, ResidT.get(1), 1e-13);

        List<Double> Tput = avgTable.get(4);
        assertEquals(0.998941736137035, Tput.get(0), 1e-13);
        assertEquals(0.99986838711003, Tput.get(1), 1e-13);
    }

    @Test
    public void test_example_forkJoin_1() throws ParserConfigurationException {
        Network model = ForkJoin.ex1_line();

        SolverOptions options = new SolverOptions(SolverType.JMT);
        options.seed = 23000;
        SolverJMT solver = new SolverJMT(model, options);

//        NetworkAvgTable avgTable = solver.getAvgTable();
        solver.jsimgView();
    }

    @Test
    public void test_example_mixedModel_1() throws ParserConfigurationException {
        Network model = MixedModel.ex1_line();

        SolverOptions options = new SolverOptions(SolverType.JMT);
        options.seed = 23000;
        options.keep = true;
        options.cutoff = 3;
        options.verbose = VerboseLevel.STD;
        SolverJMT solver = new SolverJMT(model, options);

        NetworkAvgTable avgTable = solver.getAvgTable();
//        solver.jsimgView();

        // Expected values
        double[] expectedQLen = {1.4528, 0.021961, 0.54717, 0.16391, 0, 0};
        double[] expectedUtil = {1.4528, 0.021961, 0.40216, 0.095781, 0, 0};
        double[] expectedRespT = {0.66644, 0.21581, 0.25983, 1.711, 0, 0};
        double[] expectedResidT = {0.66644, 0.21581, 0.25983, 1.711, 0, 0};
        double[] expectedTput = {2.1536, 0.10001, 2.1602, 0.10001, 0, 0.10001};

        for (int i = 0; i < expectedQLen.length; i++) {
            assertEquals(expectedQLen[i], avgTable.get(0).get(i), 1e-3);
            assertEquals(expectedUtil[i], avgTable.get(1).get(i), 1e-3);
            assertEquals(expectedRespT[i], avgTable.get(2).get(i), 1e-3);
            assertEquals(expectedResidT[i], avgTable.get(3).get(i), 1e-3);
            assertEquals(expectedTput[i], avgTable.get(4).get(i), 1e-3);
        }
    }
}
