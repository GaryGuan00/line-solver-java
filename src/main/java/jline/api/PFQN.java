package jline.api;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;

import jline.lang.JLineMatrix;
import jline.lang.nodes.Station;

public class PFQN {

	public static JLineMatrix pfqn_lldfun(JLineMatrix n, JLineMatrix lldscaling, JLineMatrix nservers) {
		int M = n.length();
		JLineMatrix r = new JLineMatrix(M, 1);
		r.fill(1.0);
		int smax = lldscaling.numCols;
		double alpha = 20;
		
		for(int i = 0; i < M; i++) {
			if (!(nservers == null || nservers.isEmpty())) {
				if (Double.isInfinite(nservers.get(i))) {
					r.set(i, 0, 1);
				} else {
					double val = r.get(i, 0) / UTIL.softmin(n.get(i), nservers.get(i), alpha);
					if (Double.isNaN(val))
						r.set(i, 0, 1.0 / Math.min(n.get(i), nservers.get(i)));
					else
						r.set(i, 0, val);
				}
			}
			
			if (lldscaling != null && !lldscaling.isEmpty()) {
				JLineMatrix lldscaling_i = new JLineMatrix(1,smax);
				JLineMatrix.extract(lldscaling, i, i+1, 0, smax, lldscaling_i, 0, 0);
				if(lldscaling_i.elementMax() != lldscaling_i.elementMin()) {
					double[] X = new double[smax];
					double[] V = new double[smax];
					for(int j = 0; j < smax; j++) {
						X[j] = j+1;
						V[j] = lldscaling.get(i,j);
					}
					r.set(i, 0, r.get(i,0) / (new SplineInterpolator().interpolate(X, V)).value(n.get(i)));
				}
			}
		}
		return r;
	}

	public static JLineMatrix pfqn_cdfun(JLineMatrix nvec, Map<Station, Function<JLineMatrix, Double>> cdscaling, List<Station> stations) {
		int M = nvec.getNumRows();
		JLineMatrix r = new JLineMatrix(M, 1);
		r.fill(1.0);
		if (!(cdscaling == null || cdscaling.size() == 0)) {
			for(int i = 0; i < M; i++) 
				r.set(i, 0, 1.0/cdscaling.get(stations.get(i)).apply(JLineMatrix.extractRows(nvec, i, i+1, null)));
		}
		return r;
	}
}
