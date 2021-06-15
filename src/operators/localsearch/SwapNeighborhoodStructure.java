package operators.localsearch;

import java.util.List;
import java.util.Random;

import com.sun.tools.javac.util.Pair;

import model.Solution;
import model.Job;
import model.MachineContainer;
import operators.function.evaluator.ObjectFunctionEvaluator;


public class SwapNeighborhoodStructure implements LocalSearch {
	
	private ObjectFunctionEvaluator evaluator;
	private boolean firstImprovement = false;
	
	
	public SwapNeighborhoodStructure(ObjectFunctionEvaluator evaluator) {
		super();
		this.evaluator = evaluator;
	}
	
	public SwapNeighborhoodStructure setFirstImprovement(boolean firstImprovement) {
		this.firstImprovement = firstImprovement;
		return this;
	}

	@Override
	public boolean run(Random rand, Solution s, long totalTime, long initialTime) {
		
		boolean foundNewBest = false;
		List<MachineContainer> maquinasNaoExploradas = s.getMachines(s.getMaquinas());
		
		int indexMachine = evaluator.getLSMachineIndex(rand, s, maquinasNaoExploradas);
		if(indexMachine < 0)return false;
		
		MachineContainer maquinaTardiness = maquinasNaoExploradas.get(indexMachine);
				
		while(!foundNewBest && maquinasNaoExploradas.size() > 0){
				
			indexMachine = rand.nextInt(maquinasNaoExploradas.size());
			MachineContainer m2 = maquinasNaoExploradas.remove(indexMachine);
			if (m2.getId() == maquinaTardiness.getId()) {
				
				foundNewBest = this.doInternalLS(s, maquinaTardiness);
				
			}else {
				
				float costM1 = evaluator.getObjectFunctionValue(s, maquinaTardiness);
				float costM2 =  evaluator.getObjectFunctionValue(s, m2);
				
				Pair<Integer, Integer> indice = getBestImprovementTrocaExterna(s, maquinaTardiness, m2, rand, costM1, costM2);
				
				if (indice != null) {
					
					Job t1 = maquinaTardiness.getJob(indice.fst);
					Job t2 = m2.getJob(indice.snd);
					maquinaTardiness.replaceJob(indice.fst, t2);
					m2.replaceJob(indice.snd, t1);
					foundNewBest = true;
					
				}
			}
			
		}
		
		return foundNewBest;
		
	}
	
	public boolean doInternalLS(Solution s, MachineContainer maquina) {
		boolean stopping = false;
		
		int numTarefasAlocadas = maquina.getNumberOfJobs();
		
		int bestI = -1;
		int bestJ = -1;
		float newCost, minCost = evaluator.getObjectFunctionValue(s, maquina);
		if (minCost == 0) return false;
				
		for (int i = 0; i < numTarefasAlocadas && !stopping; i++) {
			
			for (int j = i + 1; j < numTarefasAlocadas && !stopping; j++) {
				
				maquina.swapJobs(i, j);
				newCost = evaluator.getObjectFunctionValue(s, maquina);
				
				if(newCost < minCost){
					
					minCost = newCost;
					bestI = i;
					bestJ = j;
					if (firstImprovement) stopping = true;
					
				}
				
				maquina.swapJobs(i, j);
										
			}
			
		}
		
		if(bestI != -1){
			
			maquina.swapJobs(bestI, bestJ);
			return true;
		
		}
		
		return false;
	}
	
	private Pair<Integer, Integer> getBestImprovementTrocaExterna(Solution cell, MachineContainer m1, MachineContainer m2, Random rand, float costM1, float costM2) {
		
		boolean stopping = false;
		
		float actualCost = evaluator.getLSCost(costM1, costM2);//costM1 + costM2;
		
		Pair<Integer, Integer> indice = null;
		Job jobM1, jobM2;
		
		for (int i = 0; i < m1.getNumberOfJobs() && !stopping; i++) {
			
			jobM1 = m1.getJob(i);
			
			for (int j = 0; j < m2.getNumberOfJobs() && !stopping; j++) {
				
				jobM2 = m2.getJob(j);
				
				m1.replaceJob(i, jobM2);
				m2.replaceJob(j, jobM1);
				
				costM1 = evaluator.getObjectFunctionValue(cell, m1);
				costM2 = evaluator.getObjectFunctionValue(cell, m2);				
				
				if (evaluator.getLSCost(costM1, costM2) < actualCost) {
					
					actualCost = evaluator.getLSCost(costM1, costM2);
					indice = new Pair<Integer, Integer>(i, j);
					if (firstImprovement) stopping = true;
					
				}
				
				m1.replaceJob(i, jobM1);
				m2.replaceJob(j, jobM2);
							
			}
			
		}
		
		return indice;
		
	}	
	
	
}
