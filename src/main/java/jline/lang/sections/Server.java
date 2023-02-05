package jline.lang.sections;

import java.io.Serializable;
import java.util.List;
import jline.lang.*;
import jline.lang.constant.ServiceStrategy;
import jline.lang.distributions.*;
import jline.lang.nodes.*;
import jline.lang.sections.*;

public class Server extends ServiceSection implements Serializable {
    public Server(List<JobClass> jobClasses) {
        super("jline.Server");
        this.numberOfServers = 1;
    }

    private void initServers(List<JobClass> jobClasses) {
        for (JobClass jobClass : jobClasses) {
            this.serviceProcesses.put(jobClass, new ServiceBinding(jobClass, ServiceStrategy.LI, new Exp(0)));
        }
    }

}
