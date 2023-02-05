package jline.lang.distributions;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ejml.data.DMatrixRMaj;
import org.ejml.ops.DConvertMatrixStruct;
import org.qore.KPC.MAP;

import jline.lang.JLineMatrix;

public class Erlang extends MarkovianDistribution implements Serializable {
    public Erlang(double phaseRate, long nPhases) {
        super("jline.Erlang", 2);
        this.setParam(1, "alpha", phaseRate);
        this.setParam(2, "r", nPhases);
    }

    public List<Double> sample(int n)  {
        double alpha = (double)this.getParam(1).getValue();
        long r = (long) this.getParam(2).getValue();
        //return exprnd(1/lambda, n, 1);
        throw new RuntimeException("Not Implemented!");
    }

    public long getNumberOfPhases() {
        return (long) this.getParam(2).getValue();
    }

    public double getMean() {
        double alpha = (double)this.getParam(1).getValue();
        long r = (long) this.getParam(2).getValue();
        return r/alpha;
    }

    public double getVar() {
        double alpha = (double)this.getParam(1).getValue();
        long r = (long) this.getParam(2).getValue();
        return r/Math.pow(alpha,2);
    }

    public double getSkew() {
        long r = (long) this.getParam(2).getValue();
        return 2.0/Math.sqrt(r);
    }

    public double getSCV() {
        long r = (long) this.getParam(2).getValue();
        return 1.0/r;
    }

    public double getRate() {
        return 1.0/getMean();
    }

    public double evalCDF(double t) {
        double alpha = (double)this.getParam(1).getValue();
        long r = (long) this.getParam(2).getValue();
        double ft = 1;

        for (int j = 0; j < r; j++) {
            int fac_j = 1;
            for (int k = 2; k <= j; k++) {
                fac_j *= k;
            }
            ft -= Math.exp(-alpha*t)*(alpha*t)*j/fac_j;
        }

        return ft;
    }

    public Map<Integer, JLineMatrix> getPH()  {
        long r = (long) this.getParam(2).getValue();
		double mu = r/getMean();
		int size = (int) r;
		DMatrixRMaj D0 = new DMatrixRMaj(size, size);
		DMatrixRMaj D1 = new DMatrixRMaj(size, size);
		
		for(int i = 0; i < size - 1; i++) {
			D0.set(i, i+1, mu);
		}
		D1.set(size - 1, 0, mu);
		
		MAP map = new MAP(D0, D1);
		map.normalize();
		
		Map<Integer, JLineMatrix> res = new HashMap<Integer, JLineMatrix>();
		res.put(0, new JLineMatrix(size, size));
		res.put(1, new JLineMatrix(size, size));
		DConvertMatrixStruct.convert(map.D0, res.get(0), 0);
		DConvertMatrixStruct.convert(map.D1, res.get(1), 0);
        return res;
    }
    
    public double evalLST(double s) {
        double alpha = (double)this.getParam(1).getValue();
        long r = (long) this.getParam(2).getValue();
        return Math.pow(alpha/(alpha+s), r);
    }

    public static Erlang fitMeanAndSCV(double mean, double scv) {
        double r = Math.ceil(scv);
        double alpha = r/mean;
        return new Erlang(alpha, (long)r);
    }

    public static Erlang fitMeanAndStdDev(double mean, double stdDev) {
        return Erlang.fitMeanAndSCV(mean, (mean/Math.pow(stdDev,2)));
    }

    // Fit distribution with given mean and number of phases
    public static Erlang fitMeanAndOrder(double mean, double numPhases) {

        double SCV = 1 / numPhases;
        long r = (long) Math.ceil(1 / SCV);
        double alpha = r / mean;
        Erlang er = new Erlang(alpha, r);
        er.immediate = mean < Distribution.tolerance;
        return er;
    }
}
