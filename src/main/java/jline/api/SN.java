package jline.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import jline.lang.JLineMatrix;
import jline.lang.NetworkStruct;
import jline.lang.nodes.Node;
import jline.lang.nodes.Sink;

public class SN {
	
    public static void snRefreshVisits(NetworkStruct sn, JLineMatrix chains, JLineMatrix rt, JLineMatrix rtnodes) {
    	int I = sn.nNodes;
    	int M = sn.nStateful;
    	int K = sn.nClasses;
    	JLineMatrix refstat = sn.refstat;
    	int nchains = sn.nchains;
    	
    	/* Obtain chain characteristics */
    	Map<Integer, JLineMatrix> inchain = sn.inchain;
    	for(int c = 0; c < nchains; c++) {
    		JLineMatrix inchain_c = inchain.get(c);
			double val = refstat.get((int) inchain_c.get(0,0), 0);
			for(int col = 1; col < inchain_c.getNumCols(); col++) {
				int row = (int) inchain_c.get(0, col);
				if (val != refstat.get(row, 0))
					refstat.set(row, 0, val);
					//throw new RuntimeException("Classes within chain have different reference station");
			}
    	}
    	
    	/* Transfer inchain to List<Integer> in order to reduce the time of type conversion (double -> int) which is time consuming) */
    	Map<Integer, List<Integer>> new_inchain = new HashMap<Integer, List<Integer>>();
    	for(int c = 0; c < nchains; c++) {
    		JLineMatrix inchain_c = inchain.get(c);
    		List<Integer> inchain_c_list = new ArrayList<Integer>();
    		for(int i = 0; i < inchain_c.getNumCols(); i++)
    			inchain_c_list.add((int) inchain_c.get(i));
    		new_inchain.put(c, inchain_c_list);
    	}
    	
    	/* Generate visits */
    	Map<Integer, JLineMatrix> visits = new HashMap<Integer, JLineMatrix>();
    	for(int c = 0; c < nchains; c++) {
    		List<Integer> inchain_c = new_inchain.get(c);
    		List<Integer> cols = new ArrayList<Integer>();	//If use JLineMatrix, there would be more data type transfer in Pchain creation
    		for(int i = 0; i < M; i++) {
    			for(int ik = 0; ik < inchain_c.size(); ik++) {
    				cols.add(i*K + inchain_c.get(ik));
    			}
    		}
    		
    		//Pchain = rt(cols,cols);
    		JLineMatrix Pchain = new JLineMatrix(cols.size(), cols.size());
    		for(int row = 0; row < cols.size(); row++) {
    			for(int col = 0; col < cols.size(); col++) {
    				Pchain.set(row, col, rt.get(cols.get(row), cols.get(col)));
    			}
    		}
    		
    		//visited = sum(Pchain,2) > 0;
    		JLineMatrix visited = new JLineMatrix(Pchain.getNumRows(), 1);
    		int countTrue = 0;
    		for(int row = 0; row < Pchain.getNumRows(); row++) {
    			if (Pchain.sumRows(row) > 0) {
    				countTrue++;
    				visited.set(row, 0, 1.0);
    			}
    		}
    		
    		//alpha_visited = dtmc_solve(Pchain(visited,visited));
    		JLineMatrix input = new JLineMatrix(countTrue, countTrue);
    		int row_input = 0, col_input = 0;
    		for(int row = 0; row < visited.getNumRows(); row++) {
        		if (visited.get(row,0) > 0) {
        			for(int col = 0; col < visited.getNumRows(); col++) {
        				if (row == col || visited.get(col,0) > 0)
        					input.set(row_input, (col_input++)%countTrue, Pchain.get(row, col));
        			}
        			row_input++;
        		}
    		}
    		JLineMatrix alpha_visited = CTMC.dtmc_solve(input);
    		
    		//alpha = zeros(1,M*K); alpha(visited) = alpha_visited;
    		JLineMatrix alpha = new JLineMatrix(1, M*K);
    		int idx = 0;
    		for(int row = 0; row < visited.getNumRows(); row++) {
    			if (visited.get(row, 0) > 0)
    				alpha.set(0, row, alpha_visited.get(0, idx++));
    		}
    		
    		JLineMatrix visits_c = new JLineMatrix(M,K);
    		for(int i = 0; i < M; i++) {
    			for(int k = 0; k < inchain_c.size(); k++) {
    				visits_c.set(i, inchain_c.get(k), alpha.get(0, i*inchain_c.size()+k));
    			}
    		}
    		
    		//visits{c} = visits{c} / sum(visits{c}(refstat(inchain{c}(1)),inchain{c}));
    		double sum = 0;
    		int row = (int) sn.stationToStateful.get((int) refstat.get(inchain_c.get(0)));
    		for(int i = 0; i < inchain_c.size(); i++) {
    			sum += visits_c.get(row, inchain_c.get(i));
    		}
    		JLineMatrix visits_c_divide = new JLineMatrix(0,0);
    		visits_c.divide(sum, visits_c_divide, true);
    		
    		visits_c_divide.abs();
    		visits.put(c, visits_c_divide);
    	}
    	
    	/* Generate node visits */
    	Map<Integer, JLineMatrix> nodeVisits = new HashMap<Integer, JLineMatrix>();
    	for(int c = 0; c < nchains; c++) {
    		List<Integer> inchain_c = new_inchain.get(c);
    		List<Integer> nodes_cols = new ArrayList<Integer>();	//If use JLineMatrix, there would be more data type transfer in Pchain creation
    		for(int i = 0; i < I; i++) {
    			for(int ik = 0; ik < inchain_c.size(); ik++) {
    				nodes_cols.add(i*K + inchain_c.get(ik));
    			}
    		}
    		
    		JLineMatrix nodes_Pchain = new JLineMatrix(nodes_cols.size(), nodes_cols.size());
    		for(int row = 0; row < nodes_cols.size(); row++) {
    			for(int col = 0; col < nodes_cols.size(); col++) {
    				nodes_Pchain.set(row, col, rtnodes.get(nodes_cols.get(row), nodes_cols.get(col)));
    			}
    		}
    		
    		JLineMatrix nodes_visited = new JLineMatrix(nodes_Pchain.getNumRows(), 1);
    		int countTrue = 0;
    		for(int row = 0; row < nodes_Pchain.getNumRows(); row++) {
    			if (nodes_Pchain.sumRows(row) > 0) {
    				countTrue++;
    				nodes_visited.set(row, 0, 1.0);
    			}
    		}
    		
    		JLineMatrix input = new JLineMatrix(countTrue, countTrue);
    		int row_input = 0, col_input = 0;
    		for(int row = 0; row < nodes_visited.getNumRows(); row++) {
        		if (nodes_visited.get(row,0) > 0) {
        			for(int col = 0; col < nodes_visited.getNumRows(); col++) {
        				if (row == col || nodes_visited.get(col,0) > 0)
        					input.set(row_input, (col_input++)%countTrue, nodes_Pchain.get(row, col));
        			}
        			row_input++;
        		}
    		}
    		JLineMatrix nodes_alpha_visited = CTMC.dtmc_solve(input);
    		
    		JLineMatrix nodes_alpha = new JLineMatrix(1, I*K);
    		int idx = 0;
    		for(int row = 0; row < nodes_visited.getNumRows(); row++) {
    			if (nodes_visited.get(row, 0) > 0)
    				nodes_alpha.set(0, row, nodes_alpha_visited.get(0, idx++));
    		}
    		
    		JLineMatrix node_visits_c = new JLineMatrix(I,K);
    		for(int i = 0; i < I; i++) {
    			for(int k = 0; k < inchain_c.size(); k++) {
    				node_visits_c.set(i, inchain_c.get(k), nodes_alpha.get(0, i*inchain_c.size()+k));
    			}
    		}
    		
    		double sum = 0;
    		int row = (int) refstat.get(inchain_c.get(0));
    		for(int i = 0; i < inchain_c.size(); i++) {
    			sum += node_visits_c.get(row, inchain_c.get(i));
    		}
    		JLineMatrix node_visits_c_divide = new JLineMatrix(0,0);
    		node_visits_c.divide(sum, node_visits_c_divide, true);
    	    
    		node_visits_c_divide.removeNegative();
    		node_visits_c_divide.removeNaN();
    		nodeVisits.put(c, node_visits_c_divide);	
    	}
    	
    	/* Save result in sn */
    	sn.visits = visits;
    	sn.nodevisits = nodeVisits;
    	sn.isslc = new JLineMatrix(sn.nchains, 1);
    	for(int c = 0; c < nchains; c++) {
    		if (visits.get(c).getNonZeroLength() == 1)
    			sn.isslc.set(c, 0, 1.0);
    	}
    }

    public static snGetDemandsChainReturn snGetDemandsChain(NetworkStruct sn) {
    	int M = sn.nstations;
    	int K = sn.nClasses;
    	int C = sn.nchains;
    	JLineMatrix N = sn.njobs;
    	
    	JLineMatrix scv = sn.scv.clone();
    	scv.apply(Double.NaN, 1, "equal");
    	
    	JLineMatrix ST = new JLineMatrix(0,0);
    	sn.rates.divide(1, ST, false);
    	ST.removeNaN();
    	
    	JLineMatrix alpha = new JLineMatrix(M, K);
    	JLineMatrix Vchain = new JLineMatrix(M, C);
    	for(int c = 0; c < C; c++) {
    		JLineMatrix inchain = sn.inchain.get(c);
    		if (sn.refclass.get(0,c) > -1) {
    			for (int i = 0; i < M; i++) {
    				//Vchain(i,c) = sum(sn.visits{c}(i,inchain)) / sum(sn.visits{c}(sn.refstat(inchain(1)),sn.refclass(c)));
    				JLineMatrix visits = sn.visits.get(c);
    				double res = 0;
    				int iIdx = (int) sn.stationToStateful.get(i);
    				for(int col = 0; col < inchain.numCols; col++)
    					res += visits.get(iIdx, (int) inchain.get(0, col));
    				Vchain.set(i, c, res/visits.get((int) sn.stationToStateful.get((int)sn.refstat.get((int)inchain.get(0,0), 0)), (int)sn.refclass.get(0,c)));
    				//alpha(i,k) = alpha(i,k) + sn.visits{c}(i,k) / sum(sn.visits{c}(i,inchain));
    				for(int col = 0; col < inchain.numCols; col++) {
    					int k = (int)inchain.get(0, col);
    					alpha.set(i, k, alpha.get(i,k) + visits.get(i,k)/res);
    				}
    			}
    		} else {
    			for (int i = 0; i < M; i++) {
    				//Vchain(i,c) = sum(sn.visits{c}(i,inchain)) / sum(sn.visits{c}(sn.refstat(inchain(1)),inchain));
    				JLineMatrix visits = sn.visits.get(c);
    				double res1 = 0, res2 = 0;
    				int refIdx = (int) sn.stationToStateful.get((int) sn.refstat.get((int) inchain.get(0,0), 0));
    				int iIdx = (int) sn.stationToStateful.get(i);
    				for(int col = 0; col < inchain.numCols; col++) {
    					int idx = (int) inchain.get(0, col);
    					res1 += visits.get(iIdx, idx);
    					res2 += visits.get(refIdx, idx);
    				}
    				Vchain.set(i, c, res1/res2);
    				//alpha(i,k) = alpha(i,k) + sn.visits{c}(i,k) / sum(sn.visits{c}(i,inchain));
    				for(int col = 0; col < inchain.numCols; col++) {
    					int k = (int)inchain.get(0, col);
    					alpha.set(i, k, alpha.get(i,k) + visits.get(iIdx,k)/res1);
    				}
    			}
    		}
    	}
    	
    	Vchain.apply(Double.POSITIVE_INFINITY, 0, "equal");
    	Vchain.apply(Double.NaN, 0, "equal");
    	for(int c = 0; c < C; c++) {
    		double val = Vchain.get((int) sn.refstat.get((int) sn.inchain.get(c).get(0,0), 0), c);
    		for(int i = Vchain.col_idx[c]; i < Vchain.col_idx[c+1]; i++)
    			Vchain.nz_values[i] /= val;
    	}
    	alpha.apply(Double.POSITIVE_INFINITY, 0, "equal");
    	alpha.apply(Double.NaN, 0, "equal");
    	alpha.apply(1e-12, 0, "less");
    	
    	JLineMatrix Lchain = new JLineMatrix(M, C);
    	JLineMatrix STchain = new JLineMatrix(M, C);
    	JLineMatrix SCVchain = new JLineMatrix(M, C);
    	JLineMatrix Nchain = new JLineMatrix(1, C);
    	JLineMatrix refstatchain = new JLineMatrix(C, 1);
    	for(int c = 0; c < C; c++) {
    		JLineMatrix inchain = sn.inchain.get(c);
    		//Nchain(c) = sum(N(inchain)); isOpenChain = any(isinf(N(inchain)));
    		boolean isOpenChain = false;
    		double sum = 0;
    		for(int col = 0; col < inchain.numCols; col++) {
    			sum += N.get((int) inchain.get(0, col));
    			if(Double.isInfinite(sum)) {
    				isOpenChain = true;
    				break;
    			}
    		}
    		Nchain.set(0, c, sum);
    		
    		for(int i = 0; i < M; i++) {
    			sum = 0;
    			if (isOpenChain && i == sn.refstat.get((int) inchain.get(0,0),0)) {
    				//STchain(i,c) = 1 / sumfinite(sn.rates(i,inchain));
    				for(int col = 0; col < inchain.numCols; col++) {
    					double val = sn.rates.get(i, (int) inchain.get(0, col));
    					if (Double.isFinite(val))
    						sum += val;
    				}
    				STchain.set(i, c, 1/sum);
    			} else {
    				//STchain(i,c) = ST(i,inchain) * alpha(i,inchain)';
        			for(int col = 0; col < inchain.numCols; col++) {
        				int idx = (int) inchain.get(0, col);
        				sum += ST.get(i, idx) * alpha.get(i, idx);
        			}
        			STchain.set(i, c, sum);
    			}
    			Lchain.set(i, c, Vchain.get(i,c) * STchain.get(i,c));
    			//alphachain = sum(alpha(i,inchain(isfinite(SCV(i,inchain))))');
    			double alphachain = 0;
    			for(int col = 0; col < inchain.numCols; col++) {
    				int idx = (int) inchain.get(0, col);
    				double val = scv.get(i, idx);
    				if (Double.isFinite(val))
    					alphachain += alpha.get(i, idx);
    			}
    			if (alphachain > 0) {
    				sum = 0;
    				for(int col = 0; col < inchain.numCols; col++) {
    					int idx = (int) inchain.get(0, col);
    					sum += scv.get(i, idx) * alpha.get(i, idx);
    				}
    				SCVchain.set(i, c, sum/alphachain);
    			}
    		}
    		refstatchain.set(c, 0, sn.refstat.get((int) inchain.get(0,0), 0));
    		for(int col = 1; col < inchain.numCols; col++) {
    			int classIdx = (int) inchain.get(0, col);
    			if (sn.refstat.get(classIdx, 0) != refstatchain.get(c, 0))
    				throw new RuntimeException("Class have different reference station");
    		}
     	}
    	Lchain.apply(Double.POSITIVE_INFINITY, 0, "equal");
    	Lchain.apply(Double.NaN, 0, "equal");
    	STchain.apply(Double.POSITIVE_INFINITY, 0, "equal");
    	STchain.apply(Double.NaN, 0, "equal"); 
		return new snGetDemandsChainReturn(Lchain, STchain, Vchain, alpha, Nchain, SCVchain, refstatchain);
    }

    public static snDeaggregateChainResultsReturn snDeaggregateChainResults(NetworkStruct sn, JLineMatrix Lchain, JLineMatrix ST, JLineMatrix STchain, JLineMatrix Vchain, 
    		JLineMatrix alpha, JLineMatrix Qchain, JLineMatrix Uchain, JLineMatrix Rchain, JLineMatrix Tchain, JLineMatrix Cchain, JLineMatrix Xchain) {
    	
    	if (ST == null || ST.isEmpty()) {
    		ST = new JLineMatrix(0,0);
    		sn.rates.divide(1.0, ST, false);
    		ST.removeNaN();
    	}
    	
    	if (Cchain != null && !Cchain.isEmpty())
    		throw new RuntimeException("Cchain input to snDeaggregateChainResults not yet supported");
    	
    	int M = sn.nstations;
    	int K = sn.nClasses;
    	JLineMatrix X = new JLineMatrix(1, K);
    	JLineMatrix U = new JLineMatrix(M, K);
    	JLineMatrix Q = new JLineMatrix(M, K);
    	JLineMatrix T = new JLineMatrix(M, K);
    	JLineMatrix R = new JLineMatrix(M, K);
    	JLineMatrix C = new JLineMatrix(1, K);
    	
        int idxSink = 0;
        for (Node nodeIter : sn.nodes) {
        	if (nodeIter instanceof Sink)
        		idxSink = nodeIter.getNodeIdx();
        }
        
    	JLineMatrix Vsinktmp = new JLineMatrix(sn.nodevisits.get(0).numRows, sn.nodevisits.get(0).numCols);
    	for(int i = 0; i < sn.nodevisits.size(); i++) {
    		Vsinktmp = Vsinktmp.add(1, sn.nodevisits.get(i));
    	}
    	JLineMatrix Vsink = JLineMatrix.extractRows(Vsinktmp, idxSink, idxSink+1, null);
    	for(int c = 0; c < sn.nchains; c++) {
    		JLineMatrix inchain_c = sn.inchain.get(c);
			double sum = 0;
			for(int idx = 0; idx < inchain_c.numCols; idx++) 
				sum += sn.njobs.get((int) inchain_c.get(idx));
    		for(int idx = 0; idx < inchain_c.numCols; idx++) {
    			int k = (int) inchain_c.get(0, idx);
    			if (Double.isInfinite(sum))
    				X.set(0, k, Xchain.get(0, c) * Vsink.get(0, k));
    			else
    			X.set(0, k, Xchain.get(0, c) * alpha.get((int) sn.refstat.get(k, 0), k));
    			for(int i = 0; i < M; i++) {
    				if (Uchain == null || Uchain.isEmpty()) {
    					if (Double.isInfinite(sn.nservers.get(i, 0)))
    						U.set(i, k, ST.get(i, k) * (Xchain.get(0, c) * Vchain.get(i, c) / Vchain.get((int) sn.refstat.get(k, 0), c)) * alpha.get(i, k));
    					else
    						U.set(i, k, ST.get(i, k) * (Xchain.get(0, c) * Vchain.get(i, c) / Vchain.get((int) sn.refstat.get(k, 0), c)) * alpha.get(i, k) / sn.nservers.get(i, 0));
    				} else {
    					if (Double.isInfinite(sn.nservers.get(i, 0)))
    						U.set(i, k, ST.get(i, k) * (Xchain.get(0, c) * Vchain.get(i, c) / Vchain.get((int) sn.refstat.get(k, 0), c)) * alpha.get(i, k));
    					else
    						U.set(i, k, Uchain.get(i, c) * alpha.get(i, k));
    				}
    				
    				if (Lchain.get(i, c) > 0) {
    					if (Qchain != null && !Qchain.isEmpty()) 
    						Q.set(i, k, Qchain.get(i, c) * alpha.get(i, k));
    					else
    						Q.set(i, k, Rchain.get(i, c) * ST.get(i, k) / STchain.get(i, c) * Xchain.get(0, c) * Vchain.get(i, c) / Vchain.get((int) sn.refstat.get(k, 0), c) * alpha.get(i, k));
    					T.set(i, k, Tchain.get(i, c) * alpha.get(i, k));
    					R.set(i, k, Q.get(i, k) / T.get(i, k));
    				} else {
    					T.remove(i, k);
    					R.remove(i, k);
    					Q.remove(i, k);
    				}		
    			}
    			C.set(0, k, sn.njobs.get(0, k) / X.get(0, k));
    		}
    	}
    	
    	Q.abs();
    	R.abs();
    	X.abs();
    	U.abs();
    	T.abs();
    	C.abs();
    	Q.removeNaN(); Q.apply(Double.POSITIVE_INFINITY, 0, "equal");
    	R.removeNaN(); R.apply(Double.POSITIVE_INFINITY, 0, "equal");
    	X.removeNaN(); X.apply(Double.POSITIVE_INFINITY, 0, "equal");
    	U.removeNaN(); U.apply(Double.POSITIVE_INFINITY, 0, "equal");
    	T.removeNaN(); T.apply(Double.POSITIVE_INFINITY, 0, "equal");
    	C.removeNaN(); C.apply(Double.POSITIVE_INFINITY, 0, "equal");
    	
    	return new snDeaggregateChainResultsReturn(Q, U, R, T, C, X);
    }
    
    public static class snGetDemandsChainReturn{
    	public JLineMatrix Lchain;
    	public JLineMatrix STchain;
    	public JLineMatrix Vchain;
    	public JLineMatrix alpha;
    	public JLineMatrix Nchain;
    	public JLineMatrix SCVchain;
    	public JLineMatrix refstatchain;
    	
		public snGetDemandsChainReturn(JLineMatrix lchain, JLineMatrix sTchain, JLineMatrix vchain, JLineMatrix alpha,
				JLineMatrix nchain, JLineMatrix sCVchain, JLineMatrix refstatchain) {
			this.Lchain = lchain;
			this.STchain = sTchain;
			this.Vchain = vchain;
			this.alpha = alpha;
			this.Nchain = nchain;
			this.SCVchain = sCVchain;
			this.refstatchain = refstatchain;
		}	
    }

    public static class snDeaggregateChainResultsReturn{
    	public JLineMatrix Q;
    	public JLineMatrix U;
    	public JLineMatrix R;
    	public JLineMatrix T;
    	public JLineMatrix C;
    	public JLineMatrix X;
    	
		public snDeaggregateChainResultsReturn(JLineMatrix q, JLineMatrix u, JLineMatrix r, JLineMatrix t,
				JLineMatrix c, JLineMatrix x) {
			Q = q;
			U = u;
			R = r;
			T = t;
			C = c;
			X = x;
		}
    }
}
