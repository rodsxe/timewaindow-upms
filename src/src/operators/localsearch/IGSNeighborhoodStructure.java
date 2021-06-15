package operators.localsearch;

import java.util.Random;

import model.Solution;
import model.Job;
import model.MachineContainer;
import operators.function.evaluator.ObjectFunctionEvaluator;

public class IGSNeighborhoodStructure implements LocalSearch {
	
	private ObjectFunctionEvaluator evaluator;
	
	private int K;
	
	public IGSNeighborhoodStructure(ObjectFunctionEvaluator evaluator, int K) {
		super();
		this.evaluator = evaluator;
		this.K = K;
	}
	
	@Override
	public boolean run(Random rand, Solution s, long totalTime, long initialTime) {
		
		//if(!true) return false; 
		Solution clone = s.clone();
		MachineContainer[] maquinas = clone.getMaquinas();
		
		MachineContainer sortedMachine;
		
		int bestM = -1;
		int bestJ = -1;
		float newCost, machineCost, minCost;
		
		Job job;
		Job[] sortedJobs = new Job[K];
		
		for (int i = 0; i < K; i++){
			
			sortedMachine = maquinas[rand.nextInt(maquinas.length)];
			while(sortedMachine.getNumberOfJobs() == 0) sortedMachine = maquinas[rand.nextInt(maquinas.length)];
			sortedJobs[i] = sortedMachine.removerJob(rand.nextInt(sortedMachine.getNumberOfJobs()));
		
		}
				
		for (int i = 0; i < K; i++) {
				
			minCost = Float.MAX_VALUE;
			job = sortedJobs[i];
			
			for (int m = 0; m < maquinas.length; m++) {
				
				sortedMachine = maquinas[m];
				machineCost = evaluator.getObjectFunctionValue(clone, sortedMachine);
				
				for (int j = 0; j <= sortedMachine.getNumberOfJobs(); j++) {
					
					sortedMachine.addJob(job, j);
					newCost = evaluator.getObjectFunctionValue(clone, sortedMachine);
					
					if(newCost - machineCost < minCost){
						
						minCost = newCost - machineCost;
						bestJ = j;
						bestM = m;
						
					}
					
					sortedMachine.removerJob(j);
				
				}
				
			}
			
			maquinas[bestM].addJob(job, bestJ);
			
		}
		
		if (clone.setObjectiveFunction() < s.setObjectiveFunction()) {
			
			s.setMaquinas(clone.getMaquinas());
			s.setObjectiveFunction();
			return true;
			
		}
		
		
		return false;
		
	}

}
