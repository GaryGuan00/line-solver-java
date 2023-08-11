package jline.solvers.mva;

import jline.lang.*;
import jline.lang.constant.*;
import jline.lang.nodes.*;
import jline.lang.distributions.*;
import jline.lang.nodes.Queue;

import jline.solvers.SolverOptions;
import jline.util.NetworkAvgTable;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SolverMVAClosedExamplesTest {


	@Test
	public void test_example_closedModel_1() throws IllegalAccessException {
		Network model = new Network("example_closedModel_1");

		// Block 1: nodes			
		Delay node1 = new Delay(model, "Delay");
		Queue node2 = new Queue(model, "Queue1", SchedStrategy.FCFS);

		// Block 2: classes
		ClosedClass jobclass1 = new ClosedClass(model, "Class1", 10, node1, 0);
		
		node1.setService(jobclass1, Exp.fitMean(1.000000)); // (Delay,Class1)
		node2.setService(jobclass1, Exp.fitMean(1.500000)); // (Queue1,Class1)

		// Block 3: topology	
		RoutingMatrix routingMatrix = new RoutingMatrix(model,
                Collections.singletonList(jobclass1),
			 Arrays.asList(node1, node2));
	
		routingMatrix.addConnection(jobclass1, jobclass1, node1, node1, 0.700000); // (Delay,Class1) -> (Delay,Class1)
		routingMatrix.addConnection(jobclass1, jobclass1, node1, node2, 0.300000); // (Delay,Class1) -> (Queue1,Class1)
		routingMatrix.addConnection(jobclass1, jobclass1, node2, node1, 1.000000); // (Queue1,Class1) -> (Delay,Class1)

		model.link(routingMatrix);

		SolverMVA solver = new SolverMVA(model);

		NetworkAvgTable avgTable = solver.getAvgTable();

		List<Double> QLen = avgTable.get(0);
		assertEquals(2.2178051199232454, QLen.get(0), 1e-13);
		assertEquals(7.782194880076755, QLen.get(1), 1e-13);

		List<Double> Util = avgTable.get(1);
		assertEquals(2.2178051199232454, Util.get(0), 1e-13);
		assertEquals(0.9980123039654604, Util.get(1), 1e-13);

		List<Double> RespT = avgTable.get(2);
		assertEquals(1.0000000000000000, RespT.get(0), 1e-13);
		assertEquals(11.696541489251146, RespT.get(1), 1e-13);

		List<Double> ResidT = avgTable.get(3);
		assertEquals(1.0000000000000000, ResidT.get(0), 1e-13);
		assertEquals(3.5089624467753437, ResidT.get(1), 1e-13);

		List<Double> Tput = avgTable.get(4);
		assertEquals(2.2178051199232454, Tput.get(0), 1e-13);
		assertEquals(0.6653415359769735, Tput.get(1), 1e-13);
	}

	@Test
	public void test_example_closedModel_2() throws IllegalAccessException {
		Network model = new Network("example_closedModel_2");

		// Block 1: nodes			
		Delay node1 = new Delay(model, "Delay");
		Queue node2 = new Queue(model, "Queue1", SchedStrategy.PS);
		Router node3 = new Router(model, "CS_Delay_to_Delay"); // Dummy node, class switching is embedded in the routing matrix P 
		Router node4 = new Router(model, "CS_Queue1_to_Delay"); // Dummy node, class switching is embedded in the routing matrix P 

		// Block 2: classes
		ClosedClass jobclass1 = new ClosedClass(model, "Class1", 2, node1, 0);
		ClosedClass jobclass2 = new ClosedClass(model, "Class2", 2, node1, 0);
		
		node1.setService(jobclass1, APH.fitMeanAndSCV(0.666667,0.500000)); // (Delay,Class1)
		node1.setService(jobclass2, APH.fitMeanAndSCV(0.216667,1.579882)); // (Delay,Class2)
		node2.setService(jobclass1, APH.fitMeanAndSCV(0.190000,5.038781)); // (Queue1,Class1)
		node2.setService(jobclass2, Exp.fitMean(1.000000)); // (Queue1,Class2)

		// Block 3: topology	
		RoutingMatrix routingMatrix = new RoutingMatrix(model,
			 Arrays.asList(jobclass1, jobclass2),
			 Arrays.asList(node1, node2, node3, node4));
	
		routingMatrix.addConnection(jobclass1, jobclass1, node1, node2, 0.100000); // (Delay,Class1) -> (Queue1,Class1)
		routingMatrix.addConnection(jobclass1, jobclass1, node1, node3, 0.900000); // (Delay,Class1) -> (CS_Delay_to_Delay,Class1)
		routingMatrix.addConnection(jobclass1, jobclass1, node2, node4, 1.000000); // (Queue1,Class1) -> (CS_Queue1_to_Delay,Class1)
		routingMatrix.addConnection(jobclass1, jobclass1, node3, node1, 0.333333); // (CS_Delay_to_Delay,Class1) -> (Delay,Class1)
		routingMatrix.addConnection(jobclass1, jobclass1, node4, node1, 0.200000); // (CS_Queue1_to_Delay,Class1) -> (Delay,Class1)
		routingMatrix.addConnection(jobclass1, jobclass2, node3, node1, 0.666667); // (CS_Delay_to_Delay,Class1) -> (Delay,Class2)
		routingMatrix.addConnection(jobclass1, jobclass2, node4, node1, 0.800000); // (CS_Queue1_to_Delay,Class1) -> (Delay,Class2)
		routingMatrix.addConnection(jobclass2, jobclass1, node4, node1, 1.000000); // (CS_Queue1_to_Delay,Class2) -> (Delay,Class1)
		routingMatrix.addConnection(jobclass2, jobclass2, node1, node2, 1.000000); // (Delay,Class2) -> (Queue1,Class2)
		routingMatrix.addConnection(jobclass2, jobclass2, node2, node4, 1.000000); // (Queue1,Class2) -> (CS_Queue1_to_Delay,Class2)
		routingMatrix.addConnection(jobclass2, jobclass2, node3, node1, 1.000000); // (CS_Delay_to_Delay,Class2) -> (Delay,Class2)

		model.link(routingMatrix);

		SolverMVA solver = new SolverMVA(model);

		NetworkAvgTable avgTable = solver.getAvgTable();

		List<Double> QLen = avgTable.get(0);
		assertEquals(0.9268084538085989, QLen.get(0), 1e-13);
		assertEquals(0.20482497135805217, QLen.get(1), 1e-13);
		assertEquals(0.07796701220562233, QLen.get(2), 1e-13);
		assertEquals(2.790399562627727, QLen.get(3), 1e-13);

		List<Double> Util = avgTable.get(1);
		assertEquals(0.926808453808599, Util.get(0), 1e-13);
		assertEquals(0.20482497135805217, Util.get(1), 1e-13);
		assertEquals(0.026414027726531224, Util.get(2), 1e-13);
		assertEquals(0.9453445672762913, Util.get(3), 1e-13);

		List<Double> RespT = avgTable.get(2);
		assertEquals(0.666667, RespT.get(0), 1e-13);
		assertEquals(0.21666700000000003, RespT.get(1), 1e-13);
		assertEquals(0.5608282262908652, RespT.get(2), 1e-13);
		assertEquals(2.951727506794028, RespT.get(3), 1e-13);

		List<Double> ResidT = avgTable.get(3);
		assertEquals(0.3968255243763945, ResidT.get(0), 1e-13);
		assertEquals(0.08769857064912426, ResidT.get(1), 1e-13);
		assertEquals(0.03338262655613012, ResidT.get(2), 1e-13);
		assertEquals(1.194747161734549, ResidT.get(3), 1e-13);

		List<Double> Tput = avgTable.get(4);
		assertEquals(1.3902119856069057, Tput.get(0), 1e-13);
		assertEquals(0.945344567276291, Tput.get(1), 1e-13);
		assertEquals(0.13902119856069067, Tput.get(2), 1e-13);
		assertEquals(0.9453445672762913, Tput.get(3), 1e-13);
	}

	@Test
	public void test_example_closedModel_3() throws IllegalAccessException {
		Network model = new Network("example_closedModel_3");

		// Block 1: nodes			
		Delay node1 = new Delay(model, "Delay");
		Queue node2 = new Queue(model, "Queue1", SchedStrategy.PS);
		node2.setNumberOfServers(2);
		Router node3 = new Router(model, "CS_Delay_to_Delay"); // Dummy node, class switching is embedded in the routing matrix P 
		Router node4 = new Router(model, "CS_Queue1_to_Delay"); // Dummy node, class switching is embedded in the routing matrix P 

		// Block 2: classes
		ClosedClass jobclass1 = new ClosedClass(model, "Class1", 2, node1, 0);
		ClosedClass jobclass2 = new ClosedClass(model, "Class2", 0, node1, 0);
		ClosedClass jobclass3 = new ClosedClass(model, "Class3", 1, node1, 0);
		
		node1.setService(jobclass1, APH.fitMeanAndSCV(0.666667,0.500000)); // (Delay,Class1)
		node1.setService(jobclass2, APH.fitMeanAndSCV(0.216667,1.579882)); // (Delay,Class2)
		node1.setService(jobclass3, Exp.fitMean(1.000000)); // (Delay,Class3)
		node2.setService(jobclass1, APH.fitMeanAndSCV(0.190000,5.038781)); // (Queue1,Class1)
		node2.setService(jobclass2, Exp.fitMean(0.500000)); // (Queue1,Class2)
		node2.setService(jobclass3, Exp.fitMean(0.333333)); // (Queue1,Class3)

		// Block 3: topology	
		RoutingMatrix routingMatrix = new RoutingMatrix(model,
			 Arrays.asList(jobclass1, jobclass2, jobclass3),
			 Arrays.asList(node1, node2, node3, node4));
	
		routingMatrix.addConnection(jobclass1, jobclass1, node1, node2, 0.100000); // (Delay,Class1) -> (Queue1,Class1)
		routingMatrix.addConnection(jobclass1, jobclass1, node1, node3, 0.900000); // (Delay,Class1) -> (CS_Delay_to_Delay,Class1)
		routingMatrix.addConnection(jobclass1, jobclass1, node2, node4, 1.000000); // (Queue1,Class1) -> (CS_Queue1_to_Delay,Class1)
		routingMatrix.addConnection(jobclass1, jobclass1, node3, node1, 0.333333); // (CS_Delay_to_Delay,Class1) -> (Delay,Class1)
		routingMatrix.addConnection(jobclass1, jobclass1, node4, node1, 0.200000); // (CS_Queue1_to_Delay,Class1) -> (Delay,Class1)
		routingMatrix.addConnection(jobclass1, jobclass2, node3, node1, 0.666667); // (CS_Delay_to_Delay,Class1) -> (Delay,Class2)
		routingMatrix.addConnection(jobclass1, jobclass2, node4, node1, 0.800000); // (CS_Queue1_to_Delay,Class1) -> (Delay,Class2)
		routingMatrix.addConnection(jobclass2, jobclass1, node4, node1, 1.000000); // (CS_Queue1_to_Delay,Class2) -> (Delay,Class1)
		routingMatrix.addConnection(jobclass2, jobclass2, node1, node2, 1.000000); // (Delay,Class2) -> (Queue1,Class2)
		routingMatrix.addConnection(jobclass2, jobclass2, node2, node4, 1.000000); // (Queue1,Class2) -> (CS_Queue1_to_Delay,Class2)
		routingMatrix.addConnection(jobclass2, jobclass2, node3, node1, 1.000000); // (CS_Delay_to_Delay,Class2) -> (Delay,Class2)
		routingMatrix.addConnection(jobclass3, jobclass3, node1, node2, 1.000000); // (Delay,Class3) -> (Queue1,Class3)
		routingMatrix.addConnection(jobclass3, jobclass3, node2, node4, 1.000000); // (Queue1,Class3) -> (CS_Queue1_to_Delay,Class3)
		routingMatrix.addConnection(jobclass3, jobclass3, node3, node1, 1.000000); // (CS_Delay_to_Delay,Class3) -> (Delay,Class3)
		routingMatrix.addConnection(jobclass3, jobclass3, node4, node1, 1.000000); // (CS_Queue1_to_Delay,Class3) -> (Delay,Class3)

		model.link(routingMatrix);

		SolverOptions options = new SolverOptions(SolverType.MVA);
		options.seed = 23000;
		SolverMVA solver = new SolverMVA(model, options);

		NetworkAvgTable avgTable = solver.getAvgTable();

		List<Double> QLen = avgTable.get(0);
		//avgTable.print(options);

		assertEquals(1.1367305068825366, QLen.get(0), 1e-13);
		assertEquals(0.2512178137319010, QLen.get(1), 1e-13);
		assertEquals(0.7499951082428080, QLen.get(2), 1e-13);
		assertEquals(0.0323943742556943, QLen.get(3), 1e-13);
		assertEquals(0.5796890582153795, QLen.get(4), 1e-13);
		assertEquals(0.2499859589431528, QLen.get(5), 1e-13);

		List<Double> Util = avgTable.get(1);
		assertEquals(1.1367124595399019, Util.get(0), 1e-13);
		assertEquals(0.2512138252632773, Util.get(1), 1e-13);
		assertEquals(0.7500093077114268, Util.get(2), 1e-13);
		assertEquals(0.0161981444493714, Util.get(3), 1e-13);
		assertEquals(0.2898616601319966, Util.get(4), 1e-13);
		assertEquals(0.1250014262836865, Util.get(5), 1e-13);

		List<Double> RespT = avgTable.get(2);
		assertEquals(0.6666775845305656, RespT.get(0), 1e-13);
		assertEquals(0.2166704399760060, RespT.get(1), 1e-13);
		assertEquals(0.9999810676101313, RespT.get(2), 1e-13);
		assertEquals(0.1899887708687764, RespT.get(3), 1e-13);
		assertEquals(0.4999704496546749, RespT.get(4), 1e-13);
		assertEquals(0.3333104754472425, RespT.get(5), 1e-13);

		List<Double> ResidT = avgTable.get(3);
		assertEquals(0.3968318246910822, ResidT.get(0), 1e-13);
		assertEquals(0.0876999630207305, ResidT.get(1), 1e-13);
		assertEquals(0.9999810676101313, ResidT.get(2), 1e-13);
		assertEquals(0.0113088533894176, ResidT.get(3), 1e-13);
		assertEquals(0.2023690446700002, ResidT.get(4), 1e-13);
		assertEquals(0.3333104754472425, ResidT.get(5), 1e-13);

		List<Double> Tput = avgTable.get(4);
		assertEquals(1.7050678367759344, Tput.get(0), 1e-13);
		assertEquals(1.1594466405279866, Tput.get(1), 1e-13);
		assertEquals(0.7500093077114268, Tput.get(2), 1e-13);
		assertEquals(0.1705067836775934, Tput.get(3), 1e-13);
		assertEquals(1.1594466405279864, Tput.get(4), 1e-13);
		assertEquals(0.7500093077114268, Tput.get(5), 1e-13);
	}
}
