classdef network_integration
methods(Static)
    function java_dist = compile_distribution(line_dist)
        if isa(line_dist, 'Exp')
            java_dist = SimUtil.Exp(line_dist.getParam(1).paramValue);
        elseif isa(line_dist, 'Erlang')
            java_dist = SimUtil.Erlang(line_dist.getParam(1).paramValue, line_dist.getParam(2).paramValue);
        elseif isa(line_dist, 'Immediate')
            java_dist = SimUtil.Immediate();
        elseif isa(line_dist, 'Disabled')
            java_dist = SimUtil.DisabledDistribution();
            return;
        else
            throw(MException('Invalid Distribution'))
        end
    end
    
    function matlab_dist = compile_distribution_line(java_dist)
        if isa(java_dist, 'SimUtil.Exp')
            matlab_dist = Exp(java_dist.getRate());
        elseif isa(java_dist, 'SimUtil.Erlang')
            matlab_dist = Erlang(java_dist.getRate(),java_dist.getNumberOfPhases());
        elseif isa(java_dist, 'StochLib.Immediate')
            matlab_dist = Immediate();
        elseif isa(java_dist, 'SimUtil.Immediate')
            matlab_dist = Immediate();
        elseif isa(java_dist, 'SimUtil.DisabledDistribution')
            return;
        else
            throw(MException('Invalid Distribution'))
        end
    end
    
    function set_service(line_node, java_node, job_classes)
        for n = 1 : length(job_classes)
            if (isa(line_node, 'Queue'))
                matlab_dist = line_node.getService(job_classes{n});
            elseif (isa(line_node, 'Source'))
                matlab_dist = line_node.getArrivalProcess(n);
            end
            service_dist = network_integration.compile_distribution(matlab_dist);
            
            if (isa(line_node,'Queue'))
                java_node.setService(java_node.getModel().getClasses().get(n-1), service_dist);
            elseif (isa(line_node, 'Source'))
                java_node.setArrivalDistribution(java_node.getModel().getClasses().get(n-1), service_dist);
            end
        end
    end
    
    function set_line_service(jline_node, line_node, job_classes, line_classes)
        if (isa(line_node,'Sink'))
            return
        end
        for n = 1:job_classes.size()
            java_dist = jline_node.getServiceProcess(job_classes.get(n-1));
            matlab_dist = network_integration.compile_distribution_line(java_dist);
            
            if (isa(jline_node,'StochLib.Queue'))
                line_node.setService(line_classes{n}, matlab_dist);
            elseif (isa(jline_node, 'StochLib.Source'))
                line_node.setArrival(line_classes{n}, matlab_dist);
            end
        end
    end

    function node_object = compile_node(line_node, java_network, job_classes)
        if isa(line_node, 'Queue')
            node_object = StochLib.Queue(java_network, line_node.getName, StochLib.SchedStrategy.FCFS);
            network_integration.set_service(line_node, node_object, job_classes);
            node_object.setNumberOfServers(line_node.getNumberOfServers);
        elseif isa(line_node, 'Source')
            node_object = StochLib.Source(java_network, line_node.getName);
            network_integration.set_service(line_node, node_object, job_classes);
        elseif isa(line_node, 'Sink')
            node_object = StochLib.Sink(java_network, line_node.getName); 
        elseif isa(line_node, 'Router')
            node_object = StochLib.Router(java_network, line_node.getName);
        elseif isa(line_node, 'ClassSwitch')
            nClasses = length(line_node.model.classes);
            classMatrix = java.util.HashMap();
            for i = 1:nClasses
                outputClasses = java.util.HashMap();
                outClass = java_network.getClasses().get(i-1);
                for j = 1:nClasses
                    inClass = java_network.getClasses().get(j-1);
                    outputClasses.put(inClass, line_node.server.csFun(i,j,0,0));
                end
                classMatrix.put(outClass, outputClasses);
            end
            node_object = StochLib.ClassSwitch(java_network, line_node.getName, classMatrix);
        else
            throw(MException('Invalid node type'))
        end
    end
    
    function node_object = compile_line_node(jline_node, line_network)
        if isa(jline_node, 'StochLib.Queue')
            node_object = Queue(line_network, jline_node.getName.toCharArray', SchedStrategy.FCFS);
            node_object.setNumberOfServers(jline_node.getNumberOfServers);
        elseif isa(jline_node, 'StochLib.Source')
            node_object = Source(line_network, jline_node.getName.toCharArray');
        elseif isa(jline_node, 'StochLib.Sink')
            node_object = Sink(line_network, jline_node.getName.toCharArray');
        elseif isa(jline_node, 'StochLib.Router')
            node_object = Router(line_network, jline_node.getName.toCharArray');
        elseif isa(jline_node, 'StochLib.ClassSwitch')
            throw(MException('Unsupported node'));
        else
            throw(MException('Invalid node type'))
        end
   end

    function node_class = compile_class(line_class, java_network)
        if isa(line_class, 'OpenClass')
            node_class = StochLib.OpenClass(java_network, line_class.getName);
        elseif isa(line_class, 'ClosedClass')
            node_class = StochLib.ClosedClass(java_network, line_class.getName, 3, java_network.getNodeByName(line_class.refstat.getName));
        else
            throw(MException('Invalid class type'));
        end
    end

    function node_class = compile_line_class(java_class, line_network)
        if isa(java_class, 'StochLib.OpenClass')
            node_class = OpenClass(line_network, java_class.getName.toCharArray');
        elseif isa(java_class, 'StochLib.ClosedClass')
            node_class = ClosedClass(line_network, java_class.getName.toCharArray', 3, java_network.getNodeByName(line_class.refstat.getName));
        else
            throw(MException('Invalid class type'));
        end
    end
    
    
    function compile_links(line_network, network_object)
        connections = line_network.getConnectionMatrix();
        [m, n] = size(connections)
        nodes = network_object.getNodes();
        routing_matrix = StochLib.RoutingMatrix(network_object.getClasses(), nodes);
        % [ ] Update to consider different weights/routing for classes
        for i = 1:m
            for j = 1:n
                if (connections(i, j) ~= 0)
                    routing_matrix.addConnection(nodes.get(i-1), nodes.get(j-1));
                end
            end 
        end
        network_object.link(routing_matrix);
    end
    
    function line_network = compile_line_links(line_network, java_network)
        P = line_network.initRoutingMatrix;
        P{1}
        java_nodes = java_network.getNodes();
        n_classes = java_network.getClasses.size();
        n_nodes = java_nodes.size();
        
        for n = 1 : n_nodes
            java_node = java_nodes.get(n-1);
            output_strategies = java_node.getOutputStrategies();
            n_strategies = output_strategies.size();
            for m = 1 : n_strategies
                output_strat = output_strategies.get(m-1);
                dest = output_strat.getDestination();
                in_idx = java_network.getNodeIndex(java_node)+1;
                out_idx = java_network.getNodeIndex(dest)+1;
                if n_classes == 1
                    P{1}(in_idx,out_idx) = output_strat.getProbability();
                else
                    strat_class = output_strat.getJobClass();
                    class_idx = java_network.getJobClassIndex(strat_class)+1;
                    P{class_idx,class_idx,in_idx,out_idx} = output_strat.getProbability();
                end
            end
        end
        line_network.link(P);
    end

    function network_object = line_to_jline(line_network)
           %javaaddpath("/home/matts/IdeaProjects/IndivProj/out/artifacts/IndivProj_jar/IndivProj.jar", '-end');
           network_object = StochLib.Network(line_network.getName);
           network_nodes = line_network.getNodes;
           job_classes = line_network.classes;
           for n = 1 : length(job_classes)
                  network_integration.compile_class(job_classes{n}, network_object);
           end

           for n = 1 : length(network_nodes)
                  network_integration.compile_node(network_nodes{n}, network_object, job_classes);
           end
           
           network_integration.compile_links(line_network, network_object);
    end
    
    function line_network = jline_to_line(java_network)
           %javaaddpath("/home/matts/IdeaProjects/IndivProj/out/artifacts/IndivProj_jar/IndivProj.jar", '-end');
           line_network = Network(java_network.getName);
           network_nodes = java_network.getNodes;
           job_classes = java_network.getClasses;
           
           line_nodes = cell(network_nodes.size);
           line_classes = cell(job_classes.size);
           

           for n = 1 : network_nodes.size
                  line_nodes{n} = network_integration.compile_line_node(network_nodes.get(n-1), line_network);
           end
           
           for n = 1 : job_classes.size
                  line_classes{n} = network_integration.compile_line_class(job_classes.get(n-1), line_network);
           end
           
           for n = 1 : network_nodes.size
                  network_integration.set_line_service(network_nodes.get(n-1), line_nodes{n}, job_classes, line_classes);
           end
           
           line_network = network_integration.compile_line_links(line_network, java_network);
    end
end
end

