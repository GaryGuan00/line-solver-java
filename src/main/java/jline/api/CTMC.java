package jline.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


import jline.lang.JLineMatrix;

public class CTMC {
	
    public static JLineMatrix dtmc_stochcomp(JLineMatrix P, List<Integer> I) {
    	//Note that in this function, List is used instead of JLineMatrix for performance consideration
    	if (P == null)
    		throw new RuntimeException("The first parameter of dtmc_stochcomp cannot be null");
    	
    	int lengthP = Math.max(P.getNumCols(), P.getNumRows());
    	if (I == null || I.size() == 0) {
    		I = new ArrayList<Integer>();
    		for(int i = 0; i < (int) Math.ceil(lengthP/2.0); i++)
    			I.add(i);
    	}
    	
    	List<Integer> Ic = IntStream.rangeClosed(0, lengthP-1).boxed().collect(Collectors.toList());
    	Ic.removeAll(I);

		JLineMatrix P11 = new JLineMatrix(I.size(), I.size());
		JLineMatrix P12 = new JLineMatrix(I.size(), Ic.size());
		JLineMatrix P21 = new JLineMatrix(Ic.size(), I.size());
		JLineMatrix P22 = new JLineMatrix(Ic.size(), Ic.size());
		
		for(int colIdx = 0; colIdx < P.getNumCols(); colIdx++) {
			for(int i = P.col_idx[colIdx]; i < P.col_idx[colIdx+1]; i++) {
				int rowIdx = P.nz_rows[i];
				double value = P.nz_values[i];
				if (colIdx < I.size()) {
    				if (rowIdx < I.size())
    					P11.set(rowIdx, colIdx, value);
    				else
    					P21.set(rowIdx - I.size(), colIdx, value);
				} else {
    				if (rowIdx < I.size())
    					P12.set(rowIdx, colIdx - I.size(), value);
    				else
    					P22.set(rowIdx - I.size(), colIdx - I.size(), value);
				}
			}
		}
		
		double[] values = new double[Ic.size()];
		Arrays.fill(values, 1.0);
		JLineMatrix S2 = JLineMatrix.diagMatrix(null, values, 0, values.length).sub(1, P22);
		
		// S=P11+P12*(S2 \ P21);
		JLineMatrix s2_p21 = new JLineMatrix(0,0);
		JLineMatrix.solve(S2, P21, s2_p21);
		JLineMatrix S = P11.add(1, P12.mult(s2_p21, null));
    	return S;
    }
    
    public static JLineMatrix dtmc_solve(JLineMatrix P) {
    	
    	//P-eye(size(P))
    	for(int i = 0; i < P.getNumRows(); i++) {
    		P.set(i, i, P.get(i,i) - 1.0);
    	}
    	return ctmc_solve(P);
    }
    
    public static JLineMatrix ctmc_solve(JLineMatrix Q) {
    	if (Q.length() == 1) {
    		JLineMatrix p = new JLineMatrix(1,1,1);
    		p.set(0, 0, 1);
    		return p;
    	}
    		
    	Q = ctmc_makeinfgen(Q);
    	int n = Q.length();
    	
    	//B = abs(Q+Q')>0
    	JLineMatrix B = Q.sub(1, Q.transpose());
    	B.abs();
    	for(int colIdx = 0; colIdx < B.numCols; colIdx++) {
			int col1 = B.col_idx[colIdx];
			int col2 = B.col_idx[colIdx+1];
			
			for(int i = col1; i < col2; i++) {
				if (B.nz_values[i] > 0)
					B.nz_values[i] = 1;
			}
		}
    	
    	//[nConnComp, connComp] = weaklyconncomp(B);
    	Set<Set<Integer>> sets = UTIL.weaklyConnect(B, null);
    	if (sets.size() > 1) {
    		JLineMatrix p = new JLineMatrix(1, n);
    		
    		for(Set<Integer> set_c : sets) {
    			//Qc = Q(connComp==c,connComp==c);
    			JLineMatrix Qc = new JLineMatrix(set_c.size(), set_c.size());
    			int Qc_row = 0, Qc_col = 0;
    			for(Integer q_row : set_c) {
    				for(Integer q_col : set_c) {
    					Qc.set(Qc_row, Qc_col++, Q.get(q_row, q_col));
    				}
    				Qc_row++;
    				Qc_col = 0;
    			}
    			//Qc = ctmc_makeinfgen(Qc);
    			Qc = ctmc_makeinfgen(Qc);
    			//p(connComp==c) = ctmc_solve(Qc);
    			JLineMatrix ctmc_solve_Qc = ctmc_solve(Qc);
    			int idx = 0;
    			for(Integer i : set_c) {
    				p.set(0, i, ctmc_solve_Qc.get(0, idx++));
    			}
    		}
    		p.divide(p.sumRows(0), p, true);
    		return p;
    	}
    	
    	if (Q.getNonZeroLength() == 0) {
    		JLineMatrix p = new JLineMatrix(1, n);
    		p.fill(1/n);
    		return p;
    	}
    	
    	JLineMatrix p = new JLineMatrix(1, n);
    	JLineMatrix b = new JLineMatrix(n, 1);
    	JLineMatrix nnzel = new JLineMatrix(1, n);
    	for(int i = 0; i < n; i++)
    		nnzel.set(0, i, i);
    	JLineMatrix Qnnz = Q.clone();
    	JLineMatrix bnnz = b.clone();
    	JLineMatrix Qnnz_1 = Q.clone();
    	JLineMatrix bnnz_1 = bnnz.clone();
    	
    	boolean isReducible = false;
    	boolean goon = true;
    	while (goon) {
    		//nnzel = find(sum(abs(Qnnz),1)~=0 & sum(abs(Qnnz),2)'~=0);
    		JLineMatrix Qnnz_abs = Qnnz.clone();
    		Qnnz_abs.abs();
    		JLineMatrix Qnnz_abs_sum_col = Qnnz_abs.sumCols();
    		JLineMatrix Qnnz_abs_sum_rows = Qnnz_abs.sumRows();
    		JLineMatrix find_res = new JLineMatrix(1, n);
    		for(int i = 0; i < n; i++) {
    			if (Qnnz_abs_sum_col.get(i) != 0 && Qnnz_abs_sum_rows.get(i) != 0)
    				find_res.set(0, i, 1);
    		}
    		nnzel = find_res.find().transpose();
    		
    		if (nnzel.length() < n && !isReducible) {
    			isReducible = true;
//    	        if (nargin > 1 && options.verbose == 2) % debug
//                	fprintf(1,'ctmc_solve: the infinitesimal generator is reducible.\n');
//    	        end	
    		}
    		
    		//Qnnz = Qnnz(nnzel, nnzel);
    		JLineMatrix new_Qnnz = new JLineMatrix(nnzel.numCols, nnzel.numCols);
    		for(int i = 0; i < nnzel.numCols; i++) {
    			for(int j = 0; j < nnzel.numCols; j++) {
    				new_Qnnz.set(i, j, Qnnz.get((int)nnzel.get(0,i), (int)nnzel.get(0,j)));
    			}
    		}
    		Qnnz = new_Qnnz;
    		
    		//bnnz = bnnz(nnzel);
    		JLineMatrix new_bnnz = new JLineMatrix(nnzel.numCols, 1);
    		for(int i = 0; i < nnzel.numCols; i++) {
    			new_bnnz.set(i, 0, bnnz.get((int)nnzel.get(0,i), 0));
    		}
    		bnnz = new_bnnz;
    		
    		//Qnnz = ctmc_makeinfgen(Qnnz);
    		Qnnz = ctmc_makeinfgen(Qnnz);
    		
    		if ((Qnnz.numCols * Qnnz.numRows == Qnnz_1.numCols * Qnnz_1.numRows) && (bnnz.numCols * bnnz.numRows == bnnz_1.numCols * bnnz_1.numRows)){
    			goon = false;
    		} else {
    			Qnnz_1 = Qnnz.clone();
    			bnnz_1 = bnnz.clone();
    	    	nnzel = new JLineMatrix(1, Qnnz.length());
    	    	for(int i = 0; i < Qnnz.length(); i++)
    	    		nnzel.set(0, i, i);
    		}
    	}
    	
    	if ((Qnnz == null) || (Qnnz.isEmpty())) {
    		p.fill(1/n);
    		return p;
    	}
    	
    	//Qnnz(:,end) = 1;
    	for(int i = 0; i < n; i++)
    		Qnnz.set(i, n - 1, 1.0);
    	
    	//bnnz(end) = 1;
    	bnnz.set(n - 1, 0, 1.0);
    	
    	//p(nnzel)=Qnnz'\ bnnz;
    	JLineMatrix solve_res = new JLineMatrix(0,0);
    	JLineMatrix.solve(Qnnz.transpose(), bnnz, solve_res);
    	solve_res.removeZeros(1e-10);
    	for(int i = 0; i < nnzel.numCols; i++)
    		p.set(0, (int) nnzel.get(0,i), solve_res.get(i,0));
    	
    	if(p.hasNaN()) {
    		//B = abs(Qnnz+Qnnz')>0;
        	B = Qnnz.add(1, Qnnz.transpose());
        	B.abs();
        	for(int colIdx = 0; colIdx < B.numCols; colIdx++) {
    			int col1 = B.col_idx[colIdx];
    			int col2 = B.col_idx[colIdx+1];
    			
    			for(int i = col1; i < col2; i++) {
    				if (B.nz_values[i] > 0)
    					B.nz_values[i] = 1;
    			}
    		}
        	
        	//[nConnComp, connComp] = weaklyconncomp(B);
        	sets = UTIL.weaklyConnect(B, null);
        	if (sets.size() > 1) {
        		//p(nnzel) = zeros(1,n);
        		for(int i = 0; i < nnzel.numCols; i++)
        			p.remove(0, (int) nnzel.get(0,i));
        		
        		for(Set<Integer> set_c : sets) {
        			//Qc = Q(connComp==c,connComp==c);
        			JLineMatrix Qc = new JLineMatrix(set_c.size(), set_c.size());
        			int Qc_row = 0, Qc_col = 0;
        			for(Integer q_row : set_c) {
        				for(Integer q_col : set_c) {
        					Qc.set(Qc_row, Qc_col++, Q.get(q_row, q_col));
        				}
        				Qc_row++;
        				Qc_col = 0;
        			}
        			//Qc = ctmc_makeinfgen(Qc);
        			Qc = ctmc_makeinfgen(Qc);
        			//p(intersect(find(connComp==c),nnzel)) = ctmc_solve(Qc);
        			JLineMatrix ctmc_solve_Qc = ctmc_solve(Qc);
        			int idx = 0;
        			for(int i = 0; i < nnzel.numCols; i++) {
        				int val = (int) nnzel.get(0,i);
        				if (set_c.contains(val))
        					p.set(0, val, ctmc_solve_Qc.get(0, idx++));
        			}
        		}
        		p.divide(p.sumRows(0), p, true);
        		return p;
        	}
    	}
    	return p;
    }
    
    public static JLineMatrix ctmc_makeinfgen(JLineMatrix Q) {
    	double[] val = new double[Q.length()];
    	for(int i = 0; i < Q.length(); i++) 
    		val[i] = Q.get(i,i);
    	
    	JLineMatrix diag_diag_Q = new JLineMatrix(0,0);
    	JLineMatrix.diagMatrix(diag_diag_Q, val, 0, val.length);
    	
    	JLineMatrix A = Q.sub(1, diag_diag_Q);
    	
    	JLineMatrix diag_sum_A_row = new JLineMatrix(0,0);
    	JLineMatrix sum_A_row = A.sumRows();
    	JLineMatrix.diagMatrix(diag_sum_A_row, sum_A_row.nz_values, 0, sum_A_row.nz_length);
    	
    	Q = A.sub(1, diag_sum_A_row);
    	Q.removeZeros(0);
    	return Q;
    }
}
