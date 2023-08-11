package jline.util;

import jline.lang.constant.GlobalConstants;
import jline.solvers.SolverOptions;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NetworkAvgTable {

    ArrayList<List<Double>> T;
    List<String> classNames;
    List<String> stationNames;

    public NetworkAvgTable(List<Double> Qval, List<Double> Uval, List<Double> Rval, List<Double> Residval, List<Double> Tval) {
        this.T = new ArrayList<>(Arrays.asList(Qval, Uval, Rval, Residval, Tval));
    }

    public void setClassNames(java.util.List<String> classNames) {
        this.classNames = classNames;
    }
    public void setStationNames(java.util.List<String> stationNames) {
        this.stationNames = stationNames;
    }
    public List<Double> get(int col) {
        return this.T.get(col);
    }

    List<Double> getQLen() {
        return this.T.get(0);
    }
    List<Double> getUtil() {
        return this.T.get(1);
    }

    List<Double> getRespT() {
        return this.T.get(2);
    }

    List<Double> getResidT() {
        return this.T.get(3);
    }

    List<Double> getTput() {
        return this.T.get(4);
    }

//    List<Double> getArvR() {
//        return this.T.get(5);
//    }
    public void print(SolverOptions options){
        if (options.verbose != SolverOptions.VerboseLevel.SILENT) {
            System.out.printf(
                    "%-12s\t %-12s\t %-16s\t %-16s\t %-16s\t %-16s\t %-16s",
                    "Station", "JobClass", "QLen", "Util", "RespT", "ResidT", "Tput");
            System.out.println(
                    "\n----------------------------------------------------------------------------------------------------------------------------------");
            NumberFormat nf = NumberFormat.getNumberInstance();
            //nf.setMinimumFractionDigits(5);
            nf.setMinimumFractionDigits(16);
            for (int i = 0; i < stationNames.size(); i++) {
                if (getQLen().get(i) > GlobalConstants.Zero ||
                        getUtil().get(i) > GlobalConstants.Zero ||
                        getRespT().get(i) > GlobalConstants.Zero ||
                        getResidT().get(i) > GlobalConstants.Zero ||
                        getTput().get(i) > GlobalConstants.Zero) {
                    System.out.format(
                            "%-12s\t %-12s\t %-10s\t %-10s\t %-10s\t %-10s\t %-10s\n",
                            stationNames.get(i),
                            classNames.get(i),
                            nf.format(getQLen().get(i)),
                            nf.format(getUtil().get(i)),
                            nf.format(getRespT().get(i)),
                            nf.format(getResidT().get(i)),
                            nf.format(getTput().get(i)));
                }
            }
            System.out.println(
                    "----------------------------------------------------------------------------------------------------------------------------------");
        }
    }
}
