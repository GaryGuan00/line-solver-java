package examples;

import java.util.Arrays;

import jline.lang.ClosedClass;
import jline.lang.Network;
import jline.lang.NetworkStruct;
import jline.lang.OpenClass;
import jline.lang.RoutingMatrix;
import jline.lang.constant.SchedStrategy;
import jline.lang.distributions.Erlang;
import jline.lang.distributions.Exp;
import jline.lang.distributions.HyperExp;
import jline.lang.nodes.Queue;
import jline.lang.nodes.Sink;
import jline.lang.nodes.Source;
//import jline.util.JLineAPI;

public class TestGetStruct {
	public static void main(String[] args) {
	    
		Network model = new Network("MM1LowU");
		
		Source source = new Source(model, "MySource");
		Queue queue1 = new Queue(model, "myQueue1", SchedStrategy.FCFS);
		Queue queue2 = new Queue(model, "myQueue2", SchedStrategy.FCFS);
		Sink sink = new Sink(model, "mySink");
		
		OpenClass class1 = new OpenClass(model, "OpenClass", 0);
		ClosedClass class2 = new ClosedClass(model, "ClosedClass1", 5, queue1, 0);
		ClosedClass class3 = new ClosedClass(model, "ClosedClass2", 5, queue1, 0);
		
		source.setArrival(class1, new Exp(0.1));
		source.setArrival(class2,  new Erlang(3,2));
		source.setArrival(class3, new Exp(0.3));
		
		queue1.setService(class1, new Exp(0.5));
		queue1.setService(class2, new HyperExp(0.5, 3.0, 10.0));
		queue1.setService(class3, new Exp(1.0));
		
		queue2.setService(class1, new HyperExp(0.1, 1.0, 10.0));
		queue2.setService(class2, new Erlang(3,3));
		queue2.setService(class3, new Exp(0.2));

        RoutingMatrix routingMatrix = new RoutingMatrix(model,Arrays.asList(class1, class2, class3),
                Arrays.asList(source, queue1, queue2, sink));
        routingMatrix.addConnection(source, queue1, class1, 1.0);
        routingMatrix.addConnection(queue1, queue1, class1, 0.3);
        routingMatrix.addConnection(queue1, queue2, class1, 0.7);
        routingMatrix.addConnection(queue2, sink, class1, 1.0);
        routingMatrix.addConnection(queue2, queue1, class2, class3, 1.0);
        routingMatrix.addConnection(queue1, queue2, class3, class2, 1.0);
        model.link(routingMatrix);
		NetworkStruct sn = model.getStruct(true);
	}
}
