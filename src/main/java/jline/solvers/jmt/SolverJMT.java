package jline.solvers.jmt;

import jline.lang.JobClass;
import jline.lang.Network;
import jline.lang.NetworkStruct;
import jline.lang.constant.*;
import jline.lang.nodes.Station;
import jline.solvers.SolverHandles;
import jline.solvers.SolverOptions;
import jline.util.Matrix;
import jline.util.Numerics;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.util.*;

public class SolverJMT extends AbstractSolverJMT {
    private String jmtPath;
    private String filePath;
    private String fileName;
    private double maxSimulatedTime;
    private int maxSamples;
    private int maxEvents;
    private int seed;
    private double simConfInt;
    private double simMaxRelErr;

    public static final String XSI_NO_NAMESPACE_SCHEMA_LOCATION = "Archive.xsd";
    public static final String FILE_FORMAT = "jsimg";
    public static final String JSIMG_PATH = "";

    @Override
    public DocumentSectionPair saveArrivalStrategy(Document simDoc, Document section, int ind) {
        Element strategyNode = simDoc.createElement("parameter");
        strategyNode.setAttribute("array", "true");
        strategyNode.setAttribute("classPath", "jmt.engine.NetStrategies.ServiceStrategy");
        strategyNode.setAttribute("name", "ServiceStrategy");
        NetworkStruct sn = getStruct();
        int numOfClasses = sn.nclasses;
        double i = sn.nodeToStation.get(ind);

        for (int r = 0; r < numOfClasses; r++) {
            Element refClassNode2 = simDoc.createElement("refClass");
            refClassNode2.appendChild(simDoc.createTextNode(sn.classnames.get(r)));
            strategyNode.appendChild(refClassNode2);
            Element serviceTimeStrategyNode = simDoc.createElement("subParameter");
            serviceTimeStrategyNode.setAttribute("classPath", "jmt.engine.NetStrategies.ServiceStrategies.ServiceTimeStrategy");
            serviceTimeStrategyNode.setAttribute("name", "ServiceTimeStrategy");

            if (!Double.isInfinite(sn.njobs.get(r))) {   // Closed
                Element subParValue = simDoc.createElement("value");
                subParValue.appendChild(simDoc.createTextNode("null"));
                serviceTimeStrategyNode.appendChild(subParValue);
                strategyNode.appendChild(serviceTimeStrategyNode);
                section.appendChild(strategyNode);
            } else { // Open
                if (sn.proctype.get(i).get(r) == ProcessType.DISABLED) {
                    Element subParValue = simDoc.createElement("value");
                    subParValue.appendChild(simDoc.createTextNode("null"));
                    serviceTimeStrategyNode.appendChild(subParValue);
                } else if (sn.proctype.get(i).get(r) == ProcessType.IMMEDIATE) {
                    serviceTimeStrategyNode.setAttribute("classPath", "jmt.engine.NetStrategies.ServiceStrategies.ZeroServiceTimeStrategy");
                    serviceTimeStrategyNode.setAttribute("name", "ZeroServiceTimeStrategy");
                } else if (sn.proctype.get(i).get(r) == ProcessType.PH
                        || sn.proctype.get(i).get(r) == ProcessType.APH
                        || sn.proctype.get(i).get(r) == ProcessType.COXIAN
                        || sn.proctype.get(i).get(r) == ProcessType.HYPEREXP) {
                    Element distributionNode = simDoc.createElement("subParameter");
                    distributionNode.setAttribute("classPath", "jmt.engine.random.PhaseTypeDistr");
                    distributionNode.setAttribute("name", "Phase-Type");
                    Element distrParNode = simDoc.createElement("subParameter");
                    distrParNode.setAttribute("classPath", "jmt.engine.random.PhaseTypePar");
                    distrParNode.setAttribute("name", "distrPar");

                    Element subParNodeAlpha = simDoc.createElement("subParameter");
                    subParNodeAlpha.setAttribute("array", "true");
                    subParNodeAlpha.setAttribute("classPath", "java.lang.Object");
                    subParNodeAlpha.setAttribute("name", "alpha");
                    Element subParNodeAlphaVec = simDoc.createElement("subParameter");
                    subParNodeAlphaVec.setAttribute("array", "true");
                    subParNodeAlphaVec.setAttribute("classPath", "java.lang.Object");
                    subParNodeAlphaVec.setAttribute("name", "vector");
                    Map<Integer, Matrix> PH = sn.proc.get(i).get(r);
                    Matrix alpha = sn.pie.get(i).get(r);
                    alpha.abs();
                    for (int k = 0; k < sn.phases.get((int) i, r); k++) { //TODO Cast to int
                        Element subParNodeAlphaElem = simDoc.createElement("subParameter");
                        subParNodeAlphaElem.setAttribute("classPath", "java.lang.Double");
                        subParNodeAlphaElem.setAttribute("name", "entry");
                        Element subParValue = simDoc.createElement("value");
                        subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", alpha.get(k))));
                        subParNodeAlphaElem.appendChild(subParValue);
                        subParNodeAlphaVec.appendChild(subParNodeAlphaElem);
                    }

                    Element subParNodeT = simDoc.createElement("subParameter");
                    subParNodeT.setAttribute("array", "true");
                    subParNodeT.setAttribute("classPath", "java.lang.Object");
                    subParNodeT.setAttribute("name", "T");
                    Matrix T = PH.get(0);
                    for (int k = 0; k < sn.phases.get((int) i, r); k++) { //TODO Cast to int
                        Element subParNodeTvec = simDoc.createElement("subParameter");
                        subParNodeTvec.setAttribute("array", "true");
                        subParNodeTvec.setAttribute("classPath", "java.lang.Object");
                        subParNodeTvec.setAttribute("name", "vector");
                        for (int j = 0; j < sn.phases.get((int) i, r); j++) { //TODO Cast to int
                            Element subParNodeTElem = simDoc.createElement("subParameter");
                            subParNodeTElem.setAttribute("classPath", "java.lang.Double");
                            subParNodeTElem.setAttribute("name", "entry");
                            Element subParValue = simDoc.createElement("value");
                            if (k == j) {
                                subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", -1 * Math.abs(T.get(k, j)))));
                            } else {
                                subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", Math.abs(T.get(k, j)))));
                            }
                            subParNodeTElem.appendChild(subParValue);
                            subParNodeTvec.appendChild(subParNodeTElem);
                        }
                        subParNodeT.appendChild(subParNodeTvec);
                    }
                    subParNodeAlpha.appendChild(subParNodeAlphaVec);
                    distrParNode.appendChild(subParNodeAlpha);
                    distrParNode.appendChild(subParNodeT);
                    serviceTimeStrategyNode.appendChild(distributionNode);
                    serviceTimeStrategyNode.appendChild(distrParNode);
                } else if (sn.proctype.get(i).get(r) == ProcessType.MAP
                        || sn.proctype.get(i).get(r) == ProcessType.MMPP2) {
                    Element distributionNode = simDoc.createElement("subParameter");
                    distributionNode.setAttribute("classPath", "jmt.engine.random.MAPDistr");
                    distributionNode.setAttribute("name", "Burst (MAP)");
                    Element distrParNode = simDoc.createElement("subParameter");
                    distrParNode.setAttribute("classPath", "jmt.engine.random.MAPPar");
                    distrParNode.setAttribute("name", "distrPar");

                    Map<Integer, Matrix> MAP = sn.proc.get(i).get(r);

                    Element subParNodeD0 = simDoc.createElement("subParameter");
                    subParNodeD0.setAttribute("array", "true");
                    subParNodeD0.setAttribute("classPath", "java.lang.Object");
                    subParNodeD0.setAttribute("name", "D0");
                    Matrix D0 = MAP.get(0);

                    for (int k = 0; k < sn.phases.get((int) i, r); k++) { //TODO Cast to int
                        Element subParNodeD0vec = simDoc.createElement("subParameter");
                        subParNodeD0vec.setAttribute("array", "true");
                        subParNodeD0vec.setAttribute("classPath", "java.lang.Object");
                        subParNodeD0vec.setAttribute("name", "vector");
                        for (int j = 0; j < sn.phases.get((int) i, r); j++) { //TODO Cast to int
                            Element subParNodeD0Elem = simDoc.createElement("subParameter");
                            subParNodeD0Elem.setAttribute("classPath", "java.lang.Double");
                            subParNodeD0Elem.setAttribute("name", "entry");
                            Element subParValue = simDoc.createElement("value");
                            subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", D0.get(k, j))));
                            subParNodeD0Elem.appendChild(subParValue);
                            subParNodeD0vec.appendChild(subParNodeD0Elem);
                        }
                        subParNodeD0.appendChild(subParNodeD0vec);
                    }
                    distrParNode.appendChild(subParNodeD0);

                    Element subParNodeD1 = simDoc.createElement("subParameter");
                    subParNodeD1.setAttribute("array", "true");
                    subParNodeD1.setAttribute("classPath", "java.lang.Object");
                    subParNodeD1.setAttribute("name", "D1");
                    Matrix D1 = MAP.get(1);
                    for (int k = 0; k < sn.phases.get((int) i, r); k++) { //TODO Cast to int
                        Element subParNodeD1vec = simDoc.createElement("subParameter");
                        subParNodeD1vec.setAttribute("array", "true");
                        subParNodeD1vec.setAttribute("classPath", "java.lang.Object");
                        subParNodeD1vec.setAttribute("name", "vector");
                        for (int j = 0; j < sn.phases.get((int) i, r); j++) { //TODO Cast to int
                            Element subParNodeD1Elem = simDoc.createElement("subParameter");
                            subParNodeD1Elem.setAttribute("classPath", "java.lang.Double");
                            subParNodeD1Elem.setAttribute("name", "entry");
                            Element subParValue = simDoc.createElement("value");
                            subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", D1.get(k, j))));
                            subParNodeD1Elem.appendChild(subParValue);
                            subParNodeD1vec.appendChild(subParNodeD1Elem);
                        }
                        subParNodeD1.appendChild(subParNodeD1vec);
                    }
                    distrParNode.appendChild(subParNodeD1);
                    serviceTimeStrategyNode.appendChild(distributionNode);
                    serviceTimeStrategyNode.appendChild(distrParNode);
                } else {
                    Element distributionNode = simDoc.createElement("subParameter");
                    String javaClass = "";
                    String javaParClass = "";
                    switch (sn.proctype.get(i).get(r)) {
                        case DET:
                            javaClass = "jmt.engine.random.DeterministicDistr";
                            javaParClass = "jmt.engine.random.DeterministicDistrPar";
                            break;
                        case COXIAN:
                            javaClass = "jmt.engine.random.CoxianDistr";
                            javaParClass = "jmt.engine.random.CoxianPar";
                            break;
                        case ERLANG:
                            javaClass = "jmt.engine.random.Erlang";
                            javaParClass = "jmt.engine.random.ErlangPar";
                            break;
                        case EXP:
                            javaClass = "jmt.engine.random.Exponential";
                            javaParClass = "jmt.engine.random.ExponentialPar";
                            break;
                        case GAMMA:
                            javaClass = "jmt.engine.random.GammaDistr";
                            javaParClass = "jmt.engine.random.GammaDistrPar";
                            break;
                        case HYPEREXP:
                            javaClass = "jmt.engine.random.HyperExp";
                            javaParClass = "jmt.engine.random.HyperExpPar";
                            break;
                        case PARETO:
                            javaClass = "jmt.engine.random.Pareto";
                            javaParClass = "jmt.engine.random.ParetoPar";
                            break;
                        case WEIBULL:
                            javaClass = "jmt.engine.random.Weibull";
                            javaParClass = "jmt.engine.random.WeibullPar";
                            break;
                        case LOGNORMAL:
                            javaClass = "jmt.engine.random.Lognormal";
                            javaParClass = "jmt.engine.random.LognormalPar";
                            break;
                        case UNIFORM:
                            javaClass = "jmt.engine.random.Uniform";
                            javaParClass = "jmt.engine.random.UniformPar";
                            break;
                        case MMPP2:
                            javaClass = "jmt.engine.random.MMPP2Distr";
                            javaParClass = "jmt.engine.random.MMPP2Par";
                            break;
                        case REPLAYER:
                        case TRACE:
                            javaClass = "jmt.engine.random.Replayer";
                            javaParClass = "jmt.engine.random.ReplayerPar";
                            break;
                    }
                    distributionNode.setAttribute("classPath", javaClass);
                    switch (sn.proctype.get(i).get(r)) {
                        case REPLAYER:
                        case TRACE:
                            distributionNode.setAttribute("name", "Replayer");
                        case EXP:
                            distributionNode.setAttribute("name", "Exponential");
                        case HYPEREXP:
                            distributionNode.setAttribute("name", "Hyperexponential");
                        default:
//                            ProcessType.fromId(sn.procid(i, r))
                            distributionNode.setAttribute("name", String.valueOf(sn.proctype.get(i).get(r)));

                    }
                    serviceTimeStrategyNode.appendChild(distributionNode);

                    Element distrParNode = simDoc.createElement("subParameter");
                    distrParNode.setAttribute("classPath", javaParClass);
                    distrParNode.setAttribute("name", "distrPar");

                    Element subParNodeAlpha = simDoc.createElement("subParameter");
                    Element subParValue = simDoc.createElement("value");
                    double c = Math.sqrt(sn.scv.get((int) i, r));

                    switch (sn.proctype.get(i).get(r)) {
                        case DET:
                            subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                            subParNodeAlpha.setAttribute("name", "t");
                            subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", sn.rates.get((int) i, r))));
                            subParNodeAlpha.appendChild(subParValue);
                            distrParNode.appendChild(subParNodeAlpha);
                            break;
                        case EXP:
                            subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                            subParNodeAlpha.setAttribute("name", "lambda");
                            subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", sn.rates.get((int) i, r))));
                            subParNodeAlpha.appendChild(subParValue);
                            distrParNode.appendChild(subParNodeAlpha);
                            break;
                        case HYPEREXP:
                            subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                            subParNodeAlpha.setAttribute("name", "p");
                            subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", sn.pie.get(i).get(r).get(0))));
                            subParNodeAlpha.appendChild(subParValue);
                            distrParNode.appendChild(subParNodeAlpha);
                            subParNodeAlpha = simDoc.createElement("subParameter");
                            subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                            subParNodeAlpha.setAttribute("name", "lambda1");
                            subParValue = simDoc.createElement("value");
                            subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", -1 * sn.proc.get(i).get(r).get(0).get(0, 0))));
                            subParNodeAlpha.appendChild(subParValue);
                            distrParNode.appendChild(subParNodeAlpha);
                            subParNodeAlpha = simDoc.createElement("subParameter");
                            subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                            subParNodeAlpha.setAttribute("name", "lambda2");
                            subParValue = simDoc.createElement("value");
                            subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", -1 * sn.proc.get(i).get(r).get(0).get(1, 1))));
                            subParNodeAlpha.appendChild(subParValue);
                            distrParNode.appendChild(subParNodeAlpha);
                            break;
                        case ERLANG:
                            subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                            subParNodeAlpha.setAttribute("name", "alpha");
                            //TODO Cast to int
                            subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", sn.rates.get((int) i, r) * sn.phases.get((int) i, r))));
                            subParNodeAlpha.appendChild(subParValue);
                            distrParNode.appendChild(subParNodeAlpha);
                            subParNodeAlpha = simDoc.createElement("subParameter");
                            subParNodeAlpha.setAttribute("classPath", "java.lang.Long");
                            subParNodeAlpha.setAttribute("name", "r");
                            subParValue = simDoc.createElement("value");
                            //TODO Cast to int
                            subParValue.appendChild(simDoc.createTextNode(String.format("%d", sn.phases.get((int) i, r))));
                            subParNodeAlpha.appendChild(subParValue);
                            distrParNode.appendChild(subParNodeAlpha);
                            break;
                        case GAMMA:
                            subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                            subParNodeAlpha.setAttribute("name", "alpha");
                            //TODO Cast to int
                            subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", 1 / sn.scv.get((int) i, r))));
                            subParNodeAlpha.appendChild(subParValue);
                            distrParNode.appendChild(subParNodeAlpha);
                            subParNodeAlpha = simDoc.createElement("subParameter");
                            subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                            subParNodeAlpha.setAttribute("name", "beta");
                            subParValue = simDoc.createElement("value");
                            //TODO Cast to int
                            subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", sn.scv.get((int) i, r) / sn.rates.get((int) i, r))));
                            subParNodeAlpha.appendChild(subParValue);
                            distrParNode.appendChild(subParNodeAlpha);
                            break;
                        case PARETO:
                            //TODO Cast to int
                            double shape = Math.sqrt(1 + 1 / sn.scv.get((int) i, r)) + 1;
                            double scale = 1 / sn.rates.get((int) i, r) * (shape - 1) / shape;
                            subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                            subParNodeAlpha.setAttribute("name", "alpha"); //shape
                            subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", shape)));
                            subParNodeAlpha.appendChild(subParValue);
                            distrParNode.appendChild(subParNodeAlpha);
                            subParNodeAlpha = simDoc.createElement("subParameter");
                            subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                            subParNodeAlpha.setAttribute("name", "k"); //scale
                            subParValue = simDoc.createElement("value");
                            subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", scale)));
                            subParNodeAlpha.appendChild(subParValue);
                            distrParNode.appendChild(subParNodeAlpha);
                            break;
                        case WEIBULL:
                            //TODO Cast to int
                            double rval = Math.pow(c, -1.086); //Justus approximation (1976)
                            //TODO Cast to int, Gamma func
                            double alpha = 1 / sn.rates.get((int) i, r) / Numerics.gammaFunction(1 + 1 / rval);
                            subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                            subParNodeAlpha.setAttribute("name", "alpha"); //shape
                            subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", alpha)));
                            subParNodeAlpha.appendChild(subParValue);
                            distrParNode.appendChild(subParNodeAlpha);
                            subParNodeAlpha = simDoc.createElement("subParameter");
                            subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                            subParNodeAlpha.setAttribute("name", "r"); //scale
                            subParValue = simDoc.createElement("value");
                            subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", rval)));
                            subParNodeAlpha.appendChild(subParValue);
                            distrParNode.appendChild(subParNodeAlpha);
                            break;
                        case LOGNORMAL:
                            //TODO Cast to int
                            double mu = Math.log(1 / sn.rates.get((int) i, r) / Math.sqrt(c * c + 1));
                            double sigma = Math.sqrt(Math.log(c * c + 1));
                            subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                            subParNodeAlpha.setAttribute("name", "mu"); //shape
                            subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", mu)));
                            subParNodeAlpha.appendChild(subParValue);
                            distrParNode.appendChild(subParNodeAlpha);
                            subParNodeAlpha = simDoc.createElement("subParameter");
                            subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                            subParNodeAlpha.setAttribute("name", "sigma"); //scale
                            subParValue = simDoc.createElement("value");
                            subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", sigma)));
                            subParNodeAlpha.appendChild(subParValue);
                            distrParNode.appendChild(subParNodeAlpha);
                            break;
                        case UNIFORM:
                            //TODO Cast to int
                            double maxVal = (Math.sqrt(12 * sn.scv.get((int) i, r) / Math.pow(sn.rates.get((int) i, r), 2)) + 2 / sn.rates.get((int) i, r)) / 2;
                            double minVal = 2 / sn.rates.get((int) i, r) - maxVal;
                            subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                            subParNodeAlpha.setAttribute("name", "min"); //shape
                            subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", minVal)));
                            subParNodeAlpha.appendChild(subParValue);
                            distrParNode.appendChild(subParNodeAlpha);
                            subParNodeAlpha = simDoc.createElement("subParameter");
                            subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                            subParNodeAlpha.setAttribute("name", "max"); //scale
                            subParValue = simDoc.createElement("value");
                            subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", maxVal)));
                            subParNodeAlpha.appendChild(subParValue);
                            distrParNode.appendChild(subParNodeAlpha);
                            break;
                        case REPLAYER:
                        case TRACE:
                            subParNodeAlpha.setAttribute("classPath", "java.lang.String");
                            subParNodeAlpha.setAttribute("name", "fileName");
                            //sn.nodeparam{ind}{r}.fileName
                            subParValue.appendChild(simDoc.createTextNode(sn.nodeparam.get(ind).joinStrategy.get(r).name()));
                            subParNodeAlpha.appendChild(subParValue);
                            distrParNode.appendChild(subParNodeAlpha);
                            break;
                    }
                    serviceTimeStrategyNode.appendChild(distrParNode);
                }
                strategyNode.appendChild(serviceTimeStrategyNode);
                section.appendChild(strategyNode);
            }
        }
        return new DocumentSectionPair(simDoc, section);
    }

    @Override
    public DocumentSectionPair saveBufferCapacity(Document simDoc, Document section, int ind) {
        NetworkStruct sn = getStruct();
        Element sizeNode = simDoc.createElement("parameter");
        sizeNode.setAttribute("classPath", "java.lang.Integer");
        sizeNode.setAttribute("name", "size");
        Element valueNode = simDoc.createElement("value");
        if (sn.isstation.get(ind) != 0 || Double.isInfinite(sn.cap.get((int) sn.nodeToStation.get(ind)))) {
            valueNode.appendChild(simDoc.createTextNode(String.valueOf(-1)));
        } else {
            //TODO sum(sn.njobs)
            if (sn.cap.get((int) sn.nodeToStation.get(ind)) == sn.njobs.sumRows(0)) {
                valueNode.appendChild(simDoc.createTextNode(String.valueOf(-1)));
            } else {
                valueNode.appendChild(simDoc.createTextNode(String.valueOf(sn.cap.get((int) sn.nodeToStation.get(ind)))));
            }
        }
        sizeNode.appendChild(valueNode);
        section.appendChild(sizeNode);
        return new DocumentSectionPair(simDoc, section);
    }

    @Override
    public DocumentSectionPair saveDropStrategy(Document simDoc, Document section, int ind) {
        NetworkStruct sn = getStruct();
        int numOfClasses = sn.nclasses;

        Element schedStrategyNode = simDoc.createElement("parameter");
        schedStrategyNode.setAttribute("array", "true");
        schedStrategyNode.setAttribute("classPath", "java.lang.String");
        schedStrategyNode.setAttribute("name", "dropStrategies");
        double i = sn.nodeToStation.get(ind);
        for (int r = 0; r < numOfClasses; r++) {
            Element refClassNode = simDoc.createElement("refClass");
            refClassNode.appendChild(simDoc.createTextNode(sn.classnames.get(r)));
            schedStrategyNode.appendChild(refClassNode);

            Element subParameterNode = simDoc.createElement("subParameter");
            subParameterNode.setAttribute("classPath", "java.lang.String");
            subParameterNode.setAttribute("name", "dropStrategy");

            Element valueNode2 = simDoc.createElement("value");

            if (Double.isNaN(i) || sn.droprule.get(i).get(r) == DropStrategy.Drop) {
                valueNode2.appendChild(simDoc.createTextNode("drop"));
            } else {
                //TODO get the string of dropRule
                valueNode2.appendChild(simDoc.createTextNode(sn.droprule.get(i).get(r).name()));
            }
            subParameterNode.appendChild(valueNode2);
            schedStrategyNode.appendChild(subParameterNode);
            section.appendChild(schedStrategyNode);
        }
        return new DocumentSectionPair(simDoc, section);
    }

    @Override
    public DocumentSectionPair saveGetStrategy(Document simDoc, Document section) {
        // the get strategy is always fcfs
        Element queueGetStrategyNode = simDoc.createElement("parameter");
        queueGetStrategyNode.setAttribute("classPath", "jmt.engine.NetStrategies.QueueGetStrategies.FCFSstrategy");
        queueGetStrategyNode.setAttribute("name", "FCFSstrategy");
        section.appendChild(queueGetStrategyNode);
        return new DocumentSectionPair(simDoc, section);
    }

    @Override
    public DocumentSectionPair saveNumberOfServers(Document simDoc, Document section, int ind) {
        // [SIMDOC, SECTION] = SAVENUMBEROFSERVERS(SIMDOC, SECTION, CURRENTNODE)
        Element sizeNode = simDoc.createElement("parameter");
        sizeNode.setAttribute("classPath", "java.lang.Integer");
        sizeNode.setAttribute("name", "maxJobs");

        NetworkStruct sn = getStruct();
        Element valueNode = simDoc.createElement("value");
        //TODO Cast to int
        valueNode.appendChild(simDoc.createTextNode(String.valueOf(sn.nservers.get((int) sn.nodeToStation.get(ind)))));

        sizeNode.appendChild(valueNode);
        section.appendChild(sizeNode);

        return new DocumentSectionPair(simDoc, section);
    }

    @Override
    public DocumentSectionPair savePreemptiveStrategy(Document simDoc, Document section, int ind) {
        // [SIMDOC, SECTION] = SAVEPREEMPTIVESTRATEGY(SIMDOC, SECTION, CURRENTNODE)
        Element visitsNode = simDoc.createElement("parameter");
        visitsNode.setAttribute("array", "true");
        visitsNode.setAttribute("classPath", "jmt.engine.NetStrategies.PSStrategy");
        visitsNode.setAttribute("name", "PSStrategy");

        NetworkStruct sn = getStruct();
        int numOfClasses = sn.nclasses;
        Double i = sn.nodeToStation.get(ind);
        for (int r = 0; r < numOfClasses; r++) {
            Element refClassNode = simDoc.createElement("refClass");
            refClassNode.appendChild(simDoc.createTextNode(sn.classnames.get(r)));
            visitsNode.appendChild(refClassNode);

            Element subParameterNode = simDoc.createElement("subParameter");
            switch (sn.sched.get(i)) {
                case PS:
                    subParameterNode.setAttribute("classPath", "jmt.engine.NetStrategies.PSStrategies.EPSStrategy");
                    subParameterNode.setAttribute("name", "EPSStrategy");
                    break;
                case DPS:
                    subParameterNode.setAttribute("classPath", "jmt.engine.NetStrategies.PSStrategies.DPSStrategy");
                    subParameterNode.setAttribute("name", "DPSStrategy");
                    break;
                case GPS:
                    subParameterNode.setAttribute("classPath", "jmt.engine.NetStrategies.PSStrategies.GPSStrategy");
                    subParameterNode.setAttribute("name", "GPSStrategy");
                    break;
            }
            visitsNode.appendChild(subParameterNode);
            section.appendChild(visitsNode);
        }
        return new DocumentSectionPair(simDoc, section);
    }

    @Override
    public DocumentSectionPair savePreemptiveWeights(Document simDoc, Document section, int ind) {
        // [SIMDOC, SECTION] = SAVEPREEMPTIVEWEIGHTS(SIMDOC, SECTION, NODEIDX)
        Element visitsNode = simDoc.createElement("parameter");
        visitsNode.setAttribute("array", "true");
        visitsNode.setAttribute("classPath", "java.lang.Double");
        visitsNode.setAttribute("name", "serviceWeights");

        NetworkStruct sn = getStruct();
        int numOfClasses = sn.nclasses;
        Double i = sn.nodeToStation.get(ind);
        for (int r = 0; r < numOfClasses; r++) {
            Element refClassNode = simDoc.createElement("refClass");
            refClassNode.appendChild(simDoc.createTextNode(sn.classnames.get(r)));
            visitsNode.appendChild(refClassNode);

            Element subParameterNode = simDoc.createElement("subParameter");
            subParameterNode.setAttribute("classPath", "java.lang.Double");
            subParameterNode.setAttribute("name", "serviceWeight");

            Element valueNode2 = simDoc.createElement("value");
            //TODO Cast to int
            valueNode2.appendChild(simDoc.createTextNode(String.valueOf(sn.schedparam.get(i.intValue(), r))));

            subParameterNode.appendChild(valueNode2);
            visitsNode.appendChild(subParameterNode);
            section.appendChild(visitsNode);
        }
        return new DocumentSectionPair(simDoc, section);
    }

    @Override
    public DocumentSectionPair savePutStrategy(Document simDoc, Document section, int ind) {
        // [SIMDOC, SECTION] = SAVEPUTSTRATEGY(SIMDOC, SECTION, CURRENTNODE)
        Element queuePutStrategyNode = simDoc.createElement("parameter");
        queuePutStrategyNode.setAttribute("array", "true");
        queuePutStrategyNode.setAttribute("classPath", "jmt.engine.NetStrategies.QueuePutStrategy");
        queuePutStrategyNode.setAttribute("name", "QueuePutStrategy");

        NetworkStruct sn = getStruct();
        int numOfClasses = sn.nclasses;
        for (int r = 0; r < numOfClasses; r++) {
            Element refClassNode2 = simDoc.createElement("refClass");
            refClassNode2.appendChild(simDoc.createTextNode(sn.classnames.get(r)));

            queuePutStrategyNode.appendChild(refClassNode2);

            Element subParameterNode2 = simDoc.createElement("subParameter");
            // if not a station treat as FCFS
            if (sn.isstation.get(ind, 0) == 0) {
                subParameterNode2.setAttribute("classPath", "jmt.engine.NetStrategies.QueuePutStrategies.TailStrategy");
                subParameterNode2.setAttribute("name", "TailStrategy");
            } else { // if a station
                switch (sn.sched.get(sn.nodeToStation.get(ind))) {
                    case SIRO:
//                        subParameterNode2 = simDoc.createElement("subParameter");
                        subParameterNode2.setAttribute("classPath", "jmt.engine.NetStrategies.QueuePutStrategies.RandStrategy");
                        subParameterNode2.setAttribute("name", "RandStrategy");
                        break;
                    case LJF:
//                        subParameterNode2 = simDoc.createElement("subParameter");
                        subParameterNode2.setAttribute("classPath", "jmt.engine.NetStrategies.QueuePutStrategies.LJFStrategy");
                        subParameterNode2.setAttribute("name", "LJFStrategy");
                        break;
                    case SJF:
//                        subParameterNode2 = simDoc.createElement("subParameter");
                        subParameterNode2.setAttribute("classPath", "jmt.engine.NetStrategies.QueuePutStrategies.SJFStrategy");
                        subParameterNode2.setAttribute("name", "SJFStrategy");
                        break;
                    case LEPT:
//                        subParameterNode2 = simDoc.createElement("subParameter");
                        subParameterNode2.setAttribute("classPath", "jmt.engine.NetStrategies.QueuePutStrategies.LEPTStrategy");
                        subParameterNode2.setAttribute("name", "LEPTStrategy");
                        break;
                    case SEPT:
                        subParameterNode2.setAttribute("classPath", "jmt.engine.NetStrategies.QueuePutStrategies.SEPTStrategy");
                        subParameterNode2.setAttribute("name", "SEPTStrategy");
                        break;
                    case LCFS:
//                        subParameterNode2 = simDoc.createElement("subParameter");
                        subParameterNode2.setAttribute("classPath", "jmt.engine.NetStrategies.QueuePutStrategies.HeadStrategy");
                        subParameterNode2.setAttribute("name", "HeadStrategy");
                        break;
                    case LCFSPR:
//                        subParameterNode2 = simDoc.createElement("subParameter");
                        subParameterNode2.setAttribute("classPath", "jmt.engine.NetStrategies.QueuePutStrategies.LCFSPRStrategy");
                        subParameterNode2.setAttribute("name", "LCFSPRStrategy");
                        break;
                    case HOL:
//                        subParameterNode2 =simDoc.createElement("subParameter");
                        subParameterNode2.setAttribute("classPath", "jmt.engine.NetStrategies.QueuePutStrategies.TailStrategyPriority");
                        subParameterNode2.setAttribute("name", "TailStrategyPriority");
                        break;
                    default: // treat as FCFS -this is required for PS
//                        subParameterNode2 = simDoc.createElement("subParameter");
                        subParameterNode2.setAttribute("classPath", "jmt.engine.NetStrategies.QueuePutStrategies.TailStrategy");
                        subParameterNode2.setAttribute("name", "TailStrategy");
                        break;
                }
            }
            queuePutStrategyNode.appendChild(subParameterNode2);
            section.appendChild(queuePutStrategyNode);
        }
        return new DocumentSectionPair(simDoc, section);
    }

    @Override
    public DocumentSectionPair saveRoutingStrategy(Document simDoc, Document section, int ind) {
//        [SIMDOC, SECTION] = SAVEROUTINGSTRATEGY(SIMDOC, SECTION, NODEIDX)
        Element strategyNode = simDoc.createElement("parameter");
        strategyNode.setAttribute("array", "true");
        strategyNode.setAttribute("classPath", "jmt.engine.NetStrategies.RoutingStrategy");
        strategyNode.setAttribute("name", "RoutingStrategy");

        NetworkStruct sn = getStruct();
        int M = sn.nnodes;
        int K = sn.nclasses;
        int i = ind;
        // since the class switch node always outputs to a single node, it is faster to translate it to RAND. Also some problems with sn.rt value otherwise.
        if (sn.nodetypes.get(i) == NodeType.ClassSwitch) {
            for (JobClass jobClass : sn.routing.get(i).keySet()) {
                sn.routing.get(i).put(jobClass, RoutingStrategy.RAND);
            }
        }
        for (int r = 0; r < K; r++) {
            Element refClassNode = simDoc.createElement("refClass");
            refClassNode.appendChild(simDoc.createTextNode(sn.classnames.get(r)));
            strategyNode.appendChild(refClassNode);

            Element concStratNode = simDoc.createElement("subParameter");
            Matrix conn_i;
            Matrix conn_i_find;

            switch (sn.routing.get(i).get(r)) {
                case KCHOICES:
                    throw new RuntimeException("Routing Strategy KCHOICES is not supported in JLINE");
                case RAND:
//                    concStratNode =simDoc.createElement("subParameter");
                    concStratNode.setAttribute("classPath", "jmt.engine.NetStrategies.RoutingStrategies.RandomStrategy");
                    concStratNode.setAttribute("name", "Random");
                    break;
                case RROBIN:
//                    concStratNode =simDoc.createElement("subParameter");
                    concStratNode.setAttribute("classPath", "jmt.engine.NetStrategies.RoutingStrategies.RoundRobinStrategy");
                    concStratNode.setAttribute("name", "Round Robin");
                    break;
                case JSQ:
//                    concStratNode =simDoc.createElement("subParameter");
                    concStratNode.setAttribute("classPath", "jmt.engine.NetStrategies.RoutingStrategies.ShortestQueueLengthRoutingStrategy");
                    concStratNode.setAttribute("name", "Join the Shortest Queue (JSQ)");
                    break;
                case WRROBIN:
//                    concStratNode =simDoc.createElement("subParameter");
                    concStratNode.setAttribute("classPath", "jmt.engine.NetStrategies.RoutingStrategies.WeightedRoundRobinStrategy");
                    concStratNode.setAttribute("name", "Weighted Round Robin");
                    Element concStratNode2 = simDoc.createElement("subParameter");
                    concStratNode2.setAttribute("array", "true");
                    concStratNode2.setAttribute("classPath", "jmt.engine.NetStrategies.RoutingStrategies.WeightEntry");
                    concStratNode2.setAttribute("name", "WeightEntryArray");

                    // linked stations
                    conn_i = new Matrix(0, 0);
                    Matrix.extractRows(this.sn.connmatrix, i, i + 1, conn_i);
                    conn_i_find = conn_i.find();
                    for (int j = 0; j < conn_i_find.length(); j++) {
                        double weight = sn.nodeparam.get(ind).weights.get(r).get(j);

                        Element concStratNode3 = simDoc.createElement("subParameter");
                        concStratNode3.setAttribute("classPath", "jmt.engine.NetStrategies.RoutingStrategies.WeightEntry");
                        concStratNode3.setAttribute("name", "WeightEntry");

                        Element concStratNode4Station = simDoc.createElement("subParameter");
                        concStratNode4Station.setAttribute("classPath", "java.lang.String");
                        concStratNode4Station.setAttribute("name", "stationName");

                        Element concStratNode4StationValueNode = simDoc.createElement("value");
                        concStratNode4StationValueNode.appendChild(simDoc.createTextNode(String.format("%s", sn.nodenames.get(j))));
                        concStratNode4Station.appendChild(concStratNode4StationValueNode);
                        concStratNode3.appendChild(concStratNode4Station);
                        Element concStratNode4Weight = simDoc.createElement("subParameter");
                        concStratNode4Weight.setAttribute("classPath", "java.lang.Integer");
                        concStratNode4Weight.setAttribute("name", "weight");
                        Element concStratNode4WeightValueNode = simDoc.createElement("value");
                        concStratNode4WeightValueNode.appendChild(simDoc.createTextNode(String.format("%d", weight)));
                        concStratNode4Weight.appendChild(concStratNode4WeightValueNode);
                        concStratNode3.appendChild(concStratNode4Station);
                        concStratNode3.appendChild(concStratNode4Weight);
                        concStratNode2.appendChild(concStratNode3);
                    }
                    concStratNode.appendChild(concStratNode2);
                    break;
                case PROB:
//                    concStratNode =simDoc.createElement("subParameter");
                    concStratNode.setAttribute("classPath", "jmt.engine.NetStrategies.RoutingStrategies.EmpiricalStrategy");
                    concStratNode.setAttribute("name", String.valueOf(RoutingStrategy.PROB));
                    concStratNode2 = simDoc.createElement("subParameter");
                    concStratNode2.setAttribute("array", "true");
                    concStratNode2.setAttribute("classPath", "jmt.engine.random.EmpiricalEntry");
                    concStratNode2.setAttribute("name", "EmpiricalEntryArray");

                    // linked stations
                    conn_i = new Matrix(0, 0);
                    Matrix.extractRows(this.sn.connmatrix, i, i + 1, conn_i);
                    conn_i_find = conn_i.find();
                    for (int j = 0; j < conn_i_find.length(); j++) {
                        double probRouting = sn.rtnodes.get((i - 1) * K + r, (j - 1) * K + r);
                        if (probRouting > 0) {
                            Element concStratNode3 = simDoc.createElement("subParameter");
                            concStratNode3.setAttribute("classPath", "jmt.engine.random.EmpiricalEntry");
                            concStratNode3.setAttribute("name", "EmpiricalEntry");
                            Element concStratNode4Station = simDoc.createElement("subParameter");
                            concStratNode4Station.setAttribute("classPath", "java.lang.String");
                            concStratNode4Station.setAttribute("name", "stationName");
                            Element concStratNode4StationValueNode = simDoc.createElement("value");
                            concStratNode4StationValueNode.appendChild(simDoc.createTextNode(String.format("%s", sn.nodenames.get(j))));
                            concStratNode4Station.appendChild(concStratNode4StationValueNode);
                            concStratNode3.appendChild(concStratNode4Station);
                            Element concStratNode4Probability = simDoc.createElement("subParameter");
                            concStratNode4Probability.setAttribute("classPath", "java.lang.Double");
                            concStratNode4Probability.setAttribute("name", "probability");
                            Element concStratNode4ProbabilityValueNode = simDoc.createElement("value");
                            concStratNode4ProbabilityValueNode.appendChild(simDoc.createTextNode(String.format("%12.12f", probRouting)));
                            concStratNode4Probability.appendChild(concStratNode4ProbabilityValueNode);
                            concStratNode3.appendChild(concStratNode4Station);
                            concStratNode3.appendChild(concStratNode4Probability);
                            concStratNode2.appendChild(concStratNode3);
                        }
                    }
                    concStratNode.appendChild(concStratNode2);
                    break;
                default:
                    concStratNode.setAttribute("classPath", "jmt.engine.NetStrategies.RoutingStrategies.DisabledRoutingStrategy");
                    concStratNode.setAttribute("name", "Random");
            }
            strategyNode.appendChild(concStratNode);
            section.appendChild(strategyNode);
        }
        return new DocumentSectionPair(simDoc, section);
    }


    @Override
    public DocumentSectionPair saveServerVisits(Document simDoc, Document section) {
        // [SIMDOC, SECTION] = SAVESERVERVISITS(SIMDOC, SECTION)
        Element visitsNode = simDoc.createElement("parameter");
        visitsNode.setAttribute("array", "true");
        visitsNode.setAttribute("classPath", "java.lang.Integer");
        visitsNode.setAttribute("name", "numberOfVisits");

        NetworkStruct sn = getStruct();
        int numOfClasses = sn.nclasses;

        for (int r = 0; r < numOfClasses; r++) {
            Element refClassNode = simDoc.createElement("refClass");
            refClassNode.appendChild(simDoc.createTextNode(sn.classnames.get(r)));
            visitsNode.appendChild(refClassNode);

            Element subParameterNode = simDoc.createElement("subParameter");
            subParameterNode.setAttribute("classPath", "java.lang.Integer");
            subParameterNode.setAttribute("name", "numberOfVisits");

            Element valueNode2 = simDoc.createElement("value");
            valueNode2.appendChild(simDoc.createTextNode(String.valueOf(1)));

            subParameterNode.appendChild(valueNode2);
            visitsNode.appendChild(subParameterNode);
            section.appendChild(visitsNode);
        }

        return new DocumentSectionPair(simDoc, section);
    }

    @Override
    public DocumentSectionPair saveServiceStrategy(Document simDoc, Document section, int ind) {
        // [SIMDOC, SECTION] = SAVESERVICESTRATEGY(SIMDOC, SECTION, NODEIDX)
        Element strategyNode = simDoc.createElement("parameter");
        strategyNode.setAttribute("array", "true");
        strategyNode.setAttribute("classPath", "jmt.engine.NetStrategies.ServiceStrategy");
        strategyNode.setAttribute("name", "ServiceStrategy");

        NetworkStruct sn = this.getStruct();
        int numOfClasses = sn.nclasses;
        double i = sn.nodeToStation.get(ind);

        for (int r = 0; r < numOfClasses; r++) {
            Element refClassNode2 = simDoc.createElement("refClass");
            refClassNode2.appendChild(simDoc.createTextNode(sn.classnames.get(r)));
            strategyNode.appendChild(refClassNode2);
            Element serviceTimeStrategyNode = simDoc.createElement("subParameter");
            if (sn.proctype.get(i).get(r) == ProcessType.DISABLED) {
                serviceTimeStrategyNode.setAttribute("classPath", "jmt.engine.NetStrategies.ServiceStrategies.DisabledServiceTimeStrategy");
                serviceTimeStrategyNode.setAttribute("name", "DisabledServiceTimeStrategy");
            } else if (sn.proctype.get(i).get(r) == ProcessType.IMMEDIATE) {
                serviceTimeStrategyNode.setAttribute("classPath", "jmt.engine.NetStrategies.ServiceStrategies.ZeroServiceTimeStrategy");
                serviceTimeStrategyNode.setAttribute("name", "ZeroServiceTimeStrategy");
            } else if (sn.proctype.get(i).get(r) == ProcessType.PH || sn.proctype.get(i).get(r) == ProcessType.APH || sn.proctype.get(i).get(r) == ProcessType.COXIAN || (sn.phases.get((int) i, r) != 0 && sn.proctype.get(i).get(r) == ProcessType.HYPEREXP)) {
                serviceTimeStrategyNode.setAttribute("classPath", "jmt.engine.NetStrategies.ServiceStrategies.ServiceTimeStrategy");
                serviceTimeStrategyNode.setAttribute("name", "ServiceTimeStrategy");
                Element distributionNode = simDoc.createElement("subParameter");
                distributionNode.setAttribute("classPath", "jmt.engine.random.PhaseTypeDistr");
                distributionNode.setAttribute("name", "Phase-Type");
                Element distrParNode = simDoc.createElement("subParameter");
                distrParNode.setAttribute("classPath", "jmt.engine.random.PhaseTypePar");
                distrParNode.setAttribute("name", "distrPar");

                Element subParNodeAlpha = simDoc.createElement("subParameter");
                subParNodeAlpha.setAttribute("array", "true");
                subParNodeAlpha.setAttribute("classPath", "java.lang.Object");
                subParNodeAlpha.setAttribute("name", "alpha");
                Element subParNodeAlphaVec = simDoc.createElement("subParameter");
                subParNodeAlphaVec.setAttribute("array", "true");
                subParNodeAlphaVec.setAttribute("classPath", "java.lang.Object");
                subParNodeAlphaVec.setAttribute("name", "vector");
                Map<Integer, Matrix> PH = sn.proc.get(i).get(r);
                Matrix alpha = sn.pie.get(i).get(r);
                alpha.abs();
                //TODO Cast to int
                for (int k = 0; k < sn.phases.get((int) i, r); k++) {
                    Element subParNodeAlphaElem = simDoc.createElement("subParameter");
                    subParNodeAlphaElem.setAttribute("classPath", "java.lang.Double");
                    subParNodeAlphaElem.setAttribute("name", "entry");
                    Element subParValue = simDoc.createElement("value");
                    subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", alpha.get(k))));
                    subParNodeAlphaElem.appendChild(subParValue);
                    subParNodeAlphaVec.appendChild(subParNodeAlphaElem);
                }
                Element subParNodeT = simDoc.createElement("subParameter");
                subParNodeT.setAttribute("array", "true");
                subParNodeT.setAttribute("classPath", "java.lang.Object");
                subParNodeT.setAttribute("name", "T");
                Matrix T = PH.get(0);
                //TODO Cast to int
                for (int k = 0; k < sn.phases.get((int) i, r); k++) {
                    Element subParNodeTvec = simDoc.createElement("subParameter");
                    subParNodeTvec.setAttribute("array", "true");
                    subParNodeTvec.setAttribute("classPath", "java.lang.Object");
                    subParNodeTvec.setAttribute("name", "vector");
                    //TODO Cast to int
                    for (int j = 0; j < sn.phases.get((int) i, r); j++) {
                        Element subParNodeTElem = simDoc.createElement("subParameter");
                        subParNodeTElem.setAttribute("classPath", "java.lang.Double");
                        subParNodeTElem.setAttribute("name", "entry");
                        Element subParValue = simDoc.createElement("value");
                        if (k == j) {
                            subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", -1 * Math.abs(T.get(k, j)))));
                        } else {
                            subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", Math.abs(T.get(k, j)))));
                        }
                        subParNodeTElem.appendChild(subParValue);
                        subParNodeTvec.appendChild(subParNodeTElem);
                    }
                    subParNodeT.appendChild(subParNodeTvec);
                }
                subParNodeAlpha.appendChild(subParNodeAlphaVec);
                distrParNode.appendChild(subParNodeAlpha);
                distrParNode.appendChild(subParNodeT);
                serviceTimeStrategyNode.appendChild(distributionNode);
                serviceTimeStrategyNode.appendChild(distrParNode);
            } else if (sn.proctype.get(i).get(r) == ProcessType.MAP) {
                serviceTimeStrategyNode.setAttribute("classPath", "jmt.engine.NetStrategies.ServiceStrategies.ServiceTimeStrategy");
                serviceTimeStrategyNode.setAttribute("name", "ServiceTimeStrategy");
                Element distributionNode = simDoc.createElement("subParameter");
                distributionNode.setAttribute("classPath", "jmt.engine.random.MAPDistr");
                distributionNode.setAttribute("name", "Burst (MAP)");
                Element distrParNode = simDoc.createElement("subParameter");
                distrParNode.setAttribute("classPath", "jmt.engine.random.MAPPar");
                distrParNode.setAttribute("name", "distrPar");

                Map<Integer, Matrix> MAP = sn.proc.get(i).get(r);

                Element subParNodeD0 = simDoc.createElement("subParameter");
                subParNodeD0.setAttribute("array", "true");
                subParNodeD0.setAttribute("classPath", "java.lang.Object");
                subParNodeD0.setAttribute("name", "D0");
                Matrix D0 = MAP.get(0);

                //TODO Cast to int
                for (int k = 0; k < sn.phases.get((int) i, r); k++) {
                    Element subParNodeD0vec = simDoc.createElement("subParameter");
                    subParNodeD0vec.setAttribute("array", "true");
                    subParNodeD0vec.setAttribute("classPath", "java.lang.Object");
                    subParNodeD0vec.setAttribute("name", "vector");
                    //TODO Cast to int
                    for (int j = 0; j < sn.phases.get((int) i, r); j++) {
                        Element subParNodeD0Elem = simDoc.createElement("subParameter");
                        subParNodeD0Elem.setAttribute("classPath", "java.lang.Double");
                        subParNodeD0Elem.setAttribute("name", "entry");
                        Element subParValue = simDoc.createElement("value");
                        subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", D0.get(k, j))));
                        subParNodeD0Elem.appendChild(subParValue);
                        subParNodeD0vec.appendChild(subParNodeD0Elem);
                    }
                    subParNodeD0.appendChild(subParNodeD0vec);
                }
                distrParNode.appendChild(subParNodeD0);

                Element subParNodeD1 = simDoc.createElement("subParameter");
                subParNodeD1.setAttribute("array", "true");
                subParNodeD1.setAttribute("classPath", "java.lang.Object");
                subParNodeD1.setAttribute("name", "D1");
                Matrix D1 = MAP.get(1);

                //TODO Cast to int
                for (int k = 0; k < sn.phases.get((int) i, r); k++) {
                    Element subParNodeD1vec = simDoc.createElement("subParameter");
                    subParNodeD1vec.setAttribute("array", "true");
                    subParNodeD1vec.setAttribute("classPath", "java.lang.Object");
                    subParNodeD1vec.setAttribute("name", "vector");
                    //TODO Cast to int
                    for (int j = 0; j < sn.phases.get((int) i, r); j++) {
                        Element subParNodeD1Elem = simDoc.createElement("subParameter");
                        subParNodeD1Elem.setAttribute("classPath", "java.lang.Double");
                        subParNodeD1Elem.setAttribute("name", "entry");
                        Element subParValue = simDoc.createElement("value");
                        subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", D1.get(k, j))));
                        subParNodeD1Elem.appendChild(subParValue);
                        subParNodeD1vec.appendChild(subParNodeD1Elem);
                    }
                    subParNodeD1.appendChild(subParNodeD1vec);
                }
                distrParNode.appendChild(subParNodeD1);
                serviceTimeStrategyNode.appendChild(distributionNode);
                serviceTimeStrategyNode.appendChild(distrParNode);
            } else {
                serviceTimeStrategyNode.setAttribute("classPath", "jmt.engine.NetStrategies.ServiceStrategies.ServiceTimeStrategy");
                serviceTimeStrategyNode.setAttribute("name", "ServiceTimeStrategy");

                Element distributionNode = simDoc.createElement("subParameter");
                String javaClass = "";
                String javaParClass = "";
                switch (sn.proctype.get(i).get(r)) {
                    case DET:
                        javaClass = "jmt.engine.random.DeterministicDistr";
                        javaParClass = "jmt.engine.random.DeterministicDistrPar";
                        break;
                    case COXIAN:
                        javaClass = "jmt.engine.random.CoxianDistr";
                        javaParClass = "jmt.engine.random.CoxianPar";
                        break;
                    case ERLANG:
                        javaClass = "jmt.engine.random.Erlang";
                        javaParClass = "jmt.engine.random.ErlangPar";
                        break;
                    case EXP:
                        javaClass = "jmt.engine.random.Exponential";
                        javaParClass = "jmt.engine.random.ExponentialPar";
                        break;
                    case GAMMA:
                        javaClass = "jmt.engine.random.GammaDistr";
                        javaParClass = "jmt.engine.random.GammaDistrPar";
                        break;
                    case HYPEREXP:
                        javaClass = "jmt.engine.random.HyperExp";
                        javaParClass = "jmt.engine.random.HyperExpPar";
                        break;
                    case PARETO:
                        javaClass = "jmt.engine.random.Pareto";
                        javaParClass = "jmt.engine.random.ParetoPar";
                        break;
                    case WEIBULL:
                        javaClass = "jmt.engine.random.Weibull";
                        javaParClass = "jmt.engine.random.WeibullPar";
                        break;
                    case LOGNORMAL:
                        javaClass = "jmt.engine.random.Lognormal";
                        javaParClass = "jmt.engine.random.LognormalPar";
                        break;
                    case UNIFORM:
                        javaClass = "jmt.engine.random.Uniform";
                        javaParClass = "jmt.engine.random.UniformPar";
                        break;
                    case MMPP2:
                        javaClass = "jmt.engine.random.MMPP2Distr";
                        javaParClass = "jmt.engine.random.MMPP2Par";
                        break;
                    case REPLAYER:
                    case TRACE:
                        javaClass = "jmt.engine.random.Replayer";
                        javaParClass = "jmt.engine.random.ReplayerPar";
                        break;
                }
                distributionNode.setAttribute("classPath", javaClass);
                switch (sn.proctype.get(i).get(r)) {
                    case REPLAYER:
                    case TRACE:
                        distributionNode.setAttribute("name", "Replayer");
                        break;
                    case EXP:
                        distributionNode.setAttribute("name", "Exponential");
                    case HYPEREXP:
                        distributionNode.setAttribute("name", "Hyperexponential");
                    default:
                        distributionNode.setAttribute("name", String.valueOf(sn.proctype.get(i).get(r)));
                        break;
                }
                serviceTimeStrategyNode.appendChild(distributionNode);

                Element distrParNode = simDoc.createElement("subParameter");
                distrParNode.setAttribute("classPath", javaParClass);
                distrParNode.setAttribute("name", "distrPar");

                Element subParNodeAlpha = simDoc.createElement("subParameter");
                Element subParValue = simDoc.createElement("value");
                double c;
                switch (sn.proctype.get(i).get(r)) {
                    case DET:
                        subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                        subParNodeAlpha.setAttribute("name", "t");
                        //TODO Cast to int
                        subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", sn.rates.get((int) i, r))));
                        subParNodeAlpha.appendChild(subParValue);
                        distrParNode.appendChild(subParNodeAlpha);
                        break;
                    case EXP:
                        subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                        subParNodeAlpha.setAttribute("name", "lambda");
                        //TODO Cast to int
                        subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", sn.rates.get((int) i, r))));
                        subParNodeAlpha.appendChild(subParValue);
                        distrParNode.appendChild(subParNodeAlpha);
                        break;
                    case HYPEREXP:
                        subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                        subParNodeAlpha.setAttribute("name", "p");
                        subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", sn.pie.get(i).get(r).get(0))));
                        subParNodeAlpha.appendChild(subParValue);
                        distrParNode.appendChild(subParNodeAlpha);
                        subParNodeAlpha = simDoc.createElement("subParameter");
                        subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                        subParNodeAlpha.setAttribute("name", "lambda1");
                        subParValue = simDoc.createElement("value");
                        subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", -1 * sn.proc.get(i).get(r).get(0).get(0, 0))));
                        subParNodeAlpha.appendChild(subParValue);
                        distrParNode.appendChild(subParNodeAlpha);
                        subParNodeAlpha = simDoc.createElement("subParameter");
                        subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                        subParNodeAlpha.setAttribute("name", "lambda2");
                        subParValue = simDoc.createElement("value");
                        subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", -1 * sn.proc.get(i).get(r).get(0).get(1, 1))));
                        subParNodeAlpha.appendChild(subParValue);
                        distrParNode.appendChild(subParNodeAlpha);
                        break;
                    case ERLANG:
                        subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                        subParNodeAlpha.setAttribute("name", "alpha");
                        //TODO Cast to int
                        subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", sn.rates.get((int) i, r) * sn.phases.get((int) i, r))));
                        subParNodeAlpha.appendChild(subParValue);
                        distrParNode.appendChild(subParNodeAlpha);
                        subParNodeAlpha = simDoc.createElement("subParameter");
                        subParNodeAlpha.setAttribute("classPath", "java.lang.Long");
                        subParNodeAlpha.setAttribute("name", "r");
                        subParValue = simDoc.createElement("value");
                        //TODO Cast to int
                        subParValue.appendChild(simDoc.createTextNode(String.format("%d", sn.phases.get((int) i, r))));
                        subParNodeAlpha.appendChild(subParValue);
                        distrParNode.appendChild(subParNodeAlpha);
                        break;
                    case MMPP2:
                        subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                        subParNodeAlpha.setAttribute("name", "lambda0");
                        subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", sn.proc.get(i).get(r).get(1).get(0, 0))));
                        subParNodeAlpha.appendChild(subParValue);
                        distrParNode.appendChild(subParNodeAlpha);
                        subParNodeAlpha = simDoc.createElement("subParameter");
                        subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                        subParNodeAlpha.setAttribute("name", "lambda1");
                        subParValue = simDoc.createElement("value");
                        subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", sn.proc.get(i).get(r).get(1).get(1, 1))));
                        subParNodeAlpha.appendChild(subParValue);
                        distrParNode.appendChild(subParNodeAlpha);
                        subParNodeAlpha = simDoc.createElement("subParameter");
                        subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                        subParNodeAlpha.setAttribute("name", "sigma0");
                        subParValue = simDoc.createElement("value");
                        subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", sn.proc.get(i).get(r).get(0).get(0, 1))));
                        subParNodeAlpha.appendChild(subParValue);
                        distrParNode.appendChild(subParNodeAlpha);
                        subParNodeAlpha = simDoc.createElement("subParameter");
                        subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                        subParNodeAlpha.setAttribute("name", "sigma1");
                        subParValue = simDoc.createElement("value");
                        subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", sn.proc.get(i).get(r).get(0).get(1, 0))));
                        subParNodeAlpha.appendChild(subParValue);
                        distrParNode.appendChild(subParNodeAlpha);
                        break;
                    case GAMMA:
                        subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                        subParNodeAlpha.setAttribute("name", "alpha");
                        //TODO Cast to int
                        subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", 1 / sn.scv.get((int) i, r))));
                        subParNodeAlpha.appendChild(subParValue);
                        distrParNode.appendChild(subParNodeAlpha);
                        subParNodeAlpha = simDoc.createElement("subParameter");
                        subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                        subParNodeAlpha.setAttribute("name", "beta");
                        subParValue = simDoc.createElement("value");
                        //TODO Cast to int
                        subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", sn.scv.get((int) i, r) / sn.rates.get((int) i, r))));
                        subParNodeAlpha.appendChild(subParValue);
                        distrParNode.appendChild(subParNodeAlpha);
                        break;
                    case PARETO:
                        //TODO Cast to int
                        double shape = Math.sqrt(1 + 1 / sn.scv.get((int) i, r)) + 1;
                        double scale = 1 / sn.rates.get((int) i, r) * (shape - 1) / shape;
                        subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                        subParNodeAlpha.setAttribute("name", "alpha");
                        subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", shape)));
                        subParNodeAlpha.appendChild(subParValue);
                        distrParNode.appendChild(subParNodeAlpha);
                        subParNodeAlpha = simDoc.createElement("subParameter");
                        subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                        subParNodeAlpha.setAttribute("name", "k");
                        subParValue = simDoc.createElement("value");
                        subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", scale)));
                        subParNodeAlpha.appendChild(subParValue);
                        distrParNode.appendChild(subParNodeAlpha);
                        break;
                    case WEIBULL:
                        //TODO Cast to int
                        c = Math.sqrt(sn.scv.get((int) i, r));
                        double rval = Math.pow(c, -1.086); //Justus approximation (1976)
                        double alpha = 1 / sn.rates.get((int) i, r) / Numerics.gammaFunction(1 + 1 / rval);
                        subParNodeAlpha = simDoc.createElement("subParameter");
                        subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                        subParNodeAlpha.setAttribute("name", "alpha");
                        subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", alpha)));
                        subParNodeAlpha.appendChild(subParValue);
                        distrParNode.appendChild(subParNodeAlpha);
                        subParNodeAlpha = simDoc.createElement("subParameter");
                        subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                        subParNodeAlpha.setAttribute("name", "r");
                        subParValue = simDoc.createElement("value");
                        subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", rval)));
                        subParNodeAlpha.appendChild(subParValue);
                        distrParNode.appendChild(subParNodeAlpha);
                        break;
                    case LOGNORMAL:
                        //TODO Cast to int
                        c = Math.sqrt(sn.scv.get((int) i, r));
                        double mu = Math.log(1 / sn.rates.get((int) i, r) / Math.sqrt(c * c + 1));
                        double sigma = Math.sqrt(Math.log(c * c + 1));
                        subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                        subParNodeAlpha.setAttribute("name", "mu");
                        subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", mu)));
                        subParNodeAlpha.appendChild(subParValue);
                        distrParNode.appendChild(subParNodeAlpha);
                        subParNodeAlpha = simDoc.createElement("subParameter");
                        subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                        subParNodeAlpha.setAttribute("name", "sigma");
                        subParValue = simDoc.createElement("value");
                        subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", sigma)));
                        subParNodeAlpha.appendChild(subParValue);
                        distrParNode.appendChild(subParNodeAlpha);
                        break;
                    case UNIFORM:
                        //TODO Cast to int
                        double maxVal = (Math.sqrt(12 * sn.scv.get((int) i, r) / Math.pow(sn.rates.get((int) i, r), 2)) + 2 / sn.rates.get((int) i, r)) / 2;
                        double minVal = 2 / sn.rates.get((int) i, r) - maxVal;
                        subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                        subParNodeAlpha.setAttribute("name", "min");
                        subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", minVal)));
                        subParNodeAlpha.appendChild(subParValue);
                        distrParNode.appendChild(subParNodeAlpha);
                        subParNodeAlpha = simDoc.createElement("subParameter");
                        subParNodeAlpha.setAttribute("classPath", "java.lang.Double");
                        subParNodeAlpha.setAttribute("name", "max");
                        subParValue = simDoc.createElement("value");
                        subParValue.appendChild(simDoc.createTextNode(String.format("%.12f", maxVal)));
                        subParNodeAlpha.appendChild(subParValue);
                        distrParNode.appendChild(subParNodeAlpha);
                        break;
                    case REPLAYER:
                    case TRACE:
                        subParNodeAlpha.setAttribute("classPath", "java.lang.String");
                        subParNodeAlpha.setAttribute("name", "fileName");
                        // subParValue.appendChild(simDoc.createTextNode(sn.nodeparam{ind}{r}.fileName));
                        subParValue.appendChild(simDoc.createTextNode(sn.nodeparam.get(ind).joinStrategy.get(r).name()));
                        subParNodeAlpha.appendChild(subParValue);
                        distrParNode.appendChild(subParNodeAlpha);
                        break;
                }
                serviceTimeStrategyNode.appendChild(distrParNode);
            }
            strategyNode.appendChild(serviceTimeStrategyNode);
            section.appendChild(strategyNode);
        }

        return new DocumentSectionPair(simDoc, section);
    }

    @Override
    public DocumentSectionPair saveClassSwitchStrategy(Document simDoc, Document section, int ind) {
        // [SIMDOC, SECTION] = SAVECLASSSWITCHSTRATEGY(SIMDOC, SECTION, NODEIDX)
        Element paramNode = simDoc.createElement("parameter");
        paramNode.setAttribute("array", "true");
        paramNode.setAttribute("classPath", "java.lang.Object");
        paramNode.setAttribute("name", "matrix");

        NetworkStruct sn = getStruct();
        int K = sn.nclasses;
        int i = ind;

        //TODO usage of find() here
        Matrix conn_i = new Matrix(0, 0);
        Matrix.extractRows(this.sn.connmatrix, i, i + 1, conn_i);
        Matrix conn_i_find = conn_i.find();

        for (int j = 0; j < conn_i_find.length(); j++) {
            for (int r = 0; r < K; r++) {
                Element refClassNode = simDoc.createElement("refClass");
                refClassNode.appendChild(simDoc.createTextNode(sn.classnames.get(r)));
                paramNode.appendChild(refClassNode);

                Element subParNodeRow = simDoc.createElement("subParameter");
                subParNodeRow.setAttribute("array", "true");
                subParNodeRow.setAttribute("classPath", "java.lang.Float");
                subParNodeRow.setAttribute("name", "row");
                for (int s = 0; s < K; s++) {
                    refClassNode = simDoc.createElement("refClass");
                    refClassNode.appendChild(simDoc.createTextNode(sn.classnames.get(s)));
                    subParNodeRow.appendChild(refClassNode);

                    Element subParNodeCell = simDoc.createElement("subParameter");
                    subParNodeCell.setAttribute("classPath", "java.lang.Float");
                    subParNodeCell.setAttribute("name", "cell");
                    Element valNode = simDoc.createElement("value");
                    valNode.appendChild(simDoc.createTextNode(String.format("%12.12f", sn.rtnodes.get((i - 1) * K + r, (j - 1) * K + s))));
                    subParNodeCell.appendChild(valNode);
                    subParNodeRow.appendChild(subParNodeCell);
                }
                paramNode.appendChild(subParNodeRow);
            }
            section.appendChild(paramNode);
        }
        return new DocumentSectionPair(simDoc, section);
    }

    @Override
    public DocumentSectionPair saveLogTunnel(Document simDoc, Document section, int ind) {
        // [SIMDOC, SECTION] = SAVELOGTUNNEL(SIMDOC, SECTION, NODEIDX)

        NetworkStruct sn = getStruct();
        List<String> loggerNodesCP = new ArrayList<>();
        loggerNodesCP.add("java.lang.String"); // index 0
        loggerNodesCP.add("java.lang.String"); // index 1
        for (int i = 2; i < 9; i++) { // indices 2 to 8
            loggerNodesCP.add("java.lang.Boolean");
        }
        loggerNodesCP.add("java.lang.Integer"); // index 9
        List<String> loggerNodesNames = Arrays.asList("logfileName", "logfilePath", "logExecTimestamp",
                "logLoggerName", "logTimeStamp", "logJobID",
                "logJobClass", "logTimeSameClass", "logTimeAnyClass",
                "numClasses");

        int numOfClasses = sn.nclasses;

        // logger specific path does not work in JMT at the moment
        if (!sn.nodeparam.get(ind).filePath.endsWith(File.separator)) {
            sn.nodeparam.get(ind).filePath = sn.nodeparam.get(ind).filePath + File.separator;
        }

        List<String> loggerNodesValues = new ArrayList<>();
        loggerNodesValues.add(sn.nodeparam.get(ind).fileName);
        loggerNodesValues.add(sn.nodeparam.get(ind).filePath);
        loggerNodesValues.add(sn.nodeparam.get(ind).startTime);
        loggerNodesValues.add(sn.nodeparam.get(ind).loggerName);
        loggerNodesValues.add(sn.nodeparam.get(ind).timestamp);
        loggerNodesValues.add(sn.nodeparam.get(ind).jobID);
        loggerNodesValues.add(sn.nodeparam.get(ind).jobClass);
        loggerNodesValues.add(sn.nodeparam.get(ind).timeSameClass);
        loggerNodesValues.add(sn.nodeparam.get(ind).timeAnyClass);
        loggerNodesValues.add(String.valueOf(numOfClasses));

        for (int j = 0; j < loggerNodesValues.size(); j++) {
            Element loggerNode = simDoc.createElement("parameter");
            loggerNode.setAttribute("classPath", loggerNodesCP.get(j));
            loggerNode.setAttribute("name", loggerNodesNames.get(j));
            Element valueNode = simDoc.createElement("value");
            valueNode.appendChild(simDoc.createTextNode(loggerNodesValues.get(j)));
            loggerNode.appendChild(valueNode);
            section.appendChild(loggerNode);
        }
        return new DocumentSectionPair(simDoc, section);
    }

    @Override
    public DocumentSectionPair saveForkStrategy(Document simDoc, Document section, int ind) {
        // [SIMDOC, SECTION] = SAVEFORKSTRATEGY(SIMDOC, SECTION, NODEIDX)
        NetworkStruct sn = getStruct();

        Element jplNode = simDoc.createElement("parameter");
        jplNode.setAttribute("classPath", "java.lang.Integer");
        jplNode.setAttribute("name", "jobsPerLink");
        Element valueNode = simDoc.createElement("value");
        valueNode.appendChild(simDoc.createTextNode(String.valueOf(sn.nodeparam.get(ind).fanout)));
        jplNode.appendChild(valueNode);
        section.appendChild(jplNode);

        Element blockNode = simDoc.createElement("parameter");
        blockNode.setAttribute("classPath", "java.lang.Integer");
        blockNode.setAttribute("name", "block");
        valueNode = simDoc.createElement("value");
        valueNode.appendChild(simDoc.createTextNode(String.valueOf(-1)));
        blockNode.appendChild(valueNode);
        section.appendChild(blockNode);

        Element issimplNode = simDoc.createElement("parameter");
        issimplNode.setAttribute("classPath", "java.lang.Boolean");
        issimplNode.setAttribute("name", "isSimplifiedFork");
        valueNode = simDoc.createElement("value");
        valueNode.appendChild(simDoc.createTextNode("true"));
        issimplNode.appendChild(valueNode);
        section.appendChild(issimplNode);

        Element strategyNode = simDoc.createElement("parameter");
        strategyNode.setAttribute("array", "true");
        strategyNode.setAttribute("classPath", "jmt.engine.NetStrategies.ForkStrategy");
        strategyNode.setAttribute("name", "ForkStrategy");

        int i = ind;
        int numOfClasses = sn.nclasses;

        for (int r = 0; r < numOfClasses; r++) {
            Element refClassNode = simDoc.createElement("refClass");
            refClassNode.appendChild(simDoc.createTextNode(sn.classnames.get(r)));
            strategyNode.appendChild(refClassNode);

            Element classStratNode = simDoc.createElement("subParameter");
            classStratNode.setAttribute("classPath", "jmt.engine.NetStrategies.ForkStrategies.ProbabilitiesFork");
            classStratNode.setAttribute("name", "Branch Probabilities");
            Element classStratNode2 = simDoc.createElement("subParameter");
            classStratNode2.setAttribute("array", "true");
            classStratNode2.setAttribute("classPath", "jmt.engine.NetStrategies.ForkStrategies.OutPath");
            classStratNode2.setAttribute("name", "EmpiricalEntryArray");
            if (Objects.requireNonNull(sn.routing.get(i).get(r)) == RoutingStrategy.PROB) {//TODO usage of find() here
                Matrix conn_i = new Matrix(0, 0);
                Matrix.extractRows(this.sn.connmatrix, i, i + 1, conn_i);
                Matrix conn_i_find = conn_i.find();

                Element classStratNode3 = simDoc.createElement("subParameter");
                Element classStratNode4;
                Element classStratNode4Station = simDoc.createElement("subParameter");
                Element classStratNode4StationValueNode = simDoc.createElement("value");

                for (int k = 0; k < conn_i_find.length(); k++) {
                    classStratNode3 = simDoc.createElement("subParameter");
                    classStratNode3.setAttribute("classPath", "jmt.engine.NetStrategies.ForkStrategies.OutPath");
                    classStratNode3.setAttribute("name", "OutPathEntry");
                    classStratNode4 = simDoc.createElement("subParameter");
                    classStratNode4.setAttribute("classPath", "jmt.engine.random.EmpiricalEntry");
                    classStratNode4.setAttribute("name", "outUnitProbability");
                    classStratNode4Station = simDoc.createElement("subParameter");
                    classStratNode4Station.setAttribute("classPath", "java.lang.String");
                    classStratNode4Station.setAttribute("name", "stationName");
                    classStratNode4StationValueNode = simDoc.createElement("value");
                    classStratNode4StationValueNode.appendChild(simDoc.createTextNode(String.format("%s", sn.nodenames.get(k))));
                }
                classStratNode4Station.appendChild(classStratNode4StationValueNode);
                classStratNode3.appendChild(classStratNode4Station);
                Element classStratNode4Probability = simDoc.createElement("subParameter");
                classStratNode4Probability.setAttribute("classPath", "java.lang.Double");
                classStratNode4Probability.setAttribute("name", "probability");
                Element classStratNode4ProbabilityValueNode = simDoc.createElement("value");
                classStratNode4ProbabilityValueNode.appendChild(simDoc.createTextNode("1.0"));
                classStratNode4Probability.appendChild(classStratNode4ProbabilityValueNode);
            }
            classStratNode.appendChild(classStratNode2);
            strategyNode.appendChild(classStratNode);
        }
        section.appendChild(strategyNode);
        return new DocumentSectionPair(simDoc, section);
    }

    @Override
    public DocumentSectionPair saveJoinStrategy(Document simDoc, Document section, int ind) {
        // [SIMDOC, SECTION] = SAVEJOINSTRATEGY(SIMDOC, SECTION, NODEIDX)
        Element strategyNode = simDoc.createElement("parameter");
        strategyNode.setAttribute("array", "true");
        strategyNode.setAttribute("classPath", "jmt.engine.NetStrategies.JoinStrategy");
        strategyNode.setAttribute("name", "JoinStrategy");

        NetworkStruct sn = this.getStruct();
        int numOfClasses = sn.nclasses;

        Element refClassNode2 = simDoc.createElement("refClass");
        for (int r = 0; r < numOfClasses; r++) {
            switch (sn.nodeparam.get(ind).joinStrategy.get(r)) {
                case STD:
                    refClassNode2 = simDoc.createElement("refClass");
                    refClassNode2.appendChild(simDoc.createTextNode(sn.classnames.get(r)));
                    strategyNode.appendChild(refClassNode2);

                    Element joinStrategyNode = simDoc.createElement("subParameter");
                    joinStrategyNode.setAttribute("classPath", "jmt.engine.NetStrategies.JoinStrategies.NormalJoin");
                    joinStrategyNode.setAttribute("name", "Standard Join");
                    Element reqNode = simDoc.createElement("subParameter");
                    reqNode.setAttribute("classPath", "java.lang.Integer");
                    reqNode.setAttribute("name", "numRequired");
                    Element valueNode = simDoc.createElement("value");
                    valueNode.appendChild(simDoc.createTextNode(String.valueOf(sn.nodeparam.get(ind).fanIn.get(r))));
                    reqNode.appendChild(valueNode);
                    joinStrategyNode.appendChild(reqNode);
                    strategyNode.appendChild(joinStrategyNode);
                    section.appendChild(strategyNode);
                    break;
                case Quorum:
                case Guard:
                    refClassNode2 = simDoc.createElement("refClass");
                    refClassNode2.appendChild(simDoc.createTextNode(sn.classnames.get(r)));
                    strategyNode.appendChild(refClassNode2);

                    joinStrategyNode = simDoc.createElement("subParameter");
                    joinStrategyNode.setAttribute("classPath", "jmt.engine.NetStrategies.JoinStrategies.PartialJoin");
                    joinStrategyNode.setAttribute("name", "Quorum");
                    reqNode = simDoc.createElement("subParameter");
                    reqNode.setAttribute("classPath", "java.lang.Integer");
                    reqNode.setAttribute("name", "numRequired");
                    valueNode = simDoc.createElement("value");
                    valueNode.appendChild(simDoc.createTextNode(String.valueOf(sn.nodeparam.get(ind).joinRequired.get(r))));
                    reqNode.appendChild(valueNode);
                    joinStrategyNode.appendChild(reqNode);
                    strategyNode.appendChild(joinStrategyNode);
                    section.appendChild(strategyNode);
                    break;

            }
        }
        return new DocumentSectionPair(simDoc, section);
    }


    @Override
    public ElementDocumentPair saveClasses(Document simElem, Document simDoc) {
        // [SIMELEM, SIMDOC] = SAVECLASSES(SIMELEM, SIMDOC)
        NetworkStruct sn = getStruct();

        int numOfClasses = sn.nclasses;

        for (int r = 0; r < numOfClasses; r++) {
            Element userClass = simDoc.createElement("userClass");
            userClass.setAttribute("name", sn.classnames.get(r));
            if (Double.isInfinite(sn.njobs.get(r))) {
                userClass.setAttribute("type", "open");
            } else {
                userClass.setAttribute("type", "closed");
            }
            userClass.setAttribute("priority", String.valueOf(sn.classprio.get(r)));
            double refStatIndex = sn.refstat.get(r);
            //TODO Cast to int
            double refNodeIndex = sn.stationToNode.get((int) sn.refstat.get(r));
            //TODO Cast to int
            String refStatName = sn.nodenames.get((int) refNodeIndex);
            if (!sn.proc.get(refStatIndex).get(r).isEmpty()) {
                if (Double.isInfinite(sn.njobs.get(r))) {
                    userClass.setAttribute("customers", String.valueOf(sn.njobs.get(r)));
                    userClass.setAttribute("referenceSource", refStatName);
                } else if (sn.proc.get(refStatIndex).get(r).get(0).hasNaN()) { // open disabled in source
                    userClass.setAttribute("referenceSource", "ClassSwitch");
                } else { // if other open
                    //TODO Cast to int
                    userClass.setAttribute("referenceSource", sn.nodenames.get((int) sn.stationToNode.get((int) sn.refstat.get(r))));
                }
            } else {
                userClass.setAttribute("referenceSource", sn.nodenames.get((int) sn.stationToNode.get((int) sn.refstat.get(r))));
            }
            simElem.appendChild(userClass);
        }
        return new ElementDocumentPair(simElem, simDoc);
    }

    @Override
    public ElementDocumentPair saveLinks(Document simElem, Document simDoc) {
        // [SIMELEM, SIMDOC] = SAVELINKS(SIMELEM, SIMDOC)
        NetworkStruct sn = getStruct();

        ArrayList<Integer> I = new ArrayList<>();
        ArrayList<Integer> J = new ArrayList<>();
        for (int i = 0; i < sn.connmatrix.numRows; i++) {
            for (int j = 0; j < sn.connmatrix.numCols; j++) {
                if (sn.connmatrix.get(i, j) != 0) {
                    I.add(i);
                    J.add(j);
                }
            }
        }
//        Matrix conn_find = sn.connmatrix.find();
//        int[] I = java.util.stream.IntStream.range(0, conn_find.numRows).toArray();
//        int[] J = java.util.stream.IntStream.range(0, conn_find.numCols).toArray();
        for (int k = 0; k < I.size(); k++) {
            int i = I.get(k);
            int j = J.get(k);
            Element connectionNode = simDoc.createElement("connection");
            connectionNode.setAttribute("source", sn.nodenames.get(i));
            connectionNode.setAttribute("target", sn.nodenames.get(j));
            simElem.appendChild(connectionNode);
        }
        return new ElementDocumentPair(simElem, simDoc);
    }

    @Override
    public ElementDocumentPair saveRegions(Document simElem, Document simDoc) {
        // [SIMELEM, SIMDOC] = SAVEREGIONS(SIMELEM, SIMDOC)
        NetworkStruct sn = getStruct();

//        // TODO model.regions
//        for (int r = 0; r < this.model.regions; r++) {
//        }

        return new ElementDocumentPair(simElem, simDoc);
    }

    @Override
    public ElementDocumentPair saveMetric(Document simElem, Document simDoc, Map<Station, Map<JobClass, SolverHandles.Metric>> handles) {
        // [SIMELEM, SIMDOC] = SAVEMETRICS(SIMELEM, SIMDOC)
//        for (int i = 0; i < handles.size(); i++) {
//            for (int r = 0; r < handles.get(i).length(); r++) {
//                double currentPerformanceIndex = handles.get(i).get(r);
//                if (currentPerformanceIndex.disabled == 0){
//                    Element performanceNode = simDoc.createElement("measure");
//                    performanceNode.setAttribute("alpha", String.format("%.2f", 1 - this.simConfInt));
//                    performanceNode.setAttribute("name", "Performance_" + i);
//                    performanceNode.setAttribute("nodeType", "station");
//                    performanceNode.setAttribute("precision", String.format("%.2f", this.simMaxRelErr));
//
//                    if (isempty(currentPerformanceIndex.station)){
//                        performanceNode.setAttribute("referenceNode", "");
//                    } else {
//                        performanceNode.setAttribute("referenceNode", currentPerformanceIndex.station.name);
//                    }
//                    performanceNode.setAttribute("referenceUserClass", currentPerformanceIndex.class.name);
//                    performanceNode.setAttribute("type", currentPerformanceIndex.type);
//                    performanceNode.setAttribute("verbose", "false");
//                    simElem.appendChild(performanceNode);
//                }
//            }
//        }
        return null;
    }

    @Override
    public ElementDocumentPair saveMetrics(Document simElem, Document simDoc) {
        // [SIMELEM, SIMDOC] = SAVEMETRICS(SIMELEM, SIMDOC)
        if (this.handles == null){
            this.getAvgHandles();
        }
        SolverHandles handles = this.handles;

        //TODO: further implementation
        ElementDocumentPair res = saveMetric(simElem, simDoc, handles.Q);
        res = saveMetric(simElem, simDoc, handles.U);
        res = saveMetric(simElem, simDoc, handles.R);
        res = saveMetric(simElem, simDoc, handles.T);
        res = saveMetric(simElem, simDoc, handles.A);

        // [simElem, simDoc] = saveMetric(self, simElem, simDoc, handles.W);

        // JMT ResidT is inconsistently defined with LINE"s on some
        // difficult class switching cases, hence we recompute it at the
        // level of the NetworkSolver class to preserve consistency.
        return res;
    }

    @Override
    public ElementDocumentPair saveXMLHeader(String logPath) throws ParserConfigurationException {
        // [SIMELEM,SIMDOC] = SAVEXMLHEADER(LOGPATH)

        String xmlnsXsi = "http://www.w3.org/2001/XMLSchema-instance";
        String fname = getFileName() + ".jsimg";
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document simDoc = docBuilder.newDocument();
        Element simElem = simDoc.createElement("sim");
        simElem.setAttribute("xmlns:xsi", xmlnsXsi);
        simElem.setAttribute("name", fname);
        // simElem.setAttribute("timestamp", ""Tue Jan 1 00:00:01 GMT+00:00 2000"");
        simElem.setAttribute("xsi:noNamespaceSchemaLocation", "SIMmodeldefinition.xsd");
        simElem.setAttribute("disableStatisticStop", "true");
        simElem.setAttribute("logDecimalSeparator", ".");
        simElem.setAttribute("logDelimiter", ";");
        simElem.setAttribute("logPath", logPath);
        simElem.setAttribute("logReplaceMode", "0");
        simElem.setAttribute("maxSamples", String.valueOf(this.maxSamples));
        simElem.setAttribute("maxEvents", String.valueOf(this.maxEvents));

        if (!Double.isInfinite(this.maxSimulatedTime)){
            simElem.setAttribute("maxSimulated", String.format("%.3f", this.maxSimulatedTime));
        }
        simElem.setAttribute("polling", "1.0");
        simElem.setAttribute("seed", String.valueOf(this.options.seed));
        simDoc.appendChild(simElem);
        return new ElementDocumentPair(simElem, simDoc);
    }

    public SolverJMT(Network model) {
        this(model, SolverJMT.defaultOptions());
    }

    public SolverJMT(Network model, SolverOptions options) {
        super(model, "SolverJMT");
        this.setOptions(options);
        this.simConfInt = 0.99;
        this.simMaxRelErr = 0.03;
        this.maxEvents = -1;
        this.jmtPath = getJmtPath(); //What this mean?
    }


    public String getJmtPath() {
        return jmtPath;
    }

    public void setJmtPath(String jmtPath) {
        this.jmtPath = jmtPath;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public void jsimwView(SolverOptions options) {

    }

    @Override
    public void jsimgView(SolverOptions options) {

    }

    @Override
    public String writeJSIM(NetworkStruct sn, String outputFileName) {
        return null;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public void setJmtJarPath(String path) {

    }

    @Override
    public String getJmtJarPath() {
        return null;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public double getMaxSimulatedTime() {
        return maxSimulatedTime;
    }

    public void setMaxSimulatedTime(double maxSimulatedTime) {
        this.maxSimulatedTime = maxSimulatedTime;
    }

    public int getMaxSamples() {
        return maxSamples;
    }

    public void setMaxSamples(int maxSamples) {
        this.maxSamples = maxSamples;
    }

    public int getMaxEvents() {
        return maxEvents;
    }

    public void setMaxEvents(int maxEvents) {
        this.maxEvents = maxEvents;
    }

    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public double getSimConfInt() {
        return simConfInt;
    }

    public void setSimConfInt(double simConfInt) {
        this.simConfInt = simConfInt;
    }

    public double getSimMaxRelErr() {
        return simMaxRelErr;
    }

    public void setSimMaxRelErr(double simMaxRelErr) {
        this.simMaxRelErr = simMaxRelErr;
    }

    @Override
    protected void runAnalyzer() throws IllegalAccessException {

    }


    public static SolverOptions defaultOptions() {
        return new SolverOptions(SolverType.JMT);
    }
}
