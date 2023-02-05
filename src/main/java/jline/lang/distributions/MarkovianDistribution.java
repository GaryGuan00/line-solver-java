package jline.lang.distributions;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.ops.DConvertMatrixStruct;
import org.qore.KPC.MAP;

import jline.lang.JLineMatrix;
import jline.util.Interval;

abstract public class MarkovianDistribution extends Distribution implements Serializable {
	
	protected Map<Integer, JLineMatrix> representation; // <0, D0>, <1, D1>, <2, D2>
	
    public MarkovianDistribution(String name, int numParam) {
        super(name, numParam, new Interval(0, Double.POSITIVE_INFINITY));
    }

    public abstract long getNumberOfPhases();
    public abstract Map<Integer, JLineMatrix> getPH();
    
    public JLineMatrix getMu() {
    	JLineMatrix aph_1 = getRepres().get(0);
    	int size = Math.min(aph_1.numCols, aph_1.numRows);
    	JLineMatrix res = new JLineMatrix(size, 1, size);
    	for(int i = 0; i < size; i++) {
    		res.set(i, 0, -aph_1.get(i, i));
    	}
    	return res;
    }
    
    public JLineMatrix getPhi() {
    	Map<Integer, JLineMatrix> aph = this.getRepres();
    	JLineMatrix ones = new JLineMatrix(aph.get(0).numRows, 1, aph.get(0).numRows);
    	JLineMatrix res = new JLineMatrix(aph.get(1).numRows, 1);
    	JLineMatrix mu = getMu();
    	
    	ones.fill(1.0);
    	aph.get(1).mult(ones, res);
    	res.divideRows(mu.nz_values, 0);
    	return res;
    }
    
    public Map<Integer, JLineMatrix> getRepres() {
		try {
			if (this.representation == null)
				this.representation = getPH();
		} catch(Exception e) {
			e.printStackTrace(); //This function might not be implemented
		}
    	return this.representation;
    }

    public double getMean() {
    	Map<Integer, JLineMatrix> rep = getRepres();
    	if (rep.get(0).hasNaN()) {
    		return Double.NaN;
    	} else {
    		DMatrixRMaj D0 = DConvertMatrixStruct.convert(rep.get(0), (DMatrixRMaj)null);
    		DMatrixRMaj D1 = DConvertMatrixStruct.convert(rep.get(1), (DMatrixRMaj)null);
    		MAP map = new MAP(D0, D1);
    		return map.getMean();
    	}
    }
    
    public double getSCV() {
    	Map<Integer, JLineMatrix> rep = getRepres();
    	if (rep.get(0).hasNaN()) {
    		return Double.NaN;
    	} else {
    		DMatrixRMaj D0 = DConvertMatrixStruct.convert(rep.get(0), (DMatrixRMaj)null);
    		DMatrixRMaj D1 = DConvertMatrixStruct.convert(rep.get(1), (DMatrixRMaj)null);
    		MAP map = new MAP(D0, D1);
    		return map.getSCV();
    	}
    }
    
    public double evalCDF(double t) {
    	Map<Integer, JLineMatrix> rep = getRepres();
		DMatrixRMaj D0 = DConvertMatrixStruct.convert(rep.get(0), (DMatrixRMaj)null);
		DMatrixRMaj D1 = DConvertMatrixStruct.convert(rep.get(1), (DMatrixRMaj)null);
		MAP map = new MAP(D0, D1);
		//return map.evalCDF(t); Not implemented in MAP
		throw new RuntimeException("Not implemented");
    }
    
    public double evalLST(double t) {
    	Map<Integer, JLineMatrix> rep = getRepres();
		DMatrixRMaj D0 = DConvertMatrixStruct.convert(rep.get(0), (DMatrixRMaj)null);
		DMatrixRMaj D1 = DConvertMatrixStruct.convert(rep.get(1), (DMatrixRMaj)null);
		MAP map = new MAP(D0, D1);
		
		//Below is the function map_pie
		DMatrixRMaj PIE = new DMatrixRMaj();
		CommonOps_DDRM.mult(map.ctmc(false), D1, PIE);
		DMatrixRMaj ones = new DMatrixRMaj(D1.numCols, 1);
		CommonOps_DDRM.fill(ones, 1);
		DMatrixRMaj temp = new DMatrixRMaj();
		CommonOps_DDRM.mult(PIE, ones, temp);
		CommonOps_DDRM.divide(PIE, temp.get(0,0));
		
		DMatrixRMaj A = D0.copy();
		DMatrixRMaj e = new DMatrixRMaj(PIE.numCols, 1);
		CommonOps_DDRM.fill(e, 1);
		
		//pie*inv(s*eye(size(A))-A)*(-A)*e
		double[] diagEl = new double[Math.min(A.numRows, A.numCols)];
		Arrays.fill(diagEl, 1);
		DMatrixRMaj eye = CommonOps_DDRM.diagR(A.numRows, A.numCols, diagEl);
		CommonOps_DDRM.scale(t, eye);
		DMatrixRMaj res = new DMatrixRMaj();
		CommonOps_DDRM.subtract(eye, A, res);
		CommonOps_DDRM.invert(res);
		CommonOps_DDRM.mult(PIE, res.copy(), res);
		CommonOps_DDRM.changeSign(A);
		CommonOps_DDRM.mult(res.copy(), A, res);
		CommonOps_DDRM.mult(res.copy(), e, res);
    	return res.get(0,0);
    }
    
    public double getSkew() {
    	Map<Integer, JLineMatrix> rep = getRepres();
    	if (rep.get(0).hasNaN()) {
    		return Double.NaN;
    	} else {
    		DMatrixRMaj D0 = DConvertMatrixStruct.convert(rep.get(0), (DMatrixRMaj)null);
    		DMatrixRMaj D1 = DConvertMatrixStruct.convert(rep.get(1), (DMatrixRMaj)null);
    		MAP map = new MAP(D0, D1);
    		double[] m = map.getMoments();
    		double M3 = m[2] - 3*m[1]*m[0] + 2*Math.pow(m[0], 3);
    		return M3 / Math.pow(Math.sqrt(map.getSCV())*m[0], 3);
    	}
    }
}
