package jline.solvers.jmt;

import jline.lang.JobClass;
import jline.lang.Network;
import jline.lang.NetworkStruct;
import jline.lang.nodes.Station;
import jline.solvers.NetworkSolver;
import jline.solvers.SolverHandles;
import jline.solvers.SolverOptions;
import jline.util.Matrix;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import java.util.List;
import java.util.Map;

public abstract class AbstractSolverJMT extends NetworkSolver {

    protected AbstractSolverJMT(Network model, String name, SolverOptions options) {
        super(model, name, options);
    }

    protected AbstractSolverJMT(Network model, String name) {
        super(model, name);
    }

    public abstract DocumentSectionPair saveArrivalStrategy(Document simDoc, Document section, int ind);
    public abstract DocumentSectionPair saveBufferCapacity(Document simDoc, Document section, int ind);
    public abstract DocumentSectionPair saveDropStrategy(Document simDoc, Document section, int ind);
    public abstract DocumentSectionPair saveGetStrategy(Document simDoc, Document section);
    public abstract DocumentSectionPair saveNumberOfServers(Document simDoc, Document section, int ind);
    public abstract DocumentSectionPair savePreemptiveStrategy(Document simDoc, Document section, int ind);
    public abstract DocumentSectionPair savePreemptiveWeights(Document simDoc, Document section, int ind);
    public abstract DocumentSectionPair savePutStrategy(Document simDoc, Document section, int ind);
    public abstract DocumentSectionPair saveRoutingStrategy(Document simDoc, Document section, int ind);
    public abstract DocumentSectionPair saveServerVisits(Document simDoc, Document section);
    public abstract DocumentSectionPair saveServiceStrategy(Document simDoc, Document section, int ind);
    public abstract DocumentSectionPair saveClassSwitchStrategy(Document simDoc, Document section, int ind);
    public abstract DocumentSectionPair saveLogTunnel(Document simDoc, Document section, int ind);
    public abstract DocumentSectionPair saveForkStrategy(Document simDoc, Document section, int ind);
    public abstract DocumentSectionPair saveJoinStrategy(Document simDoc, Document section, int ind);

    public abstract ElementDocumentPair saveClasses(Document simElem, Document simDoc);
    public abstract ElementDocumentPair saveLinks(Document simElem, Document simDoc);
    public abstract ElementDocumentPair saveRegions(Document simElem, Document simDoc);
    public abstract ElementDocumentPair saveMetric(Document simElem, Document simDoc, Map<Station, Map<JobClass, SolverHandles.Metric>> handles);
    public abstract ElementDocumentPair saveMetrics(Document simElem, Document simDoc);
    public abstract ElementDocumentPair saveXMLHeader(String logPath) throws ParserConfigurationException;

    public abstract String getFileName();
    public abstract void setJmtJarPath(String path);
    public abstract String getJmtJarPath();
    public abstract String getFilePath();

    public abstract void jsimwView(SolverOptions options);
    public abstract void jsimgView(SolverOptions options);

    public abstract String writeJSIM(NetworkStruct sn, String outputFileName);

//    [result, parsed] = getResults(self)
//        [result, parsed] = getResultsJSIM(self)
//        [result, parsed] = getResultsJMVA(self)

    public NetworkStruct getStruct(){
        return this.model.getStruct(true);
    }
}
