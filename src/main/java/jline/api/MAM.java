package jline.api;

import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.qore.KPC.MAP;

import jline.lang.JLineMatrix;

public class MAM {

    public static JLineMatrix map_pie(MAP map) {
    	DMatrixRMaj A = new DMatrixRMaj();
    	DMatrixRMaj ones = new DMatrixRMaj(map.D1.numCols, 1);
    	DMatrixRMaj AMultiOnes = new DMatrixRMaj();
    	DMatrixRMaj PIE = new DMatrixRMaj();
    	
    	CommonOps_DDRM.mult(map.ctmc(false), map.D1, A);
    	CommonOps_DDRM.fill(ones, 1);
    	CommonOps_DDRM.mult(A, ones, AMultiOnes);
    	CommonOps_DDRM.divide(A, AMultiOnes.get(0,0), PIE);
    	
    	JLineMatrix res = new JLineMatrix(PIE.getNumRows(),PIE.getNumCols());	
        for (int i = 0; i < PIE.getNumRows(); i++) {
            for (int j = 0; j < PIE.getNumCols(); j++) {
            	res.set(i, j, PIE.unsafe_get(i, j));
            }
        }
    	return res;
    }
}
