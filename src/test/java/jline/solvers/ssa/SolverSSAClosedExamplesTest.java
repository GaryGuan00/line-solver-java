package jline.solvers.ssa;

import jline.lang.ClosedClass;
import jline.lang.Network;
import jline.lang.RoutingMatrix;
import jline.lang.constant.SchedStrategy;
import jline.lang.constant.SolverType;
import jline.lang.distributions.APH;
import jline.lang.distributions.Exp;
import jline.lang.nodes.Delay;
import jline.lang.nodes.Queue;
import jline.lang.nodes.Router;
import jline.solvers.SolverOptions;
import jline.solvers.mva.SolverMVA;
import jline.util.NetworkAvgTable;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SolverSSAClosedExamplesTest {


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

		SolverOptions options = new SolverOptions(SolverType.SSA);
		options.seed = 23000;
		SolverSSA solver = new SolverSSA(model, options);

		NetworkAvgTable avgTable = solver.getAvgTable();
		avgTable.print(options);

		List<Double> QLen = avgTable.get(0);
		assertEquals(2.3138582706515893, QLen.get(0), 1e-13);
		assertEquals(7.6855847974612040, QLen.get(1), 1e-13);

		List<Double> Util = avgTable.get(1);
		assertEquals(2.3138582706515893, Util.get(0), 1e-13);
		assertEquals(0.9995572131347150, Util.get(1), 1e-13);

		List<Double> RespT = avgTable.get(2);
		assertEquals(0.9907837227696332, RespT.get(0), 1e-13);
		assertEquals(11.0692995754619620, RespT.get(1), 1e-13);

		List<Double> ResidT = avgTable.get(3);
		assertEquals(0.9907837227696332, ResidT.get(0), 1e-13);
		assertEquals(3.3207898726385885, ResidT.get(1), 1e-13);

		List<Double> Tput = avgTable.get(4);
		assertEquals(2.3149491584503870, Tput.get(0), 1e-13);
		assertEquals(0.6923796835379238, Tput.get(1), 1e-13);
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

		SolverOptions options = new SolverOptions(SolverType.SSA);
		options.seed = 23000;
		SolverSSA solver = new SolverSSA(model, options);

		NetworkAvgTable avgTable = solver.getAvgTable();
		avgTable.print(options);

		List<Double> QLen = avgTable.get(0);
		assertEquals(0.7692320680865200, QLen.get(0), 1e-13);
		assertEquals(0.3601376416045630, QLen.get(1), 1e-13);
		assertEquals(0.5920977877947042, QLen.get(2), 1e-13);
		assertEquals( 2.2781919642836760, QLen.get(3), 1e-13);

		List<Double> Util = avgTable.get(1);
		assertEquals(0.7692320680865200, Util.get(0), 1e-13);
		assertEquals(0.3601376416045630, Util.get(1), 1e-13);
		assertEquals(0.2180317221079657, Util.get(2), 1e-13);
		assertEquals(0.7848039450982746, Util.get(3), 1e-13);

		List<Double> RespT = avgTable.get(2);
		assertEquals(0.6583169922053269, RespT.get(0), 1e-13);
		assertEquals(0.4578838401492799, RespT.get(1), 1e-13);
		assertEquals(5.0876182808322880, RespT.get(2), 1e-13);
		assertEquals(2.8982657444499424, RespT.get(3), 1e-13);

		List<Double> ResidT = avgTable.get(3);
		assertEquals(0.3918552825290132, ResidT.get(0), 1e-13);
		assertEquals(0.1853339839681352, ResidT.get(1), 1e-13);
		assertEquals(0.3028343674005468, ResidT.get(2), 1e-13);
		assertEquals(1.1731078712936445, ResidT.get(3), 1e-13);

		List<Double> Tput = avgTable.get(4);
		assertEquals(1.1674273904058383, Tput.get(0), 1e-13);
		assertEquals(0.7865917263093030, Tput.get(1), 1e-13);
		assertEquals(0.1167568738886241, Tput.get(2), 1e-13);
		assertEquals(0.7863167219821255, Tput.get(3), 1e-13);
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

		SolverOptions options = new SolverOptions(SolverType.SSA);
		options.seed = 23000;
		SolverSSA solver = new SolverSSA(model, options);

		NetworkAvgTable avgTable = solver.getAvgTable();
		avgTable.print(options);

		List<Double> QLen = avgTable.get(0);
		assertEquals(0.9431080569794645, QLen.get(0), 1e-13);
		assertEquals(0.4156783718708347, QLen.get(1), 1e-13);
		assertEquals(0.7359751220493120, QLen.get(2), 1e-13);
		assertEquals(0.1507721740240671, QLen.get(3), 1e-13);
		assertEquals(0.4900223228737898, QLen.get(4), 1e-13);
		assertEquals(0.2638153408247659, QLen.get(5), 1e-13);

		List<Double> Util = avgTable.get(1);
		assertEquals(0.9431080569794645, Util.get(0), 1e-13);
		assertEquals(0.4156783718708347, Util.get(1), 1e-13);
		assertEquals(0.7359751220493120, Util.get(2), 1e-13);
		assertEquals(0.1108723685564747, Util.get(3), 1e-13);
		assertEquals(0.3566450359566388, Util.get(4), 1e-13);
		assertEquals(0.1855949100026436, Util.get(5), 1e-13);

		List<Double> RespT = avgTable.get(2);
		assertEquals(0.6778709186912736, RespT.get(0), 1e-13);
		assertEquals(0.4502573278239946, RespT.get(1), 1e-13);
		assertEquals(0.9518445694626068, RespT.get(2), 1e-13);
		assertEquals(1.0685913171958386, RespT.get(3), 1e-13);
		assertEquals(0.5315267065346936, RespT.get(4), 1e-13);
		assertEquals(0.3414225272787561, RespT.get(5), 1e-13);

		List<Double> ResidT = avgTable.get(3);
		assertEquals(0.4034945224064981, ResidT.get(0), 1e-13);
		assertEquals(0.1822470615020216, ResidT.get(1), 1e-13);
		assertEquals(0.9518445694626068, ResidT.get(2), 1e-13);
		assertEquals(0.0636066146652378, ResidT.get(3), 1e-13);
		assertEquals(0.2151418186661060, ResidT.get(4), 1e-13);
		assertEquals(0.3414225272787561, ResidT.get(5), 1e-13);

		List<Double> Tput = avgTable.get(4);
		assertEquals(1.3925436891405480, Tput.get(0), 1e-13);
		assertEquals(0.9223261609187242, Tput.get(1), 1e-13);
		assertEquals(0.7734333069596180, Tput.get(2), 1e-13);
		assertEquals(0.1413673438221504, Tput.get(3), 1e-13);
		assertEquals(0.9222033439704928, Tput.get(4), 1e-13);
		assertEquals(0.7729661237836744, Tput.get(5), 1e-13);
	}
}
