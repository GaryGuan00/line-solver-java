package jline.api;

import jline.lang.JobClass;
import jline.lang.NetworkStruct;
import jline.lang.nodes.Station;
import jline.solvers.SolverOptions;
import jline.util.Matrix;

import java.util.*;

import static jline.lib.KPCToolbox.*;
import static jline.lib.M3A.*;


/**
* APIs for Matrix-Analytic Methods and Markovian Arrival Processes (MAP).
*/
public class MAM {
	public static Map<Integer,Matrix> mmap_compress(Map<Integer,Matrix> MMAP, SolverOptions config){
		Map<Integer,Matrix> mmap = new HashMap<>();
		int K = MMAP.size()-2;
		if(Objects.equals(config.method, "default") ||Objects.equals(config.method, "mixture")||Objects.equals(config.method, "mixture.order1")){
			Matrix lambda = mmap_lambda(MMAP);
			Map<Integer,Map<Integer,Matrix>> AMAPs = mmap_maps(MMAP);
				for(int k=0;k<K;k++){
					AMAPs.put(k,mmpp2_fit1(map_mean(AMAPs.get(k).get(0),AMAPs.get(k).get(1)),map_scv(AMAPs.get(k).get(0),AMAPs.get(k).get(1)),map_skew(AMAPs.get(k).get(0),AMAPs.get(k).get(1)),map_idc(AMAPs.get(k).get(0),AMAPs.get(k).get(1))));
					lambda.scale(1/lambda.elementSum());
					mmap = mmap_mixture(lambda,AMAPs);
				}

		}else if(Objects.equals(config.method,"mixture.order2")){
			//mmap = mmap_mixture_fit_mmap(MMAP).MMAP;
		}
		//todo more methods
		return mmap;
	}

	public static Map<Integer,Matrix> mmap_compress(Map<Integer,Matrix> MMAP){
		Map<Integer,Matrix> mmap = new HashMap<>();
		int K = MMAP.size()-2;

		Matrix lambda = mmap_lambda(MMAP);
		Map<Integer,Map<Integer,Matrix>> AMAPs = mmap_maps(MMAP);
		for(int k=0;k<K;k++){
			AMAPs.put(k,mmpp2_fit1(map_mean(AMAPs.get(k).get(0),AMAPs.get(k).get(1)),map_scv(AMAPs.get(k).get(0),AMAPs.get(k).get(1)),map_skew(AMAPs.get(k).get(0),AMAPs.get(k).get(1)),map_idc(AMAPs.get(k).get(0),AMAPs.get(k).get(1))));
			lambda.scale(1/lambda.elementSum());
			mmap = mmap_mixture(lambda,AMAPs);
		}

		return mmap_normalize(mmap);
	}

	public static Map<Integer,Map<Integer,Map<Integer,Matrix>>> PH_index_transform(Map<Station,Map<JobClass,Map<Integer,Matrix>>> PH, NetworkStruct sn){
		Map<Integer,Map<Integer,Map<Integer,Matrix>>> result = new HashMap<>();

		for (int i=0;i<sn.nstations;i++){
			for(int j=0; j<sn.nclasses;j++){
				if(j==0){
					result.put(i,new HashMap<>());
				}
				result.get(i).put(j,PH.get(sn.stations.get(i)).get(sn.jobclasses.get(j)));
			}
		}
		return result;
	}

	public static Map<Integer,Matrix> mmap_express_transform(Map<Integer,Matrix> mmap){
		Map<Integer,Matrix> result = new HashMap<>();
		for(int i=0;i<mmap.size();i++){
			if(i==0){
				result.put(0,mmap.get(0));
			}else if(i!=1){
				result.put(i-1,mmap.get(i));
			}
		}
		return result;
	}


}
